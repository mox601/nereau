package persistence.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import model.User;
import persistence.PersistenceException;
import persistence.UserDAO;
import persistence.VisitedURLDAO;
import util.LogHandler;

public class UserDAOPostgres implements UserDAO {

	public boolean saveUser(String username, String password, String firstName, String lastName, String email) throws PersistenceException {
		
		boolean result = true;
		
		if(username==null || password==null)
			return false;
		
		if(username.length()<1 || password.length()<1)
			return false;
		
		DataSource dataSource = DataSource.getInstance();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = null;
		try {
			
			statement = connection.prepareStatement(SQL_INSERT);
			statement.setString(1, username);
			statement.setString(2, password);
			statement.setString(3, firstName);
			statement.setString(4, lastName);
			statement.setString(5, email);
			statement.setInt(6, 0);
			statement.executeUpdate();
			
		}
		catch (SQLException e) {
			result = false;
			throw new PersistenceException(e.getMessage());
		}
		finally {
			dataSource.close(statement);
			dataSource.close(connection);
		}
		
		return result;
	}
	
	public Set<User> retrieveUsers() throws PersistenceException {
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		DataSource dataSource = DataSource.getInstance();
		Connection connection = null;
		PreparedStatement statement = null;
		Set<User> users = new HashSet<User>();
		logger.info("creazione insieme utenti");
		try {
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(SQL_RETRIEVE_ALL);
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				String username = result.getString("username");
				String password = result.getString("password");
				User user = new User(username,password);
				users.add(user);		
			}
		}
		catch (SQLException e) {
			logger.info(e.getMessage());
			throw new PersistenceException(e.getMessage());
		}
		finally {
			dataSource.close(statement);
			dataSource.close(connection);
		}
		return users;
	}
	
	public void deleteUser(User user) throws PersistenceException {

		//delete user data
		this.deleteUserData(user);
		
		//delete user
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		DataSource dataSource = DataSource.getInstance();
		Connection connection = null;
		PreparedStatement statement = null;
		logger.info("cancellazione utente");
		try {
			connection = dataSource.getConnection();
			//delete user
			if(user.getUserID()<=0) {
				statement = connection.prepareStatement(SQL_DELETE_USER);
				statement.setString(1, user.getUsername());
			}
			else {
				statement = connection.prepareStatement(SQL_DELETE_USER_BY_USERID);
				statement.setInt(1, user.getUserID());
			}
			statement.executeUpdate();
		}
		catch (SQLException e) {
			logger.info(e.getMessage());
			throw new PersistenceException(e.getMessage());
		}
		finally {
			dataSource.close(statement);
			dataSource.close(connection);
		}
		return;
		
	}
	
	public void deleteUserData(User user) throws PersistenceException {
		
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		DataSource dataSource = DataSource.getInstance();
		Connection connection = null;
		PreparedStatement statement = null;
		Set<Integer> class_id = new HashSet<Integer>();
		logger.info("cancellazione dati utente");
		try {
			connection = dataSource.getConnection();
			
			//retrieve class ids
			if(user.getUserID()<=0) {
				statement = connection.prepareStatement(SQL_RETRIEVE_CLASSES_ID);
				statement.setString(1, user.getUsername());
			}
			else {
				statement = connection.prepareStatement(SQL_RETRIEVE_CLASSES_ID_BY_USERID);
				statement.setInt(1, user.getUserID());
			}
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				int id = result.getInt("id");
				class_id.add(id);		
			}
			
			//delete cooccurrences
			for(int id: class_id) {
				statement = connection.prepareStatement(SQL_DELETE_COOCCURRENCES);
				statement.setInt(1, id);
				statement.executeUpdate();
			}
			
			//delete classes
			if(user.getUserID()<=0) {
				statement = connection.prepareStatement(SQL_DELETE_CLASSES);
				statement.setString(1, user.getUsername());
			}
			else {
				statement = connection.prepareStatement(SQL_DELETE_CLASSES_BY_USERID);
				statement.setInt(1, user.getUserID());
			}
			statement.executeUpdate();
			
			//delete visited urls
			VisitedURLDAO vUrlDAO = new VisitedURLDAOPostgres();
			vUrlDAO.deleteAllVisitedURLs(user);
			
		}
		catch (SQLException e) {
			logger.info(e.getMessage());
			throw new PersistenceException(e.getMessage());
		}
		finally {
			dataSource.close(statement);
			dataSource.close(connection);
		}
		return;
		
	}
	
	
	@Override
	public void saveLastUpdate(User user, long lastUpdate) throws PersistenceException {
		
		//delete user
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		DataSource dataSource = DataSource.getInstance();
		Connection connection = null;
		PreparedStatement statement = null;
		logger.info("last update utente");
		try {
			connection = dataSource.getConnection();
			//delete user
			if(user.getUserID()<=0) {
				statement = connection.prepareStatement(SQL_SAVE_LAST_UPDATE);
				statement.setLong(1, lastUpdate);
				statement.setString(2, user.getUsername());
			}
			else {
				statement = connection.prepareStatement(SQL_SAVE_LAST_UPDATE_BY_USERID);
				statement.setLong(1, lastUpdate);
				statement.setInt(2, user.getUserID());
			}
			statement.executeUpdate();
		}
		catch (SQLException e) {
			logger.info(e.getMessage());
			throw new PersistenceException(e.getMessage());
		}
		finally {
			dataSource.close(statement);
			dataSource.close(connection);
		}
		return;
		
	}
	
	
	@Override
	public User authenticateUser(String username, String password)
			throws PersistenceException {
		
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		DataSource dataSource = DataSource.getInstance();
		Connection connection = null;
		PreparedStatement statement = null;
		User user = null;
		logger.info("creazione insieme utenti");
		try {
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(SQL_AUTHENTICATE);
			statement.setString(1, password);
			statement.setString(2, username);
			ResultSet result = statement.executeQuery();
			if (result.next()) {
				int userID = result.getInt("id");
				String firstName = result.getString("firstname");
				String lastName = result.getString("lastname");
				String email = result.getString("email");
				int role = result.getInt("role");
				user = new User(username,password,firstName,lastName,email,role,userID);		
			}
		}
		catch (SQLException e) {
			logger.info(e.getMessage());
			throw new PersistenceException(e.getMessage());
		}
		finally {
			dataSource.close(statement);
			dataSource.close(connection);
		}
		return user;
	}
	
	
	@Override
	public boolean updateUser(User user) throws PersistenceException {
		
		boolean result = true;
		
		String username = user.getUsername();
		String password = user.getPassword();
		
		if(username==null || password==null)
			return false;
		
		if(username.length()<1 || password.length()<1)
			return false;
		
		DataSource dataSource = DataSource.getInstance();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = null;
		try {
			
			statement = connection.prepareStatement(SQL_UPDATE_USER);
			statement.setString(1, username);
			statement.setString(2, password);
			statement.setString(3, user.getFirstName());
			statement.setString(4, user.getLastName());
			statement.setString(5, user.getEmail());
			statement.setInt(6, user.getRole());
			statement.setInt(7, user.getUserID());
			statement.executeUpdate();
			
		}
		catch (SQLException e) {
			result = false;
			throw new PersistenceException(e.getMessage());
		}
		finally {
			dataSource.close(statement);
			dataSource.close(connection);
		}
		
		return result;
	}
	
	
	@Override
	public User retrieveUser(int userID) throws PersistenceException {
		
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		DataSource dataSource = DataSource.getInstance();
		Connection connection = null;
		PreparedStatement statement = null;
		User user = null;
		logger.info("creazione insieme utenti");
		try {
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(SQL_RETRIEVE_USER_BY_ID);
			statement.setInt(1, userID);
			ResultSet result = statement.executeQuery();
			if (result.next()) {
				String username = result.getString("username");
				String password = result.getString("password");
				String firstName = result.getString("firstname");
				String lastName = result.getString("lastname");
				String email = result.getString("email");
				int role = result.getInt("role");
				user = new User(username,password,firstName,lastName,email,role,userID);		
			}
		}
		catch (SQLException e) {
			logger.info(e.getMessage());
			throw new PersistenceException(e.getMessage());
		}
		finally {
			dataSource.close(statement);
			dataSource.close(connection);
		}
		return user;
	}

	private final String SQL_SAVE_LAST_UPDATE = 
		"UPDATE users " +
		"SET lastupdate=? " +
		"WHERE username=? ";
	
	private final String SQL_SAVE_LAST_UPDATE_BY_USERID = 
		"UPDATE users " +
		"SET lastupdate=? " +
		"WHERE id=? ";
		
	
	
	private final String SQL_RETRIEVE_CLASSES_ID = 
		"SELECT id " +
		"FROM classes " +
		"WHERE iduser = ( " +
		"		SELECT id " +
		"		FROM users " +
		"		WHERE username=? " +
		"	); ";
	
	private final String SQL_RETRIEVE_CLASSES_ID_BY_USERID = 
		"SELECT id " +
		"FROM classes " +
		"WHERE iduser=?; ";
	
	private final String SQL_DELETE_COOCCURRENCES = 
		"DELETE FROM cooccurrences " +
		"WHERE idclass=?;";
	
	private final String SQL_DELETE_CLASSES =
		"DELETE FROM classes " +
		"WHERE iduser = ( " +
		"		SELECT id " +
		"		FROM users " +
		"		WHERE username=? " +
		"	);";
	
	private final String SQL_DELETE_CLASSES_BY_USERID =
		"DELETE FROM classes " +
		"WHERE iduser=?; ";
	
	private final String SQL_DELETE_USER = 
		"DELETE FROM users " +
		"WHERE username=?; ";
	
	private final String SQL_DELETE_USER_BY_USERID = 
		"DELETE FROM users " +
		"WHERE id=?; ";

	private final String SQL_INSERT = 
		"INSERT INTO users (username,password,firstname,lastname,email,role,lastupdate) " +
		"VALUES (?,?,?,?,?,?,0) ";
	
	private final String SQL_RETRIEVE_ALL = 
		"SELECT * " +
		"FROM users";
	
	private final String SQL_AUTHENTICATE = 
		"SELECT id, username, firstname, lastname, email, role " +
		"FROM users " +
		"WHERE password=? AND username=? ";
	
	private final String SQL_UPDATE_USER = 
		"UPDATE users " +
		"SET username=?, password=?, firstname=?, " +
		"lastname=?, email=?, role=? " +
		"WHERE id=? ";
	
	private final String SQL_RETRIEVE_USER_BY_ID = 
		"SELECT username, password, firstname, lastname, email, role " +
		"FROM users " +
		"WHERE id=? ";




}
