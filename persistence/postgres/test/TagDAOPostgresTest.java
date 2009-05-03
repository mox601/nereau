package persistence.postgres.test;

import org.junit.Test;

import persistence.PersistenceException;
import persistence.postgres.TagDAOPostgres;


public class TagDAOPostgresTest {
	
	@Test
	public void retrieveTagIdTest() throws PersistenceException {
		String tag = "entertainment";
		TagDAOPostgres tagHandler = new TagDAOPostgres();
		int id = -1;  
		id = tagHandler.retrieveTagId(tag);
		System.out.println("tag id = " + id);
	}
	
	

}
