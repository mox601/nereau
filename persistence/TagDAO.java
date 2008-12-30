package persistence;

import java.util.Set;

import model.RankedTag;

public interface TagDAO {

	void save(Set<RankedTag> tags) throws PersistenceException;

}
