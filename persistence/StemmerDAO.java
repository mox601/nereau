package persistence;

import java.util.Map;
import java.util.Set;

public interface StemmerDAO {

	public void save(Map<String, Map<String,Integer>> stemmedword2words)
		throws PersistenceException;
	public Map<String,Map<String,Integer>> retrieveTerms(Set<String> stemmedTerms) 
		throws PersistenceException;

}
