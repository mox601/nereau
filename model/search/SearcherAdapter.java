package model.search;

import java.util.Map;
import java.util.Set;

import model.ExpandedQuery;
import model.RankedTag;
import model.SearchResult;

public interface SearcherAdapter {
	
	public Map<SearchResult[], Set<RankedTag>> search(Set<ExpandedQuery> expandedQueries);

}
