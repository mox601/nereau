package persistence.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import model.ExpandedQuery;
import model.Query;
import model.RankedTag;
import model.User;
import model.VisitedURL;
import persistence.PersistenceException;
import persistence.VisitedURLDAO;
import util.LogHandler;

public class VisitedURLDAOPostgres implements VisitedURLDAO {

	
	@Override
	public List<VisitedURL> retrieveLastVisitedURLs(User user) throws PersistenceException {
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		DataSource dataSource = DataSource.getInstance();
		Connection connection = null;
		PreparedStatement statement = null;
		List<VisitedURL> visitedURLs = new LinkedList<VisitedURL>();
		logger.info("creazione lista url visitati");
		try {
			connection = dataSource.getConnection();
			if(user.getUserID()<=0) {
				statement = connection.prepareStatement(SQL_RETRIEVE_LAST);
				statement.setString(1, user.getUsername());
			}
			else {
				statement = connection.prepareStatement(SQL_RETRIEVE_LAST_BY_USERID);
				statement.setInt(1, user.getUserID());
			}
			ResultSet result = statement.executeQuery();
			visitedURLs = extractVisitedUrlList(result);
		}
		catch (SQLException e) {
			logger.info(e.getMessage());
			throw new PersistenceException(e.getMessage());
		}
		finally {
			dataSource.close(statement);
			dataSource.close(connection);
		}
		return visitedURLs;
	}
	
	
	public List<VisitedURL> retrieveAllVisitedURLs(User user) throws PersistenceException {

		Logger logger = LogHandler.getLogger(this.getClass().getName());
		DataSource dataSource = DataSource.getInstance();
		Connection connection = null;
		PreparedStatement statement = null;
		List<VisitedURL> visitedURLs = new LinkedList<VisitedURL>();
		logger.info("creazione lista url visitati");
		try {
			connection = dataSource.getConnection();
			if(user.getUserID()<=0) {
				statement = connection.prepareStatement(SQL_RETRIEVE_ALL);
				statement.setString(1, user.getUsername());
			}
			else {
				statement = connection.prepareStatement(SQL_RETRIEVE_ALL_BY_USERID);
				statement.setInt(1, user.getUserID());
			}
			ResultSet result = statement.executeQuery();
			visitedURLs = extractVisitedUrlList(result);
		}
		catch (SQLException e) {
			logger.info(e.getMessage());
			throw new PersistenceException(e.getMessage());
		}
		finally {
			dataSource.close(statement);
			dataSource.close(connection);
		}
		return visitedURLs;
	}

	
	
	@Override
	public List<VisitedURL> retrieveVisitedURLs(User user, long fromDate)
			throws PersistenceException {

		Logger logger = LogHandler.getLogger(this.getClass().getName());
		DataSource dataSource = DataSource.getInstance();
		Connection connection = null;
		PreparedStatement statement = null;
		List<VisitedURL> visitedURLs = new LinkedList<VisitedURL>();
		logger.info("creazione lista url visitati");
		try {
			connection = dataSource.getConnection();
			if(user.getUserID()<=0) {
				statement = connection.prepareStatement(SQL_RETRIEVE_FROM_DATE);
				statement.setString(1, user.getUsername());
				statement.setLong(2, fromDate);
			}
			else {
				statement = connection.prepareStatement(SQL_RETRIEVE_FROM_DATE_BY_USERID);
				statement.setInt(1, user.getUserID());
				statement.setLong(2, fromDate);
			}
			ResultSet result = statement.executeQuery();
			visitedURLs = extractVisitedUrlList(result);
			
		}
		catch (SQLException e) {
			logger.info(e.getMessage());
			throw new PersistenceException(e.getMessage());
		}
		finally {
			dataSource.close(statement);
			dataSource.close(connection);
		}
		return visitedURLs;

	}

	@Override
	public List<VisitedURL> retrieveVisitedURLs(User user, long fromDate,
			long toDate) throws PersistenceException {

		Logger logger = LogHandler.getLogger(this.getClass().getName());
		DataSource dataSource = DataSource.getInstance();
		Connection connection = null;
		PreparedStatement statement = null;
		List<VisitedURL> visitedURLs = new LinkedList<VisitedURL>();
		logger.info("creazione lista url visitati");
		try {
			connection = dataSource.getConnection();
			if(user.getUserID()<=0) {
				statement = connection.prepareStatement(SQL_RETRIEVE_FROM_DATE_TO_DATE);
				statement.setString(1, user.getUsername());
				statement.setLong(2, fromDate);
				statement.setLong(3, toDate);
			}
			else {
				statement = connection.prepareStatement(SQL_RETRIEVE_FROM_DATE_TO_DATE_BY_USERID);
				statement.setInt(1, user.getUserID());
				statement.setLong(2, fromDate);
				statement.setLong(3, toDate);
			}
			ResultSet result = statement.executeQuery();
			visitedURLs = extractVisitedUrlList(result);
		}
		catch (SQLException e) {
			logger.info(e.getMessage());
			throw new PersistenceException(e.getMessage());
		}
		finally {
			dataSource.close(statement);
			dataSource.close(connection);
		}
		return visitedURLs;
		
	}

