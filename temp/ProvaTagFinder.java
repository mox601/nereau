package temp;

import java.util.Set;
import model.usermodel.tags.TagFinder;


import model.RankedTag;

public class ProvaTagFinder {
	
	public static void main(String[] args) {
		String urlString = "www.netvibes.com";
		TagFinder tagFinder = new TagFinder(); 
		Set<RankedTag> tags = tagFinder.findTags(urlString);
		System.out.println(tags);
	}

}
