package cluster;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ClusterBuilder {
	
	private List<Node> clustersToMerge;
	
	public ClusterBuilder(List<Node> clusters) {
		this.clustersToMerge = clusters;
	}
	
	/* metodo che costruisce i clusters iterativamente 
	 * algoritmo gerarchico di shepitsen. 
	 * TODO: Deve restituire un Tree, con una root. */
	public void buildClusters() {
		System.out.println("INIZIO");
		double similarity = 1.0;
		while (similarity > 0.0) {
			System.out.println("similarity: " + similarity);
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
				/* elimina i cluster fusi dal clustersToMerge */
				clustersToMerge.remove(actualMergingCouple.getFirst());
				clustersToMerge.remove(actualMergingCouple.getLast());
				/* aggiungi il cluster merged nel clustersToMerge */
				clustersToMerge.add(newCluster);
			}			
			similarity = similarity - 0.15;
		}
		System.out.println("FINE");
		
	}

	private LinkedList<LinkedList<Node>> getClusterWithSimilarity(
			List<Node> clustersToMerge, double similarity) {
		
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
			
			if (interClusterSimilarity == similarity) {
				/* se hanno somiglianza giusta per essere combinati, li aggiungo 
				 * alla listOfCouples */
				listOfCouples.add(actualCouple);
			}
			
		}
		
		return listOfCouples;
	}

}
