package model.usermodel.tags;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import persistence.PersistenceException;
import util.LogHandler;
import util.ParameterHandler;

import model.RankedTag;

public class StumbleUponSubUrlTagFinderStrategy extends SubUrlTagFinderStrategy {
	
	private static final String STUMBLEUPON_URL_STARTSWITH = 
		"http://www.stumbleupon.com/url/";
	
	public StumbleUponSubUrlTagFinderStrategy() {
		super();
	}

	public Set<RankedTag> findTagsForSubUrl(String urlString, double relevance, boolean exactUrl) {
		
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		
		Map<String,Integer> rankedTags = new TreeMap<String,Integer>();
		URL stumbleUponUrl = null;
		Scanner scanner = null;
		String stumbleUponUrlString = STUMBLEUPON_URL_STARTSWITH + urlString;
		try {
			stumbleUponUrl = 
				new URL(stumbleUponUrlString);
			logger.info("delicious url da analizzare: " + stumbleUponUrlString);
		} catch (MalformedURLException e) {
			logger.info("url errato: " + stumbleUponUrlString);
			e.printStackTrace();
			Set<RankedTag> exceptionResult = new HashSet<RankedTag>();
			exceptionResult.add(ParameterHandler.NULL_TAG);
			return exceptionResult;
		}
		try {
			//fixing timeout
			URLConnection urlConnection = stumbleUponUrl.openConnection();
			urlConnection.setConnectTimeout(ParameterHandler.URL_TIMEOUT);
			scanner = new Scanner(urlConnection.getInputStream());
		} catch (IOException e) {
			logger.info("url timeout!");
			e.printStackTrace();
			Set<RankedTag> exceptionResult = new HashSet<RankedTag>();
			exceptionResult.add(ParameterHandler.NULL_TAG);
			return exceptionResult;
		}
		StringBuffer pageContent = new StringBuffer();
		while(scanner.hasNextLine()) {
			pageContent.append(scanner.nextLine());
		}
		this.updateResult(rankedTags,pageContent.toString());
		logger.info("risultato parziale aggiornato: " + rankedTags);
		
		Set<RankedTag> normalizedRankedTags = 
			this.normalizeRanking(rankedTags,relevance);
		
		try {
			super.save(normalizedRankedTags);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		
		logger.info("tags normalizzati: " + normalizedRankedTags);
		
		return normalizedRankedTags;
	}
	
	protected Map<String, Integer> extractTags(String pageContent) {
		
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		logger.info("inizio estrazione tags");
		
		//result of the method
		Map<String,Integer> extractedTags = new HashMap<String,Integer> ();
		
		//first preliminary index for check
		int firstPreliminaryStartIndex = pageContent.indexOf("icon tags");
		//logger.info("index of icon tags: " + firstPreliminaryStartIndex);
		
		if(firstPreliminaryStartIndex==-1)
			return extractedTags;
		
		pageContent = pageContent.substring(firstPreliminaryStartIndex);
		
		//second preliminary index for check
		int secondPreliminaryStartIndex = pageContent.indexOf(">");
		//logger.info("index of >: " + secondPreliminaryStartIndex);
		
		if(secondPreliminaryStartIndex==-1)
			return extractedTags;
		
		pageContent = pageContent.substring(secondPreliminaryStartIndex);
		
		//first relevant character
		int startIndex = pageContent.indexOf("<");
		//logger.info("index of <: " + startIndex);
		
		//last relevant character
		int endIndex = pageContent.indexOf("<br />");
		//logger.info("index of <br />: " + endIndex);
		
		if(startIndex==-1 || endIndex==-1)
			return extractedTags;
		
		pageContent = pageContent.substring(startIndex,endIndex);
		logger.info("contenuto rilevante della pagina di stumbleupon: " + pageContent);
		
		boolean hasNext = pageContent.indexOf("</a>")!=-1;
		while(hasNext) {
			int nameStart = pageContent.indexOf(">") + (">").length();
			int nameEnd = pageContent.indexOf("</a>");
			String tagName = pageContent.substring(nameStart,nameEnd);
			
			extractedTags.put(tagName, 1);
			
			pageContent = pageContent.substring(nameEnd + ("</a>").length());
			if(!pageContent.startsWith(","))
				hasNext = false;
		}
		
		return extractedTags;
	}

}
