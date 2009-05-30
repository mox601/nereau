package model.usermodel.tags;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import persistence.PersistenceException;
import persistence.TagDAO;
import persistence.postgres.TagDAOPostgres;
import util.LogHandler;
import util.ParameterHandler;

import model.RankedTag;

public abstract class SubUrlTagFinderStrategy {
	
	private TagDAO tagHandler;
	
	public SubUrlTagFinderStrategy() {
		this.tagHandler = new TagDAOPostgres();
	}
	
	protected void save(Set<RankedTag> tags) throws PersistenceException {
		this.tagHandler.save(tags);
	}
	
	public abstract Set<RankedTag> findTagsForSubUrl(String urlString, double relevance, boolean exactUrl);
	
	protected void updateResult(Map<String, Integer> rankedTags, String pageContent) {
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		Map<String,Integer> partialResult = this.extractTags(pageContent);
		logger.info("tags parziali estratti" + partialResult);
		for(String tag: partialResult.keySet()) {
			if(rankedTags.containsKey(tag)) {
				int newKey = rankedTags.get(tag) + partialResult.get(tag);
				rankedTags.put(tag,newKey);
			}
			else
				rankedTags.put(tag, partialResult.get(tag));
		}
		return;
	}
	
	protected abstract Map<String, Integer> extractTags(String pageContent);

	
	protected Set<RankedTag> normalizeRanking(Map<String, Integer> rankedTags, double relevance) {
		int maxRanking = 0;
		//trova il ranking massimo
		for(String tag: rankedTags.keySet()) {
			int ranking = rankedTags.get(tag);
			if(ranking>maxRanking)
				maxRanking = ranking;
		}
		Set<RankedTag> normalizedRankedTags = 
			new TreeSet<RankedTag> ();
		
		for(String tag: rankedTags.keySet()) {
			double oldRanking = (double)rankedTags.get(tag);
			double newRanking = oldRanking * relevance / (double)maxRanking;
			RankedTag rTag = new RankedTag(tag,newRanking);
			//aggiungo il tag ai tag normalizzati solo se é maggiore di una soglia
			//vorrei abbassarla solo nel caso in cui prendo i tag per 
			//il tag tfidf: ne voglio di piú, quindi il valore MIN_TAG... 
			//ne prendo la stessa quantità. 
		
			//lo aggiungo comunque, senza if!
			//ritorno a come era prima, per limitare i calcoli del nereau vecchio
			//TODO: separare la quantità di tag da estrarre nei casi old e new, 
			//altrimenti l'old fa troppi calcoli con così tanti tags
			if(newRanking>(ParameterHandler.MIN_TAGFINDER_RELATIVE_VALUE*relevance))
				normalizedRankedTags.add(rTag);
			
		}
		
		//add NULL_TAG - perché?? io lo levo! 
//		normalizedRankedTags.add(ParameterHandler.NULL_TAG);
		
		return normalizedRankedTags;
	}


}
