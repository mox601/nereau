package test;

//import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
//import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

//import model.RankedTag;

import org.apache.lucene.queryParser.ParseException;

import persistence.PersistenceException;

public class CombineTasks {
	
	public static void main(String[] args) throws IOException, PersistenceException, ParseException, InterruptedException {
		
		//BenchmarkCreator bc;
		SingleTest st;
		
		//create log file
		File logFile = new File(TestParams.data_dir,"firsttest_first_try.log");
		if(!logFile.exists())
			if(!logFile.createNewFile())
				throw new IOException();
		
		//initialize filewriter and bufferedwriter
	//	FileWriter fw = new FileWriter(logFile);
	//	BufferedWriter bw = new BufferedWriter(fw);
		
		
		//ottieni mappa string < Set<string>, Set<string>, Set<string>.... >
		//che sarebbe la lista dei test da effettuare
		Map<String,List<Set<String>>> testSpecs = parseTests();
		System.out.println(testSpecs.keySet().size() + " tests.");
		//elenca i nomi dei test
		for(String test: testSpecs.keySet()) {
			System.out.println(test);
			for(Set<String> tags : testSpecs.get(test))
				System.out.println(tags);
		}
		
		
		//effettua i tests trovati nel file
		for(String test: testSpecs.keySet()) {
			st = new SingleTest(test);
			//non si fa, versione vecchia:
//			st.performFirstTest();
			//questo si:
			st.performTest();
			
			/* "problema" nel database dei tagsvisitedurls: 
			 * i profili eseguiti per ultimi risentono delle visite di tutti gli utenti 
			 * creati precedentemente. L'aspetto sociale così è enfatizzato. 
			 * si puó anche evitare, cancellando la tabella ogni volta che si 
			 * effettua un test, per valutare solo i miglioramenti individuali... 
			 * La prima volta ho svuotato tagsvisitedurls... */ 
			
		}
		
		//for(int numOfRes=5; numOfRes<=50; numOfRes++) {
		/*
		Map<model.Query,Set<RankedTag>> results = new HashMap<model.Query,Set<RankedTag>>();
			for(String test:testSpecs.keySet()) {
				//bc = new BenchmarkCreator(test,testSpecs.get(test));
				//if(!test.equals("cancer"))
					//bc.retrieveTestPages();
				//bc.addNoise();
				st = new SingleTest(test);
				//st.feedUserModel();
				results = st.performSecondTest();
				//results.putAll(st.performFirstTest(numOfRes));
				System.out.println(test);
				bw.write(test + "\n");
				for(model.Query q: results.keySet()) {
					System.out.println(q + " ---> " + results.get(q));
					bw.write(q + " ---> " + results.get(q) + "\n");
				}
				System.out.println();
				bw.write("\n");
				bw.flush();
			}
			*/
		
		
			/*
			double noExpMean=0.0, expMean=0.0;
			for(String testname: results.keySet()) {
				noExpMean += results.get(testname)[0];
				expMean += results.get(testname)[1];
			}
			noExpMean /= results.keySet().size();
			expMean /= results.keySet().size();
			
			System.out.println(numOfRes + "," + noExpMean + "," + expMean);
			bw.write(numOfRes + "," + noExpMean + "," + expMean + "\n");
			bw.flush();
			Thread.sleep(2000);
			*/
		//}
		
		//bw.close();
		//fw.close();

	}
	
	/*
	
	testName1
	[tag1, tag2, tag3, tag4]
	[tag5, tag6]
	testName2
	[tag7, tag8, tag9]
	testName3
	...
	
	
	 */
	
	private static Map<String, List<Set<String>>> parseTests() throws FileNotFoundException {

		Map<String, List<Set<String>>> result = new HashMap<String, List<Set<String>>>();
		File testSpecsFile = new File(TestParams.test_specs);
		Scanner scanner = new Scanner(testSpecsFile);
		while(scanner.hasNextLine()) {
			String testName = scanner.nextLine();
//			System.out.println("trovato nome del test: " + testName);
			List<Set<String>> tagGroups = new LinkedList<Set<String>>();
			while(scanner.hasNextLine()) {
				String tagGroup = scanner.nextLine();
				if(tagGroup.equals(""))
					break;
				Set<String> tags = new HashSet<String>();
				tagGroup = tagGroup.substring(tagGroup.indexOf('[')+1);
				int endIndex = Math.min(tagGroup.indexOf(','), tagGroup.indexOf(']'));
				while(endIndex>=0) {
					String tag = tagGroup.substring(0, endIndex);
					tags.add(tag);
					tagGroup = tagGroup.substring(endIndex+1);
					if(tagGroup.indexOf(',')>=0)
						endIndex = Math.min(tagGroup.indexOf(','), tagGroup.indexOf(']'));
					else
						endIndex = tagGroup.indexOf(']');
				}
				tagGroups.add(tags);
			}
			result.put(testName, tagGroups);
			if(!scanner.hasNextLine())
				break;
		}
		return result;
	}

}
