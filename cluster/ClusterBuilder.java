package cluster;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import model.RankedTag;

import persistence.PersistenceException;
import persistence.postgres.TagtfidfDAOPostgres;
import persistence.postgres.TreeDAOPostgres;

import util.LogHandler;

public class ClusterBuilder {
	
	/* singleton */
	
	private List<Node> clustersToMerge;
	private Tree actualClustering;
	private TreeDAOPostgres treeHandler;
	private TagtfidfDAOPostgres tagsVisitedUrlHandler;
	
	 private static final ClusterBuilder INSTANCE = new ClusterBuilder();
	 
	   public static ClusterBuilder getInstance() {
	      return INSTANCE;
	   }
	
   // should be a Private constructor: prevents instantiation from other classes
	public ClusterBuilder(List<Node> clusters) {
		this.clustersToMerge = clusters;
		this.treeHandler = new TreeDAOPostgres();
		this.tagsVisitedUrlHandler = new TagtfidfDAOPostgres();
	}
	
	//usato solo nei test
	public ClusterBuilder() {
		this.clustersToMerge = new LinkedList<Node>();
		this.treeHandler = new TreeDAOPostgres();
	}
	
	/* metodo che costruisce i clusters iterativamente 
	 * algoritmo gerarchico di shepitsen. 
	 */
	
	public void buildClusters() {
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		logger.info("START TAGTFIDF CLUSTERING");
		//problema con i decimali, moltiplico per 10 per lavorare meglio
		int similarity = 10;
		int scala = 10;
		
//		System.out.println("nodi da fondere: " + clustersToMerge.toString());
		//i nodi da fondere sono giusti!
		
		while (similarity >= 0 && clustersToMerge.size() > 1) {
//			logger.info("Similarity: " + similarity);
			double similarityValue = (double) similarity / scala;
//			logger.info("Similarity: " + similarity + " diviso 10 = " + similarityValue);
			/* calcola la somiglianza tra tutti i cluster attuali, 
			 * e accorpa quelli con somiglianza uguale alla similarity */
			
			/* prende tutti i cluster attuali */
			/* li passa a un metodo insieme alla somiglianza, e ottiene 
			 * una lista di liste di nodi con somiglianza >= similarity !! */
			LinkedList<LinkedList<Node>> mergingClusters = getClusterWithSimilarity(clustersToMerge, similarityValue);

			/* itera su queste coppie e crea un cluster fusione per ogni coppia */			
//			Iterator<LinkedList<Node>> couplesIterator = mergingClusters.iterator();
			
			for (LinkedList<Node> actualMergingCouple: mergingClusters) {
				/* crea un cluster merge */
				Node newCluster = new Node(actualMergingCouple, similarityValue);			
				/* elimina i due cluster fusi dal clustersToMerge */
//				logger.info("merging clusters: " + actualMergingCouple.getFirst().toString() 
//						 + " AND " + actualMergingCouple.getLast().toString());
				clustersToMerge.remove(actualMergingCouple.getFirst());
				clustersToMerge.remove(actualMergingCouple.getLast());
				/* aggiungi il cluster merged nel clustersToMerge */
				clustersToMerge.add(newCluster);
//				logger.info("ADDED cluster: " + newCluster.getValue());
			}			
			similarity = similarity - 1;
		}

		
//		logger.info("iterazione terminata. ora fondo i cluster che rimangono%%%%%%%%%%%%%%%%");
		
		
		if (clustersToMerge.size() > 0 && similarity <= 0) {
			if (clustersToMerge.size() > 1) {
//				logger.info("c'é una foresta, non un solo albero ");
				
				//elenco tutti i clusters da fondere
//				for(int i=0; i<clustersToMerge.size(); i++) {
//					System.out.println(i + ": " + clustersToMerge.get(i).toString());
//				}
				
				
				//finché ho clusters da fondere
				while (clustersToMerge.size() > 1) {
					/* fondo TUTTI i clusters rimanenti !! 
					 */
					LinkedList<LinkedList<Node>> mergingClusters = getClusterWithSimilarity(clustersToMerge, 0.0);				

					for (LinkedList<Node> actualMergingCouple: mergingClusters) {

						//itera tante volte quante coppie ho.

//						System.out.println("fondo i clusters con somiglianza 0: \n" + 
//								actualMergingCouple.getFirst() + 
//								"\nAND\n" + 
//								actualMergingCouple.getLast());

						Node newCluster = new Node(actualMergingCouple, 0.0);

//						System.out.println("il nuovo nodo fusione é: " + newCluster.toString());
						/* elimina i due cluster fusi dal clustersToMerge */
						//					logger.info("merging clusters: "); 
						//					logger.info(actualMergingCouple.getFirst().toString() 
						//							+ "\nAND \n" + actualMergingCouple.getLast().toString());

						//codice cruciale! 
						clustersToMerge.remove(actualMergingCouple.getFirst());
						clustersToMerge.remove(actualMergingCouple.getLast());

//						System.out.println("ho levato la coppia che ho fuso, ottengo: ");


						//elenco tutti i clusters dopo la fusione
//						System.out.println("clustersToMerge dopo la rimozione della coppia: ");
//					
//						for(int i=0; i<clustersToMerge.size(); i++) {
//							System.out.println(i + ": " + clustersToMerge.get(i).toString());
//						}
	
						/* aggiungi il cluster fusione nel clustersToMerge */
						clustersToMerge.add(newCluster);
	//					logger.info("ADDED cluster: " + newCluster.getValue());



//						System.out.println("clustersToMerge dopo l'aggiunta della fusione: ");
//						for(int i=0; i<clustersToMerge.size(); i++) {
//							System.out.println(i + ": " + clustersToMerge.get(i).toString());
//						}

					}
				}

			} //while 

			
			
			/* esiste un solo nodo, che é la radice dell'albero. */
			this.actualClustering = new Tree(clustersToMerge.get(0));
			logger.info("END CLUSTERING TFIDF");
			/* devo assegnare un id ad ogni nodo dell'albero */
			/* ora funziona con i nested sets */
			actualClustering.assignIds();

			
//			logger.info("clustersToMerge, preso come radice " + clustersToMerge.get(0));
			//rimosso, potrebbe creare problemi con la stampa dell'albero su stringa
//			logger.info("tree-clustering ottenuto: " + actualClustering.toString());
				
			

		} else {
			logger.info("nessun tag nel database, non si effettua il clustering");
			this.actualClustering = new Tree();
		}
		
	}

	
	
	
	
	
	
	
	
	
	
