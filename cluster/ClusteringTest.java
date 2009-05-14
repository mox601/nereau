package cluster;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.Test;

public class ClusteringTest {

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
		

		
	}

}
