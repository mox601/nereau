package cluster.test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import cluster.Clustering;
import cluster.Node;
import cluster.Tree;


public class TreeTest {
	
	private Tree tree;
	private Tree otherTree;
	private Node a;
	private Node b;
	private Node c;
	private Node d;
	private Node e;
	
	private Node i;
	private Node j;
	private Node k;
	private Node l;
	private Node m;
	
	/*test del costruttore con valore stringa*/
	@Test
	public void testRoot() {
		String value = "value";
		Node root = new Node(value);
		Tree tree2 = new Tree(root);
		assertEquals(root, tree2.getRoot()); 
		assertEquals(root.getValue(), tree2.getRoot().getValue()); 
		
	}
	
	@Before
	public void setUp() {
		a = new Node("A");
		b = new Node("B");
		c = new Node("C");
		d = new Node("D");
		e = new Node("E");
		
		i = new Node("I");
		j = new Node("J");
		k = new Node("K");
		l = new Node("L");
		m = new Node("M");
		
		a.addChild(b);
		a.addChild(c);
		c.addChild(d);
		c.addChild(e);
		
		/* Costruisce questo albero
		 * 			a
		 * 		  /  \
		 * 		 b    c
		 * 		   	 /  \
		 * 			d    e
		 * */
		
		
		tree = new Tree(a);

	}
	
	@Test
	public void testContains() {
		assertTrue(tree.contains(b));
	}
	
	@Test
	public void testPathToRoot() {
		List<Node> expectedPathD = new LinkedList<Node>();
		expectedPathD.add(d);
		expectedPathD.add(c);
		expectedPathD.add(a);
		List<Node> resultPathD = tree.pathToRoot(d);
		assertEquals(expectedPathD, resultPathD);

		List<Node> expectedPathE = new LinkedList<Node>();
		expectedPathE.add(e);
		expectedPathE.add(c);
		expectedPathE.add(a);
		List<Node> resultPathE = tree.pathToRoot(e);
		assertEquals(expectedPathE, resultPathE);
		assertEquals(tree.pathToRoot(d), tree.pathToRoot(d));
		
		Node f = new Node("F");
		assertNull(tree.pathToRoot(f));
	}
	
	
	@Test
	public void getSubTree() {
		
		Node subTreeRoot = c; 
		Tree subTree = tree.getSubTree(subTreeRoot);
		assertEquals(subTreeRoot, subTree.getRoot());
		assertEquals(subTree.getRoot().getChildren(), c.getChildren());
	}
	
	/* test per il merge di alberi con una nuova radice specificata, 
	 * e per il metodo getLeaves() */
	@Test
	public void mergeTrees() {
		/*		 f
		 * 		/ \
		 * 	   g   h
		 * create this tree and merge with tree. 
		 * The result should be 
		 * 		 N
		 * 		/  \  
		 *    a     f
		 *   / \   / \
		 *  b	c  g  h
		 *     / \
		 *    d   e
		 * */
		
		/* the parameter in the method specifies the new value of the new root */
		Node f = new Node("F");
		Node g = new Node("G");
		Node h = new Node("H");
		
		Node newRoot = new Node("I");
		
		f.addChild(g);
		f.addChild(h);
		
		Tree firstTree = new Tree(f);
		Tree mergedTree = tree.mergeTreesWithNewRoot(firstTree, newRoot); 
		
		otherTree = mergedTree;
		
		List<Node> oldRoots = new LinkedList<Node>();
		oldRoots.add(a);
		oldRoots.add(f);
		
		assertEquals(oldRoots, mergedTree.getRoot().getChildren());
		
		/* test per getLeaves() */
		
		List<Node> expectedLeaves = new LinkedList<Node>();
		expectedLeaves.add(b);
		expectedLeaves.add(d);
		expectedLeaves.add(e);
		expectedLeaves.add(g);
		expectedLeaves.add(h);
		
		List<Node> obtainedLeaves = mergedTree.getLeaves();
		assertEquals(expectedLeaves, obtainedLeaves);
		
	}


