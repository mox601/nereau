package combinator;
//--------------------------------------
//Systematically generate combinations.
//--------------------------------------

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import cluster.ClusterCombinator;
import cluster.Node;

public class CombinationGenerator {

private int[] a;
private int n;
private int r;
private BigInteger numLeft;
private BigInteger total;

//------------
// Constructor
//------------

public CombinationGenerator (int n, int r) {
 if (r > n) {
   throw new IllegalArgumentException ();
 }
 if (n < 1) {
   throw new IllegalArgumentException ();
 }
 this.n = n;
 this.r = r;
 a = new int[r];
 BigInteger nFact = getFactorial (n);
 BigInteger rFact = getFactorial (r);
 BigInteger nminusrFact = getFactorial (n - r);
 total = nFact.divide (rFact.multiply (nminusrFact));
 reset ();
}

//------
// Reset
//------

public void reset () {
 for (int i = 0; i < a.length; i++) {
   a[i] = i;
 }
 numLeft = new BigInteger (total.toString ());
}

//------------------------------------------------
// Return number of combinations not yet generated
//------------------------------------------------

public BigInteger getNumLeft () {
 return numLeft;
}

//-----------------------------
// Are there more combinations?
//-----------------------------

public boolean hasMore () {
 return numLeft.compareTo (BigInteger.ZERO) == 1;
}

//------------------------------------
// Return total number of combinations
//------------------------------------

public BigInteger getTotal () {
 return total;
}

//------------------
// Compute factorial
//------------------

private static BigInteger getFactorial (int n) {
 BigInteger fact = BigInteger.ONE;
 for (int i = n; i > 1; i--) {
   fact = fact.multiply (new BigInteger (Integer.toString (i)));
 }
 return fact;
}

//--------------------------------------------------------
// Generate next combination (algorithm from Rosen p. 286)
//--------------------------------------------------------

public int[] getNext () {

 if (numLeft.equals (total)) {
   numLeft = numLeft.subtract (BigInteger.ONE);
   return a;
 }

 int i = r - 1;
 while (a[i] == n - r + i) {
   i--;
 }
 a[i] = a[i] + 1;
 for (int j = i + 1; j < r; j++) {
   a[j] = a[i] + j - i;
 }

 numLeft = numLeft.subtract (BigInteger.ONE);
 return a;

}

public static void main(String[] args) {
	
	// devo ottenere una lista di liste di cluster, 
	// tutte le combinazioni
	
	String[] elements = {"a", "b", "c", "d"};
	List<Node> clustersToCombine = new LinkedList<Node>();
	LinkedList<LinkedList<Node>> combinationList = new LinkedList<LinkedList<Node>>();
	
	Node a = new Node("a");
	Node b = new Node("b");
	Node c = new Node("c");
	Node d = new Node("d");
	Node e = new Node("e");
	Node f = new Node("f");
	
	clustersToCombine.add(a);
	clustersToCombine.add(b);
	clustersToCombine.add(c);
//	clusters.add(d);
//	clusters.add(e);
//	clusters.add(f);
	
	int[] indices;
	CombinationGenerator y = new CombinationGenerator (clustersToCombine.size(), 2);
	

	while (y.hasMore ()) {
	  List<Node>  clusterCombination = new LinkedList<Node>();
	  indices = y.getNext ();
	  for (int i = 0; i < indices.length; i++) {
	    clusterCombination.add(clustersToCombine.get(indices[i]));
	  }
	  clusterPrint(clusterCombination);
	  combinationList.add((LinkedList<Node>) clusterCombination);
	  
	}
	
	
	/* **************************************************** */
	/* provo la classe clustercombinator, quella che user— poi */
	
	ClusterCombinator combinator = new ClusterCombinator(clustersToCombine);
	
	List<List<Node>> combinations = combinator.getClusterCombinations();
	
	
	
}

public static void clusterPrint(List<Node> clusterCombination) {
	// TODO Auto-generated method stub
	System.out.print("{ ");
	
	for (Node cluster : clusterCombination) {
		String value = cluster.getValue();
		System.out.print(value + " ");
	}
	System.out.println("}");
	
}




}

