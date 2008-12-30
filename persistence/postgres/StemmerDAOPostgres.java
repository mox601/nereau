package persistence.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;


import persistence.PersistenceException;
import persistence.StemmerDAO;
import util.LogHandler;

public class StemmerDAOPostgres implements StemmerDAO {
	

	public Map<String, Map<String, Integer>> retrieveTerms(Set<String> stemmedTerms) throws PersistenceException {
		DataSource dataSource = DataSource.getInstance();
		Connection connection = null;
		PreparedStatement statement = null;
		Map<String, Map<String, Integer>> stemmedterm2terms = null;
		try {
			connection = dataSource.getConnection();
			String query = this.prepareStatementForRetrieve(stemmedTerms);
			statement = connection.prepareStatement(query);
			stemmedterm2terms = new HashMap<String, Map<String, Integer>> ();
			ResultSet result = statement.executeQuery();
			while(result.next()) {
				String stemmedterm = result.getString("stemmedterm");
				String term = result.getString("term");
				int relevance = result.getInt("relevance");
				
				if(!stemmedterm2terms.containsKey(stemmedterm))
					stemmedterm2terms.put(stemmedterm, new HashMap<String, Integer>());
				Map<String, Integer> values4stemmedterm =
					stemmedterm2terms.get(stemmedterm);
				values4stemmedterm.put(term,relevance);
			}
		}
		catch (PersistenceException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		finally {
			dataSource.close(statement);
			dataSource.close(connection);
		}
		return stemmedterm2terms;
	}

	private String prepareStatementForRetrieve(Set<String> stemmedTerms) {
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		logger.info("accesso a termini a partire da termini stemmati: " + stemmedTerms);
		StringBuffer query = new StringBuffer(SQL_RETRIEVE);
		boolean firstOR = true;
		for(String stemmedTerm: stemmedTerms) {
			if(firstOR) {
				query.append("stemmedterm=\'" + stemmedTerm + "\' ");
				firstOR = false;
			}
			else {
				query.append(" OR stemmedterm=\'" + stemmedTerm + "\' ");
			}
		}
		query.append("); \n");
		logger.info("query: " + query);
		
		return query.toString();
	}

	public void save(Map<String, Map<String,Integer>> stemmedword2words) throws PersistenceException {
		DataSource dataSource = DataSource.getInstance();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = null;
		try {
			String query = this.prepareStatementForSave(stemmedword2words);
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
	
	private String prepareStatementForSave(Map<String, Map<String, Integer>> stemmedword2words) {
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		logger.info("salvataggio termini (stemmati e non) contenuti in: " + stemmedword2words);
		StringBuffer query = new StringBuffer();
		//stemmedterms
		query.append(SQL_INSERT_STEMMEDTERMS_1);
		for(String stemmedterm: stemmedword2words.keySet()) {
			String insertStemmedTermTemp =
				"INSERT INTO stemmedterms_temp (stemmedterm) \n" +
				"VALUES (\'" + stemmedterm + "\'); \n";
			query.append(insertStemmedTermTemp);
		}
		query.append(SQL_INSERT_STEMMEDTERMS_2);
		//terms
		query.append(SQL_INSERT_TERMS_1);
		for(String stemmedterm: stemmedword2words.keySet()) {
			Map<String,Integer> terms2relevance = 
				stemmedword2words.get(stemmedterm);
			for(String term: terms2relevance.keySet()) {
				int relevance = terms2relevance.get(term);
				String insertTermTemp = 
					"INSERT INTO terms_temp (idstemmedterm,term,relevance) \n" +
					"VALUES ( \n" +
					"	( \n" +
					"		SELECT id \n" +
					"		FROM stemmedterms \n" +
					"		WHERE stemmedterm=\'" + stemmedterm + "\' \n" +
					"	), \n" +
					"	\'" + term + "\', \n" +
					"	" + relevance + " \n" +
					"); \n";
				query.append(insertTermTemp);
			}
		}
		query.append(SQL_INSERT_TERMS_2);
		logger.info("query: \n" + query);
		return query.toString();
	}
	
	private final String SQL_RETRIEVE =
		"SELECT stemmedterm, term, relevance \n" +
		"FROM \n" +
		"	stemmedterms \n" +
		"	JOIN terms ON stemmedterms.id=terms.idstemmedterm \n" +
		"WHERE ( \n";

	private final String SQL_INSERT_STEMMEDTERMS_1 =
		"START TRANSACTION; \n" +
		"CREATE TEMP TABLE stemmedterms_temp \n" +
		"( \n" +
		"	id serial NOT NULL, \n" +
		"	stemmedterm text NOT NULL UNIQUE, \n" +
		"	PRIMARY KEY (id) \n" +
		") \n" +
		"ON COMMIT DROP; \n";
	
	private final String SQL_INSERT_STEMMEDTERMS_2 = 
		"INSERT INTO stemmedterms (stemmedterm) \n" +
		"SELECT stemmedterm \n" +
		"FROM stemmedterms_temp \n" +
		"WHERE NOT EXISTS ( \n" +
		"	SELECT stemmedterm \n" +
		"	FROM stemmedterms \n" +
		"	WHERE stemmedterms.stemmedterm = stemmedterms_temp.stemmedterm \n" +
		"); \n" +
		"COMMIT TRANSACTION; \n";
	
	private final String SQL_INSERT_TERMS_1 =
		"START TRANSACTION; \n" +
		"CREATE TEMP TABLE terms_temp \n" +
		"( \n" +
		"	id serial NOT NULL, \n" +
		"	idstemmedterm integer NOT NULL, \n" +
		"	term text NOT NULL, \n" +
		"	relevance integer NOT NULL, \n" +
		"	PRIMARY KEY (id), \n" +
		"	UNIQUE(idstemmedterm,term) \n" +
		") \n" +
		"ON COMMIT DROP; \n";
	
	private final String SQL_INSERT_TERMS_2 =
		"INSERT INTO terms (idstemmedterm,term) \n" +
		"SELECT idstemmedterm,term \n" +
		"FROM terms_temp \n" +
		"WHERE NOT EXISTS ( \n" +
		"	SELECT term \n" +
		"	FROM terms \n" +
		"	WHERE terms.term = terms_temp.term \n" +
		"); \n" +
		"UPDATE terms \n" +
		"SET relevance = relevance + ( \n" +
		"	SELECT relevance \n" +
		"	FROM terms_temp \n" +
		"	WHERE terms.term = terms_temp.term \n" +
		"); \n" +
		"COMMIT TRANSACTION; \n";


}
