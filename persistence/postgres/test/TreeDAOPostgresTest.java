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
	
	@Ignore
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
				
		
//		System.out.println(hierarchy.toString());
		
		/*
		for (Node node: hierarchy.getLeaves()) {
			System.out.println(node.toString());
		}
		*/
		
	}
	
	
	@Test
	public void testFromTagsToHierarchy() {
		
		
		/* conta i tag distinti sul database, nel clustering */
		java.util.List<Node> leaves = this.clustering.getLeaves();
		
		int distinctTags = leaves.size();
		
		System.out.println("distinctTags = " + distinctTags);
		
		/* sceglie un numero random di tags (1-11) e fa RankedTags */
		
		double random = (Math.random()*10) + 1;
		int tagNumber = (int) Math.rint(random);
		System.out.println("random = " + random + " e intero = " + tagNumber);
		System.out.println("elenco delle foglie: \n" + leaves.toString());
		

		LinkedList<Integer> extractedIndexes = new LinkedList<Integer>();
		LinkedList<RankedTag> rankedTags = new LinkedList<RankedTag>();
		
		
		for (int i = 0; i < tagNumber; i++) {

			boolean safeIndex = false;

			Integer tagIndex = null;
			while (!safeIndex) {
				double randomIndex = (Math.random()*distinctTags);
				tagIndex = new Integer((int) Math.rint(randomIndex));
				if (!extractedIndexes.contains(tagIndex)) {
					safeIndex = true;
				}
			}

			extractedIndexes.add(tagIndex);
			System.out.println("picking tag " + i + " from index " + tagIndex);
			String currentTagName = leaves.get(tagIndex).getValue();
			double ranking = leaves.get(tagIndex).getRanking();
			RankedTag currentTag = new RankedTag(currentTagName, ranking);
			rankedTags.add(currentTag);
			
		}
		
		
		/* stampa ranked tags estratti */
		for (RankedTag rankedTag: rankedTags) {
			System.out.println("tag scelto: " + rankedTag.toString());
		} 
		
		
		/* da List<RankedTags>, cerco di ricostruire l'albero ridotto */
		Tree hierarchy = null;
		TreeDAOPostgres treeHandler = new TreeDAOPostgres();
		
		try {
			hierarchy = treeHandler.retrieve(rankedTags);
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("albero ricostruito: \n" + hierarchy.toString());
		
		/* valuto i risultati */
		
		
	}
	
	
	
	
	
	
}
