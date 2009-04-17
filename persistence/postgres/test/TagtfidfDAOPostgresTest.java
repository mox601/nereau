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

	@Ignore
	@Test
	public void testRetrieveTag() {
		String tag = "entertainment";
		TagtfidfDAOPostgres tagTfidfHandler = new TagtfidfDAOPostgres();
		Tagtfidf tagObj = null;
		try {
			tagObj = tagTfidfHandler.retrieveTag(tag);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		
		System.out.println("tag id = " + tagObj.getTag());
		
	}
	
	@Test
	public void testRetrieveAllTags() {
		/* TODO: demo claudio */
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
	
	
	

}
