package persistence.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Logger;

import model.RankedTag;
import model.URLTags;
import persistence.PersistenceException;
import persistence.URLTagsDAO;
import util.LogHandler;

/* salva le associazioni tra gli url e i tag. 
 * deve fare distinzione tra inserimenti e aggiornamenti dei dati. 
 * La tabella Ž tagvisitedurls, con tante righe del tipo 
 * (id idtag idurl value) 
 * dove value Ž il numero di volte che si Ž incontrata una annotazione con quel tag riferita a quell'url. 
 * Ž necessario che in ogni lista passata a save(lista) NON ESISTANO URL DUPLICATI. 
 * */

public class URLTagsDAOPostgres implements URLTagsDAO {
	
	
	private String urlString;
	private String tagString;

	public void save(LinkedList<URLTags> urls) throws PersistenceException {
//		DataSource dataSource = DataSource.getInstance();
//		Connection connection = dataSource.getConnection();
		
		for (URLTags url: urls) {
			save(url);
		}
		
//		dataSource.close(connection);	
	}
	
	
	public void save(URLTags url) throws PersistenceException {
		/* salva su db tutte le coppie (tag1, url), (tag2, url)...*/
		
		/* prima, estrai il valore dell'idurl */
		DataSource dataSource = DataSource.getInstance();
		Connection connection = dataSource.getConnection();
		PreparedStatement statementUrlId = null;
		this.urlString = url.getUrlString();
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		logger.info("salvo l'url: " + this.urlString);
		
		try {
			/* mi restituisce il primo id con cui Ž apparso quell'url 
			 * l'url sar‡ sicuramente in quella tabella? 
			 * Ž gi‡ stato salvato? NO, perchŽ ancora non Ž stato inserito dall'updater...!!
			 * 
			 * Ž un problema avere due visitedurls diversi
			 * con la stessa stringa url? no, se ottengo sempre il primo
			 * id allora funziona
			 * Potrei fare una nuova tabella con solo gli url visitati, 
			 * senza distinguere due url quando sono visitati da due utenti
			 * diversi!!!
			 * */
			statementUrlId = connection.prepareStatement(SQL_RETRIEVE_VISITEDURL_ID);
			statementUrlId.setString(1, url.getUrlString());
			ResultSet result;
			result = statementUrlId.executeQuery();
			Integer idUrl = -1;
			if (result.next()) {
				idUrl = result.getInt("id");
			} else {
				logger.info("il visitedurl " + url.getUrlString() + " non Ž ancora stato salvato nel database. ");
				
			}
	
			for(RankedTag rTag: url.getTags()) {
				save(idUrl, rTag.getTag());
			}
			
		} catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		}
		
		
	}


	public void save(Integer idUrl, String tag) throws PersistenceException {
		DataSource dataSource = DataSource.getInstance();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = null;
		
		this.tagString = tag;
		
		/* estrai l'id di ogni tag prima di eseguire l'inserimento */
		
		/* se il tag Ž gi‡ stato incontrato ed Ž 
		 * presente nel database, allora prendi l'id da l’ */
		
		try {
			statement = connection.prepareStatement(SQL_RETRIEVE_TAG_ID);
			statement.setString(1, tag);
			ResultSet result;
			result = statement.executeQuery();
			Integer idTag = -1;
			if (result.next()) {
				idTag = result.getInt("id");
			}
//			System.out.println("valore id del tag: " + idTag);
			
			/* il tag Ž gi‡ presente nel database? */
			if (idTag > 0) {
				save(idUrl, idTag);	
			} else {
				/* SALVO il nuovo tag nei tags */
//				System.out.println("il tag Ž nuovo nella tabella tags");
				TagDAOPostgres tagHandler = new TagDAOPostgres();
				RankedTag newTagToSave = new RankedTag(tag);
				Set<RankedTag> tags = new HashSet<RankedTag>();
				tags.add(newTagToSave);
				tagHandler.save(tags);
				
				/* CERCO di nuovo il tag nel database per ottenere l'id: 
				 * stavolta DEVO trovarlo */
//				System.out.println("ho salvato il tag, ora cerco l'id");
				
				try {
					PreparedStatement statementID = connection.prepareStatement(SQL_RETRIEVE_TAG_ID);
					statementID.setString(1, tag);
					ResultSet resultID;
					resultID = statementID.executeQuery();
					idTag = -1;
					if (resultID.next()) {
						idTag = resultID.getInt("id");
					}
					/* salvo url-tag sul db */
					save(idUrl, idTag);
//					System.out.println("tag nuovo salvato");
				}
				catch (SQLException e) {
					throw new PersistenceException(e.getMessage());
				} // per la ricerca dell'id
				
				
			}
		}
		catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		}// per il salvataggio dell' URLTags
	}
	

