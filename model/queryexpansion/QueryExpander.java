package model.queryexpansion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import persistence.PersistenceException;
import persistence.StemmerDAO;
import persistence.postgres.StemmerDAOPostgres;
import util.LogHandler;
import util.ParameterHandler;

import model.ExpandedQuery;
import model.Query;
import model.RankedTag;
import model.RankedTerm;
import model.User;
import model.UserModel;

public class QueryExpander {
	
	private ExpansionTagsStrategy expansionTagsStrategy;
	private StemmerDAO stemmerHandler;
	
	public QueryExpander() {
		this.stemmerHandler = new StemmerDAOPostgres();
		this.expansionTagsStrategy = 
			new CommonExpansionTagsStrategy();
	}

	public Set<ExpandedQuery> expandQuery(String queryString, User user) {
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		logger.info("query originale: " + queryString);
		Query query = new Query(queryString);
		Set<String> stemmedQueryTerms = query.getStemmedTerms();
		
		System.out.println("stemmed query terms: " + stemmedQueryTerms);
		
		//
		//System.out.println("Stemmed query terms: \n" + stemmedQueryTerms);
		
		UserModel userModel = user.getUserModel();
		Map<String,Map<RankedTag,Map<String,Double>>> subMatrix = 
			userModel.getSubMatrix(stemmedQueryTerms);
		
		System.out.println("user model for submitted query: " + subMatrix);
		
		//logger.info("modello utente relativo alla query: " + subMatrix);
		Set<ExpandedQuery> expandedQueries = null;
		if(subMatrix.isEmpty())
			expandedQueries = new HashSet<ExpandedQuery>();
		else
			expandedQueries = this.expand(stemmedQueryTerms,subMatrix);
		
		//
		//System.out.print("Expanded queries: " + expandedQueries);
		
		return expandedQueries;
	}
	
	protected Set<ExpandedQuery> expand(Set<String> stemmedQueryTerms,
			Map<String, Map<RankedTag, Map<String, Double>>> subMatrix) {

		Logger logger = LogHandler.getLogger(this.getClass().getName());
		
		//weight to be assigned to every query term
		double termWeight = 1.0 / (double)stemmedQueryTerms.size();
		
		Set<RankedTag> expansionTags = 
			this.expansionTagsStrategy.findExpansionTags(stemmedQueryTerms, subMatrix);
		
		//logger.info("tags per espansione: " + expansionTags);
		
		//
		//System.out.println("expansion tags: " + expansionTags);
		
		Map<ExpandedQuery, Set<RankedTag>> expandedQueries = 
			new HashMap<ExpandedQuery, Set<RankedTag>> ();
		
		for(RankedTag tag: expansionTags) {
			Map<String,Double> coOccurrenceValues4tag =
				this.initCoOccurrenceValues4tag(tag,subMatrix);
			for(String term1: subMatrix.keySet()) {
				Map<String,Double> coOccurrenceValues4term4tag = 
					subMatrix.get(term1).get(tag);
				if(coOccurrenceValues4term4tag!=null) {
					for(String term2: coOccurrenceValues4term4tag.keySet()) {
						double sumValue =
							coOccurrenceValues4tag.remove(term2) 
							+ (termWeight * coOccurrenceValues4term4tag.get(term2));
						coOccurrenceValues4tag.put(term2, sumValue);
					}
				}
			}
			//logger.info("co-occurrence values for tag " + tag + ": " + coOccurrenceValues4tag);

			Map<String,Map<String,Integer>> expansionTerms = 
				this.selectRelevantTerms(stemmedQueryTerms,coOccurrenceValues4tag);
			
			if(expansionTerms!=null) {
				ExpandedQuery expandedQuery = new ExpandedQuery(expansionTerms);
				Set<RankedTag> rankedTags = null;
				if(expandedQueries.containsKey(expandedQuery))
					rankedTags = expandedQueries.get(expandedQuery);
				else {
					rankedTags = new HashSet<RankedTag>();
					expandedQueries.put(expandedQuery, rankedTags);
				}
				rankedTags.add(tag);
			}
			
		}
		/*
		if(expansionTags.isEmpty()) {
			
			Map<String,Map<String,Integer>> expansionTerms = 
				this.selectRelevantTerms(stemmedQueryTerms, new HashMap<String,Double>());
			
			
			//
			//System.out.println("Expansion terms: " + expansionTerms);
			
			ExpandedQuery expandedQuery = new ExpandedQuery(expansionTerms);
			Set<RankedTag> setWithNullTag = new HashSet<RankedTag>();
			setWithNullTag.add(ParameterHandler.NULL_TAG);
			expandedQueries.put(expandedQuery, setWithNullTag);
		}
		*/
		Set<ExpandedQuery> result = new HashSet<ExpandedQuery>();
		for(ExpandedQuery eq: expandedQueries.keySet()) {
			eq.setExpansionTags(expandedQueries.get(eq));
			result.add(eq);
		}
		
		logger.info("query espanse: " + expandedQueries);
		return result;
		
	}
	
