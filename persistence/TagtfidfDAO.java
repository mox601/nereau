package persistence;

import cluster.Tagtfidf;

public interface TagtfidfDAO {
	
	public Tagtfidf retrieveTag(String tag) throws PersistenceException;

}
