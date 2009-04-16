package cluster.test;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import persistence.PersistenceException;
import persistence.postgres.TagtfidfDAOPostgres;

import cluster.ClusterBuilder;
import cluster.Node;
import cluster.Tagtfidf;


public class ClusterBuilderTest {

	
	private List<Node> clustersToMerge;
	
	@Before
	public void setUpNodesForClusteringFromDatabase() {
		/* estrae tutti i tags dal database e ne costruisce una lista di
		 *  clusters (Node) */
		/* TODO: DEMO */
		TagtfidfDAOPostgres tagTfidfHandler = new TagtfidfDAOPostgres();
		try {
			List<Tagtfidf> extractedTags = tagTfidfHandler.retrieveAllTags();
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/* poi costruisce un cluster a partire da ogni tag 
		 * for (Tagtfidf tag: extractedTags) {
		/* 		Node currentNode = Node(nodeValue, tag);
		 * 		this.clustersToMerge.add(currentNode); 
		 * }
		 * */
		
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
