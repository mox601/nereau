package model.queryexpansion;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import model.RankedTag;

public class WeightedExpansionTagsStrategy extends ExpansionTagsStrategy {

	public Set<RankedTag> findExpansionTags(Set<String> stemmedQueryTerms,
			Map<String, Map<RankedTag, Map<String, Double>>> subMatrix) {
		
		//weight to be assigned to every query term
		double termWeight = 1.0 / (double)stemmedQueryTerms.size();
		
		Map<String,Double> weightedTagsMap = new HashMap<String,Double>();
		for(String queryTerm: stemmedQueryTerms) {
			Map<RankedTag, Map<String,Double>> values4term = 
				subMatrix.get(queryTerm);
			if(values4term!=null) {
				for(RankedTag rTag: values4term.keySet()) {
					String tag = rTag.getTag();
					double ranking = termWeight * rTag.getRanking();
					if(weightedTagsMap.containsKey(tag))
						ranking += weightedTagsMap.get(tag);
					weightedTagsMap.put(tag, ranking);
				}
			}
		}
		
		TreeSet<RankedTag> weightedTags = new TreeSet<RankedTag> ();
		for(String tag: weightedTagsMap.keySet())
			weightedTags.add(new RankedTag(tag,weightedTagsMap.get(tag)));
		
		Set<RankedTag> rankedTags = this.selectRelevantTags(weightedTags);
		
		return rankedTags;
	}



}
