package model.usermodel.tags;

import java.util.Set;
import java.util.logging.Logger;

import util.LogHandler;
import util.ParameterHandler;

import model.RankedTag;


public class TagFinder {
	
	private SubUrlTagFinderStrategy subUrlTagFinder;
	private boolean exactUrl;
	
	public TagFinder() {
		this.subUrlTagFinder = new DeliciousSubUrlTagFinderStrategy();
		this.exactUrl = false;
	}
	
	public TagFinder(SubUrlTagFinderStrategy subUrlTagFinder) {
		this.subUrlTagFinder = subUrlTagFinder;
	}
	
	public TagFinder(SubUrlTagFinderStrategy subUrlTagFinder, boolean exactUrl) {
		this.subUrlTagFinder = subUrlTagFinder;
		this.exactUrl = exactUrl;
	}
	
	
	public Set<RankedTag> findTags(String urlString) {
		
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		logger.info("url string originale: " + urlString);
		
		//url formatting before tag search
		if(!exactUrl) {
			int HTTP_INDEX = 6;
			int HTTPS_INDEX = 7;
			int WWW_INDEX = 10;
			if(urlString.endsWith("/"))
				urlString = urlString.substring(0,urlString.length()-1);
			if(urlString.startsWith("http://www.") && urlString.lastIndexOf(".")!=WWW_INDEX)
				urlString = urlString.substring(WWW_INDEX+1);
			else if(urlString.startsWith("http://"))
				urlString = urlString.substring(HTTP_INDEX+1);
			else if(urlString.startsWith("https://"))
				urlString = urlString.substring(HTTPS_INDEX+1);
		}
		
		//tag search (at level 1 with max relevance)
		return this.findTags(urlString, 1.0);
	}
	
	private Set<RankedTag> findTags(String urlString, double relevance) {
		
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		
		logger.info("url parziale con rilevanza : (" + urlString + ", " + relevance + ")");
		
		int lastIndexOfSlash = urlString.lastIndexOf("/");
		int firstIndexOfDot = urlString.indexOf(".");
		int lastIndexOfDot = urlString.lastIndexOf(".");
		
		Set<RankedTag> result = this.subUrlTagFinder.findTagsForSubUrl(urlString, relevance, exactUrl);
		
		if(result.size()<=1 && !exactUrl) {
			
			double newRelevance = ParameterHandler.SUBURL_RELEVANCE_COEFFICIENT;
			
			if(lastIndexOfSlash!=-1) {
				String newUrlString = urlString.substring(0,lastIndexOfSlash);
				result = this.findTags(newUrlString,relevance * newRelevance);
			}
			else if(firstIndexOfDot!=lastIndexOfDot) {
				String newUrlString = urlString.substring(firstIndexOfDot+1);
				result = this.findTags(newUrlString,relevance * newRelevance);
			}
		}

		return result;
		
	}

}
