package persistence.postgres.test;

import static org.junit.Assert.*;

import org.junit.Test;

import cluster.Tagtfidf;

import persistence.PersistenceException;
import persistence.postgres.TagDAOPostgres;
import persistence.postgres.TagtfidfDAOPostgres;

public class TagtfidfDAOPostgresTest {

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

}
