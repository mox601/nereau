package persistence.postgres.test;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import model.RankedTag;
import model.URLTags;

import persistence.PersistenceException;
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
	
	@Test
	public void testUrlTagsSave() {
		
		try {
			this.URLTagHandler.save(urlToSave);
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

	
	
	
	
	
	
	
	
	
}
