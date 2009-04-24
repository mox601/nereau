package model;

import java.util.LinkedList;

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
	 * 
	 * e aggiornare le corrispondenti co-occorrenze sul database. */
	public void updateProfile() {
		
	}

}
