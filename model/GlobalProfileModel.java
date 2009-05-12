package model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


import cluster.Tagtfidf;

import persistence.GlobalProfileModelDAO;
import persistence.PersistenceException;
import persistence.UserDAO;
import persistence.UserModelDAO;
import persistence.postgres.URLTagsDAOPostgres;
import util.LogHandler;
import persistence.URLTagsDAO;

public class GlobalProfileModel {
	/* 
	 * Sarebbe meglio che il globalProfileModel contenesse degli 
	 * (url, List<tags>) che vengono passati dallo usermodelupdater quando 
	 * estrae i tag dagli url non ancora aggiornati rispetto al lastupdate dell'
	 * utente. */
	/* peró deve esistere sempre una e una sola istanza di globalprofilemodel, 
	 * altrimenti si sovrappongono le esecuzioni e diventa non thread-safe. 
	 * Posso trascurare questa considerazione perché giá esiste solo un userModelUpdater? */
	
	 /* Poi é lui il responsabile della creazione dei Tagtfidf e del loro
	 * successivo salvataggio sul db. 
	 * */


//	private GlobalProfile globalProfile;
	/* gestori del profilo globale */
//	private UserModelDAO userModelHandler;
	private GlobalProfileModelDAO globalProfileModelHandler;
//	private UserDAO userHandler;
//	private GlobalProfileDAO globalProfileHandler;
	
	/* lista di Tagtfidf */
//	private Map<String, Map<RankedTag, Map<String, Double>>> userMatrix;
	private List<Tagtfidf> tags;
	private int usersNumber;
	// url ricevuti dallo usermatrixcalculator. 
	private LinkedList<URLTags> urlsToSave;
	private URLTagsDAO URLTagsHandler;
	
	public GlobalProfileModel(LinkedList<URLTags> urlTagsToSave) {
		this.urlsToSave = urlTagsToSave;
		this.tags = new LinkedList<Tagtfidf>();
		//
		this.convertUrlsToTagtfidf(this.urlsToSave);
		this.URLTagsHandler = new URLTagsDAOPostgres();
		this.convertUrlsToTagCoOcc();
	}
	

	
	private void convertUrlsToTagCoOcc() {
		// TODO trasformazione in rappresentazione dei tag in co-occorrenze. 
		
	}



	/* cerca se é presente il tagtfidf con il tagValue per parametro */
	private Tagtfidf findTagtfidf(String tagValue) {
//		Logger logger = LogHandler.getLogger(this.getClass().getName());
//		logger.info("inizio la ricerca di " + tagValue +" nei tagstfidf del globalprofilemodel");
		Tagtfidf foundTag = null;
		Iterator<Tagtfidf> tagsIterator = this.tags.iterator();
		while (tagsIterator.hasNext()) {
			Tagtfidf currentTagtfidf = tagsIterator.next();
//			logger.info("esamino il tag " + currentTagtfidf.getTag());
			if(currentTagtfidf.getTag().equals(tagValue)) {
//				logger.info(currentTagtfidf.getTag() +" e " + tagValue + " sono uguali");
				foundTag = currentTagtfidf;
			}
		}
		
		return foundTag;
	}
	
	
	
	
	private void convertUrlsToTagtfidf(LinkedList<URLTags> urlsToSave) {

		/* logger */
//		Logger logger = LogHandler.getLogger(this.getClass().getName());
//		logger.info("inizio la conversione da URLTags a Tagtfidf");
		
		Iterator <URLTags> urlIterator = urlsToSave.iterator();
		/* itera sugli url */
		while (urlIterator.hasNext()) {
			URLTags currentUrl = urlIterator.next();	
//			logger.info("URLTags da convertire: " + currentUrl.getUrlString() + " con tags: " + currentUrl.getTags());
			Iterator<RankedTag> tagIterator = currentUrl.getTags().iterator();
			/* itera sui tags del currentUrl */
			/* costruisci un tag tfidf per ogni tag associato all'url, ma se giá esiste nei tags giá incontrati 
			 * un tag con quel valore, aggiungi il currentUrl a quel tag */
			while(tagIterator.hasNext()) {
				RankedTag currentTag = tagIterator.next();
//				logger.info("url: " + currentUrl.getUrlString() + " tag: " + currentTag.getTag());
				Tagtfidf presentTag = null;  
				presentTag = findTagtfidf(currentTag.getTag());
				if ( presentTag != null) {
					/* se hai giá incontrato questo tag, aggiungi un url al tag 
					 * esistente
					 */
//					logger.info("il tag " + currentTag.getTag() + " era giá presente nei tags incontrati");
//					logger.info("aggiungo l'url " + currentUrl.getUrlString() +" al tag " + currentTag.getTag());
					/* verifica! */
//					System.out.println("tag trovato: " + presentTag.getTag());
					presentTag.addUrlOccurrences(currentUrl.getUrlString(), 1.0);	
				} else {
					/* altrimenti, crea un nuovo tag Tfidf */
					Map<String, Double> tagUrlsMap = new HashMap<String, Double>();
					tagUrlsMap.put(currentUrl.getUrlString(), 1.0);
					Tagtfidf newTagtfidf = new Tagtfidf(currentTag.getTag(), tagUrlsMap);
					this.tags.add(newTagtfidf);
//					logger.info("tag nuovo: " + newTagtfidf.getTag());
				}
			}
			
			/* ho terminato la conversione */
		}
	}

	

	public void updateGlobalProfile() {
		try {
			this.URLTagsHandler.save(urlsToSave);
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/**
	 * @return the tags
	 */
	public List<Tagtfidf> getTags() {
		return tags;
	}

	/**
	 * @param tags the tags to set
	 */
	public void setTags(List<Tagtfidf> tags) {
		this.tags = tags;
	}



	public String tagTfidfToString() {
		String tagstfidf = "";
		StringBuffer buff = new StringBuffer();
		
		for (Tagtfidf tag: this.getTags()) {
			buff.append(tag.toString() + "; ");
		}
		return tagstfidf;
	}




	
	
}
