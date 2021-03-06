package persistence.postgres.test;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import cluster.Tagtfidf;

import persistence.PersistenceException;
import persistence.postgres.TagDAOPostgres;
import persistence.postgres.TagtfidfDAOPostgres;

public class TagtfidfDAOPostgresTest {

	@Test
	public void testRetrieveTag() {
		String tag = "search";
		TagtfidfDAOPostgres tagTfidfHandler = new TagtfidfDAOPostgres();
		Tagtfidf tagObj = null;
		try {
			tagObj = tagTfidfHandler.retrieveTag(tag);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		
		System.out.println("tag = " + tagObj.toString());
		
	}
	
	@Test
	public void testRetrieveAllTags() {
		TagtfidfDAOPostgres tagsHandler = new TagtfidfDAOPostgres();
		List<Tagtfidf> tagsList = new LinkedList<Tagtfidf>();
		
		try {
			tagsList = tagsHandler.retrieveAllTags();
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (Tagtfidf tag: tagsList) {
			System.out.println(tag.toString());
		}
		
	}
	
	@Ignore
	@Test
	public void testDeleteAllTags() {
		TagtfidfDAOPostgres tagsHandler = new TagtfidfDAOPostgres();
		
		try {
			tagsHandler.deleteTagVisitedUrls();
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("deleted!");
		

	}
	
	
	

}
