package cluster;

import java.util.Map;

import model.RankedTag;

public class TagCoOccurrence extends Tag {

	private String value;
	private Map<String, Double> tagTag;
	/* contiene le cooccorrenze con gli altri tag */
//	private Map<String, Double> tagCoOccurrence;
	
	
	public TagCoOccurrence(String value, Map<String, Double> tagTag) {
		this.value = value; 
		this.tagTag = tagTag;
	}

}
