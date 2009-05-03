package persistence.postgres.test;

import static org.junit.Assert.*;

import java.util.LinkedList;

import model.RankedTag;

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
	
	@Test
	public void retrieveTreeTest() {
		/* testa la costruzione di una gerarchia a partire da un 
		 * insieme di RankedTags. 
		 * Ž utile sapere quale gerarchia Ž salvata sul database */
		
		/**/
		
		/* costruisco dei ranked tags */
		/* scelgo di estrarre 3 tags: {blog, sapere, wikipedia} */
		RankedTag blog = new RankedTag("blog");
		RankedTag sapere = new RankedTag("sapere");
		RankedTag wikipedia = new RankedTag("wikipedia");
		LinkedList<RankedTag> tags = new LinkedList<RankedTag>();
		tags.add(blog);
		tags.add(sapere);
		tags.add(wikipedia);
		
		/* estraggo la gerarchia relativa a solo questi ranked tags */
		
		Tree hierarchy = null;
		TreeDAOPostgres treeHandler = new TreeDAOPostgres();
		
		try {
			hierarchy = treeHandler.retrieve(tags);
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		/* le foglie della gerarchia devono essere quelle della lista */
		assertEquals(tags, hierarchy.getLeaves());
		
		
	}
	
	
	
	
	
	
	
}
