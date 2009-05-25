package temp;

import model.User;
import persistence.PersistenceException;
import persistence.UserDAO;
import persistence.postgres.UserDAOPostgres;

public class ProvaAutenticazione {
	
	public static void main(String[] args) throws PersistenceException {
		
		UserDAO uDao = new UserDAOPostgres();
		
		User user = uDao.authenticateUser("test", "test");
		
		if(user==null)
			System.out.println("non autenticato");
		
		else {
			System.out.println("userID: " + user.getUserID());
			System.out.println("nome utente: " + user.getUsername());
			System.out.println("password utente: " + user.getPassword());
			System.out.println("firstName utente: " + user.getFirstName());
			System.out.println("lastName utente: " + user.getLastName());
			System.out.println("email utente: " + user.getEmail());
			System.out.println("ruolo utente: " + user.getRole());

			/* ora visita qualche url, per aggiornare il modello utente */
//			visitUrls(user);
			
			
		}
		
	}
	
	
	public static void visitUrls(User user) {
		
		String url1 = "http://woork.blogspot.com/2009/02/free-ajax-components-for-advanced-web.html";
		String url2 = "http://www.smashingmagazine.com/2009/02/25/ruby-on-rails-tips/";
		String url3 = "http://philbaumann.com/2009/01/16/140-health-care-uses-for-twitter/";
		String url4 = "http://i.gizmodo.com/5155942/giz-explains-why-more-megapixels-isnt-always-more-better";
		String url5 = "http://www.gridplane.com/html/";
		
		
		
	}
	
	
	
	

}
