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
import java.util.logging.Logger;

import cluster.Tagtfidf;
import persistence.PersistenceException;
import persistence.TagtfidfDAO;
import util.LogHandler;

public class TagtfidfDAOPostgres implements TagtfidfDAO {

	@Override
	public Tagtfidf retrieveTag(String tag) throws PersistenceException {
		TagDAOPostgres tagHandler = new TagDAOPostgres();

		Tagtfidf tagExtracted = null;
		/* trova l'id del tag */
		
		int tagId = 0; 
		tagId = tagHandler.retrieveTagId(tag);
		
		 /* poi costruisci un tagtfidf estraendo i dati dalla tabella visitedurltags */
		
		DataSource dataSource = DataSource.getInstance();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = null;
		
		try {
			String query = SQL_SELECT_TAG_AND_ITS_URLS;
			statement = connection.prepareStatement(query);
			statement.setInt(1, tagId);
			
			ResultSet result = statement.executeQuery();
			//
			Map<String, Double> tagUrlsMap = new HashMap<String, Double>();
			while (result.next()) {
//				System.out.println(result.getInt("id") + " " + 
//						result.getInt("idtag") + " " +
//						result.getInt("idurl") + " " + 
//						result.getInt("value") + " " + 
//						result.getString("url"));
				
				//costruisci il tagtfidf, con i valori degli url, non con gli id
				
			tagUrlsMap.put(result.getString("url"), new Double(result.getInt("value")));
				
			}
			
			tagExtracted = new Tagtfidf(tag, tagUrlsMap);

			
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
		"SELECT * " +
		"FROM (tagvisitedurls join visitedurls " +
		"on tagvisitedurls.idurl = visitedurls.id) " +
		"WHERE idtag = ?;";



	@Override
	public List<Tagtfidf> retrieveAllTags() throws PersistenceException {
		// estrai tutti i tagtfidf dalla tabella tagvisitedurls
		
		/* trova tutti i tag salvati nella tabella tagvisitedurls, 
		 * ho bisogno sia della stringa tag che della stringa url, 
		 * quindi deve essere fatto un join tra tags e visitedurls Êcon tagvisitedurls */
		
		List<Tagtfidf> allTags = new LinkedList<Tagtfidf>();
		
		DataSource dataSource = DataSource.getInstance();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = null;
		
		try {
			String query = SQL_JOIN_TAGVISITEDURLS_URLS_TAGS_UNIQUE_TAGS; 
			/* Potrei anche fare una query piœ intelligente, 
			 * che mi da tutti i record che hanno un certo tag. 
			 */
			
			/* TODO: faccio due query diverse: 
			 * 1) vedo tutti i tag presenti e costruisco per ognuno un Tagtfidf; 
			 * 2) itero su ogni Tagtfidf costruito e faccio una query per estrarre
			 * tutti gli url associati a quel tag. */
			
			statement = connection.prepareStatement(query);
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				String actualTagString = result.getString("tag");
				/* creo un tagtfidf vuoto per ogni record trovato 
				 * e lo aggiungo alla lista */
				Tagtfidf actualTag = new Tagtfidf(actualTagString);
				allTags.add(actualTag);
//				System.out.println("aggiunto alla lista il tag: " + actualTag.getTag());
			}
		}
		catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		}
		
		/* secondo batch di query: una per ogni tagtfidf. 
		 * faccio una query sul db per ottenere gli url relativi al tagtfidf 
		 * in questione */
	
		
		PreparedStatement statementJoin = null;

		try {
			String queryJoin = SQL_JOIN_TAGVISITEDURLS_URLS_TAGS_EACH_TAG_URLS; 

			for (Tagtfidf tag: allTags) {
				statementJoin = connection.prepareStatement(queryJoin);
				statementJoin.setString(1, tag.getTag());
				ResultSet resultTag = statementJoin.executeQuery();
//				System.out.println("building tag: " + tag.getTag());
				
				while (resultTag.next()) {
					/* TODO: crea un Tagtfidf completo, con tutti gli attributi */
					String actualUrl = resultTag.getString("url");
//					System.out.println("url estratto: " + actualUrl);
					tag.addUrlOccurrences(actualUrl, 1.0);
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
		
		
		return allTags;
	}
	
	
	/* cancella tutte le righe dalla tabella tagvisitedurls, per pulire tra un 
	 * test e l'altro */
	public void deleteTagVisitedUrls() throws PersistenceException {
		Logger logger = LogHandler.getLogger(this.getClass().getName());

		DataSource dataSource = DataSource.getInstance();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = null;
		try {
			System.out.println("deleting all tagvisitedurls");
			statement = connection.prepareStatement(SQL_DELETE_ALL_TAGVISITEDURLS);
			statement.executeUpdate();

		} catch (SQLException e){
			throw new PersistenceException(e.getMessage());
		}
		finally {
			dataSource.close(statement);
			dataSource.close(connection);
		}

	}
	
	
	
	private static String SQL_DELETE_ALL_TAGVISITEDURLS = 
		"DELETE from tagvisitedurls;";
	




	private static String SQL_JOIN_TAGVISITEDURLS_URLS_TAGS = 
		"SELECT * FROM " +
		"(SELECT * FROM visitedurls " +
		"INNER JOIN tagvisitedurls ON visitedurls.id = tagvisitedurls.idurl) " +
		"AS TAGS_URL " +
		"INNER JOIN tags ON TAGS_URL.idtag = tags.id;";
	
	
	private static String SQL_JOIN_TAGVISITEDURLS_URLS_TAGS_EACH_TAG_URLS = 
		"SELECT  TAGS_URL.url, tags.tag FROM " +
		"(SELECT * FROM visitedurls " +
		"INNER JOIN tagvisitedurls ON visitedurls.id = tagvisitedurls.idurl) " +
		"AS TAGS_URL " +
		"INNER JOIN tags ON TAGS_URL.idtag = tags.id " +
		"WHERE tags.tag = ?;";

	
	private static final String SQL_JOIN_TAGVISITEDURLS_URLS_TAGS_UNIQUE_TAGS = 
		"SELECT DISTINCT tag FROM " +
		"(SELECT * FROM visitedurls " +
		"INNER JOIN tagvisitedurls ON visitedurls.id = tagvisitedurls.idurl) " +
		"AS TAGS_URL " +
		"INNER JOIN tags ON TAGS_URL.idtag = tags.id;";


	

}