/* aggiunge UNA occorrenza per la coppia tag url 
 * significa che si Ž visitato un url con quel tag */
	private void save(Integer idUrl, Integer idTag) throws PersistenceException {
		DataSource dataSource = DataSource.getInstance();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = null;
		Logger logger = LogHandler.getLogger(this.getClass().getName());
//		logger.info("saving " + this.urlString + " " + this.tagString + 
//				" idurl: " + idUrl + " idtag: " + idTag);
		
		/* ho l'id url e l'id tag:
		 * 1 - faccio una query e vedo se ottengo dei risultati 
		 * 2a - se ottengo un risultato (id), aggiorno il valore sul database con
		 * value += 1 
		 * 2b - altrimenti, faccio un inserimento con  valore 1 */
		
		try {
			statement = connection.prepareStatement(SQL_SEARCH_TAG_URL);
			
			statement.setInt(1, idTag);
			statement.setInt(2, idUrl);
			
			ResultSet firstResult;
			//ottengo id e value
			firstResult = statement.executeQuery();
			int rowIdTagUrl = -1;
			Float value = new Float(-1.0);
			//vedi se Ž presente la coppia tag-url
			if (firstResult.next()) {
				rowIdTagUrl = firstResult.getInt("id");
				value = firstResult.getFloat("value");				
			}
			
			//non Ž presente la riga tag-url che voglio inserire
			//quindi la AGGIUNGO
			if (rowIdTagUrl == -1) {
				
				try {
					PreparedStatement insertStatement = connection.prepareStatement(SQL_INSERT_TAG_URL);
					insertStatement.setInt(1, idTag);
					insertStatement.setInt(2, idUrl);
					insertStatement.setFloat(3, new Float(1.0));
					int insertResult;
					insertResult = insertStatement.executeUpdate();
				} catch(SQLException e) {
					throw new PersistenceException(e.getMessage());
				}

				
				
			} else {
				//se Ž presente, la AGGIORNO
				try {
					PreparedStatement updateStatement = connection.prepareStatement(SQL_UPDATE_TAG_URL);
					updateStatement.setInt(1, rowIdTagUrl);
					updateStatement.setInt(2, idTag);
					updateStatement.setInt(3, idUrl);
					int updateResultRows;
					updateResultRows = updateStatement.executeUpdate();
					
				} catch(SQLException e) {
						throw new PersistenceException(e.getMessage());
				}
			}
			
			
			
			
			
			
			
		}
		catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		}
		
		
	
	/* old style, stored procedure !!! */	
//		try {
//			
//			statement = connection.prepareStatement(SQL_UPSERT_TAG_URL);
//			int occurrence = 1;			
//			statement.setInt(1, idTag);
//			statement.setInt(2, idUrl);
//			statement.setInt(3, occurrence);
//			ResultSet result;
//			result = statement.executeQuery();
//	
//			
//		}
//		catch (SQLException e) {
//			throw new PersistenceException(e.getMessage());
//		}
		
		
	}

	/* UPSERT */
	/* dalla documentazione: http://www.postgresql.org/docs/8.3/static/sql-update.html*/
	
	/* errore: deve accedere alla tabella tagvisitedurls, non visitedurltags!! */
	/*
	CREATE FUNCTION merge_tagvisitedurls(tag INT, url INT, val INT) RETURNS VOID AS
	$$
	BEGIN 
	LOOP
	UPDATE tagvisitedurls SET value = (value + val) WHERE idtag = tag AND idurl = url;
	IF found THEN 
	RETURN;
	END IF;
	BEGIN
	INSERT INTO tagvisitedurls(id, idtag, idurl, value) VALUES (DEFAULT, tag, url, val);
	RETURN;
	EXCEPTION WHEN unique_violation THEN
	END;
	END LOOP;
	END;
	$$
	LANGUAGE plpgsql; 
	 * */
	
	private static final String SQL_UPDATE_TAG_URL = 
		"UPDATE tagvisitedurls " +
		"SET value = value + 1 " +
		"WHERE id = ? AND idtag = ? AND idurl = ?;";
	
	private static final String SQL_INSERT_TAG_URL = 
		"INSERT INTO tagvisitedurls " +
		"VALUES (default, ?, ?, ?);";
	
	
	/* URL, TAG, VALUE */
	private final String SQL_UPSERT_TAG_URL = 
		"SELECT merge_tagvisitedurls(?, ?, ?);";


	private static final String SQL_SEARCH_TAG_URL = 
		"SELECT id, value " + 
		"FROM tagvisitedurls " +
		"WHERE idtag = ? " +
		"AND idurl = ?;";
	
	
	
	
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
