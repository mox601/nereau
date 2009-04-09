package persistence.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import model.URLTags;
import model.User;
import cluster.Tagtfidf;
import persistence.GlobalProfileModelDAO;
import persistence.PersistenceException;
import util.LogHandler;

public class GlobalProfileModelDAOPostgres implements GlobalProfileModelDAO {


	private URLTagsDAO urlTagsHandler;
	
	@Override
	public boolean updateGlobalProfile(LinkedList<URLTags> tags) throws PersistenceException {
		/* aggiorna il profilo globale, inserendo gli URLTags nel database
		 * */
		boolean updated = false;
		/* salvo cos’ come sono gli URLTags, non devo convertirli in Tagtfidf, 
		 * non serve */
		this.urlTagsHandler.save(tags);

		
		
		
		
		return updated;
	}

	@Override
	public List<Tagtfidf> retrieveTags() throws PersistenceException {
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		DataSource dataSource = DataSource.getInstance();
		Connection connection = null;
		PreparedStatement statement = null;
		/* lista di tag estratti */
		List<Tagtfidf> tags = new LinkedList<Tagtfidf>();
		logger.info("estrazione dei Tagtfidf");

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
	public boolean updateTagtfidf(Tagtfidf tag) throws PersistenceException {
		// TODO Auto-generated method stub
		return false;
	}

	private final String SQL_RETRIEVE_ALL = 
		"SELECT * " +
		"FROM tagvisitedurls";

	
	
}
