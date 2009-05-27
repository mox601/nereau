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
	
	/* TODO: dovrebbe essere un singleton? */
	
	private List<Node> clustersToMerge;
	private Tree actualClustering;
	private TreeDAOPostgres treeHandler;
	
	 private static final ClusterBuilder INSTANCE = new ClusterBuilder();
	 
	   public static ClusterBuilder getInstance() {
	      return INSTANCE;
	   }
	
   // should be a Private constructor: prevents instantiation from other classes
	public ClusterBuilder(List<Node> clusters) {
		this.clustersToMerge = clusters;
	}
	
	public ClusterBuilder() {
		this.clustersToMerge = new LinkedList<Node>();
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
		while (similarity >= 0 && clustersToMerge.size() > 1) {
			logger.info("Similarity: " + similarity);
			double similarityValue = (double) similarity / scala;
			logger.info("Similarity: " + similarity + " diviso 10 = " + similarityValue);
			/* calcola la somiglianza tra tutti i cluster attuali, 
			 * e accorpa quelli con somiglianza uguale alla similarity */
			
			/* prende tutti i cluster attuali */
			/* li passa a un metodo insieme alla somiglianza, e ottiene 
			 * una lista di coppie con somiglianza >= similarity !! */
			/* il problema é qui, dovrei poter fondere non coppie, ma liste di nodi */
			LinkedList<LinkedList<Node>> mergingClusters = getClusterWithSimilarity(clustersToMerge, similarityValue);

			/* itera su queste coppie e crea un cluster fusione per ogni coppia */			
//			Iterator<LinkedList<Node>> couplesIterator = mergingClusters.iterator();
			
			for (LinkedList<Node> actualMergingCouple: mergingClusters) {
				/* crea un cluster merge */
				Node newCluster = new Node(actualMergingCouple, similarityValue);			
				/* elimina i due cluster fusi dal clustersToMerge */
				logger.info("merging clusters: " + actualMergingCouple.getFirst().toString() 
						 + " AND " + actualMergingCouple.getLast().toString());
				clustersToMerge.remove(actualMergingCouple.getFirst());
				clustersToMerge.remove(actualMergingCouple.getLast());
				/* aggiungi il cluster merged nel clustersToMerge */
				clustersToMerge.add(newCluster);
				logger.info("ADDED cluster: " + newCluster.getValue());
			}			
			similarity = similarity - 1;
		}

		
		
		if (clustersToMerge.size() > 0) {
			if (clustersToMerge.size() > 1) {
				logger.info("c'é una foresta, e la similarity é a 0: " + 
						clustersToMerge.toString());
				/* fondo tutti i nodi rimanenti */
				LinkedList<LinkedList<Node>> mergingClusters = getClusterWithSimilarity(clustersToMerge, 0.0);				

				for (LinkedList<Node> actualMergingCouple: mergingClusters) {
					Node newCluster = new Node(actualMergingCouple, 0.0);			
					/* elimina i due cluster fusi dal clustersToMerge */
					logger.info("merging clusters: " + actualMergingCouple.getFirst().toString() 
							+ " AND " + actualMergingCouple.getLast().toString());
					clustersToMerge.remove(actualMergingCouple.getFirst());
					clustersToMerge.remove(actualMergingCouple.getLast());
					/* aggiungi il cluster merged nel clustersToMerge */
					clustersToMerge.add(newCluster);
					logger.info("ADDED cluster: " + newCluster.getValue());
				}
			}
			
			
			/* esiste un solo nodo, che é la radice dell'albero. */
			this.actualClustering = new Tree(clustersToMerge.get(0));
			logger.info("END CLUSTERING TFIDF");
			/* devo assegnare un id ad ogni nodo dell'albero */
			/* ora funziona con i nested sets */
			actualClustering.assignIds();

		} else {
			logger.info("nessun tag trovato nel database, non si effettua il clustering");
			this.actualClustering = new Tree();
		}
		
	}

	
	
	
	
	
	public void retrieveAllTagsFromDatabase() {
		/* estrae tutti i tags dal database e ne costruisce una lista di
		 *  clusters (Node) */
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		logger.info("extracting all tags from database");
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
			logger.info("creating singleton clusters to be merged");
			/* poi costruisce un cluster a partire da ogni tag */
			for (Tagtfidf tag: extractedTags) {
				Node currentNode = new Node(tag.getTag(), tag);
				this.clustersToMerge.add(currentNode); 
				logger.info("from Tagtfidf to Node tag: " + currentNode.toString());
			}

		}

	}	


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
			//TODO: check
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

		System.out.println(isSafe);		
		
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

}
