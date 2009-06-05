package model.test;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import persistence.PersistenceException;
import persistence.VisitedURLDAO;
import persistence.postgres.VisitedURLDAOPostgres;

import cluster.Tagtfidf;

import model.GlobalProfileModel;
import model.RankedTag;
import model.URLTags;
import model.User;
import model.VisitedURL;
import model.usermodel.tags.TagFinder;


public class GlobalProfileModelTest {

	private LinkedList<URLTags> urlTagsToSave;
	private VisitedURLDAO visitedURLHandler;
	
	@Before
	public void setup(){
		this.urlTagsToSave = new LinkedList<URLTags>();
		
		/* START primo url */
		String firstUrlString = "http://www.uniroma3.it/";
		VisitedURL firstUrl = new VisitedURL(firstUrlString, null, null);
		RankedTag tag1 = new RankedTag("video");
		RankedTag tag2 = new RankedTag("news");
		RankedTag tag3 = new RankedTag("search");
		
		Set<RankedTag> tags = new HashSet<RankedTag>();
		tags.add(tag1);
		tags.add(tag2);
		tags.add(tag3);
		URLTags firstUrlTags = new URLTags(firstUrl, tags);
		this.urlTagsToSave.add(firstUrlTags);
		/* END primo url */
		
		/* START secondo url */
		String secondUrlString = "http://www.amaroma.it/";
		VisitedURL secondUrl = new VisitedURL(secondUrlString, null, null);
		RankedTag tag4 = new RankedTag("news");
		RankedTag tag5 = new RankedTag("design");
		RankedTag tag6 = new RankedTag("search");
		
		Set<RankedTag> tagsTwo = new HashSet<RankedTag>();
		tagsTwo.add(tag4);
		tagsTwo.add(tag5);
		tagsTwo.add(tag6);
		URLTags secondUrlTags = new URLTags(secondUrl, tagsTwo);
		this.urlTagsToSave.add(secondUrlTags);
		/* END secondo url */
		
		/* START terzo url */
		String thirdUrlString = "http://www.myspace.com/neilonimpression";
		VisitedURL thirdUrl = new VisitedURL(thirdUrlString, null, null);
		RankedTag tag7 = new RankedTag("news");
		RankedTag tag8 = new RankedTag("design");
		RankedTag tag9 = new RankedTag("software");
		
		Set<RankedTag> tagsThree = new HashSet<RankedTag>();
		tagsThree.add(tag7);
		tagsThree.add(tag8);
		tagsThree.add(tag9);
		URLTags thirdUrlTags = new URLTags(thirdUrl, tagsThree);
		this.urlTagsToSave.add(thirdUrlTags);
		/* END terzo url */
	}

	@Ignore
	@Test
	public void costructorTest() {
		GlobalProfileModel globalProfile = new GlobalProfileModel(urlTagsToSave);
		/* visualizza i tags */
		printListTagstfidf(globalProfile.getTags());	
		/* TODO: non Ž completo, non ci sono assert */
		
//		assertEquals(urlTagsToSave, globalProfile.getTags());
		globalProfile.updateGlobalProfile();
		
	}
	
	
	
	@Test
	public void constructorVisitedURLTest() {
		this.visitedURLHandler = new VisitedURLDAOPostgres();
		User user = new User("mox601","ciaoclaudio");
		
		List<VisitedURL> visitedURLs = null;
		try {
			visitedURLs = this.visitedURLHandler.retrieveLastVisitedURLs(user);
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//TODO: boolean
		GlobalProfileModel fasterGlobalProfile = new GlobalProfileModel(visitedURLs, false);
		fasterGlobalProfile.updateGlobalProfile();
		
		
	}
	
	
	
	
	@Ignore
	@Test
	public void findTagsForUrlTest() {
		String url = "http://dzineblog.com/2009/05/50-best-sites-to-get-design-inspiration.html";
		TagFinder tagFinderMoreTags = new TagFinder();
		Set<RankedTag> tags = tagFinderMoreTags.findTags(url);
		for (RankedTag tag: tags) {
			System.out.println(tag.toString());
		}
		
		TagFinder tagFinder = new TagFinder();
		
		
		//TODO: fare una strategy che estragga piœ tag da ogni url
		
		
	}

	/*stampa lista di tagstfidf*/
	private void printListTagstfidf(List<Tagtfidf> tags) {
		Iterator<Tagtfidf> tagIterator = tags.iterator();
		while (tagIterator.hasNext()) {
			Tagtfidf currentTfidf = tagIterator.next();
			System.out.println("tagtfidf: " + currentTfidf.getTag() + " chiavi: " + currentTfidf.getTagUrlsMap().toString());
		}
	}

}
