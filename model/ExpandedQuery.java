package model;

import java.util.Map;
import java.util.Set;

public class ExpandedQuery extends Query {
	
	private Map<String,Map<String,Integer>> expansionTerms;
	private String expandedQueryString;
	private Set<RankedTag> expansionTags;
	
	public ExpandedQuery(Set<String> terms) {
		super(terms);
	}

	public ExpandedQuery(Map<String,Map<String,Integer>> expansionTerms) {
		super();
		this.expansionTerms = expansionTerms;
		this.expandedQueryString = null;
		this.expansionTags = null;
	}
	
	public ExpandedQuery(Map<String,Map<String,Integer>> expansionTerms, Set<RankedTag> expansionTags) {
		super();
		this.expansionTerms = expansionTerms;
		this.expandedQueryString = null;
		this.expansionTags = expansionTags;
	}
	
	public ExpandedQuery(String expandedQueryString, Set<RankedTag> expansionTags) {
		super();
		this.expansionTerms = null;
		this.expandedQueryString = expandedQueryString;
		this.expansionTags = expansionTags;
	}
	
	public Set<RankedTag> getExpansionTags() {
		return expansionTags;
	}
	
	public boolean isExpanded() {
		return true;
	}
	
	public String toString() {
		
		if(this.expandedQueryString==null)
			this.initExpandedQueryString();
		
		return this.expandedQueryString;
		
	}
	
	private void initExpandedQueryString() {

		boolean firstAND = true;
		StringBuffer buffer = new StringBuffer();
		for(String stemmedTerm: expansionTerms.keySet()) { 
			boolean onlyOneTerm = expansionTerms.get(stemmedTerm).size()==1;;
			if(firstAND==false) {
				if(!onlyOneTerm)
					buffer.append(" AND (");
				else
					buffer.append(" AND ");
			}
			else {
				if(!onlyOneTerm)
					buffer.append("(");
				firstAND = false;
			}
			Map<String,Integer> term2relevance = expansionTerms.get(stemmedTerm);
			boolean firstOR = true;
			for(String term: term2relevance.keySet()) {
				if(firstOR) {
					buffer.append(term);
					firstOR = false;
				}
				else
					buffer.append(" OR " + term);
			}
			if(!onlyOneTerm)
				buffer.append(")");
		}
		this.expandedQueryString = buffer.toString().trim();
		
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((expansionTerms == null) ? 0 : expansionTerms.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ExpandedQuery other = (ExpandedQuery) obj;
		if (expansionTerms == null) {
			if (other.expansionTerms != null)
				return false;
		} else if (!expansionTerms.equals(other.expansionTerms))
			return false;
		return true;
	}

	public void setExpansionTags(Set<RankedTag> expansionTags) {
		this.expansionTags = expansionTags;
	}


}
