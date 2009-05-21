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
 * La tabella � tagvisitedurls, con tante righe del tipo 
 * (id idtag idurl value) 
 * dove value � il numero di volte che si � incontrata una annotazione con quel tag riferita a quell'url. 
 * � necessario che in ogni lista passata a save(lista) NON ESISTANO URL DUPLICATI. 
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
			/* mi restituisce il primo id con cui � apparso quell'url 
			 * l'url sar� sicuramente in quella tabella? 
			 * � gi� stato salvato? si, perch� sono visitedurls!
			 * � un problema avere due visitedurls diversi
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
		
		/* se il tag � gi� stato incontrato ed � 
		 * presente nel database, allora prendi l'id da l� */
		
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
			
			/* il tag � gi� presente nel database? */
			if (idTag > 0) {
				save(idUrl, idTag);	
			} else {
				/* SALVO il nuovo tag */
				TagDAOPostgres tagHandler = new TagDAOPostgres();
				RankedTag newTagToSave = new RankedTag(tag);
				Set<RankedTag> tags = new HashSet<RankedTag>();
				tags.add(newTagToSave);
				tagHandler.save(tags);
				
				/* CERCO di nuovo il tag nel database per ottenere l'id: 
				 * stavolta devo trovarlo */
				PreparedStatement statementID = connection.prepareStatement(SQL_RETRIEVE_TAG_ID);
				statementID.setString(1, tag);
				ResultSet resultID;
				resultID = statement.executeQuery();
				idTag = -1;
				if (resultID.next()) {
					idTag = result.getInt("id");
				}
				/* salvo url-tag sul db */
				save(idUrl, idTag);
				
			}
			
			
		}
		catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		}
	}
	

/* aggiunge UNA occorrenza per la coppia tag url 
 * significa che si � visitato un url con quel tag */
	private void save(Integer idUrl, Integer idTag) throws PersistenceException {
		DataSource dataSource = DataSource.getInstance();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = null;
		Logger logger = LogHandler.getLogger(this.getClass().getName());
//		logger.info("saving " + this.urlString + " " + this.tagString + 
//				" idurl: " + idUrl + " idtag: " + idTag);
		
		try {
			
			/* TODO: sostituire la stored procedure di postgres con delle query 
			 * semplici per aumentare la portabilit� */
			statement = connection.prepareStatement(SQL_UPSERT_TAG_URL);
			int occurrence = 1;
			/* TODO: trova un modo di salvare la stored procedure pgpsql una volta
			 * invece di riscriverla ogni volta. COME? */
			
			
			statement.setInt(1, idTag);
			statement.setInt(2, idUrl);
			statement.setInt(3, occurrence);
			ResultSet result;
			result = statement.executeQuery();
			
			/*
			if (result.next()) {
				System.out.println("idurl: " + result.getInt("idvisitedurl") + 
						" idtag:  " + result.getInt("idtag") + 
						" value: " + result.getInt("value"));
			}
			*/
			
		}
		catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		}
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
	
	
	/* URL, TAG, VALUE */
	private final String SQL_UPSERT_TAG_URL = 
		"SELECT merge_tagvisitedurls(?, ?, ?);";


	private final String SQL_RETRIEVE_VISITEDURL_ID = 
		"SELECT id " +
		"FROM visitedurls " +
		"WHERE url = ?;";
	
	
	private final String SQL_RETRIEVE_TAG_ID = 
		"SELECT id " +
		"FROM tags " +
		"WHERE tag = ?;";
		
	/* crea una tabella temporanea e poi la cancella quando c'� il commit. 
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
	
	/* inserisce nella tabella vera solamente le tuple per cui non esiste gi� un valore. 
	 * qui diventa pi� complesso, si dovrebbero sommare i valori */
	
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
