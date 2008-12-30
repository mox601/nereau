package model;

import java.util.Map;
import java.util.Set;

import model.queryexpansion.QueryExpansionFacade;
import model.search.SearchFacade;
import model.user.UserFacade;
import model.usermodel.UserModelFacade;


public class Nereau {
	
	private static Nereau instance;
	
	private QueryExpansionFacade queryExpansionFacade;
	private SearchFacade searchFacade;
	private UserModelFacade userModelFacade;
	private UserFacade userFacade;
	
	
	public Nereau() {
		this.queryExpansionFacade = 
			QueryExpansionFacade.getInstance();
		this.searchFacade =
			SearchFacade.getInstance();
		this.userModelFacade = 
			UserModelFacade.getInstance();
		this.userFacade = 
			UserFacade.getInstance();
	}
	
	public static synchronized Nereau getInstance() {
		if(instance==null)
			instance = new Nereau();
		return instance;
	}
	
	public Map<SearchResult[], Set<RankedTag>> performSearch(String queryString, User user) {
		Set<ExpandedQuery> expandedQueries = 
			this.queryExpansionFacade.expandQuery(queryString,user);
		Map<SearchResult[], Set<RankedTag>> result = 
			this.searchFacade.performSearch(expandedQueries);
		return result;
	}
	
	public Set<ExpandedQuery> expandQuery(String queryString, User user) {
		Set<ExpandedQuery> expandedQueries = 
			this.queryExpansionFacade.expandQuery(queryString,user);
		return expandedQueries;
	}
	
	public Map<SearchResult[], Set<RankedTag>> performSearch(Set<ExpandedQuery> queries) {
		Map<SearchResult[], Set<RankedTag>> result = 
			this.searchFacade.performSearch(queries);
		return result;
	}
	
	public void updateUserModel(User user) {
		this.userModelFacade.updateUserModel(user);
	}
	
	public void updateAllUserModels() {
		this.userModelFacade.updateAllUserModels();
	}
	
	public void saveVisitedURL(VisitedURL vUrl, User user) {
		this.userModelFacade.saveVisitedURL(vUrl,user);
	}
	
	public User authenticateUser(String username, String password) {
		return this.userFacade.authenticateUser(username,password);
	}
	
	public boolean saveUser(String username, String password, String firstName,
			String lastName, String email) {
		return this.userFacade.saveUser(username,password,firstName,lastName,email);
	}
	
	public boolean updateUser(User user) {
		return this.userFacade.updateUser(user);
	}
	
	public User retrieveUser(int userID) {
		return this.userFacade.retrieveUser(userID);
	}

}
