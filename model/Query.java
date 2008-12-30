package model;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import util.Stemmer;


public class Query {
	
	protected Set<String> terms;
	private String queryString;
	protected Stemmer stemmer;

	public Query(String queryString) {
		this.queryString = queryString;
		this.terms = null;
		this.stemmer = new Stemmer();
	}
	
	public Query(Set<String> terms) {
		this.terms = terms;
		this.queryString = null;
		this.stemmer = new Stemmer();
	}
	
	public Query() {
		super();
		this.stemmer =new Stemmer();
	}

	public Set<String> getTerms() {
		if(this.terms==null)
			this.init();
		return terms;
	}
	
	public Set<String> getStemmedTerms() {
		Set<String> stemmedTerms = stemmer.stemQuery(this.getTerms());
		return stemmedTerms;
	}
	
	public Set<RankedTag> getExpansionTags() {
		return null;
	}
	
	public void setExpansionTags(Set<RankedTag> expansionTags) {
		//
	}
	
	public boolean isExpanded() {
		return false;
	}
	
	private void init() {
		Scanner scanner = new Scanner(queryString);
		Pattern delimiter = Pattern.compile("\\W+");
		scanner.useDelimiter(delimiter);
		this.terms = new HashSet<String>();
		while(scanner.hasNext()) {
			terms.add(scanner.next());
		}
	}
	
	public String toString() {
		return this.queryString;
	}

}
 