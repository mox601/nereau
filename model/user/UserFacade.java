package model.user;

import persistence.PersistenceException;
import persistence.UserDAO;
import persistence.postgres.UserDAOPostgres;
import model.User;

public class UserFacade {
	
	private static UserFacade instance;
	
	private UserDAO userDAO;

	private UserFacade() {
		//this.instance = new UserFacade();
		this.userDAO = new UserDAOPostgres();
	}
	
	
	public static synchronized UserFacade getInstance() {
		if(instance==null)
			instance = new UserFacade();
		return instance;
	}

	public boolean saveUser(String username, String password, String firstName, String lastName, String email) {
		
		boolean result = false;
		
		try {
			result = this.userDAO.saveUser(username,password,firstName,lastName,email);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return result;
	}

	public User authenticateUser(String username, String password) {
		User user = null;
		try {
			user = this.userDAO.authenticateUser(username, password);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return user;
	}
	
	public void saveLastUpdate(User user, long lastUpdate) {
		try {
			this.userDAO.saveLastUpdate(user,lastUpdate);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
	}


	public boolean updateUser(User user) {
		
		boolean result = false;
		
		try {
			result = this.userDAO.updateUser(user);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return result;
		
	}


	public User retrieveUser(int userID) {
		User user = null;
		try {
			user = this.userDAO.retrieveUser(userID);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		return user;
	}

}
