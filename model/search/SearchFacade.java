package model.search;

import java.util.Map;
import java.util.Set;

import model.ExpandedQuery;
import model.RankedTag;
import model.SearchResult;

public class SearchFacade {
	
	private static SearchFacade instance;
	
	private SearcherAdapter searcher;
	
	public SearchFacade() {
		this.searcher = 
			new YahooSearcherAdapter();
	}
	
	public static synchronized SearchFacade getInstance() {
		if(instance==null)
			instance = new SearchFacade();
		return instance;
	}

	public Map<SearchResult[], Set<RankedTag>> performSearch(Set<ExpandedQuery> expandedQueries) {
		Map<SearchResult[], Set<RankedTag>> searchResults = 
			this.searcher.search(expandedQueries);
		return searchResults;
	}



}
