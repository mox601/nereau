package model.queryexpansion;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import util.LogHandler;

import model.RankedTag;

public class CommonExpansionTagsStrategy extends ExpansionTagsStrategy {
	
	//TODO c'è qualche problema!

	public Set<RankedTag> findExpansionTags(Set<String> stemmedQueryTerms, Map<String, Map<RankedTag, Map<String, Double>>> subMatrix) {
		
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		TreeSet<RankedTag> commonTags = new TreeSet<RankedTag>();
		boolean tagsInitialized = false;
		for(String term: stemmedQueryTerms) {
			if(!subMatrix.containsKey(term)) {
				commonTags.clear();
				break;
			}
			Set<RankedTag> tags4term = subMatrix.get(term).keySet();
			logger.info("set parziale di tags per il termine " + term + ": " + tags4term);
			if(!tagsInitialized) {
				commonTags.addAll(tags4term);
				tagsInitialized = true;
			}
			else
				commonTags.retainAll(tags4term);
			logger.info("intersezione: " + commonTags);
		}
		
		commonTags = this.selectRelevantTags(commonTags);
		
		return commonTags;
	}

}
