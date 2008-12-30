package model.usermodel;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import util.LogHandler;
import util.ParameterHandler;
import util.Stemmer;

import model.Query;
import model.RankedTag;
import model.RankedTerm;
import model.VisitedURL;
import model.usermodel.parser.URLParser;
import model.usermodel.tags.TagFinder;
import model.usermodel.tags.TxtSubUrlTagFinderStrategy;

public class UserMatrixCalculator {

	public Map<RankedTag, Map<String, Map<String, Double>>> 
		createTemporaryMatrix(List<VisitedURL> visitedURLs, boolean alreadyRetrieved) {
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		
		URLParser urlParser = new URLParser();
		TagFinder tagFinder = new TagFinder();
		Map<RankedTag, Map<String, Map<String, Double>>> tempMatrix = 
			new HashMap<RankedTag, Map<String, Map<String, Double>>> ();
		
		//examine every visited url (url + query)
		for(VisitedURL visitedURL: visitedURLs) {
			
			String urlString = visitedURL.getURL();
			Query query = visitedURL.getQuery();
			Stemmer stemmer = new Stemmer();
			Set<String> queryTerms = query.getStemmedTerms();
			List<String> parsedURL = new LinkedList<String>();
	
			if(!alreadyRetrieved) {
				logger.info("lavoro sull'URL " + urlString);
				
				tagFinder = new TagFinder();
				
				try{
					//retrieve visited page
					URL url = new URL(urlString);
					URLConnection urlConnection = url.openConnection();
					//fix timeout
					urlConnection.setConnectTimeout(ParameterHandler.URL_TIMEOUT);
					//parse retrieved page
					parsedURL = urlParser.parse(urlConnection,stemmer);
					logger.info("url dopo parsing: " + parsedURL);
				}
				catch(Exception e) {
					e.printStackTrace();
				} 
			}
			
			else {
				
				logger.info("lavoro sul file " + urlString);
				tagFinder = new TagFinder(new TxtSubUrlTagFinderStrategy());
				try {
					parsedURL = urlParser.parse(new File(urlString), stemmer);
				} catch (Exception e) {
					e.printStackTrace();
				}
				String preUrlString = urlString.substring(0, urlString.lastIndexOf('/')+1);
				String postUrlString = urlString.substring(urlString.lastIndexOf('/')+1);
				urlString = preUrlString + "tags_" + postUrlString;
			}
			
			//limit the number of analized words
			Map<String,Double> keywords = this.extractKeywords(parsedURL);
			logger.info("keywords: " + keywords);
			
			stemmer.saveTerms(keywords);
			
			//normalize occurrence values
			Map<String,Double> occurrenceMatrix =
				this.calculateNormalizedOccurrences(keywords);
			logger.info("occorrenze normalizzate: " + occurrenceMatrix);
			
			//query terms are put into map with max normalized occurrence value
			this.manageQueryTerms(occurrenceMatrix,queryTerms);
			logger.info("occorrenze normalizzate con termini query: " + occurrenceMatrix);
			
			//calculate co-occurrence values between keywords
			Map<String,Map<String,Double>> coOccurrenceMatrix =
				this.calculateCoOccurrences(occurrenceMatrix);
			//logger.info("co-occorrenze: " + coOccurrenceMatrix);
			logger.info("co-occorrenze: (omesso)");
			
			//find associated tags
			Set<RankedTag> tags = tagFinder.findTags(urlString);
			logger.info("tags associati: " + tags);
			
			//update temporary matrix with keywords and tags
			this.updateTempMatrix(tempMatrix,tags,coOccurrenceMatrix);
			//logger.info("matrice temporanea dopo studio " + visitedURL + ": " + tempMatrix);
			logger.info("matrice temporanea dopo studio " + visitedURL + ": (omesso)");
			//print to log file
			StringBuffer tempMatrixForLog = new StringBuffer("{");
			for(RankedTag rTag: tempMatrix.keySet()) {
				tempMatrixForLog.append(rTag + "=" + tempMatrix.get(rTag).keySet() + ",");
			}
			tempMatrixForLog.append("}");
			//logger.info("matrice temporanea (solo keyset): " + tempMatrixForLog);
		}
		
		return tempMatrix;
		
	}
	
