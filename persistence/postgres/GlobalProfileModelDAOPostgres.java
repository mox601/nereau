package persistence.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import model.User;
import cluster.Tagtfidf;
import persistence.GlobalProfileModelDAO;
import persistence.PersistenceException;
import util.LogHandler;

public class GlobalProfileModelDAOPostgres implements GlobalProfileModelDAO {

	@Override
	public void deleteUserData(User user) throws PersistenceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Tagtfidf> retrieveTags() throws PersistenceException {
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		DataSource dataSource = DataSource.getInstance();
		Connection connection = null;
		PreparedStatement statement = null;
		/* lista di tag estratti */
		List<Tagtfidf> tags = new LinkedList<Tagtfidf>();
		logger.info("creazione insieme Tagtfidf");

		try {
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(SQL_RETRIEVE_ALL);
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				/* estraggo i dati dal result */
				
				/* creazione del tagtfidf */
				Tagtfidf tag = new Tagtfidf(tags);
				tags.add(tag);
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
		
		
		
		
		
		return tags;
	}

	@Override
	public boolean saveGlobalProfile(String username, String password,
			String firstName, String lastName, String email)
			throws PersistenceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateTagtfidf(Tagtfidf tag) throws PersistenceException {
		// TODO Auto-generated method stub
		return false;
	}

	/* TODO: modifica il nome della tabella - users*/
	private final String SQL_RETRIEVE_ALL = 
		"SELECT * " +
		"FROM users";
	
}
