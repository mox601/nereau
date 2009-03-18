package cluster.test;

import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import cluster.ClusterCombinator;
import cluster.Node;


public class ClusterCombinatorTest {

	
	
	@Test
	public void clusterCombination() {
		
		
		// devo ottenere una lista di liste di cluster, 
		// tutte le combinazioni
		
		List<Node> clustersToCombine = new LinkedList<Node>();
//		LinkedList<LinkedList<Node>> combinationsList = new LinkedList<LinkedList<Node>>();
		
		Node a = new Node("a");
		Node b = new Node("b");
		Node c = new Node("c");
//		Node d = new Node("d");
//		Node e = new Node("e");
//		Node f = new Node("f");
		
		clustersToCombine.add(a);
		clustersToCombine.add(b);
		clustersToCombine.add(c);
//		clustersToCombine.add(d);
//		clustersToCombine.add(e);
//		clustersToCombine.add(f);
	
		
		
		/* **************************************************** */
		/* provo la classe clustercombinator, quella che user— poi. 
		 * funziona. */
		
		ClusterCombinator combinator = new ClusterCombinator(clustersToCombine);
		LinkedList<LinkedList<Node>> combinations = combinator.getClusterCombinations();
		
		
		  /* stampa i risultati */
//		  for (LinkedList<Node> combination : combinations) {
//			  for (Node cluster: combination){
//				  System.out.println("cluster: " + cluster.toString());
//			  }
//			  System.out.println(); //fine della combinazione
//		  }
		  
		  
		  /* verifico che siano presenti tutte le coppie, creando le liste a mano */
		  /* non Ž molto robusto come test, dovrei trascurare le posizioni delle 
		   * combinazioni: ad esempio: ab = ba 
		   * Ma per come il ClusterCombinator le costruisce, va bene cos’ */
		  
		  //ab
		  LinkedList<Node> ab = new LinkedList<Node>();
		  ab.add(a);
		  ab.add(b);
		  assertTrue(combinations.contains(ab));
		
		  //ac
		  LinkedList<Node> ac = new LinkedList<Node>();
		  ac.add(a);
		  ac.add(c);
		  assertTrue(combinations.contains(ac));
		
		  //bc
		  LinkedList<Node> bc = new LinkedList<Node>();
		  bc.add(b);
		  bc.add(c);
		  assertTrue(combinations.contains(bc));
		
		
		
	}
	
	
	
}
