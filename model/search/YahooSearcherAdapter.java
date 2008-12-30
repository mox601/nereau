package model.search;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import model.ExpandedQuery;
import model.Query;
import model.RankedTag;
import model.SearchResult;

import com.yahoo.search.SearchClient;
import com.yahoo.search.SearchException;
import com.yahoo.search.WebSearchRequest;
import com.yahoo.search.WebSearchResult;

public class YahooSearcherAdapter implements SearcherAdapter {

	public Map<SearchResult[], Set<RankedTag>> search(Set<ExpandedQuery> expandedQueries) {
		Map<SearchResult[], Set<RankedTag>> result = 
			new HashMap<SearchResult[], Set<RankedTag>>();
		SearchClient client = new SearchClient("javasdktest");
		for(Query query: expandedQueries) {
			String queryString = query.toString();
			Set<RankedTag> tags = query.getExpansionTags();
			WebSearchRequest request = new WebSearchRequest(queryString);
            SearchResult[] searchResults = null;
	        try {
	            WebSearchResult[] results = client.webSearch(request).listResults();
	            
	            searchResults = new SearchResult[results.length];

	            for (int i = 0; i < searchResults.length; i++) {
	            	String url = 
	            		results[i].getUrl();
	            	String title = 
	            		results[i].getTitle();
	            	String summary =
	            		results[i].getSummary();
	                searchResults[i] = 
	                	new SearchResult(url,title,summary);
	            }
	        }
	        catch (IOException e) {
	            System.err.println("Error calling Yahoo! Search Service: " +
	                    e.toString());
	            e.printStackTrace(System.err);
	        }
	        catch (SearchException e) {
	            System.err.println("Error calling Yahoo! Search Service: " +
	                    e.toString());
	            e.printStackTrace(System.err);
	        }
	        
	        result.put(searchResults,tags);
		}
		return result;
	}


}
