package model;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import model.usermodel.tags.TagFinder;
import model.usermodel.tags.TxtSubUrlTagFinderStrategy;


import cluster.Tagtfidf;

import persistence.GlobalProfileModelDAO;
import persistence.PersistenceException;
import persistence.UserDAO;
import persistence.UserModelDAO;
import persistence.postgres.URLTagsDAOPostgres;
import util.LogHandler;
import util.ParameterHandler;
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

	private boolean alreadyRetrieved;
	
	public GlobalProfileModel(LinkedList<URLTags> urlTagsToSave) {
		this.urlsToSave = urlTagsToSave;
		this.tags = new LinkedList<Tagtfidf>();
		//
		this.convertUrlsToTagtfidf(this.urlsToSave);
		this.URLTagsHandler = new URLTagsDAOPostgres();
		this.convertUrlsToTagCoOcc();
	}
	
	/* prendo direttamente i visitedURLs ed estraggo i tag per conto mio */
	public GlobalProfileModel(List<VisitedURL> visitedURLs, boolean alreadyRetrieved) {
		
		
		this.alreadyRetrieved = alreadyRetrieved;
//		System.out.println("gli url sono tutti sul filesystem? " + this.alreadyRetrieved);
		
		
		
		if(alreadyRetrieved) {
			//devo ancora salvare gli url su database, altrimenti non avranno ID!!
		}
		
		
		
		/* devo passare da visitedURL a URLTags */
		LinkedList<URLTags> urlTagsToSave = null;
		
		LinkedList<URLTags> listUrlTags = this.convertVisitedUrlsToURLTags(visitedURLs);
		/* ho ottenuto i tag per gli url che ho visitato, 
		 * posso convertirli in Tagtfidf e salvarli su db */
		
		this.urlsToSave = listUrlTags;
		this.tags = new LinkedList<Tagtfidf>();
		//
		if(urlsToSave.size() > 0) {
			this.convertUrlsToTagtfidf(this.urlsToSave);	
		}
		
		this.URLTagsHandler = new URLTagsDAOPostgres();
		
	
		
	}


	private LinkedList<URLTags> convertVisitedUrlsToURLTags(List<VisitedURL> visitedURLs) {
		/* deve estrarre i tag da ogni url e costruire una lista di URLTags */
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		
		TagFinder tagFinder = null;		
		LinkedList<URLTags> listUrlTags = new LinkedList<URLTags>();
		
		for (VisitedURL visitedURL: visitedURLs) {
			
			String urlString = visitedURL.getURL();
			//se devo lavorare sugli url
			if (!this.alreadyRetrieved) {
				logger.info("lavoro sull'URL " + urlString);
				tagFinder = new TagFinder();				
			} else {
				//o direttamente sui files
				
				tagFinder = new TagFinder(new TxtSubUrlTagFinderStrategy());
				//modifica l'url per il salvataggio
				String preUrlString = urlString.substring(0, urlString.lastIndexOf('/')+1);
				String postUrlString = urlString.substring(urlString.lastIndexOf('/')+1);
				urlString = preUrlString + "tags_" + postUrlString;
				
				System.out.println("lavoro sul file " + urlString);
				
			}
				
			logger.info("Estraggo i tag dell'url: " + visitedURL.toString());
			Set<RankedTag> tags = tagFinder.findTags(urlString);
			URLTags currentUrlTag = new URLTags(urlString, tags);
			logger.info("ho ottenuto i tags: " + tags);
			listUrlTags.add(currentUrlTag);
			
			
			
		}
		
		/* ho gli URLTags in una lista, ora posso salvarli sul database */
		
		return listUrlTags;
		
		
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
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		logger.info("inizio la conversione da URLTags a Tagtfidf");
		/* itera sugli url */
		for (URLTags currentUrl: urlsToSave) {
			/* itera sui tags del currentUrl */
			/* costruisci un tag tfidf per ogni tag associato all'url, ma se giá esiste nei tags giá incontrati 
			 * un tag con quel valore, aggiungi il currentUrl a quel tag */
			for (RankedTag currentTag: currentUrl.getTags()) {
					Tagtfidf presentTag = null;
					presentTag = findTagtfidf(currentTag.getTag());
					if ( presentTag != null) {
						/* se hai giá incontrato questo tag, aggiungi un url al tag 
						 * esistente
						 */
						presentTag.addUrlOccurrences(currentUrl.getUrlString(), 1.0);	
					} else {
						/* altrimenti, crea un nuovo tag Tfidf */
						Map<String, Double> tagUrlsMap = new HashMap<String, Double>();
						tagUrlsMap.put(currentUrl.getUrlString(), 1.0);
						Tagtfidf newTagtfidf = new Tagtfidf(currentTag.getTag(), tagUrlsMap);
						this.tags.add(newTagtfidf);
					}
				
			}

			/* ho terminato la conversione */
		}
		
		
		/* posso eliminare tutti i tag no_tag, non mi é utile */
	
		int index = -1;
		for (Tagtfidf tag: tags) {
			logger.info(tag.toString());
			if (tag.getTag().equals("no_tag")) {
				index = tags.indexOf(tag);
				logger.info("levo il tag no_tag all'indice " + index);
			}
		}
		
		if (index > -1) {
			tags.remove(index);	
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
