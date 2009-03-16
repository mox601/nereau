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
		Tree tree = new Tree(root);
		assertEquals(tree.getRoot(), root); 
		assertEquals(tree.getRoot().getValue(), root.getValue()); 
		
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
		assertEquals(resultPathD, expectedPathD);

		List<Node> expectedPathE = new LinkedList<Node>();
		expectedPathE.add(e);
		expectedPathE.add(c);
		expectedPathE.add(a);
		List<Node> resultPathE = tree.pathToRoot(e);
		assertEquals(resultPathE, expectedPathE);
		assertEquals(tree.pathToRoot(d), tree.pathToRoot(d));
		
		Node f = new Node("F");
		assertNull(tree.pathToRoot(f));
	}
	

}
