package model.queryexpansion;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import util.LogHandler;

import model.RankedTag;

public class TfidfExpansionTagsStrategy extends ExpansionTagsStrategy {

	/* trova TUTTI i tag dell'espansione, senza filtrarli */
	@Override
	public Set<RankedTag> findExpansionTags(Set<String> stemmedQueryTerms,
			Map<String, Map<RankedTag, Map<String, Double>>> subMatrix) {

		Logger logger = LogHandler.getLogger(this.getClass().getName());
//		TreeSet<RankedTag> commonTags = new TreeSet<RankedTag>();
		TreeSet<RankedTag> allTags = new TreeSet<RankedTag>();
//		boolean tagsInitialized = false;
		
		for (String term: stemmedQueryTerms) {
			if(!subMatrix.containsKey(term)) {
				//nella matrice non c'Ž il termine della query
				break;
			}
			Set<RankedTag> tags4term = subMatrix.get(term).keySet();
			logger.info("set di tags trovati per il termine " + term + ": " + tags4term);
			//aggiungo tutti i tag all'insieme, solo se non c'Ž gi‡ un RankedTag con la stessa chiave nel set
			
			for (RankedTag tagToAdd: tags4term) {
				if(!existsRankedTag(tagToAdd, allTags)) {
					allTags.add(tagToAdd);
				}
			}
			
			
//			allTags.addAll(tags4term);
		}
		
		
		// filtra i tags, li restituisce tutti tranne il null
		
		allTags = this.filterNullTags(allTags);
		
		return allTags;
	}

	/* verifica se in un set esiste un RankedTag riferito allo stesso tag che gli passo (senza considerare la somiglianza) */
	private boolean existsRankedTag(RankedTag tagToAdd,
			TreeSet<RankedTag> allTags) {
		boolean present = false;

		for (RankedTag tag: allTags) {
			if (tag.getTag().equals(tagToAdd.getTag())) {
				present = true;
			}
		}
		return present;
	}

}
