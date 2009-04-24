package persistence.postgres.test;

import org.junit.Before;
import org.junit.Test;

import persistence.PersistenceException;
import persistence.postgres.TreeDAOPostgres;

import cluster.ClusterBuilder;
import cluster.Tree;


public class TreeDAOPostgresTest {
	
	private Tree clustering;
	TreeDAOPostgres treeHandler;

	@Before
	public void setUpClustersToSave() {
		ClusterBuilder clusterer = new ClusterBuilder();
		clusterer.retrieveAllTagsFromDatabase();
		clusterer.buildClusters();
		clustering = clusterer.getActualClustering();	
	}
	
	
	
	@Test
	public void testVisitTree() throws PersistenceException {
		TreeDAOPostgres treeHandler = new TreeDAOPostgres();
		treeHandler.save(this.clustering);
		
	}
	
}
