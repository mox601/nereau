package model.queryexpansion;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import util.LogHandler;
import util.ParameterHandler;

import model.RankedTag;

public abstract class ExpansionTagsStrategy {
	
	public abstract Set<RankedTag> findExpansionTags(Set<String> stemmedQueryTerms,
			Map<String, Map<RankedTag, Map<String, Double>>> subMatrix);
	
	protected TreeSet<RankedTag> selectRelevantTags(TreeSet<RankedTag> tags) {
		
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		logger.info("tags prima della selezione: " + tags);
		
		
		//NUOVO: tolgo il null_tag, sempre
		tags.remove(ParameterHandler.NULL_TAG);
		
		
		double maxRanking = 0;
		boolean maxIsNullTag = false;
		for(RankedTag rTag: tags) {
			if(rTag.getRanking()>maxRanking) {
				maxRanking = rTag.getRanking();
				if(rTag.getTag().equals(ParameterHandler.NULL_TAG.getTag()))
					maxIsNullTag = true;
			}

		}
		
		logger.info("max ranking: " + maxRanking);
		
		TreeSet<RankedTag> result = new TreeSet<RankedTag>();
		
		for(int i=ParameterHandler.MAX_EXPANSION_TAGS;i>0 && tags.size()>0;i--) {
			RankedTag temp = tags.pollFirst();
			
			if(temp.getTag().equals(ParameterHandler.NULL_TAG.getTag()))
				continue;
			
			
			logger.info("ranked tag esaminato: " + temp);
			double tempRanking = temp.getRanking();
			if(tempRanking / maxRanking < ParameterHandler.MIN_EXPANSIONTAGS_RELATIVE_VALUE) {
				if(maxIsNullTag)
					result.add(temp);
				break;
			}
			result.add(temp);
		}
		return result;
	}

}
