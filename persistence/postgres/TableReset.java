package persistence.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import persistence.PersistenceException;

public class TableReset {
	
	public static void main(String[] args) throws PersistenceException {
		resetDB();
	}
	
	public static void resetDB() throws PersistenceException {
		DataSource dataSource = DataSource.getInstance();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = null;
		try {

			statement = connection.prepareStatement(SQL_DELETE);
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
	
	private static final String SQL_DELETE = 
		"DELETE FROM cooccurrences; " +
		"DELETE FROM classes; " +
		"DELETE FROM tags; " +
		"DELETE FROM terms; " +
		"DELETE FROM visitedurls; " +
		"DELETE FROM testqueries; " +
		"DELETE FROM users; " +
		"DELETE FROM stemmedterms; ";

}
