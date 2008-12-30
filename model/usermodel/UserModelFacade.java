package model.usermodel;

import model.User;
import model.VisitedURL;
import persistence.PersistenceException;
import persistence.VisitedURLDAO;
import persistence.postgres.VisitedURLDAOPostgres;

public class UserModelFacade {
	
	private static UserModelFacade instance;
	
	private UserModelUpdater userModelUpdater;
	private VisitedURLDAO visitedURLHandler;
	
	private UserModelFacade() {
		this.userModelUpdater = 
			UserModelUpdater.getInstance();
		this.visitedURLHandler = 
			new VisitedURLDAOPostgres();
	}

	public static synchronized UserModelFacade getInstance() {
		if(instance==null)
			instance = new UserModelFacade();
		return instance;
	}
	
	public void updateUserModel(User user) {
		this.userModelUpdater.update(user);
	}

	public void saveVisitedURL(VisitedURL vUrl, User user) {
		try {
			this.visitedURLHandler.saveVisitedURL(vUrl, user);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
	}

	public void updateAllUserModels() {
		this.userModelUpdater.updateAll();
	}


}
