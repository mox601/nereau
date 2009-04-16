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
		 * quindi deve essere fatto un join tra tags e visitedurls Êcon visitedurltags */
		
		HashMap<String, Tagtfidf> allTags = new HashMap<String, Tagtfidf>();
		
		DataSource dataSource = DataSource.getInstance();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = null;
		
		try {
			String query = SQL_JOIN_VISITEDURLTAGS_URLS_TAGS; 
			/* Potrei anche fare una query piœ intelligente, 
			 * che mi da tutti i record che hanno un certo tag
			 * TODO: come fare la query? */
			statement = connection.prepareStatement(query);
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				String actualTagString = result.getString("tag");
				String actualUrlString = result.getString("url");
				Integer actualFrequency = result.getInt("value");
				System.out.println(actualUrlString + " " + 
						actualTagString + " " + actualFrequency);
				/* per ogni riga del risultato, */ 
				 /* query stupida, metodo di costruzione stupido... */
				if (allTags.containsKey(actualTagString)) {
					allTags.get(actualTagString).addUrlOccurrences(actualUrlString, actualFrequency);
				} else {
					Map<String, Integer> tagUrlsMap = new HashMap<String, Integer>();
					/* costruzione della mappa degli url per il tag corrente */
					
					Tagtfidf tagTfidf = new Tagtfidf(actualTagString, tagUrlsMap);
					allTags.put(actualTagString, tagTfidf);
				}

				
			
			}
		}
		catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		}
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

	

}
