package persistence;

import java.util.List;

import persistence.postgres.TagDAOPostgres;
import cluster.Tagtfidf;

public interface TagtfidfDAO {

	public Tagtfidf retrieveTag(String tag) throws PersistenceException;
	public List<Tagtfidf> retrieveAllTags() throws PersistenceException;

}
