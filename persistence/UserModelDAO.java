package persistence;

import java.util.Map;
import java.util.Set;

import model.RankedTag;
import model.User;

public interface UserModelDAO {

	public Map<String, Map<RankedTag, Map<String, Double>>> 
		retrieveSubMatrix(Set<String> terms, User user) throws PersistenceException;
	public void save(Map<String, Map<RankedTag, Map<String, Double>>> subMatrix, 
		Map<String, Map<RankedTag, Map<String, Double>>> newMatrix, User user) throws PersistenceException;

}
