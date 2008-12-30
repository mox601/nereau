package model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import persistence.PersistenceException;
import persistence.UserDAO;
import persistence.UserModelDAO;
import persistence.postgres.UserDAOPostgres;
import persistence.postgres.UserModelDAOPostgres;

public class UserModel {
	
	private User user;
	private UserModelDAO userModelHandler;
	private UserDAO userHandler;
	private Map<String, Map<RankedTag, Map<String, Double>>> userMatrix;
	
	public UserModel(User user) {
		this.user = user;
		this.userModelHandler = new UserModelDAOPostgres();
		this.userHandler = new UserDAOPostgres();
		this.userMatrix = new HashMap<String, Map<RankedTag, Map<String, Double>>>();
	}

	public Map<String, Map<RankedTag, Map<String, Double>>> getSubMatrix(Set<String> terms) {
		Set<String> newTerms = new HashSet<String>();
		newTerms.addAll(terms);
		newTerms.removeAll(this.userMatrix.keySet());
		Set<String> oldTerms = new HashSet<String>();
		oldTerms.addAll(terms);
		oldTerms.removeAll(newTerms);
		Map<String, Map<RankedTag, Map<String, Double>>> persistenceMatrix =
			new HashMap<String, Map<RankedTag, Map<String, Double>>>();
		try {
			persistenceMatrix = this.userModelHandler.retrieveSubMatrix(newTerms,this.user);
		} catch (PersistenceException e) {
			e.printStackTrace();
			//System.exit(1);
		}
		
		this.userMatrix.putAll(persistenceMatrix);
		
		Map<String, Map<RankedTag, Map<String, Double>>> subMatrix = 
			new HashMap<String, Map<RankedTag, Map<String, Double>>> ();
		
		for(String term: terms)
			if(this.userMatrix.get(term)!=null)
				subMatrix.put(term, this.userMatrix.get(term));

		return subMatrix;
	}

	public void update(Map<String, Map<RankedTag, Map<String, Double>>> newInsertMatrix, Map<String, Map<RankedTag, Map<String, Double>>> newUpdateMatrix, long lastUpdate) {
		try {
			//save updates
			this.userModelHandler.save(newInsertMatrix,newUpdateMatrix,this.user);
			Set<String> modifiedTerms = new HashSet<String>();
			if(newInsertMatrix!=null)
				modifiedTerms.addAll(newInsertMatrix.keySet());
			if(newUpdateMatrix!=null)
				modifiedTerms.addAll(newUpdateMatrix.keySet());
			for(String mTerm: modifiedTerms)
				this.userMatrix.remove(mTerm);
			
			//notify last update
			this.userHandler.saveLastUpdate(this.user,lastUpdate);
			
		} catch (PersistenceException e) {
			e.printStackTrace();
			//System.exit(1);
		}

	}

}
