package util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import persistence.PersistenceException;
import persistence.StemmerDAO;
import persistence.postgres.StemmerDAOPostgres;

public class Stemmer {
	
	private StemmerDAO stemmedWordsHandler;
	private PorterStemmingAlgorithm porterStemming;
	private Map<String,Map<String,Integer>> stemmedword2words;
	
	public Stemmer() {
		this.stemmedWordsHandler = new StemmerDAOPostgres();
		this.porterStemming = new PorterStemmingAlgorithm();
		this.stemmedword2words = new HashMap<String,Map<String,Integer>> ();
	}
	
	public Set<String> stem(Set<String> terms) {
		List<String> termList = new LinkedList<String>(terms);
		Set<String> stemmedTerms = new HashSet<String>(this.stem(termList));
		return stemmedTerms;
	}

	public List<String> stem(List<String> words) {
		
		List<String> result = new LinkedList<String>();
		
		//stem all words
		for(String word: words) {
			this.stemWord(word,result,this.stemmedword2words);
		}
		
		//return list of stemmed words
		return result;
	}

	private void stemWord(String word, List<String> result, Map<String, Map<String, Integer>> stemmedword2words) {
		String newWord = this.stem(word);
		result.add(newWord);
		Map<String,Integer> word2relevance = null;
		if(!stemmedword2words.containsKey(newWord)) {
			word2relevance = new HashMap<String,Integer>();
			word2relevance.put(word, 1);
			stemmedword2words.put(newWord, word2relevance);
		}
		else {
			word2relevance = stemmedword2words.get(newWord);
			int newRelevance = 1;
			if(word2relevance.containsKey(word))
				newRelevance += word2relevance.get(word);
			word2relevance.put(word, newRelevance);
		}
	}

	private String stem(String word) {
		porterStemming.add(word.toCharArray(),word.length());
		porterStemming.stem();
		String result = porterStemming.toString();
		return result;
	}
	
	private void saveTerms() {
		//save correspondences between words and stemmed words
		try {
			this.stemmedWordsHandler.save(stemmedword2words);
			this.stemmedword2words = new HashMap<String,Map<String,Integer>> ();
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
	}

	public void saveTerms(Map<String, Double> keywords) {
		
		//get submap for keywords
		Map<String,Map<String,Integer>> stemmedword2keywords = 
			new HashMap<String,Map<String,Integer>> ();
		
		for(String keyword: keywords.keySet())
			stemmedword2keywords.put(keyword,this.stemmedword2words.get(keyword));
		
		//save correspondences between words and stemmed words
		try {
			this.stemmedWordsHandler.save(stemmedword2keywords);
			this.stemmedword2words = new HashMap<String,Map<String,Integer>> ();;
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
	}

	public Set<String> stemQuery(Set<String> terms) {
		// TODO Auto-generated method stub
		Set<String> stemmedQuery = this.stem(terms);
		this.saveTerms();
		return stemmedQuery;
	}

}