	@Ignore
	@Test
	public void testPrintTree() {
		System.out.println(tree.toString());
	}	
	
	
	@Test
	public void testCutTreeAndGetClustering() {
		
		/* dato un albero con le similarity, 
		 * costruisci una lista di liste di nodi che 
		 * rappresentano il clustering effettuato tagliando l'albero al 
		 * valore similarity specificato */
		
		a.setSimilarity(new Float(0.1));
		b.setSimilarity(new Float(0.2));
		c.setSimilarity(new Float(0.8));
		d.setSimilarity(new Float(1.0));
		e.setSimilarity(new Float(1.0));
		
		System.out.println(tree.toString());
		
		Clustering clustering = tree.cutTreeAtSimilarity(0.8);
		
		for (HashSet<Node> cluster: clustering.getClustering()) {
			System.out.print("<");
			for (Node node: cluster) {
				System.out.print(node.toString() + ", ");
			}
			System.out.println("> ");
		}
		
		i.setSimilarity(new Float(0.5));
		a.addChild(i);
		j.setSimilarity(new Float(0.6));
		b.addChild(j);
		k.setSimilarity(new Float(1.0));
		a.addChild(k);
		
		l.setSimilarity(new Float(1.0));
		m.setSimilarity(new Float(1.0));
		i.addChild(l);
		i.addChild(m);
		
		
		System.out.println(tree.toString());

		
		clustering = tree.cutTreeAtSimilarity(0.2);
		
		for (HashSet<Node> cluster: clustering.getClustering()) {
			System.out.print("<");
			for (Node node: cluster) {
				System.out.print(node.toString() + ", ");
			}
			System.out.println("> ");
		}	
		
		
		
		
		
		
	}
	
	
	@Test
	public void testCutWithMean() {
		//nodi foglia
		Node one = new Node("1", new Float(1.0));
		Node two = new Node("2", new Float(1.0));
		Node three = new Node("3", new Float(1.0));
		Node four = new Node("4", new Float(1.0));
		Node five = new Node("5", new Float(1.0));
		Node six = new Node("6", new Float(1.0));
		Node seven = new Node("7", new Float(1.0));
		Node eight = new Node("8", new Float(1.0));
		
		//fusioni: 
		//1-2
		Node oneTwo = new Node("1-2", new Float(0.9));
		oneTwo.addChild(one);
		oneTwo.addChild(two);
		//3-4
		Node threeFour = new Node("3-4", new Float(0.8));
		threeFour.addChild(three);
		threeFour.addChild(four);
		//3-4 - 5
		Node threeFourFive = new Node("3-4-5", new Float(0.7));
		threeFourFive.addChild(threeFour);
		threeFourFive.addChild(five);
		//7-8
		Node sevenEight = new Node("7-8", new Float(0.6));
		sevenEight.addChild(seven);
		sevenEight.addChild(eight);
		//6 - 7-8
		Node sixSevenEight = new Node("6-7-8", new Float(0.5));
		sixSevenEight.addChild(six);
		sixSevenEight.addChild(sevenEight);
		//3-4-5 - 6-7-8
		Node threeToEight = new Node("3-4-5-6-7-8", new Float(0.4));
		threeToEight.addChild(threeFourFive);
		threeToEight.addChild(sixSevenEight);
		//root
		Node root = new Node("1-2-3-4-5-6-7-8", new Float(0.1));
		root.addChild(oneTwo);
		root.addChild(threeToEight);
		
		Tree tree = new Tree(root);
		
		System.out.println(tree.toString());
		
		
		/* */
		
		Clustering clustering = tree.cutTreeAtSimilarity(0.5);
		HashSet<Clustering> setOfClusterings = new HashSet<Clustering>();
		
		/**/
		System.out.println("clustering tagliando a 0.5");
		
		for (HashSet<Node> cluster: clustering.getClustering()) {
			System.out.print("<");
			for (Node node: cluster) {
				System.out.print(node.toString() + ", ");
			}
			System.out.println("> ");
		}	
		
		Clustering clusteringMean = tree.calculateClusteringByMean();

		System.out.println("clustering ottimo by mean");
		for (HashSet<Node> cluster: clusteringMean.getClustering()) {
			System.out.print("<");
			for (Node node: cluster) {
				System.out.print(node.toString() + ", ");
			}
			System.out.println("> ");
		}	
		
	}

	
	
	
	
	

}
