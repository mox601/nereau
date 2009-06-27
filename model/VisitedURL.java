package model;


public class VisitedURL {
	
	private String urlString;
	private Query query;
	private ExpandedQuery expandedQuery;
	private String expQueryString;
	private final long date;
	//aggiunti per i test
	private String title;
	private String summary;
	private int expansionType;
	


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
	
	//aggiunta per i test
	public VisitedURL(String urlString, String title, String summary, Query query) {
		this.urlString = urlString;
		this.title = title;
		this.summary = summary;
		this.query = query;
		this.date = System.currentTimeMillis();
	}
	
	
	/* used for saving visited url tags TODO */
	public VisitedURL(String urlString, Query query, ExpandedQuery expandedQuery,
			int expansionType) {
		this.urlString = urlString;
		this.title = title;
		this.summary = summary;
		this.expandedQuery = expandedQuery;
		this.query = query;
		this.date = System.currentTimeMillis();
		this.expansionType = expansionType;
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
			"(url=" + urlString + ",query=" + query + ",expansionType=" + expansionType 
			+ " expandedQueryString=" + expandedQuery.toString() +")";
		return result;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the summary
	 */
	public String getSummary() {
		return summary;
	}

	/**
	 * @param summary the summary to set
	 */
	public void setSummary(String summary) {
		this.summary = summary;
	}
	
	/**
	 * @return the expansionType
	 */
	public int getExpansionType() {
		return expansionType;
	}

	/**
	 * @param expansionType the expansionType to set
	 */
	public void setExpansionType(int expansionType) {
		this.expansionType = expansionType;
	}
	

}
