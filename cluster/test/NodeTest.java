package cluster.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import cluster.Node;

public class NodeTest {
	
	private Node a;
	private Node b;
	private Node c;
	
	@Before
	public void setUp() {
		a = new Node("A"); 
		b = new Node("B");
		c = new Node("C");
	}
	
	
	/*
	@Test
	public void simpleAdd() {
		int result = 1; 
		int expected = 1; 
		assertEquals(result, expected);
	} 
	*/
	
	/* test del costruttore con valore stringa */
	@Test
	public void constructor_string_value() {
		String value = "node_value";
		Node node = new Node(value);
		assertEquals(node.getValue(), value); 
		assertNull(a.getFather());
	}
	
	/* test del costruttore che sar‡ usato effettivamente, devo salvare nel nodo 
	 * un oggetto che sia progettato per modellare clusters */
	@Test
	public void costructorClusterData() {
		
	}
	
	
	@Test
	public void testEquals() {
		Node c2 = new Node("C");
		String stringC = "C";
		Object object = null;
		assertTrue(c.equals(c2));
		assertFalse(c.equals(stringC));
		assertFalse(c.equals(object));
	}
	
	@Test
	public void testAddChild() {
		
		/* figlio di a */
		a.addChild(b);
		assertTrue(a.getChildren().contains(b)); 
		assertEquals(a.getChildren().get(0).getValue(), "B");
		/* figlio di b */
		b.addChild(c);
		assertTrue(b.getChildren().contains(c));
		assertEquals(b.getChildren().get(0).getValue(), "C");

	}

	@Test
	public void testChildRelation() {
		Node b2 = new Node("B");
		Node c2 = new Node("C");
		a.addChild(b2);
		b2.addChild(c2);
		
		a.addChild(b);
		b.addChild(c);
		assertTrue(b.hasAncestor(a));
		assertTrue(c.hasAncestor(a));
		assertTrue(a.hasDescendant(b));
		assertTrue(a.hasDescendant(c));

		assertTrue(a.hasDescendant(b2));
		assertTrue(b2.hasDescendant(c2));

	}
	
	
	@Test
	public void testChild() {
		a.addChild(b);
		assertTrue(a.hasChild(b));
	}
	
	
	@Test
	public void testSiblings() {
	
		a.addChild(b);
		a.addChild(c);
		
		/* hanno lo stesso padre */
		assertTrue(b.hasFather(a));
		assertTrue(c.hasFather(a));
		/* e sono fratelli */
		assertTrue(b.hasSibling(c));
		assertTrue(c.hasSibling(b));

	}
	
	

}
