package persistence;

import java.util.LinkedList;
import java.util.Set;

import model.RankedTag;
import model.URLTags;

public interface URLTagsDAO {
	
	void save(LinkedList<URLTags> urls) throws PersistenceException;


}
