package cluster;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import persistence.PersistenceException;
import persistence.postgres.TagtfidfDAOPostgres;
import persistence.postgres.TreeDAOPostgres;

import util.LogHandler;

public class ClusterBuilder {
	
	private List<Node> clustersToMerge;
	private Tree actualClustering;
	private TreeDAOPostgres treeHandler;
	
	public ClusterBuilder(List<Node> clusters) {
		this.clustersToMerge = clusters;
	}
	
	public ClusterBuilder() {
		this.clustersToMerge = new LinkedList<Node>();
	}
	
	/* metodo che costruisce i clusters iterativamente 
	 * algoritmo gerarchico di shepitsen. 
	 * TODO: Deve restituire un Tree, con una root. */
	
	public void buildClusters() {
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		logger.info("START TAGTFIDF CLUSTERING");
		double similarity = 1.0;
		while (similarity > 0.0) {
			logger.info("current similarity: " + similarity);
			/* calcola la somiglianza tra tutti i cluster attuali, 
			 * e accorpa quelli con somiglianza uguale alla similarity */
			
			/* prende tutti i cluster attuali */
			/* li passa a un metodo insieme alla somiglianza, e ottiene 
			 * una lista di coppie con somiglianza = similarity */
			LinkedList<LinkedList<Node>> mergingClusters = getClusterWithSimilarity(clustersToMerge, similarity);

			/* itera su queste coppie e crea un cluster fusione per ogni coppia */			
			Iterator<LinkedList<Node>> couplesIterator = mergingClusters.iterator();
			while(couplesIterator.hasNext()) {
				LinkedList<Node> actualMergingCouple = couplesIterator.next();
				/* crea un cluster merge */
				Node newCluster = new Node(actualMergingCouple, similarity);
				int a = actualMergingCouple.getFirst().getIdNode();
				int b = actualMergingCouple.getLast().getIdNode();
				/* TODO: trova modo di assegnare un id unico basandosi sugli id 
				 * dei figli... */
//				newCluster.setIdNode(a + b);
				/* elimina i due cluster fusi dal clustersToMerge */
				logger.info("merging clusters: " + actualMergingCouple.getFirst() 
						 + " AND " + actualMergingCouple.getLast());
				clustersToMerge.remove(actualMergingCouple.getFirst());
				clustersToMerge.remove(actualMergingCouple.getLast());
				/* aggiungi il cluster merged nel clustersToMerge */
				clustersToMerge.add(newCluster);
			}			
			similarity = similarity - 0.15;
		}
		/* esiste un solo nodo, che é la radice dell'albero. */
		this.actualClustering = new Tree(clustersToMerge.get(0));
		logger.info("END CLUSTERING TFIDF");

		
		/* devo assegnare un id ad ogni nodo dell'albero, altrimenti nel 
		 * salvataggio sul database non saprei come modellare la relazione
		 * figlio-padre: quale id metto nella colonna father del figlio? */
		
		
		/* ora funziona con i nested sets */
		actualClustering.assignIds();
		
		/* ora si puó salvare il risultato del clustering gerarchico nel database */
		logger.info("saving hierarchical clustering on database");

//		this.treeHandler.saveClusteringOnDatabase(actualClustering);
		
	}
	
	
	public void retrieveAllTagsFromDatabase() {
		/* estrae tutti i tags dal database e ne costruisce una lista di
		 *  clusters (Node) */
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		logger.info("extracting all tags from database");
		this.clustersToMerge = new LinkedList<Node>();
		TagtfidfDAOPostgres tagTfidfHandler = new TagtfidfDAOPostgres();
		List<Tagtfidf> extractedTags = new LinkedList<Tagtfidf>();
		try {
			extractedTags = tagTfidfHandler.retrieveAllTags();
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.info("creating singleton clusters to be clustered");
		

		/* poi costruisce un cluster a partire da ogni tag */
		for (Tagtfidf tag: extractedTags) {
			Node currentNode = new Node(tag.getTag(), tag);
			this.clustersToMerge.add(currentNode); 
			logger.info("tag: " + currentNode.getCentroid().toString() + "with id: " + currentNode.getIdNode());
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
		
		/* TODO: da queste coppie vanno levati questi casi limite. 
		 * NON devono esistere coppie con un elemento almeno in un'altra coppia */
			
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
			
			if (interClusterSimilarity >= similarity) {
				/* se hanno somiglianza giusta per essere combinati, li aggiungo 
				 * alla listOfCouples 
				 * solo se nessuno dei due tag é giá presente in una delle coppie 
				 * fatte precedentemente */

				boolean safeToAdd = false;
				safeToAdd = isItSafeToAdd(listOfCouples, actualCouple);
				if (safeToAdd) {
					listOfCouples.add(actualCouple);
					logger.info("somiglianza tra " + firstCentroidTag.getTag() + 
							" e " + secondCentroidTag.getTag() +": " + interClusterSimilarity);
				}

			}
			
		}
		
		return listOfCouples;
	}

	private boolean isItSafeToAdd(LinkedList<LinkedList<Node>> listOfCouples,
			LinkedList<Node> coupleToTest) {

		boolean isSafe = true;
		
		for (LinkedList<Node> couple: listOfCouples) {
			String firstTagInCouple = couple.getFirst().getValue();
			String secondTagInCouple = couple.getLast().getValue();
			
			if ( (firstTagInCouple == couple.getFirst().getValue()) || 
					(firstTagInCouple == couple.getLast().getValue()) || 
					(secondTagInCouple == couple.getFirst().getValue()) || 
					(secondTagInCouple == couple.getLast().getValue()) ) {
				isSafe = false;
			}
		}
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

}
