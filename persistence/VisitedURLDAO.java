package persistence;

import java.util.List;

import model.User;
import model.VisitedURL;

public interface VisitedURLDAO {

	public List<VisitedURL> retrieveAllVisitedURLs(User user) throws PersistenceException;
	public List<VisitedURL> retrieveLastVisitedURLs(User user) throws PersistenceException;
	public List<VisitedURL> retrieveVisitedURLs(User user, long fromDate) throws PersistenceException;
	public List<VisitedURL> retrieveVisitedURLs(User user, long fromDate, long toDate) throws PersistenceException;
	public void saveVisitedURL(VisitedURL vUrl, User user) throws PersistenceException;
	public void deleteVisitedURLs(List<VisitedURL> visitedURLs, User user) throws PersistenceException;
	public void deleteAllVisitedURLs(User user) throws PersistenceException;
	
}
