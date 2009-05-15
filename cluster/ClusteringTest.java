package cluster;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.Ignore;
import org.junit.Test;

public class ClusteringTest {

	@Ignore
	@Test
	public void testEqualsClustering() {
		/* clustering one */
		HashSet<HashSet<Node>> setOfClustersA = new HashSet<HashSet<Node>>();
		Node a = new Node("A");
		Node b = new Node("B");
		HashSet<Node> clusterOne = new HashSet<Node>();
		clusterOne.add(a);
		clusterOne.add(b);
		setOfClustersA.add(clusterOne);
		Clustering one = new Clustering(setOfClustersA);
		

		/* clustering two */
		HashSet<HashSet<Node>> setOfClustersB = new HashSet<HashSet<Node>>();
		Node a1 = new Node("A");
		Node b1 = new Node("B");
		HashSet<Node> clusterOne1 = new HashSet<Node>();
		clusterOne1.add(b1);
		clusterOne1.add(a1);
		setOfClustersB.add(clusterOne1);
		Clustering two = new Clustering(setOfClustersB);
		
		assertTrue(two.equals(one));
		assertTrue(one.equals(two));
				
		/* clustering three */
		HashSet<HashSet<Node>> setOfClustersC = new HashSet<HashSet<Node>>();
		Node a2 = new Node("A");
		Node b2 = new Node("B");
		Node c2 = new Node("C");
		HashSet<Node> clusterOne3 = new HashSet<Node>();
		HashSet<Node> clusterTwo3 = new HashSet<Node>();
		clusterOne3.add(b2);
		clusterOne3.add(a2);
		clusterTwo3.add(c2);
		setOfClustersC.add(clusterOne3);
		setOfClustersC.add(clusterTwo3);
		Clustering three = new Clustering(setOfClustersC);
		
		assertFalse(two.equals(three));
		assertFalse(three.equals(two));
		
	}

}
