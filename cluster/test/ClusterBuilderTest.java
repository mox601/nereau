package cluster.test;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import persistence.PersistenceException;
import persistence.postgres.TagtfidfDAOPostgres;

import cluster.ClusterBuilder;
import cluster.Node;
import cluster.Tagtfidf;
import cluster.Tree;


public class ClusterBuilderTest {

	
	private List<Node> clustersToMerge;
	private List<Node> clustersBeforeClustering;
	
	@Before
	public void setUpNodesForClusteringFromDatabase() {
		/* estrae tutti i tags dal database e ne costruisce una lista di
		 *  clusters (Node) */
		/* TODO: DEMO */
		this.clustersToMerge = new LinkedList<Node>();
		//copia per dopo
		this.clustersBeforeClustering = new LinkedList<Node>();
		
		TagtfidfDAOPostgres tagTfidfHandler = new TagtfidfDAOPostgres();
		List<Tagtfidf> extractedTags = new LinkedList<Tagtfidf>();
		try {
			extractedTags = tagTfidfHandler.retrieveAllTags();
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/* poi costruisce un cluster a partire da ogni tag */
		for (Tagtfidf tag: extractedTags) {
			Node currentNode = new Node(tag.getTag(), tag);
			this.clustersToMerge.add(currentNode);
			//faccio una copia per dopo
			this.clustersBeforeClustering.add(currentNode);
//			System.out.println("tag: " + currentNode.getCentroid().toString());
		}
		
		// i tag estratti vanno a formare una lista di Node lunga il giusto... 
		assertEquals(extractedTags.size(), this.clustersToMerge.size());
		
	}
	
	
	@Test
	public void constructorTest() {
		/* costruisce dei Node e li passa al clusterBuilder */
		ClusterBuilder clusterer = new ClusterBuilder(clustersToMerge);
		clusterer.buildClusters();
		// buildClusters cambia l'oggetto clustersToMerge
		/* verifica le propriet‡ del clustering costruito: 
		 * c'Ž un unico cluster, con i tag di tutti i cluster iniziali 
		 * ha come foglie i cluster iniziali*/
		List<Node> toMergeClusters = clusterer.getClustersToMerge();
		List<Node> leaves = clusterer.getActualClustering().getLeaves();
		
		/* verifica che le foglie del clustering ci fossero nei cluster da organizzare */
		for (Node leaf: leaves) {
			assertTrue(this.clustersBeforeClustering.contains(leaf));
		}
		
		
	}
	
	
	@Test
	public void buildClustersAndSaveOnDatabase() {
		ClusterBuilder clusterer = new ClusterBuilder();
		/* retrieve tags and build the singleton clusters to be clustered */
		clusterer.retrieveAllTagsFromDatabase();
		clusterer.buildClusters();
		Tree actualClustering = clusterer.getActualClustering();
		
		
	}
	
	
}
