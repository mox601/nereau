package model.usermodel.tags;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.RankedTag;

public class CompositeSubUrlTagFinderStrategy extends SubUrlTagFinderStrategy {
	
	private List<SubUrlTagFinderStrategy> strategies;
	
	public CompositeSubUrlTagFinderStrategy() {
		super();
		this.strategies = new LinkedList<SubUrlTagFinderStrategy> ();
		this.strategies.add(new BadgesDeliciousSubUrlTagFinderStrategy());
		this.strategies.add(new DeliciousSubUrlTagFinderStrategy());
		this.strategies.add(new StumbleUponSubUrlTagFinderStrategy());
	}

	protected Map<String, Integer> extractTags(String pageContent) {
		return null;
	}

	public Set<RankedTag> findTagsForSubUrl(String urlString, double relevance, boolean exactUrl) {
		Set<RankedTag> foundTags = new HashSet<RankedTag>();
		for(SubUrlTagFinderStrategy strategy: this.strategies) {
			foundTags = strategy.findTagsForSubUrl(urlString, relevance, exactUrl);
			if(foundTags.size()>1)
				break;
		}
		return foundTags;
	}

}
