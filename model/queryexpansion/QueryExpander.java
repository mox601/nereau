package model.queryexpansion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import cluster.ClusterBuilder;
import cluster.Clustering;
import cluster.Node;
import cluster.Tree;

import persistence.PersistenceException;
import persistence.StemmerDAO;
import persistence.postgres.StemmerDAOPostgres;
import persistence.postgres.TreeDAOPostgres;
import util.LogHandler;
import util.ParameterHandler;

import model.ExpandedQuery;
import model.Query;
import model.RankedTag;
import model.RankedTerm;
import model.User;
import model.UserModel;

public class QueryExpander {
	
	/* TODO: é questa classe che si occupa di espandere la query?
	 * devo aggiungere una strategy? */
	
	private ExpansionTagsStrategy expansionTagsStrategy;
	private ExpansionTagsStrategy tfidfExpansionTagsStrategy;
	private StemmerDAO stemmerHandler;
	
	public QueryExpander() {
		this.stemmerHandler = new StemmerDAOPostgres();
		this.expansionTagsStrategy = 
			new CommonExpansionTagsStrategy();
		this.tfidfExpansionTagsStrategy = 
			new TfidfExpansionTagsStrategy(); 
	}

	public Set<ExpandedQuery> expandQuery(String queryString, User user) {
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		logger.info("query originale: " + queryString);
		Query query = new Query(queryString);
		Set<String> stemmedQueryTerms = query.getStemmedTerms();
		
		logger.info("stemmed query terms: " + stemmedQueryTerms);
				
		UserModel userModel = user.getUserModel();
		Map<String,Map<RankedTag,Map<String,Double>>> subMatrix = 
			userModel.getSubMatrix(stemmedQueryTerms);
		
		logger.info("user model for submitted query: " + subMatrix);
		
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

	public Set<ExpandedQuery> expandQueryTfidf(String queryString, User user) {
		/* si deve basare sui tag che hanno partecipato all'espansione 
		 * vecchia, deve ricevere in qualche modo i vettori dei tag e
		 * i valori dei termini per poi sommarli e formare espansioni diverse */
		
		/* comincia nello stesso modo dell'espansione vecchia */
		
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		logger.info("query originale: " + queryString);
		Query query = new Query(queryString);
		Set<String> stemmedQueryTerms = query.getStemmedTerms();
		
		System.out.println("stemmed query terms: " + stemmedQueryTerms);
				
		UserModel userModel = user.getUserModel();
		/* subMatrix contiene i valori di co-occorrenza, per ogni tag, 
		 * tra RankedTag e stemmed Terms */
		Map<String,Map<RankedTag,Map<String,Double>>> subMatrix = 
			userModel.getSubMatrix(stemmedQueryTerms);
		
		System.out.println("user model for submitted query: " + subMatrix);
		
		//logger.info("modello utente relativo alla query: " + subMatrix);
		Set<ExpandedQuery> expandedQueries = null;
		if(subMatrix.isEmpty())
			expandedQueries = new HashSet<ExpandedQuery>();
		else
			expandedQueries = this.expandTfidf(stemmedQueryTerms,subMatrix);
		
		//
		//System.out.print("Expanded queries: " + expandedQueries);
		
		return expandedQueries;
	}
	
	

	private Set<ExpandedQuery> expandTfidf(Set<String> stemmedQueryTerms,
			Map<String, Map<RankedTag, Map<String, Double>>> subMatrix) {

		Logger logger = LogHandler.getLogger(this.getClass().getName());
		
		//weight to be assigned to every query term
		double termWeight = 1.0 / (double)stemmedQueryTerms.size();
		
		/* devo prendere tutti i tags ! */
//		vecchio metodo, da rimuovere
//		Set<RankedTag> expansionTags = 
//			this.expansionTagsStrategy.findExpansionTags(stemmedQueryTerms, subMatrix);
//		
		// altro insieme, con TUTTI i tags, anche quelli meno rilevanti, senza fare intersezioni tra i tag 
		// associati ai termini della query
		Set<RankedTag> allExpansionTags = 
			this.tfidfExpansionTagsStrategy.findExpansionTags(stemmedQueryTerms, subMatrix);
		
		logger.info("tags (associati a qualche termine della query) per espansione: " + allExpansionTags);
		
		Map<ExpandedQuery, Set<RankedTag>> expandedQueries = 
			new HashMap<ExpandedQuery, Set<RankedTag>> ();
		
		/* ho trovato tutti i tag in relazione con i termini della query. */		
		/* avendo i rankedTags, posso estrarre le loro gerarchie dal database */
		
		TreeDAOPostgres treeHandler = new TreeDAOPostgres();
		
		LinkedList<RankedTag> tagsList = new LinkedList<RankedTag>(allExpansionTags);
		Tree hierarchicalClustering = null;
		try {
			hierarchicalClustering = treeHandler.retrieve(tagsList);
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			hierarchicalClustering = new Tree();
			e.printStackTrace();
		}

		/* potrebbe essere che l'albero sia nullo perché non ho trovato tag 
		 * di cui posso ricostruire la gerarchia.  */		
		if(hierarchicalClustering.getRoot() != null) {
			logger.info("clustering gerarchico dei tag");
			logger.info(hierarchicalClustering.toString());
		} 

		/* dopo che ho le gerarchie, costruisco l'albero ridotto e 
		 * faccio il taglio del clustering gerarchico dove mi conviene 
		 * */

		/* calcolo del taglio piú adatto del clustering gerarchico */
		
		/* si puó fare in due modi: 
		 * 1 con media e varianza delle dimensioni dei clusters
		 * 2 oppure osservando il profilo dell'utente nelle co-occorrenze dei tag
		 *  */
		
		/* 1 - TODO: media e varianza delle dimensioni dei clusters */
//		Clustering clusteringMean = hierarchicalClustering.calculateClusteringByMean();
		
		
		/* per ora il taglio della similarity la faccio a 0.5 */
		Clustering clustering = hierarchicalClustering.cutTreeAtSimilarity(0.5);
		
		/* ogni cluster avrá la sua espansione, calcolata su diversi tag */
		Set<ExpandedQuery> clustersExpansion = new HashSet<ExpandedQuery>();

		int number = 0;
		
		for (HashSet<Node> cluster: clustering.getClustering()) {
			logger.info("< cluster " + number + " >");
			/* per ogni tag del cluster devo sommare i vettori dei pesi 
			 * dei termini associati ai tags */
			/* somma di tutti i tag appartenenti al cluster */
			Map<String, Double> clusterValues = new HashMap<String, Double>(); 
			for (Node tag: cluster) {
				logger.info("calcolo le co-occorrenze per il tag: " 
						+ tag.toString());
				/* trasformo in rankedtag per poter usare le funzioni vecchie */
				RankedTag rTag = new RankedTag(tag.getValue());
				/* calcola i valori di cooccorrenza per il tag nella subMatrix */
				Map<String,Double> coOccurrenceValues4tag =
					this.initCoOccurrenceValues4tag(rTag,subMatrix);
				for(String term1: subMatrix.keySet()) {
					/* per ogni termine contenuto nelle chiavi della submatrix, 
					 * che sarebbero i termini della query stemmati,   
					 * calcola i valori di co-occorrenza termine-tag */
					Map<String,Double> coOccurrenceValues4term4tag = 
						subMatrix.get(term1).get(rTag);
					if(coOccurrenceValues4term4tag!=null) {
						for(String term2: coOccurrenceValues4term4tag.keySet()) {
							double sumValue =
								coOccurrenceValues4tag.remove(term2) 
								+ (termWeight * coOccurrenceValues4term4tag.get(term2));
							coOccurrenceValues4tag.put(term2, sumValue);
						} //for term2
					}

				} //for term1
				
				/* somma i valori di ogni rankedTag in una mappa clusterValues */
				HashMap<String, Double> tempClusterValues = mergeMaps(clusterValues, coOccurrenceValues4tag); 
				clusterValues = tempClusterValues;//TODO???
			}//for node in cluster
			
			/* crea un'espansione a partire da clusterValues */
			
			/* TODO: seleziona solo alcuni termini rilevanti, i primi k? */
			Map<String,Map<String,Integer>> clusterExpansionTerms = 
				this.selectRelevantTerms(stemmedQueryTerms, clusterValues);
			

			/* se ho trovato dei termini con cui fare l'espansione, */

			if(clusterExpansionTerms!=null) {
				ExpandedQuery expandedQuery = new ExpandedQuery(clusterExpansionTerms);
				Set<RankedTag> rankedTags = null;
				if(expandedQueries.containsKey(expandedQuery))
					rankedTags = expandedQueries.get(expandedQuery);
				else {
					rankedTags = new HashSet<RankedTag>();//?
					expandedQueries.put(expandedQuery, rankedTags);
				}
	
				//dovrei aggiungere tutti i tag del cluster corrente
				for (Node tag: cluster) {
					RankedTag rTag = new RankedTag(tag.getValue());
					rankedTags.add(rTag);
				}
	
			}
			
			number++;
		}// for cluster: clustering
		
		
		/* ottengo alla fine un 
		 * Set<ExpandedQuery> result = new HashSet<ExpandedQuery>(); 
		 * che contiene ExpandedQuery fatte cosí: 
		 * una per ogni cluster. */
		
		/* costruisco il risultato, un insieme di ExpandedQueries */
		Set<ExpandedQuery> result = new HashSet<ExpandedQuery>();
		
		for(ExpandedQuery eq: expandedQueries.keySet()) {
			eq.setExpansionTags(expandedQueries.get(eq));
			result.add(eq);
		}
		
		logger.info("query espanse: " + expandedQueries);
		
		
		
		
		/* TODO: OLD SCHOOL */
		/* OLD SCHOOL: per ogni ranked tag  (faró quasi la stessa cosa per ogni 
		 * tag del cluster)*/
//		for(RankedTag tag: expansionTags) {
//			/* calcola i valori di cooccorrenza per il tag nella subMatrix */
//			Map<String,Double> coOccurrenceValues4tag =
//				this.initCoOccurrenceValues4tag(tag,subMatrix);
//			for(String term1: subMatrix.keySet()) {
//				/* per ogni termine contenuto nelle chiavi della submatrix, 
//				 * che sarebbero i termini della query stemmati,   
//				 * calcola i valori di co-occorrenza termine-tag */
//				Map<String,Double> coOccurrenceValues4term4tag = 
//					subMatrix.get(term1).get(tag);
//				if(coOccurrenceValues4term4tag!=null) {
//					for(String term2: coOccurrenceValues4term4tag.keySet()) {
//						double sumValue =
//							coOccurrenceValues4tag.remove(term2) 
//							+ (termWeight * coOccurrenceValues4term4tag.get(term2));
//						coOccurrenceValues4tag.put(term2, sumValue);
//					}
//				}
//			}
//	
//			logger.info("co-occurrence values for tag " + tag + ": " + coOccurrenceValues4tag);
//
//			/* seleziona solo alcuni termini rilevanti, i primi k? */
//			Map<String,Map<String,Integer>> expansionTerms = 
//				this.selectRelevantTerms(stemmedQueryTerms,coOccurrenceValues4tag);
//			
//			/* se ho trovato dei termini con cui fare l'espansione, */
//			
//			if(expansionTerms!=null) {
//				ExpandedQuery expandedQuery = new ExpandedQuery(expansionTerms);
//				Set<RankedTag> rankedTags = null;
//				if(expandedQueries.containsKey(expandedQuery))
//					rankedTags = expandedQueries.get(expandedQuery);
//				else {
//					rankedTags = new HashSet<RankedTag>();
//					expandedQueries.put(expandedQuery, rankedTags);
//				}
//				rankedTags.add(tag);
//			}
//			
//		}
//		
		
		
		
		
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
		
		
		return result;
		
	}
	
	

	public HashMap<String, Double> mergeMaps(Map<String, Double> firstMap,
			Map<String, Double> secondMap) {
		
		HashMap<String, Double> mergedMaps = new HashMap();
		
		for(String key: firstMap.keySet()) {
			Double sumValue = new Double(0.0);
			if (secondMap.containsKey(key)) {
				sumValue = firstMap.get(key) + secondMap.get(key);
				mergedMaps.put(key, sumValue);
				secondMap.remove(key);
			
			} else {
				sumValue = firstMap.get(key);
				mergedMaps.put(key, sumValue);
			}
		}
		
		/* metto le chiavi rimanenti della seconda mappa */
		for (String key: secondMap.keySet()) {
			mergedMaps.put(key, secondMap.get(key));
		}
		return mergedMaps;
	}





}