	public void retrieveAllTagsFromDatabase() {
		/* estrae tutti i tags dal database e ne costruisce una lista di
		 *  clusters (Node) */
		Logger logger = LogHandler.getLogger(this.getClass().getName());
//		logger.info("extracting all tags from database");
		this.clustersToMerge = new LinkedList<Node>();
		TagtfidfDAOPostgres tagTfidfHandler = new TagtfidfDAOPostgres();
		List<Tagtfidf> extractedTags = null;
		try {
			extractedTags = tagTfidfHandler.retrieveAllTags();
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		if (extractedTags != null) {
//			logger.info("creating singleton clusters to be merged");
			/* poi costruisce un cluster a partire da ogni tag */
			for (Tagtfidf tag: extractedTags) {
				Node currentNode = new Node(tag.getTag(), tag);
				this.clustersToMerge.add(currentNode); 
//				logger.info("found tagtfidf, convert to Node tag: " + currentNode.toString());
//				logger.info("nodo creato: " + currentNode.toString());
			}

		}

	}	

	/* TODO: in futuro, potrebbe restituire liste? */
	private LinkedList<LinkedList<Node>> getClusterWithSimilarity(
			List<Node> clustersToMerge, double similarity) {

		Logger logger = LogHandler.getLogger(this.getClass().getName());
		
		/* in queste coppie é possibile che ci siano dei tag ripetuti, 
		 * che si devono fondere con due tag diversi: 
		 * a con b
		 * a con c
		 * perché entrambi hanno somiglianza = 1.0. 
		 * */
		
		/* da queste coppie vanno levati questi casi limite. 
		 * NON devono esistere coppie con un elemento almeno in un'altra coppia */
			
		/* genero tutte le combinazioni possibili */
		LinkedList<LinkedList<Node>> listOfAllCouples = new LinkedList<LinkedList<Node>>();
		ClusterCombinator clusterCombinator = new ClusterCombinator(clustersToMerge);
		listOfAllCouples = clusterCombinator.getClusterCombinations();
//		System.out.println("lista di coppie con somiglianza: " + listOfAllCouples);
		/* itero sulle coppie per vedere quali hanno somiglianza = similarity */
		Iterator<LinkedList<Node>> couplesIterator = listOfAllCouples.iterator();
		/* elenco di coppie cluster da restituire */
		LinkedList<LinkedList<Node>> listOfCouples = new LinkedList<LinkedList<Node>>();
		
		while (couplesIterator.hasNext()) {
			LinkedList<Node> actualCouple = couplesIterator.next();
			Tagtfidf secondCentroidTag = actualCouple.getLast().getCentroid();
			Tagtfidf firstCentroidTag = actualCouple.getFirst().getCentroid();
			
			/* confronto i tag dei cluster (centroidi) */
			double interClusterSimilarity = firstCentroidTag.compareToTag(secondCentroidTag);

			if (interClusterSimilarity >= similarity) {
				/* se hanno somiglianza giusta per essere combinati, li aggiungo 
				 * alla listOfCouples, 
				 * ma solo se NESSUNO dei due clusters é giá presente in una delle coppie 
				 * fatte precedentemente */

				boolean safeToAdd = false;
				safeToAdd = isItSafeToAdd(listOfCouples, actualCouple);
				if (safeToAdd) {
					listOfCouples.add(actualCouple);
//					System.out.println("la coppia da fondere é: " + actualCouple);
//					logger.info("somiglianza tra " + firstCentroidTag.getTag() + 
//							" e " + secondCentroidTag.getTag() +": " + interClusterSimilarity);
				}

			}
			
		}
		
		return listOfCouples;
	}

	private boolean isItSafeToAdd(LinkedList<LinkedList<Node>> listOfCouples,
			LinkedList<Node> coupleToTest) {

		boolean isSafe = true;
		
//		System.out.println("la coppia " + coupleToTest); 
//		System.out.println(" puó essere aggiunta in " + 
//				" " + listOfCouples + "?");
		
		String firstTagInCouple = coupleToTest.getFirst().getValue();
		String secondTagInCouple = coupleToTest.getLast().getValue();
		
		for (LinkedList<Node> actualTestingCouple: listOfCouples) {
			
			String firstTagInActualTestingCouple = actualTestingCouple.getFirst().getValue();
			String secondTagInActualTestingCouple = actualTestingCouple.getLast().getValue();
		
			if ( (firstTagInCouple.equals(firstTagInActualTestingCouple)) || 
					(firstTagInCouple.equals(secondTagInActualTestingCouple)) || 
					(secondTagInCouple.equals(firstTagInActualTestingCouple)) ||
					(secondTagInCouple.equals(secondTagInActualTestingCouple)) ) {
				isSafe = false;
			}
	
		}

//		System.out.println(isSafe);		
		
		return isSafe;
	}

	/**
	 * @return the clustersToMerge
	 */
	public List<Node> getClustersToMerge() {
		return clustersToMerge;
	}

	/**
	 * @param clustersToMerge the clustersToMerge to set
	 */
	public void setClustersToMerge(List<Node> clustersToMerge) {
		this.clustersToMerge = clustersToMerge;
	}

	/**
	 * @return the actualClustering
	 */
	public Tree getActualClustering() {
		return actualClustering;
	}

	/**
	 * @param actualClustering the actualClustering to set
	 */
	public void setActualClustering(Tree actualClustering) {
		this.actualClustering = actualClustering;
	}


	public String actualClusteringToString() {
		String clusterString = "";
		if (this.getActualClustering().getRoot() != null) {
			clusterString = this.getActualClustering().toString();
		} else {
			clusterString = "clustering non costruito: nessun tag trovato";
		}
	return clusterString;
	}
	
	

	public void saveActualClustering() {
		try {
			
			if (actualClustering.getRoot() != null) {
				this.treeHandler.save(actualClustering);
			}
			
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}

	
	/* cancella tutti i tagsvisited urls, utile durante i test per cancellarli tra un 
	 * test e l'altro. 
	*/
	
	public void deleteAllTagsFromDatabase() {
		//cancella il clustering di tutti i tags sul database
		//INUTILE: lo fa comunque il clustering
//		try {
//			this.treeHandler.deleteClustering();
//		} catch (PersistenceException e) {
//			e.printStackTrace();
//		}
		
		//cancella tutti gli url visitati e i tag a loro associati di TUTTI gli utenti
		try {
			this.tagsVisitedUrlHandler.deleteTagVisitedUrls();
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
			
	}

}
