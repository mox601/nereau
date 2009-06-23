package persistence.postgres.test;

import static org.junit.Assert.*;

import java.util.LinkedList;

import model.RankedTag;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sun.tools.javac.util.List;

import persistence.PersistenceException;
import persistence.postgres.TreeDAOPostgres;

import cluster.ClusterBuilder;
import cluster.Node;
import cluster.Tree;



/* testing intensivo!!! */
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
	
	@Ignore
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
		/* scelgo di estrarre 3 tags */
	
		double blogRank = 1.0;
		double linguisticsRank = 2.0;
		double scholarshipsRank = 3.0;
		
		RankedTag blog = new RankedTag("blog", blogRank);
		RankedTag linguistics = new RankedTag("linguistics", linguisticsRank);
		RankedTag scholarships = new RankedTag("scholarships", scholarshipsRank);
		LinkedList<RankedTag> tags = new LinkedList<RankedTag>();
		tags.add(blog);
		tags.add(linguistics);
		tags.add(scholarships);
		
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
		/* le foglie sono dei Node, ma contengono proprio dati del RankedTag:  
		 * devo fare l'assert su quelli. */
		/* value e ranking*/
		
		List<Node> leaves = (List<Node>) hierarchy.getLeaves();
		
		for (Node node: leaves) {
//			assertEquals();
		}
		
//		assertEquals(tags, hierarchy.getLeaves());
				
		
		System.out.println(hierarchy.toString());
		
		/*
		for (Node node: hierarchy.getLeaves()) {
			System.out.println(node.toString());
		}
		*/
		
	}
	
	
	
	
	
	
	
}
