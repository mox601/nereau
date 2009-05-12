package model;

import java.util.LinkedList;

import cluster.TagCoOccurrence;

public class PersonalProfileModel {
	
	private LinkedList<URLTags> urlsToSave;
	
	public PersonalProfileModel(LinkedList<URLTags> urls) {
		this.urlsToSave = urls;
	}

	/* si occupa di trasformare gli URLTags dell'oggetto in 
	 * TagCoOccurrences e di salvarli nel database */
	
	/* devo tradurre da rappresentazione 
	 * (URL1: tag1, tag2, tag3, ..., tagn)
	 * a
	 * (TAG1: tag2, tag5, tag9, tag4)
	 * e aggiornare le corrispondenti co-occorrenze sul database. */
	public void updatePersonalProfile() {
		
		/* per ogni url della lista, estrai i tag associati e aggiungi UNA 
		 * Co-occorrenza per ognuno di loro in una struttura appropriata */
		
		new 
		
		for(URLTags url: urlsToSave) {
			for(RankedTag tag: url.getTags()){
				
				
				
				
				
				TagCoOccurrence tag = new TagCoOccurrence();		
			}
		}
		
		
		
	}

}
