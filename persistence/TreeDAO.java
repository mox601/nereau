package persistence;

import java.util.List;
import java.util.Map;

import model.RankedTag;

import cluster.Tree;

public interface TreeDAO {
	
	public void save(Tree clustering) throws PersistenceException;
	public Tree retrieve(List<RankedTag> tags) throws PersistenceException;

}
