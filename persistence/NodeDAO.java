package persistence;

import cluster.Node;

public interface NodeDAO {

		
	public void save(Node node) throws PersistenceException;

}
