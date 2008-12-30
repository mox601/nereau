package persistence.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import persistence.PersistenceException;

public class TableCreator {
	
	public static void main(String[] args) throws PersistenceException {
		initDB();
	}
/*
	private static final String SQL_CREATE_USERS =
		"CREATE TABLE users " +
		"	(" +
		"		id serial NOT NULL, " +
		"		username text NOT NULL UNIQUE, " +
		"		password text NOT NULL, " +
		"		PRIMARY KEY (id) " +
		"	)";
	
	private static final String SQL_CREATE_STEMMEDTERMS = 
		"CREATE TABLE stemmedterms" +
		"	(" +
		"		id serial NOT NULL, " +
		"		stemmedterm text NOT NULL UNIQUE, " +
		"		PRIMARY KEY (id) " +
		"	)";
	*/
	
	private static final String SQL_CREATE_TESTQUERIES = 
		"CREATE TABLE testqueries " +
		"	(	" +
		"		id serial NOT NULL,	" +
		"		iduser integer NOT NULL, " +
		"		testquery text NOT NULL, " +
		"		PRIMARY KEY (id),	" +
		"		FOREIGN KEY(iduser) " +
		"			REFERENCES users(id) " +
		"	)";
	/*
	private static final String SQL_CREATE_TERMS = 
		"CREATE TABLE terms" +
		"	(" +
		"		id serial NOT NULL, " +
		"		idstemmedterm integer NOT NULL, " +
		"		term text NOT NULL, " +
		"		relevance integer DEFAULT 0, " +
		"		PRIMARY KEY (id), " +
		"		UNIQUE(idstemmedterm,term), " +
		"		FOREIGN KEY(idstemmedterm) " +
		"			REFERENCES stemmedterms(id) " +
		"	)";

	private static final String SQL_CREATE_TAGS = 
		"CREATE TABLE tags " +
		"	(	" +
		"		id serial NOT NULL,	" +
		"		tag text NOT NULL UNIQUE,	" +
		"		PRIMARY KEY (id)	" +
		"	)";
		
	private static final String SQL_CREATE_VISITEDURLS = 
		"CREATE TABLE visitedurls" +
		"	(" +
		"		id serial NOT NULL, " +
		"		iduser integer NOT NULL, " +
		"		url text NOT NULL, " +
		"		query text NOT NULL, " +
		"		date bigint NOT NULL, " +
		"		PRIMARY KEY (id), " +
		"		FOREIGN KEY(iduser) " +
		"			REFERENCES users(id)" +
		"	)";
	
	private static final String SQL_CREATE_CLASSES = 
		"CREATE TABLE classes	" +
		"	(	" +
		"		id serial NOT NULL,	" +
		"		idterm integer NOT NULL,	" +
		"		idtag integer NOT NULL,	" +
		"		iduser integer NOT NULL,	" +
		"		value real NOT NULL," +
		"		PRIMARY KEY (id),	" +
		"		UNIQUE(idterm,idtag,iduser)," +
		"		FOREIGN KEY(idterm) " +
		"			REFERENCES stemmedterms(id),	" +
		"		FOREIGN KEY(idtag) " +
		"			REFERENCES tags(id), " +
		"		FOREIGN KEY(iduser) " +
		"			REFERENCES users(id)" +
		"	)";
	
	private static final String SQL_CREATE_COOCCURRENCES =
		"CREATE TABLE cooccurrences " +
		"	(	" +
		"		id serial NOT NULL, " +
		"		idclass integer NOT NULL, " +
		"		idterm integer NOT NULL, " +
		"		value real NOT NULL, " +
		"		PRIMARY KEY (id), " +
		"		UNIQUE(idclass,idterm), " +
		"		FOREIGN KEY(idclass) " +
		"			REFERENCES classes(id), " +
		"		FOREIGN KEY(idterm) " +
		"			REFERENCES stemmedterms(id)	" +
		")";
	*/
	public static void initDB() throws PersistenceException {
		DataSource dataSource = DataSource.getInstance();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = null;
		try {

			statement = connection.prepareStatement(SQL_CREATE_TESTQUERIES);
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

}
