package model.queryexpansion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import persistence.PersistenceException;
import persistence.UserDAO;
import persistence.postgres.UserDAOPostgres;

import model.ExpandedQuery;
import model.Query;
import model.RankedTag;
import model.User;
import model.UserModel;
import util.LogHandler;

public class MultipleUserQueryExpander extends QueryExpander {
	
	
	
	
	public Set<ExpandedQuery> expandQuery(String queryString, User user) {
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		logger.info("query originale: " + queryString);
		Query query = new Query(queryString);
		Set<String> stemmedQueryTerms = query.getStemmedTerms();
		Map<String,Map<RankedTag,Map<String,Double>>> subMatrix = 
			new HashMap<String,Map<RankedTag,Map<String,Double>>>();
		
		//retrieve all users
		UserDAO userDAO = new UserDAOPostgres();
		Set<User> allUsers = new HashSet<User>();
		try {
			allUsers = userDAO.retrieveUsers();
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		
		//filter users with provided name
		for(User u: allUsers) {
			if(u.getUsername().startsWith(user.getUsername()))
				this.updateSubMatrix(u,stemmedQueryTerms,subMatrix);
		}

		//logger.info("modello utente relativo alla query: " + subMatrix);
		Set<ExpandedQuery> expandedQueries = 
			this.expand(stemmedQueryTerms,subMatrix);
		return expandedQueries;
	}

	private void updateSubMatrix(User user, Set<String> stemmedQueryTerms, Map<String, Map<RankedTag, Map<String, Double>>> subMatrix) {

		UserModel userModel = user.getUserModel();
		Map<String,Map<RankedTag,Map<String,Double>>> userSubMatrix = 
			userModel.getSubMatrix(stemmedQueryTerms);
		
		//update existing matrix
		for(String term1: userSubMatrix.keySet()) {
			
			Map<RankedTag,Map<String,Double>> userValues4term1 = userSubMatrix.get(term1);
			if(!subMatrix.containsKey(term1))
				subMatrix.put(term1, userValues4term1);
			else {
				Map<RankedTag,Map<String,Double>> values4term1 = subMatrix.get(term1);
				
				for(RankedTag rt: userValues4term1.keySet()) {
					
					Map<String,Double> userValues4tag = userValues4term1.get(rt);
					if(!values4term1.containsKey(rt))
						values4term1.put(rt, userValues4tag);
					else {
						
						Map<String,Double> values4tag = values4term1.get(rt);
						
						for(String term2: userValues4tag.keySet()) {
							
							double userValue4term2 = userValues4tag.get(term2);
							if(!values4tag.containsKey(term2))
								values4tag.put(term2,userValue4term2);
							else {
								double value4term2 = values4tag.get(term2);
								value4term2 += userValue4term2;
								values4tag.put(term2, value4term2);
							}
							
						}
						
					}
				}
				
			}
		}
		
	}

}
