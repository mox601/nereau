
package test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.ExpandedQuery;
import model.Query;
import model.RankedTag;
import model.User;
import model.VisitedURL;
import model.queryexpansion.MultipleUserQueryExpander;
import model.queryexpansion.QueryExpander;
import model.usermodel.UserModelUpdater;
import util.ParameterHandler;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import cluster.ClusterBuilder;

import persistence.PersistenceException;
import persistence.UserDAO;
import persistence.postgres.UserDAOPostgres;

public class SingleTest {
	
	private String testName;
	User testUser;
	private Set<User> dbTestUsers;
	Directory fsDir;
	IndexSearcher is;
	private File logFile;
	private FileWriter fw;
	private BufferedWriter bw;
	private String suffix;
	private ClusterBuilder clusterer;

	
	
	
	/*
	è un test che si fa per un termine in un contesto semantico, determinato dai tag che trovo come nome della
	directory associata. 
	dentro ogni directory (victoria, amazon... )
	tagGroupDirs sono DUE cartelle che rappresentano i DUE significati della parola testName
	per ogni cartella faccio UN UTENTE che (ha cercato, rappresenta) un termine in un dato contesto. 
	*/

	public SingleTest(String testName) {
		this.testName = testName;
		this.testUser = new User(testName,testName);
		this.dbTestUsers = new HashSet<User>();
		File testDir = new File(TestParams.data_dir,testName);
		File[] tagGroupDirs = testDir.listFiles();
		//create db users
		for(File tagGroupDir: tagGroupDirs) {
			if(!tagGroupDir.isDirectory())
				continue;
			this.suffix = "";
			if(ParameterHandler.MAX_FOR_QUERY_TERMS_ONLY_IF_PRESENT)
				this.suffix = " ONLYIFPRESENT";
			if(!ParameterHandler.MAX_OCCURRENCE_VALUE_FOR_QUERY_TERMS)
				this.suffix = " NOMAXFORQUERY";
			
			//ultima modifica!!!! meglio...
			this.suffix = " PURECOOCCURRENCE";
			
			//password = tag group... yes it sucks.
			User tagGroupUser = 
				new User(testName + ' ' + tagGroupDir.getName() + this.suffix,tagGroupDir.getName());
				
			this.dbTestUsers.add(tagGroupUser);
		}
		try {
			this.fsDir = FSDirectory.getDirectory(new File(TestParams.index_dir));
			this.is = new IndexSearcher(fsDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void performTest() throws PersistenceException, IOException, ParseException {
		
		//create log file
		this.logFile = new File(TestParams.data_dir,this.testName + "_um.log");
		if(!this.logFile.exists())
			if(!this.logFile.createNewFile())
				throw new IOException();
		
		//initialize filewriter and bufferedwriter
		this.fw = new FileWriter(logFile);
		this.bw = new BufferedWriter(fw);
		
		System.out.println(this.testName.toUpperCase() + "\n\n");
		bw.write(this.testName.toUpperCase() + "\n\n\n");
		bw.flush();
		
		
		//feed user model with data from tag groups
		//fai il modello utente, visitando le pagine del training
		//posso commentarlo, una volta effettuato. per velocizzare
//		this.feedUserModel();
		
		
		//dopo aver fatto il feed user model, devo lanciare l'algoritmo di clustering
		//che mi salvi sul database i clusters risultanti. 
		
		/* start of clustering */
		System.out.println("INIZIO DEL CLUSTERING DEI TAG E SALVATAGGIO SU DB");
		
		ClusterBuilder clusterer = this.clusterer.getInstance();
		/* retrieve tags and build the singleton clusters to be clustered */
		clusterer.retrieveAllTagsFromDatabase();
		clusterer.buildClusters();
//		Tree actualClustering = clusterer.getActualClustering();
		

		if (clusterer.getActualClustering() != null) {
			System.out.println("SAVING CLUSTERING ON DATABASE");
			clusterer.saveActualClustering();
		}

		System.out.println("FINE DEL CLUSTERING DEI TAG E SALVATAGGIO SUL DATABASE");
		
		
		/* end of clustering */
		
		//perform test 1
		//cartella del test set, per verificare le espansioni
		this.performFirstTest(TestParams.docs_per_evaluation);
		
		//perform test 2
		//prendo i due utenti e li fondo, creandone uno solo che √© un utente che ha visitato tutte le pagine
		//che conterrano i termini in due contesti diversi. molti pi√∫ problemi. 
		//this.performSecondTest();
		
		
		/* al termine del test, restano sul database le associazioni url-tag ottenute 
		 * dopo le visite degli url di training: ogni utente risente del profilo precedente. 
		 * nel caso base, devo eliminarle prima di ogni test */
		
		/* cancello la tabella tagvisitedurls */
//		clusterer.deleteAllTagsFromDatabase();
		
	}
	
	@SuppressWarnings("unused")
	public Set<ExpandedQuery> performSecondTest() {

		QueryExpander qef = new MultipleUserQueryExpander();
		//OLD
//		Map<Query, Set<RankedTag>> expQueries = null;
		//cambiato il metodo expandQuery di QueryExpander
		Set<ExpandedQuery> expQueries = qef.expandQuery(testName, testUser);
		
		
		//for(model.Query q: expQueries.keySet())
		//	System.out.println(q + " ---> " + expQueries.get(q));
		//System.out.println();
		return expQueries;
		
	}

	
	public Map<String,Double[]> performFirstTest() throws IOException, ParseException {
		return this.performFirstTest(TestParams.docs_per_evaluation);
	}
	
	
	// evaluationResults è il numero di risultati iniziali che devo considerare
	public Map<String,Double[]> performFirstTest(int evaluationResults) throws IOException, ParseException {
		
		//create log file
		this.logFile = new File(TestParams.data_dir,this.testName + "_test1" + this.suffix + ".log");
		if(!this.logFile.exists())
			if(!this.logFile.createNewFile())
				throw new IOException();
		
		//initialize filewriter and bufferedwriter
		this.fw = new FileWriter(logFile);
		this.bw = new BufferedWriter(fw);
		
		//initialize result map
		Map<String,Double[]> result = new HashMap<String,Double[]>();
		
		System.out.println(this.testName.toUpperCase() + "\n\n");
		bw.write(this.testName.toUpperCase() + "\n\n\n");
		bw.flush();
		
		System.out.println("FIRST TEST");
		bw.write("FIRST TEST\n\n");
		bw.flush();
		
		for(User user: this.dbTestUsers) {
			
			String currentTagGroup = user.getPassword();
			System.out.println("Tag Group: " + user.getPassword());
			bw.write("Tag Group: " + user.getPassword() + "\n\n");
			bw.flush();
			
			//search results without expansion (trivial)
			System.out.println("Results without Query Expansion:");
			bw.write("Results without Query Expansion:\n\n");
			bw.flush();
			
			//lucene
			QueryParser qp = new QueryParser("contents",new StemmingAnalyzer());
			Hits noExp = this.is.search(qp.parse(testName));
			int hitsLimit = Math.min(noExp.length(), evaluationResults);
			System.out.println(noExp.length() + " distinct docs retrieved (" + hitsLimit + " shown):");
			bw.write(noExp.length() + " distinct docs retrieved (" + hitsLimit + " shown):\n");
			bw.flush();
			int countFirstCorrectResults = 0;
			int countAllCorrectResults = 0;
			
			//results senza espansione
			for(int i=0; i<noExp.length(); i++) {
				Document doc = noExp.doc(i);
				//evaluationResults è il numero di risultati iniziali che devo considerare
				//se i √© minore, allora verifica se 
				if(i<evaluationResults) {
					System.out.println(doc.get("taggroup") + " " + doc.get("filename"));
					bw.write(doc.get("taggroup") + " " + doc.get("filename") + '\n');
					bw.flush();
				}
				//
				if(doc.get("taggroup").equals(currentTagGroup)) {
					countAllCorrectResults++;
			  		if(i<evaluationResults)
						countFirstCorrectResults++;
				}
			}
			
			//nei primi N che mostro, quanti ricadono tra quelle 50 pagine "giuste"
			//che stanno nel noise
			double precisionNOEXP = (double)countFirstCorrectResults / evaluationResults;
			//quante pagine giuste totali riesco ad estrarre con la query su tutte le pagine "giuste"
			double recallNOEXP = (double)countAllCorrectResults / TestParams.test_docs_per_tag_group;
			
			double fmeasureNOEXP = (2.0*precisionNOEXP*recallNOEXP)/(precisionNOEXP+recallNOEXP);
			System.out.println("Precision: " + precisionNOEXP + "\nRecall: " + recallNOEXP);
			bw.write("Precision: " + precisionNOEXP + "\nRecall: " + recallNOEXP + "\n");
			System.out.println("F1-measure: " + fmeasureNOEXP);
			bw.write("F1-measure: " + fmeasureNOEXP + "\n\n");
			bw.flush();
			
			
			
			
			
			
			
			
			/* RISULTATI DI NEREAU 0.7 OLD */
			
			//risultati suddivisi in base al numero e al peso delle espansioni che ottengo 
			//da nereau. 
			//search results with expansions (not so trivial...)
			System.out.println("Results with Query Expansion:");
			bw.write("Results with Query Expansion:\n\n");
			bw.flush();
			QueryExpander qef = new QueryExpander();
			
			//proviamo a cambiare questo codice, lasciando le espansioni invariate
//			Map<Query,Set<RankedTag>> expQueries = qef.expandQuery(testName, user);
			/* ****** ESPANSIONE OLD NEREAU 0.7 ****** */
			Set<ExpandedQuery> expQueries = qef.expandQuery(testName, user);
			
			Set<Document> allDocs;
			List<Document> selectedDocs = new LinkedList<Document>();
			
			//changed arrangeResult to work with Set<ExpandedQuery>
			allDocs = this.arrangeResults(expQueries,selectedDocs,evaluationResults);
			//ho raccolto tutti i documenti che si ottengono facendo la query
			
			System.out.println(allDocs.size() + " distinct docs retrieved (" + selectedDocs.size() + " shown):");
			bw.write(allDocs.size() + " distinct docs retrieved (" + selectedDocs.size() + " shown):\n");
			bw.flush();
			
			/* ho estratto i documenti, ora calcolo le misure di precision e recall */
			
			
			countFirstCorrectResults = 0;
			countAllCorrectResults = 0;
			for(Document doc: selectedDocs) {
				System.out.println(doc.get("taggroup") + " " + doc.get("filename"));
				bw.write(doc.get("taggroup") + " " + doc.get("filename") + '\n');
				bw.flush();
				if(doc.get("taggroup").equals(currentTagGroup))
					countFirstCorrectResults++;
			}
			for(Document doc: allDocs) {
				if(doc.get("taggroup").equals(currentTagGroup))
					countAllCorrectResults++;
			}
			double precisionEXP = (double)countFirstCorrectResults / evaluationResults;
			double recallEXP = (double)countAllCorrectResults / TestParams.test_docs_per_tag_group;
			double fmeasureEXP = (2.0*precisionEXP*recallEXP)/(precisionEXP+recallEXP);
			System.out.println("Precision: " + precisionEXP + "\nRecall: " + recallEXP);
			bw.write("Precision: " + precisionEXP + "\nRecall: " + recallEXP + "\n");
			System.out.println("F1-measure: " + fmeasureEXP);
			bw.write("F1-measure: " + fmeasureEXP + "\n\n");
			bw.flush();
			
			
			
			
			
			
			//TODO: integra, integra!!
			/* RISULTATI DI NEREAU 0.7 NEW  */

			System.out.println("Results with Query Expansion and Tag Clustering:");
			bw.write("Results with Query Expansion and Tag Clustering:\n\n");
			bw.flush();
			QueryExpander qefTfidf = new QueryExpander();
			//qui le espansioni devono avere dei RankedTags PESATI. DONE
			Set<ExpandedQuery> expQueriesTfidf = qefTfidf.expandQueryTfidf(testName, user);

			Set<Document> allDocsTfidf;
			List<Document> selectedDocsTfidf = new LinkedList<Document>();
			
			//changed arrangeResult to work with Set<ExpandedQuery>
			allDocsTfidf = this.arrangeResults(expQueriesTfidf,selectedDocsTfidf,evaluationResults);
			//ho raccolto tutti i documenti che si ottengono facendo la query
			
			System.out.println(allDocsTfidf.size() + " distinct docs retrieved (" + selectedDocsTfidf.size() + " shown):");
			bw.write(allDocsTfidf.size() + " distinct docs retrieved (" + selectedDocsTfidf.size() + " shown):\n");
			bw.flush();
			
			/* ho estratto i documenti, ora calcolo le misure di precision e recall */
			
			countFirstCorrectResults = 0;
			countAllCorrectResults = 0;
			for(Document doc: selectedDocsTfidf) {
				System.out.println(doc.get("taggroup") + " " + doc.get("filename"));
				bw.write(doc.get("taggroup") + " " + doc.get("filename") + '\n');
				bw.flush();
				if(doc.get("taggroup").equals(currentTagGroup))
					countFirstCorrectResults++;
			}
			for(Document doc: allDocsTfidf) {
				if(doc.get("taggroup").equals(currentTagGroup))
					countAllCorrectResults++;
			}
			double precisionEXPTFIDF = (double)countFirstCorrectResults / evaluationResults;
			double recallEXPTFIDF = (double)countAllCorrectResults / TestParams.test_docs_per_tag_group;
			double fmeasureEXPTFIDF = (2.0*precisionEXPTFIDF*recallEXPTFIDF)/(precisionEXPTFIDF+recallEXPTFIDF);
			System.out.println("Precision: " + precisionEXPTFIDF + "\nRecall: " + recallEXPTFIDF);
			bw.write("Precision: " + precisionEXPTFIDF + "\nRecall: " + recallEXPTFIDF + "\n");
			System.out.println("F1-measure: " + fmeasureEXPTFIDF);
			bw.write("F1-measure: " + fmeasureEXPTFIDF + "\n\n");
			bw.flush();
		
			
			

			/* ********END_NEW************* */
			

			//update results
			Double[] measures = {fmeasureNOEXP,fmeasureEXP, fmeasureEXPTFIDF};
			String testname = user.getUsername();
			result.put(testname, measures);
			
		}
		return result;
		
	}

	/* dispone i risultati: 
	 * restituisce un insieme di documenti, a partire da un insieme di query espanse. 
	 * È utile nel caso di nereau vecchio, nel mio caso ho solo x espansioni, che non 
	 * hanno peso peró. Devo avere una diversa politica di riordinamento. */	
	private Set<Document> arrangeResults(Set<ExpandedQuery> expQueries, List<Document> selectedDocs, int evaluationResults) throws IOException, ParseException {

		//risultati ottenuti a fronte di ogni expQuery
		Map<model.Query,Hits> expHits = new HashMap<model.Query,Hits>();
		//rank di ogni query
		Map<model.Query,Double> expQueryRanks = new HashMap<model.Query,Double>();
		//numero di risultati garantiti per ogni query (nella lista filtrata)
		Map<model.Query,Integer> expQueryResNums = new HashMap<model.Query,Integer>();
		//lista delle query ordinate per rilevanza
		List<model.Query> sortedExpQueries = new LinkedList<model.Query>();
		//somma dei rank (per normalizzare rispetto alla dim. della lista filtrata)
		double queryRankSum = 0.0;
		System.out.println("List of expanded queries:");
		bw.write("List of expanded queries:\n");
		bw.flush();
		
		
		//calcola la somma dei pesi DEI TAGS di OGNI QUERY
		//esegui la ricerca con la query espansa usando lucene
		for(model.Query expQuery: expQueries) {
			QueryParser qp = new QueryParser("contents",new StemmingAnalyzer());
			Hits expHit = this.is.search(qp.parse(expQuery.toString()));
			expHits.put(expQuery, expHit);
			double expQueryRank = 0.0;
			
			//per ogni RANKEDTAG presente nella query espansa corrente
			for(RankedTag rt: expQuery.getExpansionTags())
				if(rt.getRanking()>expQueryRank)
					expQueryRank = rt.getRanking();
			
			//metto in una mappa con il suo ranking
			expQueryRanks.put(expQuery, expQueryRank);
			queryRankSum += expQueryRank;
			
			System.out.println("query: " + expQuery.toString() + " ranking dei tags: " + expQueryRank);
//			System.out.println("somma dei ranking di tutte le queries: " + queryRankSum);
		}
		//ho calcolato la queryRankSum, utile per pesare il valore dell'espansione. 
		
		//normalize si usa per moltiplicare il rank di ogni expQuery
		double normalize = (double)evaluationResults/(queryRankSum);
		System.out.println(normalize + " = " + evaluationResults + " / " + queryRankSum);
		
		//aggiorna i rankings delle query espanse in base al valore normalize
		for(model.Query expQuery: expQueryRanks.keySet()) {
			double rank = expQueryRanks.get(expQuery);
			rank *= normalize;
			System.out.println(expQuery + " (rank=" + rank + ") for tags: " + expQuery.toString());
			bw.write(expQuery + "(rank=" + rank + ") for tags: " + expQuery.toString() + "\n");
			bw.flush();
			expQueryRanks.put(expQuery, rank);
		}
		
		/* riordino le query per rilevanza */
		
		//ordino le query per rilevanza (scritto malissimo. pessimo.)
		List<model.Query> temp = new LinkedList<model.Query>(expQueries);
		System.out.println("\nQueries (sorted by relevance):");
		bw.write("\nQueries (sorted by relevance):\n");
		bw.flush();
		while(temp.size()>0) {
			model.Query minRank = temp.get(0);
			for(model.Query q: temp)
				if(expQueryRanks.get(q)<expQueryRanks.get(minRank))
					minRank = q;
			temp.remove(minRank);
			sortedExpQueries.add(minRank);
			System.out.println(minRank + " (rank=" + expQueryRanks.get(minRank) + 
					",hits=" + expHits.get(minRank).length() + ")");
			bw.write(minRank + " (rank=" + expQueryRanks.get(minRank) + 
					",hits=" + expHits.get(minRank).length() + ")\n");
			bw.flush();
		}
		
		//prima fase, parto dal piú basso, assegno resNums temporanei
		System.out.println("\nTemporary results per query (1):");
		//devo assegnare ancora remainingRes risultati
		int remainingRes = evaluationResults;
		for(model.Query expQ: sortedExpQueries) {
			int tempResNum = (int)expQueryRanks.get(expQ).doubleValue();
			remainingRes -= tempResNum;
			expQueryResNums.put(expQ, tempResNum);
			System.out.println(expQ + " ---> " + tempResNum + " results.");
			bw.write(expQ + " ---> " + tempResNum + " results.\n");
			bw.flush();
		}
		
		//seconda fase, parto dal piú basso, togliendo i risultati in eccesso
		//(rispetto a quelli effettivamente reperiti)
		System.out.println("\nTemporary results per query (2):");
		int availableRes = 0;
		for(model.Query expQ: sortedExpQueries) {
			int tempResNum = expQueryResNums.get(expQ);
			tempResNum += availableRes;
			availableRes = 0;
			if(tempResNum > expHits.get(expQ).length()) {
				availableRes = tempResNum - expHits.get(expQ).length();
				tempResNum = expHits.get(expQ).length();
			}
			expQueryResNums.put(expQ, tempResNum);
			System.out.println(expQ + " ---> " + tempResNum + " results.");
		}
		
		//terza fase, parto dal piú alto (assegno ulteriori risultati fino ad esaurirli)
		remainingRes += availableRes;
		for(int i=sortedExpQueries.size()-1; i>=0 && remainingRes>0; i--) {
			model.Query expQ = sortedExpQueries.get(i);
			int tempResNum = expQueryResNums.get(expQ);
			if(tempResNum < expHits.get(expQ).length()) {
				int toBeAdded = Math.min(remainingRes, expHits.get(expQ).length()-tempResNum);
				tempResNum += toBeAdded;
				remainingRes -= toBeAdded;
			}
			expQueryResNums.put(expQ, tempResNum);
		}
		
		System.out.println("\nResults per query:");
		bw.write("\nResults per query:\n");
		bw.flush();
		
		for(model.Query expQ: sortedExpQueries) {
			System.out.println(expQ + " ---> " + expQueryResNums.get(expQ) + " results.");
			bw.write(expQ + " ---> " + expQueryResNums.get(expQ) + " results.\n");
			bw.flush();
		}
		
		
		//ora per ogni query ho il numero di risultati da considerare
		//(ma dovevo fare tutto questo casino?)
		
		Set<Document> allDocs = new HashSet<Document>();
		Set<String> allDocsPaths = new HashSet<String>();
		Set<String> selectedDocsPaths = new HashSet<String>();
		boolean firstIteration = true;
		//create list with selected results//
		while(selectedDocs.size()<evaluationResults) {
			boolean noMoreResults = true;
			for(model.Query expQuery: sortedExpQueries) {
				Hits hits = expHits.get(expQuery);
				int chosenRes = 0;
				int allowedRes = expQueryResNums.get(expQuery);
				for(int i=0; i<hits.length() && chosenRes<allowedRes; i++) {
					if(!selectedDocsPaths.contains(hits.doc(i).get("filename"))) {
						if(firstIteration)
							selectedDocs.add(0,hits.doc(i));
						else
							selectedDocs.add(hits.doc(i));
						selectedDocsPaths.add(hits.doc(i).get("filename"));
						chosenRes++;
					}
				}
				if(chosenRes>0) {
					noMoreResults = false;
					expQueryResNums.put(expQuery, allowedRes-chosenRes);
				}
			}
			
			if(noMoreResults)
				break;
			
			firstIteration = false;
			
		}
		
		//create set with all retrieved docs
		for(model.Query expQuery: expHits.keySet()) {
			Hits hits = expHits.get(expQuery);
			for(int i=0; i<hits.length(); i++) {
				if(!allDocsPaths.contains(hits.doc(i).get("filename"))) {
					allDocs.add(hits.doc(i));
					allDocsPaths.add(hits.doc(i).get("filename"));
				}
			}
		}
		
		System.out.println();
		bw.write("\n\n");
		bw.flush();
		
		return allDocs;
	}
	
	
	
	
	
	private Set<Document> arrangeResultsTfidf(Set<ExpandedQuery> expQueries, List<Document> selectedDocs, int evaluationResults) throws IOException {
		
		//risultati ottenuti a fronte di ogni expQuery
		Map<model.Query,Hits> expHits = new HashMap<model.Query,Hits>();
		//rank di ogni query
		Map<model.Query,Double> expQueryRanks = new HashMap<model.Query,Double>();
		//numero di risultati garantiti per ogni query (nella lista filtrata)
		Map<model.Query,Integer> expQueryResNums = new HashMap<model.Query,Integer>();
		//lista delle query ordinate per rilevanza
		List<model.Query> sortedExpQueries = new LinkedList<model.Query>();
		//somma dei rank (per normalizzare rispetto alla dim. della lista filtrata)
		double queryRankSum = 0.0;
		System.out.println("List of expanded queries TFIDF:");
		bw.write("List of expanded queries with TFIDF:\n");
		bw.flush();
		
		
		
		Set<Document> allDocs = new HashSet<Document>();
		Set<String> allDocsPaths = new HashSet<String>();
		Set<String> selectedDocsPaths = new HashSet<String>();
		boolean firstIteration = true;
		//create list with selected results
		while(selectedDocs.size()<evaluationResults) {
			
			boolean noMoreResults = true;
			
			for(model.Query expQuery: sortedExpQueries) {
				Hits hits = expHits.get(expQuery);
				int chosenRes = 0;
				int allowedRes = expQueryResNums.get(expQuery);
				for(int i=0; i<hits.length() && chosenRes<allowedRes; i++) {
					if(!selectedDocsPaths.contains(hits.doc(i).get("filename"))) {
						if(firstIteration)
							selectedDocs.add(0,hits.doc(i));
						else
							selectedDocs.add(hits.doc(i));
						selectedDocsPaths.add(hits.doc(i).get("filename"));
						chosenRes++;
					}
				}
				if(chosenRes>0) {
					noMoreResults = false;
					expQueryResNums.put(expQuery, allowedRes-chosenRes);
				}
			}
			
			if(noMoreResults)
				break;
			
			firstIteration = false;
			
		}
		
		//create set with all retrieved docs
		for(model.Query expQuery: expHits.keySet()) {
			Hits hits = expHits.get(expQuery);
			for(int i=0; i<hits.length(); i++) {
				if(!allDocsPaths.contains(hits.doc(i).get("filename"))) {
					allDocs.add(hits.doc(i));
					allDocsPaths.add(hits.doc(i).get("filename"));
				}
			}
		}
		
		System.out.println();
		bw.write("\n\n");
		bw.flush();
		
		return allDocs;
		
		
		
	}
	
	
	
	
	

	@SuppressWarnings("unused")
	public void feedUserModel() throws PersistenceException, IOException {
		
		
		//create log file
		this.logFile = new File(TestParams.data_dir,this.testName + "_um" + this.suffix + ".log");
		if(!this.logFile.exists())
			if(!this.logFile.createNewFile())
				throw new IOException();
		
		//initialize filewriter and bufferedwriter
		this.fw = new FileWriter(logFile);
		this.bw = new BufferedWriter(fw);
		
		System.out.println(this.testName.toUpperCase() + "\n\n");
		bw.write(this.testName.toUpperCase() + "\n\n\n");
		bw.flush();
		
		UserDAO ud = new UserDAOPostgres();
		File testDir = new File(TestParams.data_dir,testName);
		
		System.out.println("FEED USER MODEL\n");
		bw.write("FEED USER MODEL\n\n");
		bw.flush();
		
		//delete already existing data
		ud.deleteUser(testUser);
		for(User user: dbTestUsers)
			ud.deleteUser(user);
		
		//create new user in the database
		for(User user: dbTestUsers) {
			
			ud.saveUser(user);
			File tagGroupDir = new File(testDir,user.getPassword());
			File trainingDir = new File(tagGroupDir,TestParams.training_dir);
			Set<File> dataDirs = new HashSet<File>();
			dataDirs.add(trainingDir);
			
			this.updateUserModel(user,dataDirs);
			
		}
		
	}
	
	private void updateUserModel(User testUser, Set<File> dataDirs) throws IOException {
		
		
		System.out.println("update user model for " + testUser.getUsername());
		bw.write("update user model for " + testUser.getUsername() + "\n");
		bw.flush();
		
		UserModelUpdater umu = new UserModelUpdater();
		
		//FIXME: per ridurre i tempi di test, poi si cambia
			
		int totalPagesToVisit = 5;
		
		for(File dataDir: dataDirs) {
			int pagesVisited = 0;			
			for(File doc: dataDir.listFiles()) {
				//totalPages
				if(!doc.getName().startsWith("tags_") && pagesVisited < totalPagesToVisit) {
					List<VisitedURL> vUrls = new LinkedList<VisitedURL>();
					VisitedURL vUrl;
					model.Query query = new model.Query(this.testName);
					vUrl = new VisitedURL(doc.getAbsolutePath(),null,null,query);
					vUrls.add(vUrl);
					System.out.println("extracting data from '" + doc.getName() + "'... pageNumber " + pagesVisited);
					bw.write("extracting data from '" + doc.getName() + "'... ");
					bw.flush();
					umu.update(testUser, vUrls);
					
					pagesVisited++;
										
					System.out.println("done.");
					bw.write("done.\n");
					bw.flush();
				}
			}
		}
		
		System.out.println("user model updated.\n");
		bw.write("user model updated.\n\n");
		bw.flush();
		
		
		
	}

	public static void main(String[] args) throws PersistenceException, IOException, ParseException {
		
		SingleTest st = new SingleTest("victoria");
		st.performTest();
		
	}

}