	public void saveVisitedURL(VisitedURL vUrl, User user) throws PersistenceException {

		DataSource dataSource = DataSource.getInstance();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = null;
		try {
			ResultSet result;
			int visitedUrlId = 0;
			String username = user.getUsername();
			String url = vUrl.getURL();
			String query = vUrl.getQuery().toString();
			long date = vUrl.getDate();
			String expandedQuery = null;
			Set<RankedTag> expansionTags = null;
			if(vUrl.getExpandedQuery()!=null) {
				expandedQuery = vUrl.getExpandedQuery().toString();
				expansionTags = vUrl.getExpandedQuery().getExpansionTags();
			}
			else
				expansionTags = new HashSet<RankedTag>();
			
			//save visited url
			if(user.getUserID()<=0) {
				statement = connection.prepareStatement(SQL_INSERT);
				statement.setString(1, username);
				statement.setString(2, url);
				statement.setString(3, query);
				if(expandedQuery!=null)
					statement.setString(4, expandedQuery);
				else
					statement.setString(4, "");
				statement.setLong(5, date);
			}
			else {
				statement = connection.prepareStatement(SQL_INSERT_BY_USERID);
				statement.setInt(1, user.getUserID());
				statement.setString(2, url);
				statement.setString(3, query);
				if(expandedQuery!=null)
					statement.setString(4, expandedQuery);
				else
					statement.setString(4, "");
				statement.setLong(5, date);
			}
			statement.executeUpdate();
			
			//retrieve visited url id
			if(user.getUserID()<=0) {
				statement = connection.prepareStatement(SQL_RETRIEVE_ID);
				statement.setString(1, username);
				statement.setString(2, url);
				statement.setString(3, query);
				statement.setLong(4, date);
			}
			else {
				statement = connection.prepareStatement(SQL_RETRIEVE_ID_BY_USERID);
				statement.setInt(1, user.getUserID());
				statement.setString(2, url);
				statement.setString(3, query);
				statement.setLong(4, date);
			}
			result = statement.executeQuery();
			if(result.next()) {
				visitedUrlId = result.getInt("id");
			}
			
			//save associated tags
			for(RankedTag rTag: expansionTags) {
				String tag = rTag.getTag();
				double rank = rTag.getRanking();
				statement = connection.prepareStatement(SQL_INSERT_VISITEDURLTAG);
				statement.setInt(1, visitedUrlId);
				statement.setString(2, tag);
				statement.setDouble(3, rank);
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
	
	private List<VisitedURL> extractVisitedUrlList(ResultSet result) throws SQLException {
		
		List<VisitedURL> resultList = new LinkedList<VisitedURL>();
		
		long date = 0;
		String urlString = "";
		String queryString = "";
		String expandedQueryString = "";
		Set<RankedTag> expansionTags = new HashSet<RankedTag>();
		while (result.next()) {
			long dateTemp = result.getLong("date");
			String urlStringTemp = result.getString("url");
			String queryStringTemp = result.getString("query");
			String expandedQueryStringTemp = result.getString("expandedquery");
			String tag = result.getString("tag");
			double rank = result.getDouble("value");
			
			//se sono valori nuovi
			if(dateTemp!=date || !urlStringTemp.equals(urlString) ||
					!queryStringTemp.equals(queryString) || 
					!expandedQueryStringTemp.equals(expandedQueryString)) {
				
				//se non siamo all'inizio aggiungi il nuovo url
				if(date!=0) {
					
					VisitedURL vUrl;
					boolean noExpansion = false;
					
					if(expandedQueryString==null)
						noExpansion = true;
					else if(expandedQueryString.isEmpty())
						noExpansion = true;
					
					if(noExpansion)
						vUrl = 
							new VisitedURL(urlString,new Query(queryString),null,date);
					else
						vUrl = 
							new VisitedURL(urlString,new Query(queryString),
									new ExpandedQuery(expandedQueryString,expansionTags),date);
					
					resultList.add(vUrl);
					
				}
				
				//modifica i vecchi valori con quelli del prossimo url
				date = dateTemp;
				urlString = urlStringTemp;
				queryString = queryStringTemp;
				expandedQueryString = expandedQueryStringTemp;
				expansionTags = new HashSet<RankedTag>();	
				
			}
			
			//se sono valori vecchi
			if(tag!=null)
				expansionTags.add(new RankedTag(tag,rank));
			
		}
		
		if(date!=0) {
			
			VisitedURL vUrl;
			boolean noExpansion = false;
			
			if(expandedQueryString==null)
				noExpansion = true;
			else if(expandedQueryString.isEmpty())
				noExpansion = true;
			
			if(noExpansion)
				vUrl = 
					new VisitedURL(urlString,new Query(queryString),null,date);
			else
				vUrl = 
					new VisitedURL(urlString,new Query(queryString),
							new ExpandedQuery(expandedQueryString,expansionTags),date);
			
			resultList.add(vUrl);
			
		}
		
		
		return resultList;
		
	}
	
	@Override
	public void deleteAllVisitedURLs(User user) throws PersistenceException {
		DataSource dataSource = DataSource.getInstance();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = null;
		try {
			String username = user.getUsername();
			if(user.getUserID()<=0) {
				statement = connection.prepareStatement(SQL_DELETE_ALL);
				statement.setString(1, username);
			}
			else {
				statement = connection.prepareStatement(SQL_DELETE_ALL_BY_USERID);
				statement.setInt(1, user.getUserID());
			}
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
	
	
	public void deleteVisitedURLs(List<VisitedURL> visitedURLs, User user) throws PersistenceException {
		DataSource dataSource = DataSource.getInstance();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = null;
		try {
			String username = user.getUsername();
			long maxDate = 0L;
			for(VisitedURL vUrl: visitedURLs) {
				long date = vUrl.getDate();
				if(date>maxDate)
					maxDate = date;
			}
			if(user.getUserID()<=0) {
				statement = connection.prepareStatement(SQL_DELETE);
				statement.setString(1, username);
				statement.setLong(2, maxDate);
			}
			else {
				statement = connection.prepareStatement(SQL_DELETE_BY_USERID);
				statement.setInt(1, user.getUserID());
				statement.setLong(2, maxDate);
			}
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
	
	private final String SQL_DELETE = 
		"DELETE FROM visitedurls " +
		"WHERE " +
		"	iduser = ( " +
		"		SELECT id " +
		"		FROM users " +
		"		WHERE username=? " +
		"	) " +
		"	AND date<=? ;";
	
	private final String SQL_DELETE_BY_USERID = 
		"DELETE FROM visitedurls " +
		"WHERE " +
		"	iduser=? " +
		"	AND date<=? ;";
	
	private final String SQL_DELETE_ALL = 
		"DELETE FROM visitedurls " +
		"WHERE " +
		"	iduser = ( " +
		"		SELECT id " +
		"		FROM users " +
		"		WHERE username=? " +
		"	); ";
	
	private final String SQL_DELETE_ALL_BY_USERID = 
		"DELETE FROM visitedurls " +
		"WHERE iduser=?; ";
	
	private final String SQL_INSERT = 
		"INSERT INTO visitedurls (iduser,url,query,expandedquery,date) " +
		"VALUES ( " +
		"	( " +
		"		SELECT id " +
		"		FROM users " +
		"		WHERE username=? " +
		"	), " +
		"	?, " +
		"	?, " +
		"	?, " +
		"	? " +
		") ";
	
	private final String SQL_INSERT_BY_USERID = 
		"INSERT INTO visitedurls (iduser,url,query,expandedquery,date) " +
		"VALUES ( ?, ?, ?, ?, ? ) ";
	
	private final String SQL_INSERT_VISITEDURLTAG =
		"INSERT INTO visitedurltags (idvisitedurl,idtag,value) " +
		"VALUES ( " +
		"	?, " +
		"	( " +
		"		SELECT id " +
		"		FROM tags " +
		"		WHERE tag=? " +
		"	), " +
		"	? " +
		") ";
	
	private final String SQL_RETRIEVE_ID = 
		"SELECT visitedurls.id AS id " +
		"FROM " +
		"	visitedurls " +
		"	JOIN users ON visitedurls.iduser=users.id " +
		"WHERE " +
		"	users.username=? AND url=? AND query=? AND date=? ";
	
	private final String SQL_RETRIEVE_ID_BY_USERID = 
		"SELECT visitedurls.id AS id " +
		"FROM visitedurls " +
		"WHERE iduser=? AND url=? AND query=? AND date=? ";
	
	private final String SQL_RETRIEVE_ALL = 
		"SELECT " +
		"	url, " +
		"	query, " +
		"	expandedquery, " +
		"	date, " +
		"	tag, " +
		"	value " +
		"FROM " +
		"	visitedurls " +
		"	JOIN users ON visitedurls.iduser=users.id " +
		"	LEFT JOIN visitedurltags ON visitedurls.id=visitedurltags.idvisitedurl " +
		"	LEFT JOIN tags ON visitedurltags.idtag=tags.id " +
		"WHERE " +
		"	users.username=? " +
		"ORDER BY date, url, query, expandedquery, tag ";
	
	private final String SQL_RETRIEVE_ALL_BY_USERID = 
		"SELECT " +
		"	url, " +
		"	query, " +
		"	expandedquery, " +
		"	date, " +
		"	tag, " +
		"	value " +
		"FROM " +
		"	visitedurls " +
		"	LEFT JOIN visitedurltags ON visitedurls.id=visitedurltags.idvisitedurl " +
		"	LEFT JOIN tags ON visitedurltags.idtag=tags.id " +
		"WHERE iduser=? " +
		"ORDER BY date, url, query, expandedquery, tag ";
	
	private final String SQL_RETRIEVE_LAST = 
		"SELECT " +
		"	url, " +
		"	query, " +
		"	expandedquery, " +
		"	visitedurls.date AS date, " +
		"	tag, " +
		"	value " +
		"FROM " +
		"	visitedurls " +
		"	JOIN users ON visitedurls.iduser=users.id " +
		"	LEFT JOIN visitedurltags ON visitedurls.id=visitedurltags.idvisitedurl " +
		"	LEFT JOIN tags ON visitedurltags.idtag=tags.id " +
		"WHERE " +
		"	users.username=? AND " +
		"	visitedurls.date>users.lastupdate " +
		"ORDER BY date, url, query, expandedquery, tag ";
	
	private final String SQL_RETRIEVE_LAST_BY_USERID = 
		"SELECT " +
		"	url, " +
		"	query, " +
		"	expandedquery, " +
		"	visitedurls.date AS date, " +
		"	tag, " +
		"	value " +
		"FROM " +
		"	visitedurls " +
		"	JOIN users ON visitedurls.iduser=users.id " +
		"	LEFT JOIN visitedurltags ON visitedurls.id=visitedurltags.idvisitedurl " +
		"	LEFT JOIN tags ON visitedurltags.idtag=tags.id " +
		"WHERE " +
		"	iduser=? AND " +
		"	visitedurls.date>users.lastupdate " +
		"ORDER BY date, url, query, expandedquery, tag ";
	
	private final String SQL_RETRIEVE_FROM_DATE = 
		"SELECT " +
		"	url, " +
		"	query, " +
		"	expandedquery, " +
		"	date, " +
		"	tag, " +
		"	value " +
		"FROM " +
		"	visitedurls " +
		"	JOIN users ON visitedurls.iduser=users.id " +
		"	LEFT JOIN visitedurltags ON visitedurls.id=visitedurltags.idvisitedurl " +
		"	LEFT JOIN tags ON visitedurltags.idtag=tags.id " +
		"WHERE " +
		"	users.username=? AND visitedurls.date>? " +
		"ORDER BY date, url, query, expandedquery, tag ";
	
	private final String SQL_RETRIEVE_FROM_DATE_BY_USERID = 
		"SELECT " +
		"	url, " +
		"	query, " +
		"	expandedquery, " +
		"	date, " +
		"	tag, " +
		"	value " +
		"FROM " +
		"	visitedurls " +
		"	JOIN users ON visitedurls.iduser=users.id " +
		"	LEFT JOIN visitedurltags ON visitedurls.id=visitedurltags.idvisitedurl " +
		"	LEFT JOIN tags ON visitedurltags.idtag=tags.id " +
		"WHERE " +
		"	iduser=? AND visitedurls.date>? " +
		"ORDER BY date, url, query, expandedquery, tag ";
	
	private final String SQL_RETRIEVE_FROM_DATE_TO_DATE = 
		"SELECT " +
		"	url, " +
		"	query, " +
		"	expandedquery, " +
		"	date, " +
		"	tag, " +
		"	value " +
		"FROM " +
		"	visitedurls " +
		"	JOIN users ON visitedurls.iduser=users.id " +
		"	LEFT JOIN visitedurltags ON visitedurls.id=visitedurltags.idvisitedurl " +
		"	LEFT JOIN tags ON visitedurltags.idtag=tags.id " +
		"WHERE " +
		"	users.username=? AND visitedurls.date>? AND visitedurls.date<? " +
		"ORDER BY date, url, query, expandedquery, tag ";
	
	private final String SQL_RETRIEVE_FROM_DATE_TO_DATE_BY_USERID = 
		"SELECT " +
		"	url, " +
		"	query, " +
		"	expandedquery, " +
		"	date, " +
		"	tag, " +
		"	value " +
		"FROM " +
		"	visitedurls " +
		"	JOIN users ON visitedurls.iduser=users.id " +
		"	LEFT JOIN visitedurltags ON visitedurls.id=visitedurltags.idvisitedurl " +
		"	LEFT JOIN tags ON visitedurltags.idtag=tags.id " +
		"WHERE " +
		"	iduser=? AND visitedurls.date>? AND visitedurls.date<? " +
		"ORDER BY date, url, query, expandedquery, tag ";






}
