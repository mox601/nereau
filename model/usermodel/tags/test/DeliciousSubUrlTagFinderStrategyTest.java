package model.usermodel.tags.test;

import static org.junit.Assert.*;

import java.util.Set;

import model.RankedTag;
import model.usermodel.tags.DeliciousSubUrlTagFinderStrategy;

import org.junit.Test;

public class DeliciousSubUrlTagFinderStrategyTest {

	@Test
	public void testExtractTags() {
		String pageContent = "";
		
		DeliciousSubUrlTagFinderStrategy tagFinder = new DeliciousSubUrlTagFinderStrategy();
		String urlString = "http://del.icio.us/url/e7543f4e2fc7021a71a3da45886f00c6?settagview=list";
		
		Set<RankedTag> tags = tagFinder.findTagsForSubUrl(urlString, 1.0, true);
		
		System.out.println("tags found: " + tags.toString());
	
	
	
	}

}
