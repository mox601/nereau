package model.queryexpansion;

import java.util.Set;

import model.ExpandedQuery;
import model.User;

public class QueryExpansionFacade {
	
	private static QueryExpansionFacade instance;
	
	private QueryExpander queryExpander;
	
	private QueryExpansionFacade() {
		this.queryExpander =
			new QueryExpander();
	}
	
	public static synchronized QueryExpansionFacade getInstance() {
		if(instance==null)
			instance = new QueryExpansionFacade();
		return instance;
	}

	public Set<ExpandedQuery> expandQuery(String queryString, User user) {
		Set<ExpandedQuery> expandedQueries =
			this.queryExpander.expandQuery(queryString,user);
		return expandedQueries;
	}



}