	private void manageQueryTerms(Map<String, Double> occurrenceMatrix, Set<String> queryTerms) {
		
		double newOccurrenceValue;
		
		for(String stemmedQueryTerm: queryTerms) {
			
			newOccurrenceValue = 1.0;
			
			if(!ParameterHandler.MAX_OCCURRENCE_VALUE_FOR_QUERY_TERMS) {
				double oldOccurrenceValue = 0;
				if(occurrenceMatrix.containsKey(stemmedQueryTerm))
					oldOccurrenceValue = occurrenceMatrix.get(stemmedQueryTerm);
				newOccurrenceValue = //(newOccurrenceValue + oldOccurrenceValue)/2.0;
					oldOccurrenceValue * ParameterHandler.QUERY_TERMS_WEIGHT_FOR_MAX + 
					newOccurrenceValue * (1-ParameterHandler.QUERY_TERMS_WEIGHT_FOR_MAX);
			}
			
			if(
					!ParameterHandler.MAX_FOR_QUERY_TERMS_ONLY_IF_PRESENT ||
					(
							ParameterHandler.MAX_FOR_QUERY_TERMS_ONLY_IF_PRESENT && 
							occurrenceMatrix.containsKey(stemmedQueryTerm)
					)
				)
			occurrenceMatrix.put(stemmedQueryTerm, newOccurrenceValue);
			
		}
		
	}

	private Map<String, Map<String, Double>> calculateCoOccurrences(Map<String, Double> occurrenceMatrix) {
		Map<String,Map<String,Double>> coOccurrences = 
			new HashMap<String,Map<String,Double>>();
		for(String term1: occurrenceMatrix.keySet()) {
			double oldValue1 = occurrenceMatrix.get(term1);
			Map<String,Double> values4term = 
				new HashMap<String,Double>();
			for(String term2: occurrenceMatrix.keySet()) {
				double oldValue2 = occurrenceMatrix.get(term2);
				double newValue = oldValue1 * oldValue2;
				values4term.put(term2,newValue);
			}
			coOccurrences.put(term1, values4term);
		}
		return coOccurrences;
	}
	
	private void updateTempMatrix(Map<RankedTag, Map<String, Map<String, Double>>> tempMatrix, 
			Set<RankedTag> tags, Map<String, Map<String, Double>> coOccurrenceMatrix) {
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		logger.info("update della matrice temporanea");
		for(RankedTag tag: tags) {
			logger.info("tag esaminato: " + tag);
			Map<String,Map<String,Double>> values4tag = 
				this.calculateCoOccurrences4tag(tag,coOccurrenceMatrix);
			logger.info("co-occorrenze per il tag: (omesso)");
			//logger.info("co-occorrenze per il tag: " + values4tag);
			if(!tempMatrix.containsKey(tag)) {
				logger.info("tempMatrix non conteneva il tag");
				tempMatrix.put(tag,values4tag);
			}
			else {
				logger.info("tempMatrix già conteneva il tag");
				Map<String,Map<String,Double>> oldValues4tag =
					tempMatrix.remove(tag);
				//logger.info("vecchi valori per il tag: " + oldValues4tag);
				Map<String,Map<String,Double>> newValues4tag =
					this.sumValues4tag(oldValues4tag,values4tag);
				//logger.info("nuovi valori per il tag: " + newValues4tag);
				tempMatrix.put(tag, newValues4tag);
			}
		}
	}
	
	private Map<String, Double> extractKeywords(List<String> parsedURL) {
		
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		logger.info("estrazione keywords da una lista di dimensione " + parsedURL.size());
		
		Map<String,Double> occurrences =
			new HashMap<String,Double>();
		for(String term: parsedURL) {
			if(!occurrences.containsKey(term))
				occurrences.put(term,1.0);
			else {
				double oldValue =
					occurrences.remove(term);
				oldValue++;
				occurrences.put(term, oldValue);	
			}
			//logger.info("occorrenze parziali dopo analisi termine " + term + ": " + occurrences);
		}
		
		Set<RankedTerm> rankedTerms = new TreeSet<RankedTerm> ();
		for(String term: occurrences.keySet()) {
			RankedTerm rTerm = new RankedTerm(term,occurrences.get(term));
			//logger.info("nuovo rankedterm: " + rTerm);
			rankedTerms.add(rTerm);
			//logger.info("set dopo analisi rankedterm " + term + ": " + rankedTerms);
		}
		logger.info("insieme di termini estratti: " + rankedTerms);
		Map<String,Double> keywords = new HashMap<String,Double>();
		int keywords_limit = ParameterHandler.MAX_KEYWORDS;
		for(RankedTerm term: rankedTerms) {
			if(keywords_limit==0)
				break;
			keywords.put(term.getTerm(),term.getRanking());
			keywords_limit--;
			//logger.info("termine aggiunto: " + term + ", keywords rimamenti: " + keywords_limit);
		}
		
		logger.info("insieme di termini selezionati: " + keywords);
		
		
		
		
		return keywords;
		
		
		
	}
	
