package model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cluster.Tagtfidf;

import persistence.GlobalProfileModelDAO;
import persistence.UserDAO;
import persistence.UserModelDAO;

public class GlobalProfileModel {
	/* 
	 * Sarebbe meglio che il globalProfileModel contenesse degli 
	 * (url, List<tags>) che vengono passati dallo usermodelupdater quando 
	 * estrae i tag dagli url non ancora aggiornati rispetto al lastupdate dell'
	 * utente. 
	 * Poi Ž lui il responsabile della creazione dei Tagtfidf e del loro
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
	
	
	
	
}
