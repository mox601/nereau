package cluster.test;

import java.util.List;

import org.junit.Test;

import cluster.ClusterBuilder;
import cluster.Node;


public class ClusterBuilderTest {

	
	@Test
	public void constructorTest() {
		/* costruisce dei Node e li passa al clusterBuilder */
		List<Node> clustersToMerge = null;
		ClusterBuilder clusterer = new ClusterBuilder(clustersToMerge);
		clusterer.buildClusters();
		/* verifica le propriet‡ del clustering costruito: 
		 * c'Ž un unico cluster, con i tag di tutti i cluster iniziali 
		 * ha come foglie i cluster iniziali*/
	}
}
