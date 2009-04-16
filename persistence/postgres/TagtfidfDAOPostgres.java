package persistence.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
		// TODO: DEMO
		// estrai tutti i tagtfidf dalla tabella visitedurltags
		
		/* trova tutti i tag salvati nella tabella visitedurltags, 
		 * ho bisogno sia della stringa tag che della stringa url, 
		 * quindi deve essere fatto un join tra tags e visitedurls �con visitedurltags */
		
		HashMap<String, Tagtfidf> allTags = new HashMap<String, Tagtfidf>();
		
		DataSource dataSource = DataSource.getInstance();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = null;
		
		try {
			String query = SQL_JOIN_VISITEDURLTAGS_URLS_TAGS_UNIQUE_TAGS; 
			/* Potrei anche fare una query pi� intelligente, 
			 * che mi da tutti i record che hanno un certo tag
			 */
			
			/* TODO: faccio due query diverse: 
			 * 1) vedo tutti i tag presenti e costruisco per ognuno un Tagtfidf; 
			 * 2) itero su ogni Tagtfidf costruito e faccio una query per estrarre
			 * tutti gli url associati a quel tag. */
			statement = connection.prepareStatement(query);
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				String actualTagString = result.getString("tag");
				System.out.println(actualTagString);
				/* creo un tagtfidf vuoto! per ogni record trovato 
				 * e lo aggiungo alla lista */
//				Tagtfidf actualTag = new Tagtfidf(actualTagString);
//				allTags.put(actualTagString, actualTag);
			}
		}
		catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		}
		/* secondo batch di query: una per ogni tagtfidf. 
		 * faccio una query sul db per ottenere gli url relativi al tagtfidf 
		 * in questione */
		
		
		
		
		
		
		finally {
			dataSource.close(statement);
			dataSource.close(connection);
		}
		
		
		return null;
	}
	
	private static String SQL_JOIN_VISITEDURLTAGS_URLS_TAGS = 
		"SELECT * FROM " +
		"(SELECT * FROM visitedurls " +
		"INNER JOIN visitedurltags ON visitedurls.id = visitedurltags.idvisitedurl) " +
		"AS TAGS_URL " +
		"INNER JOIN tags ON TAGS_URL.idtag = tags.id;";

	
	private static final String SQL_JOIN_VISITEDURLTAGS_URLS_TAGS_UNIQUE_TAGS = 
		"SELECT DISTINCT tag FROM " +
		"(SELECT * FROM visitedurls " +
		"INNER JOIN visitedurltags ON visitedurls.id = visitedurltags.idvisitedurl) " +
		"AS TAGS_URL " +
		"INNER JOIN tags ON TAGS_URL.idtag = tags.id;";


	

}
