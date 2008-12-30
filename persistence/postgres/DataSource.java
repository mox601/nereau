package persistence.postgres;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.Scanner;
import java.util.logging.Logger;
import org.postgresql.Driver;


import persistence.PersistenceException;
import util.ParameterHandler;

public class DataSource {
	private static DataSource singleton;
	
	private String dbURI;
	private String userName;
	private String password;
	
	private DataSource() {
		try {
			Scanner scanner = new Scanner(new File(ParameterHandler.DATABASE_CONFIG));
			dbURI = scanner.nextLine();
			userName = scanner.nextLine();
			password = scanner.nextLine();
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized DataSource getInstance() {
        if (singleton == null) {
            singleton = new DataSource();
        }
        return singleton;
    }
	
	public Connection getConnection() throws PersistenceException {
		Logger logger = Logger.getLogger(this.getClass().getName());
		Connection connection = null;
		try {
		    //Class.forName("org.postgresql.Driver");
			Driver d = new Driver();
			DriverManager.registerDriver(d);
		    connection = DriverManager.getConnection(dbURI,userName,password);
		} catch(SQLException sqle) {
			logger.info("Eccezione: SQLException");
			throw new PersistenceException(sqle.getMessage());
		}
		return connection;
	}
	
    public void close(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException sqle) {
            System.err.println(sqle);
        }
    }

    public void close(Statement statement) {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException sqle) {
            System.err.println(sqle);
        }
    }

    public void close(ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (SQLException sqle) {
            System.err.println(sqle);
        }
    }
	
}
