package test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import org.htmlparser.Parser;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.TextExtractingVisitor;

import util.ParameterHandler;

import model.RankedTag;
import model.usermodel.parser.ContentExtractingVisitor;
import model.usermodel.tags.CompositeSubUrlTagFinderStrategy;
import model.usermodel.tags.TagFinder;

public class BenchmarkCreator {
	
	private String word;
	private List<Set<String>> tagGroups;
	private File logFile, noiseLogFile;
	private FileWriter fw, nfw;
	private BufferedWriter bw, nbw;
	// to switch delicious url string, it's different
	
	public BenchmarkCreator(String word, List<Set<String>> tagGroups) throws IOException {
		super();
		this.word = word;
		this.tagGroups = tagGroups;

	}
	
	public void retrieveTestPages() throws IOException {
		
		//create directory hierarcy to host downloaded data
		Map<File,Set<String>> subDirs = this.createSubDirectoriesAndLogs(word,tagGroups);
		//for(File f: subDirs)
		//	System.out.println(f.getName());
		
		// aggiunta mia
//		this.retrieveTestPages = true;
		
		//iterate over tag groups
		for(File subDir: subDirs.keySet()) {
			
			Set<String> goodTags = subDirs.get(subDir);
			
			//create test and training subdirectories
			File testDir = new File(subDir,TestParams.test_dir);
			File trainingDir = new File(subDir,TestParams.training_dir);
			if(!testDir.exists())
				if(!testDir.mkdir())
					throw new IOException();
			if(!trainingDir.exists())
				if(!trainingDir.mkdir())
					throw new IOException();
			
			//generate delicious query to search appropriate bookmarks
			//cambio le stringhe, cambio la modalitˆ di ricerca!! altrimenti non funziona
			//OLD
//			String deliciousQuery = 
//				TestParams.delicious_prefix + word + " AND " + subDir.getName() + TestParams.delicious_suffix;
			
			String deliciousQuery = 
				TestParams.delicious_prefix + word + " AND " + subDir.getName() + TestParams.delicious_suffix_mytags;
								
//			System.out.println("delicious query before bad/good tags " + deliciousQuery);
			
			Set<String> badTags = null;
			for(Set<String> tagGroup: tagGroups)
				if(!tagGroup.equals(goodTags)) {
					badTags = tagGroup;
					break;
				}
			
			deliciousQuery = this.generateDeliciousQuery(goodTags,badTags);

			//aggiungo uno spazio prima delle parentesi chiuse
			deliciousQuery = deliciousQuery.replace(")", " )");
			
			System.out.println("deliciousQuery: " + deliciousQuery);
			
			bw.write("deliciousQuery: " + deliciousQuery + "\n");
			bw.flush();
			
			//select urls whose content has to be downloaded
			List<String> selectedUrls = this.selectUrls(deliciousQuery, TestParams.docs_list);
			System.out.println("Retrieved " + selectedUrls.size() + " urls.");
			bw.write("Retrieved " + selectedUrls.size() + " urls.\n\n");
			bw.flush();
			
			//casually choose between test and training set (HORRIBLE CODE!)
			//faccio una lista di interi, che sono gli indici dei documenti
			//trainingSet e testSet
			//100 docs per tag
			List<Integer> indList = new LinkedList<Integer>();
			for(int i=0; i<TestParams.docs_per_tag_group; i++)
				indList.add(i);
			
//			System.out.println("indList prima del remove: " + indList);
			
			Set<Integer> trainingSet = new HashSet<Integer>();
			Random random = new Random();
			//levo 50 interi di training dalla indList
			for(int i=0; i<TestParams.training_docs_per_tag_group; i++)
				trainingSet.add(indList.remove(random.nextInt(indList.size())));
			
			
//			System.out.println("indList dopo del remove: " + indList);
			System.out.println("numero di docs per trainingSet: " + trainingSet.size());

			indList = null;

			int stored=0;
			//retrieve and store urls with associated tags
			for(String urlString: selectedUrls) {
				//nel training set c'Ž l'indice corrente?
				//se si, allora Ž un training doc, altrimenti Ž di test
				boolean trainingDoc = trainingSet.contains(stored);
				File storingDir = trainingDoc ? trainingDir : testDir;
				//codice che scarica effettivamente la pagina su disco
				if(this.retrieveUrlWithTags(urlString,storingDir,trainingDoc,bw)) {
					stored++;
					System.out.println(stored + " docs stored.");
					bw.write(stored + " docs stored.\n\n");
					bw.flush();
				}
				if(stored>=TestParams.docs_per_tag_group)
					break;
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			
		}
		
		
	}
	
	private String generateDeliciousQuery(Set<String> goodTags, Set<String> badTags) {

		StringBuffer buf = new StringBuffer();
		
		buf.append(TestParams.delicious_prefix + word + " (");
	
		int i = 1;
		for(String goodTag: goodTags) {
			buf.append("tag:" + goodTag);
			if(i!=goodTags.size())
				buf.append(" OR ");
			else
				buf.append(") "); 
			i++;
		}
		for(String badTag: badTags)
			buf.append("-tag:" + badTag + ' ');
		//	only in mytags
		buf.append(TestParams.delicious_suffix_mytags);
		
		return buf.toString();
	}

	private boolean retrieveUrlWithTags(String urlString, File subDir, boolean trainingDoc, BufferedWriter writer) throws IOException {
		
		//normalize file names
		//String fileName = urlString.substring(urlString.indexOf("://") + ("://").length());
		int truncate = Math.min(urlString.length(), TestParams.max_filename_length);
		String fileName = urlString.substring(0, truncate);
		fileName = fileName.replaceAll("/", "_");
		
		String[] allowedExts = {".txt",".html",".shtml",".htm",".shtm",".php",".phps",".jsp",
				".xml",".nfo",".aspx",".asp",".stm",".cfm",".c",".java",".pm"};
		
		String[] blackList = {
				"http://digg.com/links/Bruce_Lee_s_One_Inch_Punch_-_The_How%2C_The_Why%2C_The_Wow",
				"http://remedicated.com/2008/05/18/5-common-cooking-ingredients-that-act-like-medicines/",
				"https://sharepoint.uwaterloo.ca/sites/LibraryISR/porter/default.aspx",
				"http:///blogde1ap-victoria.blogspot.com/",
				"http://ugenie.com/",
				"http://library.thinkquest.org/2847/authors/lee.htm?tqskip1=1",
				"http://www.bmj.com/cgi/content/short/334/7599/866-a?etoc"};
		
		//controlla blacklist
		for(String bl: blackList)
			if(urlString.equals(bl)) {
				System.out.println("(blacklisted!)");
				writer.write("(blacklisted!)\n");
				writer.flush();
				return false;
			}
		
		//controlla estensioni consentite
		if(urlString.matches("(?i).*\\.\\w+$")) {
			System.out.println("url WITH file extension: " + urlString);
			writer.write("url WITH file extension: " + urlString + '\n');
			writer.flush();
			boolean matchesAllowedExt = false;
			for(String ext: allowedExts)
				if(urlString.endsWith(ext)) {
					matchesAllowedExt = true;
					break;
				}
			if(!matchesAllowedExt) {
				System.out.println("(it is not allowed!)");
				writer.write("(it is not allowed!)\n");
				writer.flush();
				return false;
			}
		}
		else {
			//problemi con il contenuto dell'urlString: prende molto html
			System.out.println("url WITHOUT file extension: " + urlString);
			writer.write("url WITHOUT file extension: " + urlString + '\n');
			writer.flush();
		}

		URL url = new URL(urlString);
		URLConnection urlConnection;
		try {
			urlConnection = url.openConnection();
			//fix timeout
			urlConnection.setConnectTimeout(ParameterHandler.URL_TIMEOUT);
			
			//download parse and store web document
			System.out.print("parsing and saving '" + urlString + "'... ");
			writer.write("parsing and saving '" + urlString + "'... ");
			writer.flush();
			Parser parser = new Parser(urlConnection);
			TextExtractingVisitor visitor = new ContentExtractingVisitor();
			parser.visitAllNodesWith(visitor);
			String textInPage = visitor.getExtractedText();
			if(textInPage.isEmpty()) {
				System.out.print("empty document!");
				writer.write("empty document!\n\n");
				writer.flush();
				return false;
			}
	        
			Set<RankedTag> rtags = new HashSet<RankedTag>();
		
	        //download and store related tags
	        if(trainingDoc) {
		        System.out.println("with related tags (training set)...");
		        writer.write("with related tags (training set)...\n");
		        writer.flush();
		        //TODO: strategia per trovare i tags!!
		        // exacturl = true
				TagFinder tf = new TagFinder(new CompositeSubUrlTagFinderStrategy(),true);
				rtags = tf.findTags(urlString);
				//per ogni url almeno un tag! OK
				if(rtags.size()<=1) {
					System.out.println("no tags retrieved for training doc! (discarded)");
					writer.write("no tags retrieved for training doc! (discarded)\n");
					writer.flush();
			        return false;
				}
				else {
					System.out.print("tags retrieved: ");
					writer.write("tags retrieved: ");
			        //bw.flush();
			        for(RankedTag rt: rtags) {
						System.out.print(rt.getTag() + ' ');
						writer.write(rt.getTag() + ' ');
				        //bw.flush();
			        }
					System.out.println();
					writer.write("\n");
					writer.flush();
				}

	        }
	        else {
	        	System.out.println("without tags (test set)...");
	        	writer.write("without tags (test set)...\n");
	        	writer.flush();
	        }
	        
			//create files
			Scanner scanner = new Scanner(textInPage);
			File docFile = new File(subDir,fileName);
			if(!docFile.exists())
				if(!docFile.createNewFile())
					throw new IOException();
			FileWriter fstream = new FileWriter(docFile);
	        BufferedWriter out = new BufferedWriter(fstream);
	        while(scanner.hasNext())
	        	out.write(scanner.next() + ' ');
	        out.close();
	        fstream.close();
	        scanner.close();
	        
	        if(trainingDoc) {
				File tagFile = new File(subDir,"tags_" + fileName);
				if(!tagFile.exists())
					if(!tagFile.createNewFile())
						throw new IOException();
				fstream = new FileWriter(tagFile);
		        out = new BufferedWriter(fstream);
		        for(RankedTag rt: rtags)
		        	out.write(rt.getTag() + " " + rt.getRanking() + "\n");
		        out.close();
		        fstream.close();
	        }
	        
		}
		catch(SocketTimeoutException e) {
			//e.getMessage();
			System.out.println("timeout");
			writer.write("timeout!\n");
			writer.flush();
			return false;
		} catch (ParserException e) {
			System.out.println("parsing problem");
			writer.write("parsing problem!\n");
			writer.flush();
			return false;
		} catch (Exception e) {
			System.out.println("exception (" + e.getCause() + ")");
			writer.write("exception (" + e.getCause() + ")\n");
			writer.flush();
			return false;
		}
        

        
		return true;
		
	}
	
	private List<String> selectUrls(String deliciousQuery, int listSize) throws IOException {

		int pageNumber = 1;
		List<String> result = new LinkedList<String>();
		
		while(result.size()<listSize) {
		
			//obtain a new (already parsed) list of urls
			List<String> deliciousList = getDeliciousList(deliciousQuery + pageNumber);
			if(deliciousList.size()==0)
				break;
			pageNumber++;
			
			//populate the result list
			result.addAll(deliciousList.subList(0, 
					Math.min(deliciousList.size(), listSize-result.size())));
		}
		
		return result;
	
	}

	private List<String> getDeliciousList(String deliciousQuery) 
		throws IOException{

		
		System.out.println("delicious query before char replacement: " + deliciousQuery);
		String example = "cancer+(tag:medicine+OR+tag:health+)+-tag:horoscope+-tag:astrology";

		//rimpiazza gli spazi con +
		
		String example2 = "cancer+(tag%3Amedicine+OR+tag%3Ahealth+)+-tag%3Ahoroscope+-tag%3Aastrology";
		
		List<String> deliciousList = new LinkedList<String>();
		deliciousQuery = deliciousQuery.replaceAll("\\(", "%28");
		deliciousQuery = deliciousQuery.replaceAll("tag:", "tag%3A");
		deliciousQuery = deliciousQuery.replaceAll("\\)", "%29");
//		deliciousQuery = deliciousQuery.replaceAll(" ", "%20");
		deliciousQuery = deliciousQuery.replaceAll(" ", "+");
		
		System.out.println("query sottomessa a delicious, filtrata: ");
		System.out.println(deliciousQuery);
		
	
		

		//works

		Scanner scanner = new Scanner(new URL(deliciousQuery).openStream());
		StringBuffer buf = new StringBuffer();
		
		//obtain unparsed delicious page
		while(scanner.hasNextLine())
			buf.append(scanner.nextLine() + "\n");
		int tempIndex = buf.indexOf("context-bookmarklist");
		if(tempIndex<0)
			return deliciousList;
		//indice del primo blocco del primo post
		int startIndex = buf.indexOf("<li",tempIndex);
		//indice della paginazione, per avanzare nei risultati
		int endIndex = buf.lastIndexOf("pagination");
		//indici del contenuto della pagina dei risultati di delicious
		System.out.println("startIndex=" + startIndex + ", endIndex=" + endIndex);
			
		if(startIndex<0 || endIndex<0 || startIndex>=endIndex)
			return deliciousList;
		
		//estrai solo il contenuto interessante
		String pageContent = buf.substring(startIndex, endIndex);
		//System.out.println("analyzing delicious page content...");
		//System.out.println("index of bookmark NOTHUMB = " + pageContent.indexOf("bookmark NOTHUMB"));
		
		//retrieve document list
		//estrazione della lista dei documenti contenuti nella pagina dei risultati
		while(pageContent.indexOf("bookmark  NOTHUMB")>=0) {
			//find next url
			//aggiunto uno spazio dopo taggedlink!!
			String urlContext = "<a rel=\"nofollow\" class=\"taggedlink \" href=\"";
			int startUrl = pageContent.indexOf(urlContext) +
			(urlContext).length();
			int endUrl = pageContent.indexOf("\" >", startUrl);
			String urlString = pageContent.substring(startUrl, endUrl);
			deliciousList.add(urlString);
			System.out.println("url retrieved: " + urlString);
			pageContent = pageContent.substring(endUrl);
			
		}
		
		return deliciousList;
		
	}

	private Map<File,Set<String>> createSubDirectoriesAndLogs(String word, List<Set<String>> tagGroups) 
		throws IOException {
	
		Map<String,Set<String>> subDirNames = new HashMap<String,Set<String>>();
		Map<File,Set<String>> result = new HashMap<File,Set<String>>();
		
		//check root dir existence
		File rootDir = new File(TestParams.dir);
		if(!rootDir.exists())
			if(!rootDir.mkdir())
				throw new IOException();
		File dataDir = new File(TestParams.data_dir);
		if(!dataDir.exists())
			if(!dataDir.mkdir())
				throw new IOException();
		
		//normalize subdirectory names
		for(Set<String> tags: tagGroups) {
			
			StringBuffer buf = new StringBuffer();
			int i = 1;
		
			for(String tag: tags) {
				buf.append("tag:" + tag);
				if(i!=tags.size())
					buf.append(" OR ");
				i++;
			}
			subDirNames.put("(" + buf.toString().trim() + ")",tags);
			
		}
		
		//create a directory for the specified word
		File wordDir = new File(TestParams.data_dir,word);
		if(!wordDir.exists())
			if(!wordDir.mkdir())
				throw new IOException();
		
		//create subdirectories for tag groups
		for(String subDirName: subDirNames.keySet()) {
			File subDir = new File(wordDir,subDirName);
			if(!subDir.exists())
				if(!subDir.mkdir())
					throw new IOException();
			result.put(subDir,subDirNames.get(subDirName));
		}
		
		//create log file
		this.logFile = new File(TestParams.data_dir,this.word + "_data.log");
		if(!this.logFile.exists())
			if(!this.logFile.createNewFile())
				throw new IOException();
		
		//initialize filewriter and bufferedwriter
		this.fw = new FileWriter(logFile);
		this.bw = new BufferedWriter(fw);

		
		return result;
		
	}
	
	public static void main(String[] args) throws IOException {
		
		//never launch!! 
		//it overwrites dataset directories!!
		List<Set<String>> tagGroups = new LinkedList<Set<String>>();
		Set<String> group1 = new HashSet<String>();
		Set<String> group2 = new HashSet<String>();
		group1.add("health");
		group1.add("medicine");
		group2.add("astrology");
		group2.add("horoscope");
		tagGroups.add(group1);
		tagGroups.add(group2);
		//crea un BenchMarkCreator con (parola, lista di tags) 
		//per creare un contesto semantico con i tags
		BenchmarkCreator bc = new BenchmarkCreator("cancer",tagGroups);
//		bc.retrieveTestPages();
//		bc.addNoise();
		
	}

	public void addNoise() throws IOException {
		
		//create noise log file
		this.noiseLogFile = new File(TestParams.data_dir,this.word + "_noise.log");
		if(!this.noiseLogFile.exists())
			if(!this.noiseLogFile.createNewFile())
				throw new IOException();
		
		//initialize filewriter and bufferedwriter
		this.nfw = new FileWriter(noiseLogFile);
		this.nbw = new BufferedWriter(nfw);
		
		String deliciousNoiseQuery = this.generateNoiseQuery();
		System.out.println("deliciousNoiseQuery: " + deliciousNoiseQuery);
		nbw.write("deliciousNoiseQuery: " + deliciousNoiseQuery + "\n");
		nbw.flush();
		List<String> noiseUrls = this.selectUrls(deliciousNoiseQuery, TestParams.noise_list);
		System.out.println("Retrieved " + noiseUrls.size() + " urls.");
		nbw.write("Retrieved " + noiseUrls.size() + " urls.\n\n");
		nbw.flush();
		//retrieve and store noise urls
		int stored=0;
		for(String urlString: noiseUrls) {
			File noiseDir = new File(TestParams.noise_dir);
			File storingDir = new File(noiseDir,this.word);
			if(!noiseDir.exists())
				if(!noiseDir.mkdir())
					throw new IOException();
			if(!storingDir.exists())
				if(!storingDir.mkdir())
					throw new IOException();
			if(this.retrieveUrlWithTags(urlString,storingDir,false,nbw)) {
				stored++;
				System.out.println(stored + " docs stored.");
				nbw.write(stored + " docs stored.\n\n");
				nbw.flush();
			}
			if(stored>=TestParams.docs_per_noise_group)
				break;
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private String generateNoiseQuery() {
		
		StringBuffer buf = new StringBuffer();
		
		buf.append(TestParams.delicious_prefix);
		buf.append(this.word + " ");
		
		for(Set<String> tags: tagGroups)
			for(String tag: tags)
				buf.append("-tag:" + tag + " ");
		
		buf.append("-tag:" + this.word);
		
		buf.append(TestParams.delicious_suffix);
		
		return buf.toString();
	}

}