	private Map<String,Map<String,Integer>> selectRelevantTerms(Set<String> stemmedQueryTerms, Map<String, Double> coOccurrenceValues) {
		//Logger logger = LogHandler.getLogger(this.getClass().getName());
		
		//find all terms that satisfy parameters
		Set<RankedTerm> rankedTerms = new HashSet<RankedTerm>();
		double maxValue = 0;
		for(String term: coOccurrenceValues.keySet()) {
			if(!stemmedQueryTerms.contains(term)) {
				if(coOccurrenceValues.get(term)>maxValue)
					maxValue = coOccurrenceValues.get(term);
			}
		}
		for(String term: coOccurrenceValues.keySet()) {
			double newValue = coOccurrenceValues.get(term);
			if(newValue>ParameterHandler.MIN_QUERYEXPANDER_ABSOLUTE_VALUE) {
				newValue /= maxValue;
				if(newValue>ParameterHandler.MIN_QUERYEXPANDER_RELATIVE_VALUE) {
					if(!stemmedQueryTerms.contains(term))
						rankedTerms.add(new RankedTerm(term, newValue));
				}
			}
		}
		
		//set of original terms
		Set<RankedTerm> originalTerms = new TreeSet<RankedTerm>();
		for(String originalQueryTerm: stemmedQueryTerms)
			originalTerms.add(new RankedTerm(originalQueryTerm,1.));
		
		//remove all original terms from expansion terms
		rankedTerms.removeAll(originalTerms);
		
		//check if query was really expanded
		boolean queryWasExpanded = rankedTerms.size()>0;
		if(!queryWasExpanded) {
			Map<String, Map<String, Integer>> originalTermsNoStemming = null;

			try {
				originalTermsNoStemming = this.stemmerHandler.retrieveTerms(stemmedQueryTerms);
			} catch (PersistenceException e) {
				e.printStackTrace();
				originalTermsNoStemming = new HashMap<String,Map<String,Integer>> ();
			}
			return originalTermsNoStemming;
		}
		
		//put all found terms in an ordered set
		Set<RankedTerm> orderedTerms = new TreeSet<RankedTerm> (rankedTerms);
		
		//select most relevant expansion terms
		Set<String> relevantTerms = new HashSet<String>();
		
		
		int expansionterms_limit = ParameterHandler.MAX_EXPANSION_TERMS;
		
		//limit expansion terms
		if(ParameterHandler.LIMIT_EXPANSION_TERMS)
			if(expansionterms_limit>originalTerms.size())
				expansionterms_limit = originalTerms.size();
		
		int queryterms_limit = 
			ParameterHandler.MAX_QUERY_TERMS - stemmedQueryTerms.size();
		for(RankedTerm term: orderedTerms) {
			if(expansionterms_limit<=0 || queryterms_limit<=0)
				break;
			relevantTerms.add(term.getTerm());
			expansionterms_limit--;
			queryterms_limit--;
			//logger.info("termine aggiunto: " + term + ", expansion terms rimamenti: " + expansionterms_limit);
		}
		
		//add original terms
		for(RankedTerm originalTerm: originalTerms)
			relevantTerms.add(originalTerm.getTerm());
		
		//find terms associated with selected stemmed terms
		Map<String, Map<String, Integer>> expansionTerms = null;
		try {
			expansionTerms = this.stemmerHandler.retrieveTerms(relevantTerms);
		} catch (PersistenceException e) {
			e.printStackTrace();
			expansionTerms = new HashMap<String,Map<String,Integer>> ();
		}
		//logger.info("corrispondenze tra termini stemmati e termini: " + expansionTerms);
		
		return expansionTerms;

	}

	private Map<String, Double> initCoOccurrenceValues4tag(RankedTag tag, Map<String, Map<RankedTag, Map<String, Double>>> subMatrix) {
		//Logger logger = LogHandler.getLogger(this.getClass().getName());
		//logger.info("init cooccurrence values for tag: " + tag);
		Map<String,Double> result = new HashMap<String,Double>();
		for(String term1: subMatrix.keySet()) {
			//logger.info("analizzo termine: " + term1);
			//logger.info("contenuto della subMatrix per termine: " + term1 + "\n\n" + subMatrix.get(term1));
			Map<String,Double> terms2value = subMatrix.get(term1).get(tag);
			if(terms2value!=null)
				for(String term2: terms2value.keySet()) {
					result.put(term2, 0.);
				}
		}
		return result;
	}





}