	private Map<String, Double> calculateNormalizedOccurrences(Map<String, Double> keywords) {
		Map<String,Double> normalizedOccurrences =
			new HashMap<String,Double>();
		double maxValue = 0.0;
		for(String term: keywords.keySet()) {
			double termValue = keywords.get(term);
			if(termValue>maxValue)
				maxValue = termValue;
		}
		for(String term: keywords.keySet()) {
			double oldValue = keywords.get(term);
			double newValue = oldValue/maxValue;
			normalizedOccurrences.put(term,newValue);
		}
		return normalizedOccurrences;
	}
	
	private Map<String, Map<String, Double>> sumValues4tag(Map<String, Map<String, Double>> oldValues4tag, 
			Map<String, Map<String, Double>> values4tag) {
		Map<String,Map<String,Double>> newValues4tag =
			new HashMap<String,Map<String,Double>> ();
		//bug fixing!! 
		Set<String> termUnion4tag = new HashSet<String>();
		termUnion4tag.addAll(oldValues4tag.keySet());
		termUnion4tag.addAll(values4tag.keySet());
		for(String term1: termUnion4tag) {
			Map<String,Double> values4tag4term =
				values4tag.get(term1);
			Map<String,Double> oldValues4tag4term = 
				oldValues4tag.get(term1);
			if(!oldValues4tag.containsKey(term1)) {
				newValues4tag.put(term1, values4tag4term);
			}
			else if(!values4tag.containsKey(term1)) {
				newValues4tag.put(term1, oldValues4tag4term);
			}
			else {
				Set<String> termUnion4tag4term = new HashSet<String> ();
				termUnion4tag4term.addAll(oldValues4tag4term.keySet());
				termUnion4tag4term.addAll(values4tag4term.keySet());
				Map<String,Double> newValues4tag4term =
					new HashMap<String,Double>();
				for(String term2: termUnion4tag4term)  {
					if(!oldValues4tag4term.containsKey(term2)) {
						double newValue = values4tag4term.get(term2);
						newValues4tag4term.put(term2, newValue);
					}
					else if(!values4tag4term.containsKey(term2)) {
						double oldValue = oldValues4tag4term.get(term2);
						newValues4tag4term.put(term2, oldValue);
					}
					else {
						double newValue = values4tag4term.get(term2);
						double oldValue = oldValues4tag4term.get(term2);
						newValue += oldValue;
						newValues4tag4term.put(term2, newValue);
					}
				}
				newValues4tag.put(term1, newValues4tag4term);
			}
		}
		return newValues4tag;
	}

	private Map<String, Map<String, Double>> calculateCoOccurrences4tag(RankedTag tag, 
			Map<String, Map<String, Double>> coOccurrenceMatrix) {
		Map<String,Map<String,Double>> coOccurrences4tag = 
			new HashMap<String,Map<String,Double>> ();
		double ranking = tag.getRanking();
		for(String term1: coOccurrenceMatrix.keySet()) {
			Map<String,Double> values4term = coOccurrenceMatrix.get(term1);
			Map<String,Double> coOcc4tag4term =
				new HashMap<String,Double>();
			for(String term2: values4term.keySet()) {
				double oldValue = values4term.get(term2);
				double newValue = oldValue * ranking;
				coOcc4tag4term.put(term2, newValue);
			}
			coOccurrences4tag.put(term1, coOcc4tag4term);
		}
		return coOccurrences4tag;
	}

}
