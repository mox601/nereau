package model;

import java.util.Set;
import model.VisitedURL;


public class URLTags {
	
	private String urlString;
	private Set<RankedTag> tags;

	/**
	 * @return the urlString
	 */
	public String getUrlString() {
		return urlString;
	}

	/**
	 * @param urlString the urlString to set
	 */
	public void setUrlString(String urlString) {
		this.urlString = urlString;
	}

	/**
	 * @return the tags
	 */
	public Set<RankedTag> getTags() {
		return tags;
	}

	/**
	 * @param tags the tags to set
	 */
	public void setTags(Set<RankedTag> tags) {
		this.tags = tags;
	}


	public URLTags(VisitedURL vu, Set<RankedTag> tags) {
		this.urlString = vu.getURL();
		this.tags = tags;
	}
	/* questi sono gli url visitati dagli utenti, e contengono i tag associati. 
	 * Vanno salvati nella tabella tagvisitedurls, che contiene (id idurl idtag). 
	 * quindi dovr— gi‡ aver effettuato il salvataggio degli url e dei tag.  */
	
	
	

}
