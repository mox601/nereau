package persistence.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cluster.Tagtfidf;
import persistence.PersistenceException;
import persistence.TagtfidfDAO;

public class TagtfidfDAOPostgres implements TagtfidfDAO {

	private TagDAOPostgres tagHandler;
	
	@Override
	public Tagtfidf retrieveTag(String tag) throws PersistenceException {
		Tagtfidf tagExtracted = null;
		/* trova l'id del tag */
		
		int tagId = tagHandler.retrieveTagId(tag);
		
		 /* poi costruisci un tagtfidf estraendo i dati dalla tabella visitedurltags */
		
		
		DataSource dataSource = DataSource.getInstance();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = null;
		
		try {
			String query = this.prepareStatementForRetrieve(tagId);
			statement = connection.prepareStatement(query);
			statement.setString(1, tag);
			ResultSet result = statement.executeQuery();
			//
			if (result.next()) {
//				id = result.getInt("id");
			}
		}
		catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		}
		finally {
			dataSource.close(statement);
			dataSource.close(connection);
		}
		
		
		
		
		
		return tagExtracted;
	}

}
