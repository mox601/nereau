package persistence.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Logger;

import model.RankedTag;
import model.URLTags;
import persistence.PersistenceException;
import persistence.URLTagsDAO;
import util.LogHandler;

/* salva gli url e i tag associati. 
 * deve fare distinzione tra inserimenti e aggiornamenti dei dati. 
 * La tabella Ž tagvisitedurls, con tante righe del tipo 
 * (id idtag idurl value) 
 * dove value Ž il numero di volte che si Ž incontrata una annotazione con quel tag riferita a quell'url. 
 * Ž necessario che in ogni lista passata a save(lista) NON ESISTANO URL DUPLICATI. 
 * */

public class URLTagsDAOPostgres implements URLTagsDAO {

	public void save(LinkedList<URLTags> urls) throws PersistenceException {
		DataSource dataSource = DataSource.getInstance();
		Connection connection = dataSource.getConnection();
		
		for (URLTags url: urls) {
			save(url);
		}
		
		dataSource.close(connection);	
	}
	
	
	public void save(String tag, Integer idUrl) throws PersistenceException {
		DataSource dataSource = DataSource.getInstance();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = null;
		
		/* estrai l'id di ogni tag prima di eseguire l'inserimento */
		try {
			statement = connection.prepareStatement(SQL_RETRIEVE_TAG_ID);
			statement.setString(1, tag);
			ResultSet result;
			result = statement.executeQuery();
			Integer idTag = -1;
			if (result.next()) {
				idTag = result.getInt("id");
			}
			save(idTag, idUrl);		
		}
		catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		}
	}
	


	private void save(Integer idTag, Integer idUrl) throws PersistenceException {
		DataSource dataSource = DataSource.getInstance();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = null;
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		logger.info("saving idtag: " + idTag + " idurl: " + idUrl);
		
		try {
			statement = connection.prepareStatement(SQL_UPSERT_TAG_URL);
			int occurrence = 1;
			/* TODO: trova un modo di salvare la stored procedure pgpsql una volta
			 * invece di riscriverla ogni volta. COME? */
			statement.setInt(1, idTag);
			statement.setInt(2, idUrl);
			statement.setInt(3, occurrence);
			ResultSet result;
			result = statement.executeQuery();
			if (result.next()) {
//				System.out.println("idurl: " + result.getInt("idvisitedurl") + 
//						" idtag:  " + result.getInt("idtag") + 
//						" value: " + result.getInt("value"));
			}
			
		}
		catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		}
	}

	
	/* dalla documentazione: http://www.postgresql.org/docs/8.3/static/sql-update.html*/
	
	/*
	CREATE FUNCTION merge_visitedurltags(url INT, tag INT, val INT) RETURNS VOID AS
	$$
	BEGIN 
	LOOP
	UPDATE visitedurltags SET value = (value + val) WHERE idtag = tag AND idvisitedurl = url;
	IF found THEN 
	RETURN;
	END IF;
	BEGIN
	INSERT INTO visitedurltags(id, idvisitedurl, idtag, value) VALUES (DEFAULT, url, tag, val);
	RETURN;
	EXCEPTION WHEN unique_violation THEN
	END;
	END LOOP;
	END;
	$$
	LANGUAGE plpgsql; 
	 * */
	
	
	
	
	/* URL, TAG, VALUE */
	private final String SQL_UPSERT_TAG_URL = 
		"SELECT merge_visitedurltags(?, ?, ?);";


	public void save(URLTags url) throws PersistenceException {
		/* salva su db tutte le coppie (tag1, url), (tag2, url)...*/
		
		/* prima, estrai il valore dell'idurl */
		DataSource dataSource = DataSource.getInstance();
		Connection connection = dataSource.getConnection();
		PreparedStatement statementUrlId = null;
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		logger.info("salvo l'url: " + url.getUrlString());
		
		try {
			statementUrlId = connection.prepareStatement(SQL_RETRIEVE_VISITEDURL_ID);
			statementUrlId.setString(1, url.getUrlString());
			ResultSet result;
			result = statementUrlId.executeQuery();
			Integer idUrl = -1;
			if (result.next()) {
				idUrl = result.getInt("id");
			}

			for(RankedTag rTag: url.getTags()) {
				save(rTag.getTag(), idUrl);
			}
			
		} catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		}
		
		
	}
	
	private final String SQL_RETRIEVE_VISITEDURL_ID = 
		"SELECT id " +
		"FROM visitedurls " +
		"WHERE url = ?;";
	
	
	private final String SQL_RETRIEVE_TAG_ID = 
		"SELECT id " +
		"FROM tags " +
		"WHERE tag = ?;";
		
	/* crea una tabella temporanea e poi la cancella quando c'Ž il commit. 
	 * contiene invece degli id, i valori testuali dei tag e degli url */
	private final String SQL_INSERT_URL_TAGS_1 = 
		"START TRANSACTION; \n" +
//		"CREATE TEMP TABLE tagvisitedurls_temp \n" +
		"CREATE TABLE tagvisitedurls_temp \n" +
		"( \n" +
		"	id serial NOT NULL, \n" +
		"	tag text NOT NULL, \n" +
		"	visitedurl text NOT NULL, " +
		"	\"value\" real NOT NULL, " + 	
		"	PRIMARY KEY (id) \n" +
		"); \n";
//		"ON COMMIT DROP; \n";
	
	/* inserisce nella tabella vera solamente le tuple per cui non esiste gi‡ un valore. 
	 * qui diventa piœ complesso, si dovrebbero sommare i valori */
	
	private final String SQL_INSERT_URL_TAGS_2 = 
		"INSERT INTO tagvisitedurls (tag) \n" +
		"SELECT tag \n" +
		"FROM tagvisitedurls_temp \n" +
		"WHERE NOT EXISTS ( \n" +
		"	SELECT tag \n" +
		"	FROM tags \n" +
		"	WHERE tags.tag = tags_temp.tag \n" +
		"); \n" +
		"COMMIT TRANSACTION; \n";
	
}
