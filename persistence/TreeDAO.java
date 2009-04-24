package persistence;

import java.util.Map;

import cluster.Tree;

public interface TreeDAO {
	
	public void save(Tree clustering) throws PersistenceException;

}
