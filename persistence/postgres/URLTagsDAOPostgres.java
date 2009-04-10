package persistence.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
 * */

public class URLTagsDAOPostgres implements URLTagsDAO {

	public void save(LinkedList<URLTags> tags) throws PersistenceException {
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
	
	private String prepareStatementForSave(LinkedList<URLTags> tags) {
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		logger.info("salvataggio url + tags contenuti in: " + tags);
		StringBuffer query = new StringBuffer();
		//tags
	
		/* inserisce tutti gli url + tags in una tabella temporanea */
		query.append(SQL_INSERT_URL_TAGS_1);
		for(URLTags urlTags: tags) {
			/* itera sui rankedTags di ogni url */
			for (RankedTag rTag: urlTags.getTags()) {
				/* inserisci la tupla (tagString, urlString) */
				String tag = rTag.getTag();
				String url = urlTags.getUrlString();
				System.out.println("salvo la tupla: " + tag + " " + url);
			}
			
			
			
			
			String insertTagTemp =
				"INSERT INTO tagvisitedurls_temp (tag) \n" +
				"VALUES (\'" + urlTags.getUrlString() + "\'); \n";
			query.append(insertTagTemp);
		}
		/* inserisci nella tabella vera solo le tuple nuove e aggiorna quelle di url-tag gi‡ incontrati */
		query.append(SQL_INSERT_URL_TAGS_2);
		logger.info("query: \n" + query);
		return query.toString();
	}
	
	
	/* crea una tabella temporanea e poi la cancella quando c'Ž il commit */
	private final String SQL_INSERT_URL_TAGS_1 = 
		"START TRANSACTION; \n" +
		"CREATE TEMP TABLE tagvisitedurls_temp \n" +
		"( \n" +
		"	id serial NOT NULL, \n" +
		"	tag text NOT NULL, \n" +
		"	PRIMARY KEY (id) \n" +
		") \n" +
		"ON COMMIT DROP; \n";
	
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
