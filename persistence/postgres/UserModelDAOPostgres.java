package persistence.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import model.RankedTag;
import model.User;
import persistence.PersistenceException;
import persistence.UserModelDAO;
import util.LogHandler;

public class UserModelDAOPostgres implements UserModelDAO {

	public Map<String, Map<RankedTag, Map<String, Double>>> retrieveSubMatrix(Set<String> terms, User user) 
		throws PersistenceException {
		
		if(terms==null)
			return new HashMap<String, Map<RankedTag, Map<String, Double>>> ();
		if(terms.isEmpty())
			return new HashMap<String, Map<RankedTag, Map<String, Double>>> ();
		DataSource dataSource = DataSource.getInstance();
		Connection connection = null;
		PreparedStatement statement = null;
		Map<String, Map<RankedTag, Map<String, Double>>> subMatrix = null;
		try {
			connection = dataSource.getConnection();
			//TODO questa cosa va alleggerita...!!
			String query = this.prepareStatementForRetrieve(terms,user);
			statement = connection.prepareStatement(query);
			subMatrix = new HashMap<String, Map<RankedTag, Map<String, Double>>> ();
			ResultSet result = statement.executeQuery();
			while(result.next()) {
				String term1 = result.getString("term1");
				String tag = result.getString("tag");
				double tagranking = result.getDouble("tagranking");
				String term2 = result.getString("term2");
				double value = result.getDouble("value");
				
				if(!subMatrix.containsKey(term1))
					subMatrix.put(term1, new HashMap<RankedTag, Map<String, Double>>());
				Map<RankedTag, Map<String,Double>> values4term =
					subMatrix.get(term1);
				RankedTag rtag = new RankedTag(tag,tagranking);
				if(!values4term.containsKey(rtag))
					values4term.put(rtag,new HashMap<String, Double>());
				Map<String, Double> values4term4tag = 
					values4term.get(rtag);
				
				values4term4tag.put(term2, value);
			}
		} catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		}
		finally {
			dataSource.close(statement);
			dataSource.close(connection);
		}
		return subMatrix;
	}

	public void save(Map<String, Map<RankedTag, Map<String, Double>>> newInsertMatrix, 
			Map<String, Map<RankedTag, Map<String, Double>>> newUpdateMatrix, User user) throws PersistenceException {
		DataSource dataSource = DataSource.getInstance();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = null;
		Set<String> terms = new HashSet<String>();
		terms.addAll(newInsertMatrix.keySet());
		terms.addAll(newUpdateMatrix.keySet());
		try {
			for(String term: terms) {
				Map<RankedTag,Map<String,Double>> newInsertValues4term =
					newInsertMatrix.get(term);
				Map<RankedTag,Map<String,Double>> newUpdateValues4term =
					newUpdateMatrix.get(term);
				String query = this.prepareStatementForSave(term,newInsertValues4term,newUpdateValues4term,user);
				statement = connection.prepareStatement(query);
				statement.executeUpdate();
			}
		}
		catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		}
		finally {
			dataSource.close(statement);
			dataSource.close(connection);
		}
	}
	
	private String prepareStatementForSave(String term, Map<RankedTag, Map<String, Double>> newInsertValues4term, 
			Map<RankedTag, Map<String, Double>> newUpdateValues4term, User user) {
		
		String userID = "";
		if(user.getUserID()<=0)
			userID = "( \n" +
					"		SELECT id \n" +
					"		FROM users \n" +
					"		WHERE username=\'" + user.getUsername() + "\' \n" +
					"	)";
		else
			userID = "" + user.getUserID() + "";
		
		
		
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		logger.info("query di modifica al database:");
		//StringBuffer query = new StringBuffer(SQL_TRANSACTION);
		StringBuffer query = new StringBuffer();

		logger.info("nuovi valori da inserire: \n" + newInsertValues4term + 
				"\nvalori da aggiornare: \n" + newUpdateValues4term);
		
		Set<RankedTag> updateTags = new HashSet<RankedTag>();
		Set<RankedTag> insertTags = new HashSet<RankedTag>();
		if(newUpdateValues4term!=null)
			updateTags.addAll(newUpdateValues4term.keySet());
		if(newInsertValues4term!=null)
			insertTags.addAll(newInsertValues4term.keySet());
		if(newUpdateValues4term!=null)
			insertTags.removeAll(newUpdateValues4term.keySet());
		logger.info("nuovi tag da inserire: \n" + insertTags + 
				"\ntag da aggiornare: \n" + updateTags);

		//update classes
		for(RankedTag updateTag: updateTags) {
			String tag = updateTag.getTag();
			double ranking = updateTag.getRanking();
			String updateClasses = 
				"UPDATE classes \n" +
				"SET value=" + ranking + " \n" +
				"WHERE \n" +
				"	idterm = ( \n" +
				"		SELECT id \n" +
				"		FROM stemmedterms \n" +
				"		WHERE stemmedterm=\'" + term + "\' \n" +
				"	) \n" +
				"	AND idtag = ( \n" +
				"		SELECT id \n" +
				"		FROM tags \n" +
				"		WHERE tag=\'" + tag + "\' \n" +
				"	) \n" +
				"	AND iduser = " + userID + "; \n";
			query.append(updateClasses);
			logger.info("update classes:\n" + updateClasses);
		}

		//insert classes
		for(RankedTag insertTag: insertTags) {
			String tag = insertTag.getTag();
			double ranking = insertTag.getRanking();
			String insertClasses = 
				"INSERT INTO classes (idterm,idtag,iduser,value) \n" +
				"VALUES (\n" +
				"	(\n" +
				"		SELECT id \n" +
				"		FROM stemmedterms \n" +
				"		WHERE stemmedterm=\'" + term + "\' \n" +
				"	), \n" +
				"	(\n" +
				"		SELECT id \n" +
				"		FROM tags \n" +
				"		WHERE tag=\'" + tag + "\' \n" +
				"	), \n" +
				"	" + userID + ", \n" +
				"	" + ranking + " \n" +
				"); \n";
			query.append(insertClasses);
			logger.info("insert classes:\n" + insertClasses);
		}
	
		//update cooccurrences
		if(newUpdateValues4term!=null && !newUpdateValues4term.isEmpty()) {
			for(RankedTag updateTag: newUpdateValues4term.keySet()) {
				String tag = updateTag.getTag();
				Map<String,Double> newUpdateValues =
					newUpdateValues4term.get(updateTag);
				for(String term2: newUpdateValues.keySet()) {
					double value = newUpdateValues.get(term2);
					String updateCooccurrences = 
						"UPDATE cooccurrences \n" +
						"SET value=" + value + " \n" +
						"WHERE \n" +
						"	idclass = (\n" +
						"		SELECT id \n" +
						"		FROM classes \n" +
						"		WHERE \n" +
						"			idterm = ( \n" +
						"				SELECT id \n" +
						"				FROM stemmedterms \n" +
						"				WHERE stemmedterm=\'" + term + "\' \n" +
						"			) \n" +
						"			AND idtag = ( \n" +
						"				SELECT id \n" +
						"				FROM tags \n" +
						"				WHERE tag=\'" + tag + "\' \n" +
						"			) \n" +
						"			AND iduser = " + userID + " \n" +
						"	) \n" +
						"	AND idterm = (\n" +
						"		SELECT id \n" +
						"		FROM stemmedterms \n" +
						"		WHERE stemmedterm=\'" + term2 + "\' \n" +
						"	); \n";
					query.append(updateCooccurrences);
					logger.info("update cooccurrences:\n" + updateCooccurrences);
				}
			}
		}
	
		//insert cooccurrences
		if(newInsertValues4term!=null && !newInsertValues4term.isEmpty()) {
			logger.info("inserimento cooccorrenze: valori = \n" + newInsertValues4term +
					", \nkeyset di tags = \n" + newInsertValues4term.keySet().getClass());
			for(RankedTag insertTag: newInsertValues4term.keySet()) {
				logger.info("tag preso in considerazione = " 
						+ insertTag + "\nvalori: " + newInsertValues4term.get(insertTag)
						/*+ "\ntipo struttura: " + newInsertValues4term.get(insertTag).getClass()*/);
				Map<String,Double> newInsertValues =
					newInsertValues4term.get(insertTag);
				logger.info("valori per il tag = " 
						+ newInsertValues);
				logger.info("valori per il tag = " 
						+ newInsertValues4term.get(insertTag));
				
				if(newInsertValues!=null) {
					logger.info("keyset associato = "
							+ newInsertValues4term.get(insertTag).keySet());
					String tag = insertTag.getTag();
					for(String term2: newInsertValues.keySet()) {
						double value = newInsertValues.get(term2);
						String insertCooccurrences = 
							"INSERT INTO cooccurrences (idclass,idterm,value) \n" +
							"VALUES ( \n" +
							"	(\n" +
							"		SELECT id \n" +
							"		FROM classes \n" +
							"		WHERE \n" +
							"			idterm = ( \n" +
							"				SELECT id \n" +
							"				FROM stemmedterms \n" +
							"				WHERE stemmedterm=\'" + term + "\' \n" +
							"			) \n" +
							"			AND idtag = ( \n" +
							"				SELECT id \n" +
							"				FROM tags \n" +
							"				WHERE tag=\'" + tag + "\' \n" +
							"			) \n" +
							"			AND iduser = " + userID + " \n" +
							"	), \n" +
							"	(\n" +
							"		SELECT id \n" +
							"		FROM stemmedterms \n" +
							"		WHERE stemmedterm=\'" + term2 + "\' \n" +
							"	),\n" +
							"	" + value + " \n" +
							"); \n";
						query.append(insertCooccurrences);
						logger.info("insert cooccurrences:\n" + insertCooccurrences);
					}
				}
				
			}
		}

		//query.append("COMMIT TRANSACTION; ");
		return query.toString();
	}

	private String prepareStatementForRetrieve(Set<String> terms, User user) throws SQLException {
		StringBuffer query = new StringBuffer(SQL_RETRIEVE);
		if(user.getUserID()<=0)
			query.append("WHERE users.username=\'" + user.getUsername() + "\' AND (");
		else
			query.append("WHERE classes.iduser=" + user.getUserID() + " AND (");
		boolean firstOR = true;
		for(String term: terms) {
			term.trim();
			if(firstOR) {
				query.append("terms1.stemmedterm=\'" + term + "\' ");
				firstOR = false;
			}
			else {
				query.append(" OR terms1.stemmedterm=\'" + term + "\' ");
			}
		}
		query.append(");");
		return query.toString();
	}
	
	//private final String SQL_TRANSACTION = "BEGIN TRANSACTION; ";
	
	private final String SQL_RETRIEVE = 
		"SELECT	" +
		"	terms1.stemmedterm AS term1, " +
		"	tags.tag AS tag, " +
		"	classes.value AS tagranking, " +
		"	terms2.stemmedterm AS term2, " +
		"	cooccurrences.value AS value " +
		"FROM " +
		"	stemmedterms AS terms1 " +
		"	JOIN classes ON terms1.id=classes.idterm " +
		"	JOIN tags ON classes.idtag=tags.id " +
		"	JOIN users ON classes.iduser=users.id " +
		"	JOIN cooccurrences ON cooccurrences.idclass=classes.id " +
		"	JOIN stemmedterms AS terms2 ON cooccurrences.idterm=terms2.id ";



}
