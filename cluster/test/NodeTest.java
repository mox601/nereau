package cluster.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import cluster.Node;
import cluster.Tagtfidf;

public class NodeTest {
	
	private Node a;
	private Node b;
	private Node c;
	
	/*
	 * i test si scrivono con questa forma: assert*(expected, actual)
	 * */
	
	@Before
	public void setUp() {
		a = new Node("A"); 
		b = new Node("B");
		c = new Node("C");
	}
	
	
	/* test del costruttore con valore stringa */
	@Test
	public void constructor_string_value() {
		String value = "node_value";
		Node node = new Node(value);
		assertEquals(value, node.getValue()); 
		assertNull(a.getFather());
	}
	
	/* test del costruttore che sar‡ usato effettivamente, devo salvare nel nodo 
	 * un oggetto che sia progettato per modellare clusters */
	@Test
	public void constructorClusterData() {
		String value = "cluster_value";
		Float sim = new Float("2.0");
		Node node = new Node(value, sim);		
		assertEquals(sim, node.getSimilarity());
		
	}
	
	
	@Test
	public void mergeClusters() {
		/* costruisco 2 nodi che contengono solo 1 tag ciascuno e li fondo 
		 * poi costruisco un nuovo nodo con altri 2 tag e lo fondo con il cluster 
		 * precedente */
		
		String tag1name = "design";
		Map<String, Integer> tag1UrlsMap = new HashMap<String, Integer>();
		String alist = "www.alistapart.com/";
		int freqAlist = 1; 
		tag1UrlsMap.put(alist, freqAlist);
		String beyond ="www.googlebeyond.com/";
		int freqBeyond = 2;
		tag1UrlsMap.put(beyond, freqBeyond);
		Tagtfidf tag1 = new Tagtfidf(tag1name, tag1UrlsMap);
		
		
		String tag2name = "creativity";
		Map<String, Integer> tag2UrlsMap = new HashMap<String, Integer>();
		int freqAlist2 = 3;
		tag2UrlsMap.put(alist, freqAlist2);
		String google = "www.googlehacker.com/";
		int freqGoogle = 7;
		tag2UrlsMap.put(google, freqGoogle);
		Tagtfidf tag2 = new Tagtfidf(tag2name, tag2UrlsMap);
		
		
		/* costruisco i clusters singoli */
		
		Node cluster1 = new Node(tag1name, tag1);
		Node cluster2 = new Node(tag2name, tag2);
		
		/* e costruisco il cluster come fusione di tag1 e tag2 */
		List<Node> clustersToMerge = new LinkedList<Node>(); 
		Node mergedCluster1 = new Node(clustersToMerge);
		
	}
	
	
	

	
	@Test
	public void constructorClusterTag() {
		String cluster_value = "cluster_value";
		Float sim = new Float("2.0");
		String tag_name = "search";
		HashMap<String, Integer> tagUrlsMap = new HashMap<String, Integer>();
		
		String url1 = "www.google.it";
		Integer url1freq = 7;
		
		String url2 = "www.yahoo.it";
		Integer url2freq = 14;
		
		tagUrlsMap.put(url1, url1freq);
		tagUrlsMap.put(url2, url2freq);
		
		Tagtfidf tag = 	new Tagtfidf(tag_name, tagUrlsMap);
		/* ho cambiato il costruttore */
//		Node node = new Node(cluster_value, sim, tag);

	}
	

	@Test
	public void testAverageTag() {
		
//		Tagtfidf averageTag = Tagtfidf(tagsToMerge)
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
		assertEquals("B", a.getChildren().get(0).getValue());
		/* figlio di b */
		b.addChild(c);
		assertTrue(b.getChildren().contains(c));
		assertEquals("C", b.getChildren().get(0).getValue());

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
