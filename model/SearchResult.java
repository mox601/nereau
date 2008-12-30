package model;

public class SearchResult {
	
	private String url;
	private String title;
	private String summary;
	
	public SearchResult(String url, String title, String summary) {
		this.url = url;
		this.title = title;
		this.summary = summary;
	}

	public String getSummary() {
		return this.summary;
	}

	public String getTitle() {
		return this.title;
	}

	public String getURL() {
		return this.url;
	}
	
	public String toString() {
		return this.url;
	}

}
