package persistence.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import cluster.Tagtfidf;
import persistence.PersistenceException;
import persistence.TagtfidfDAO;

public class TagtfidfDAOPostgres implements TagtfidfDAO {

	
	@Override
	public Tagtfidf retrieveTag(String tag) throws PersistenceException {
		TagDAOPostgres tagHandler = null;

		Tagtfidf tagExtracted = null;
		/* trova l'id del tag */
		
		int tagId = 0; 
		tagId = tagHandler.retrieveTagId(tag);
		
		 /* TODO: poi costruisci un tagtfidf estraendo i dati dalla tabella visitedurltags */
		
		DataSource dataSource = DataSource.getInstance();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = null;
		
		try {
			String query = SQL_SELECT_TAG_AND_ITS_URLS;
			statement = connection.prepareStatement(query);
			statement.setString(1, tag);
			
			ResultSet result = statement.executeQuery();
			//
			while (result.next()) {
				System.out.println(result.getInt("id") + " " + 
						result.getInt("idvisitedurl") + " " +
						result.getInt("idtag") + " " + 
						result.getInt("value"));
			
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


	
	private static String SQL_SELECT_TAG_AND_ITS_URLS = 
		"SELECT *" +
		"FROM visitedurltags" +
		"WHERE idtag = ?;";



	@Override
	public List<Tagtfidf> retrieveAllTags() throws PersistenceException {
		// TODO estrai tutti i tagtfidf dalla tabella visitedurltags
		return null;
	}
	
	

	

}
