package temp;

import model.User;
import persistence.PersistenceException;
import persistence.UserDAO;
import persistence.postgres.UserDAOPostgres;

public class ProvaAutenticazione {
	
	public static void main(String[] args) throws PersistenceException {
		
		UserDAO uDao = new UserDAOPostgres();
		
		User user = uDao.authenticateUser("iddio", "padre");
		
		if(user==null)
			System.out.println("non autenticato");
		else
			System.out.println("userID: " + user.getUserID());
		
	}

}
