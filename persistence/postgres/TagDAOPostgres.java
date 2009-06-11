package persistence.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.logging.Logger;

import model.RankedTag;
import persistence.PersistenceException;
import persistence.TagDAO;
import util.LogHandler;

public class TagDAOPostgres implements TagDAO {

	public void save(Set<RankedTag> tags) throws PersistenceException {
		DataSource dataSource = DataSource.getInstance();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = null;
		try {
			String query = this.prepareStatementForSave(tags);
			statement = connection.prepareStatement(query);
			statement.executeUpdate();
		}
		catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		}
		finally {
			dataSource.close(statement);
			dataSource.close(connection);
		}
	}

	private String prepareStatementForSave(Set<RankedTag> tags) {
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		logger.info("salvataggio tags contenuti in: " + tags);
		StringBuffer query = new StringBuffer();
		//tags
		query.append(SQL_INSERT_TAGS_1);
		for(RankedTag rTag: tags) {
			String insertTagTemp =
				"INSERT INTO tags_temp (tag) \n" +
				"VALUES (\'" + rTag.getTag() + "\'); \n";
			query.append(insertTagTemp);
		}
		query.append(SQL_INSERT_TAGS_2);
		logger.info("query: \n" + query);
		return query.toString();
	}
	
	private final String SQL_INSERT_TAGS_1 = 
		"START TRANSACTION; \n" +
		"CREATE TEMP TABLE tags_temp \n" +
		"( \n" +
		"	id serial NOT NULL, \n" +
		"	tag text NOT NULL, \n" +
		"	PRIMARY KEY (id) \n" +
		") \n" +
		"ON COMMIT DROP; \n";
	
	private final String SQL_INSERT_TAGS_2 = 
		"INSERT INTO tags (tag) \n" +
		"SELECT tag \n" +
		"FROM tags_temp \n" +
		"WHERE NOT EXISTS ( \n" +
		"	SELECT tag \n" +
		"	FROM tags \n" +
		"	WHERE tags.tag = tags_temp.tag \n" +
		"); \n" +
		"COMMIT TRANSACTION; \n";

	@Override
	public int retrieveTagId(String tag) throws PersistenceException {
		DataSource dataSource = DataSource.getInstance();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = null;
		int id = -1;
		try {
			String query = this.prepareStatementForRetrieve(tag);
			statement = connection.prepareStatement(query);
			statement.setString(1, tag);
			ResultSet result = statement.executeQuery();
			if (result.next()) {
				id = result.getInt("id");
			}
		}
		catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		}
		finally {
			dataSource.close(statement);
			dataSource.close(connection);
		}
		
		return id;
	}

	private String prepareStatementForRetrieve(String tag) {
		Logger logger = LogHandler.getLogger(this.getClass().getName());
//		logger.info("estrazione tag: " + tag);
		StringBuffer query = new StringBuffer();
		query.append(SQL_SELECT_ID_TAG);
//		logger.info("query: \n" + query);
		return query.toString();
	}

		
	private final String SQL_SELECT_ID_TAG = 
		"SELECT id " +
		"FROM tags " +
		"WHERE tag = ?;";
	
//	la solitudine ripaga con l'eccellenza

	
}
