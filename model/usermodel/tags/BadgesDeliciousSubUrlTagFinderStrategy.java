package model.usermodel.tags;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import util.LogHandler;


public class BadgesDeliciousSubUrlTagFinderStrategy extends
		MD5SubUrlTagFinderStrategy {

	private static final String BADGES_DELICIOUS_URL_STARTSWITH = "http://badges.del.icio.us/feeds/json/url/data?hash=";
	private static final String BADGES_DELICIOUS_URL_ENDSWITH = "";
	
	public BadgesDeliciousSubUrlTagFinderStrategy() {
		super(BADGES_DELICIOUS_URL_STARTSWITH,BADGES_DELICIOUS_URL_ENDSWITH);
	}
	
	public Map<String, Integer> extractTags(String pageContent) {
		
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		
		//result of the method
		Map<String,Integer> extractedTags = new HashMap<String,Integer> ();
		
		//if page is empty or if it does not contain tags, skip
		if(pageContent.equals("[]") || pageContent.indexOf("top_tags\":{")<0)
			return extractedTags;

		//first relevant character
		int startIndex = pageContent.indexOf("top_tags\":{") + ("top_tags\":{").length();
		//last relevant character
		int endIndex = pageContent.lastIndexOf("}") - 1;
		
		//page content relevant substring
		pageContent = pageContent.substring(startIndex,endIndex);
		logger.info("contenuto rilevante della pagina di badges delicious: " + pageContent);
		
		Scanner scanner = new Scanner(pageContent);
		scanner.useDelimiter(",");
		Pattern pattern = Pattern.compile("\".+?\":\\d+");
		
		String tagName = null;
		int tagFrequency = 0;
		String next = scanner.findInLine(pattern);
		while(next!=null) {
			logger.info("contenuto relativo al singolo tag: " + next);
			tagName = next.substring(1,next.lastIndexOf("\""));
			tagFrequency = 
				Integer.valueOf(next.substring(next.lastIndexOf(":")+1));
			extractedTags.put(tagName, tagFrequency);
			next = scanner.findInLine(pattern);
		}
		return extractedTags;
	}

}
