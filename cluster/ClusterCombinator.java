package cluster;

import java.util.LinkedList;
import java.util.List;

import combinator.CombinationGenerator;

public class ClusterCombinator {
	
	private CombinationGenerator combinator;
	private List<Node> clustersToCombine;
	
	public ClusterCombinator(List<Node> clusters) {
		this.clustersToCombine = clusters;
		int clusterNumber = clusters.size();
		this.combinator = new CombinationGenerator(clusterNumber, 2);
	}
	
	/* ottengo una lista di liste di coppie di nodi, le combinazioni di cui 
	 * calcolare le distanze */
	public List<List<Node>> getClusterCombinations() {
		
		// devo ottenere una lista di liste di cluster, 
		// tutte le combinazioni
		
		List<Node> clustersToCombine = new LinkedList<Node>();
		LinkedList<LinkedList<Node>> combinationList = new LinkedList<LinkedList<Node>>();
	
		int[] indices;

		while (combinator.hasMore ()) {
		  List<Node>  actualClusterCombination = new LinkedList<Node>();
		  indices = combinator.getNext ();
		  for (int i = 0; i < indices.length; i++) {
		    actualClusterCombination.add(clustersToCombine.get(indices[i]));
		  }
		  CombinationGenerator.clusterPrint(actualClusterCombination);
		  combinationList.add((LinkedList<Node>) actualClusterCombination);
		  
		}
		
		
		
		
		
		
		
		
		
		return null;
		
	}
	

}
