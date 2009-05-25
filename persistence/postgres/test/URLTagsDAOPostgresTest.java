package persistence.postgres.test;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import model.RankedTag;
import model.URLTags;

import persistence.PersistenceException;
import persistence.postgres.TagDAOPostgres;
import persistence.postgres.URLTagsDAOPostgres;

public class URLTagsDAOPostgresTest {

	
	private URLTagsDAOPostgres URLTagHandler;
	private URLTags urlToSave;
	
	@Before
	public void setup() {
		this.URLTagHandler = new URLTagsDAOPostgres();
		String urlString = "http://www.madonna.com/";
		String tagString1 = "madonna";
		String tagString2 = "blog";
		String tagString3 = "celebrity";
		Set<RankedTag> tags = new HashSet<RankedTag>();
		RankedTag tag1 = new RankedTag(tagString1);
		RankedTag tag2 = new RankedTag(tagString2);
		RankedTag tag3 = new RankedTag(tagString3);
		tag1.setRanking(3.0);
		
		tags.add(tag1);
		tags.add(tag2);
		tags.add(tag3);
		this.urlToSave = new URLTags(urlString, tags);
		
	}
	
	@Ignore
	@Test
	public void testUrlTagsSave() {
		
		try {
			this.URLTagHandler.save(urlToSave);
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	
	
	
	
	@Test
	public void testSaveNewTag() {
		/* test per salvataggio di un URLTags con uno dei tag che non 
		 * era presente nel database */
		
		String urlString = "http://www.amaroma.it/";
		String tagString1 = "madonna";
		String tagString2 = "blog";
		String tagString3 = "GWamfgaj49494";
		Set<RankedTag> tags = new HashSet<RankedTag>();
		RankedTag tag1 = new RankedTag(tagString1);
		RankedTag tag2 = new RankedTag(tagString2);
		RankedTag tag3 = new RankedTag(tagString3);
		tag1.setRanking(3.0);
		
		tags.add(tag1);
		tags.add(tag2);
		tags.add(tag3);
		
		URLTags url = new URLTags(urlString, tags);
	
		try {
			this.URLTagHandler.save(url);
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int idString3 = -1;
		
		TagDAOPostgres tagHandler = new TagDAOPostgres();
		try {
			idString3 = tagHandler.retrieveTagId(tagString3);
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("l'id inserito Ž: " + idString3);
		//OK
		
	}

	
	
	
	
	
	
	
	
	
}
