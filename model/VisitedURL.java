package model;


public class VisitedURL {
	
	private String urlString;
	private Query query;
	private ExpandedQuery expandedQuery;
	private final long date;
	
	public VisitedURL(String urlString, Query query, ExpandedQuery expandedQuery) {
		this.urlString = urlString;
		this.query = query;
		this.expandedQuery = expandedQuery;
		this.date = System.currentTimeMillis();
	}
	
	public VisitedURL(String urlString, Query query, ExpandedQuery expandedQuery, long date) {
		this.urlString = urlString;
		this.query = query;
		this.expandedQuery = expandedQuery;
		this.date = date;
	}

	public Query getQuery() {
		return query;
	}

	public String getURL() {
		return urlString;
	}

	public long getDate() {
		return date;
	}
	
	public ExpandedQuery getExpandedQuery() {
		return expandedQuery;
	}
	
	public String toString() {
		String result = 
			"(url=" + urlString + ",query=" + query + ")";
		return result;
	}

}
