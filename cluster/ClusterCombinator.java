package cluster;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import util.LogHandler;

import combinator.CombinationGenerator;

public class ClusterCombinator {
	
	/* si costruisce passandogli la lista dei clusters da combinare
	 * restituisce una lista di liste di combinazioni dei cluster presi 2 alla volta */
	
	private CombinationGenerator combinator;
	private List<Node> clustersToCombine;
	
	public ClusterCombinator(List<Node> clusters) {
		Logger logger = LogHandler.getLogger(this.getClass().getName());

		this.clustersToCombine = clusters;
		int clusterNumber = clusters.size();
		logger.info("calling combinator with clusterNumber = " + clusterNumber);
		this.combinator = new CombinationGenerator(clusterNumber, 2);
	}
	
	/* ottengo una lista di liste di coppie di nodi, le combinazioni tra cui 
	 * calcolare le distanze */
	
	public LinkedList<LinkedList<Node>> getClusterCombinations() {
		// devo ottenere una lista di liste di cluster, 
		// tutte le combinazioni
//		List<Node> clustersToCombine = new LinkedList<Node>();
//		List<List<Node>> combinationList = new List<List<Node>>();
		LinkedList<LinkedList<Node>> combinationList = new LinkedList<LinkedList<Node>>();
		
		int[] indices;

		while (combinator.hasMore()) {
		  List<Node>  actualClusterCombination = new LinkedList<Node>();
		  indices = combinator.getNext ();
		  for (int i = 0; i < indices.length; i++) {
		    actualClusterCombination.add(clustersToCombine.get(indices[i]));
		  }
//		  CombinationGenerator.clusterPrint(actualClusterCombination);
		  combinationList.add((LinkedList<Node>) actualClusterCombination);
		}
		
		return combinationList;
		
	}
	

}
