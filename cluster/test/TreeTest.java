package cluster.test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import cluster.Node;
import cluster.Tree;


public class TreeTest {
	
	private Tree tree; 
	private Node a;
	private Node b;
	private Node c;
	private Node d;
	private Node e;
	
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
	

}
