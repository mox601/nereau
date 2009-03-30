package persistence;

//import java.util.Set;
import java.util.List;

import cluster.Tagtfidf;

import model.User;

public interface GlobalProfileModelDAO {
	

	public boolean saveGlobalProfile(String username, String password, String firstName, String lastName, String email) throws PersistenceException;

	public List<Tagtfidf> retrieveTags() throws PersistenceException;
	
	public boolean updateTagtfidf(Tagtfidf tag) throws PersistenceException;
//	public void deleteUser(User user) throws PersistenceException;
	
	public void deleteUserData(User user) throws PersistenceException;

//	public void saveLastUpdate(User user, long lastUpdate) throws PersistenceException;
	
//	public User authenticateUser(String username, String password) throws PersistenceException;

//	public boolean updateUser(User user) throws PersistenceException;
	
//	public User retrieveUser(int userID) throws PersistenceException;

}
