package model.usermodel.tags.test;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import model.RankedTag;
import model.usermodel.tags.CompositeSubUrlTagFinderStrategy;
import model.usermodel.tags.DeliciousSubUrlTagFinderStrategy;
import model.usermodel.tags.TagFinder;

import org.junit.Test;

public class DeliciousSubUrlTagFinderStrategyTest {

	@Test
	public void testExtractTags() {
		String pageContent = "";
		String urlString = "http://www.causecast.org/news_items/8544-three-benefits-of-going-vegetarian";
		
		Set<RankedTag> rtags = new HashSet<RankedTag>();
		TagFinder tf = new TagFinder(new CompositeSubUrlTagFinderStrategy(),true);
		rtags = tf.findTags(urlString);
		
		System.out.println("tags retrieved: ");
		for(RankedTag rt: rtags) 
			System.out.println(rt.getTag());
	
	
	
	
	}

}
