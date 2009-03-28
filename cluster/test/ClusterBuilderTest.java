package cluster.test;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import cluster.ClusterBuilder;
import cluster.Node;


public class ClusterBuilderTest {

	
	private List<Node> clustersToMerge;
	
	@Before
	public void setUpNodesForClustering() {
		
		/* estrae tutti i tags dal database e ne costruisce una lista di
		 *  clusters (Node) */
		
		/* usa questo costruttore per ogni tag estratto */
		// new Tagtfidf(String tag, Map<String, Integer> tagUrlsMap);

		
		
	}
	
	@Test
	public void constructorTest() {
		/* costruisce dei Node e li passa al clusterBuilder */
		ClusterBuilder clusterer = new ClusterBuilder(clustersToMerge);
		clusterer.buildClusters();
		/* verifica le propriet‡ del clustering costruito: 
		 * c'Ž un unico cluster, con i tag di tutti i cluster iniziali 
		 * ha come foglie i cluster iniziali*/
	}
}
