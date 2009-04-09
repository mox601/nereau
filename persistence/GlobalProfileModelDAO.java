package persistence;

//import java.util.Set;
import java.util.LinkedList;
import java.util.List;

import cluster.Tagtfidf;

import model.URLTags;
import model.User;

public interface GlobalProfileModelDAO {
	
	public List<Tagtfidf> retrieveTags() throws PersistenceException;
	// lo devo trasferire nel TagtfidfDAO
	public boolean updateTagtfidf(Tagtfidf tag) throws PersistenceException;
	
	public boolean updateGlobalProfile(LinkedList<URLTags> linkedList) throws PersistenceException;

}
