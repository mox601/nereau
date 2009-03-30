package model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cluster.Tagtfidf;

import persistence.GlobalProfileModelDAO;
import persistence.UserDAO;
import persistence.UserModelDAO;

public class GlobalProfileModel {
	/* il Profile Model, che contiene 
	 * tutti gli oggetti Tagtfidf del sistema, che vengono aggiornati mano 
	 * mano che gli utenti cliccano sui siti. 
	 * Quando si clicca su un sito infatti, i tag di delicious associati a questo url 
	 * vengono salvati nel GlobalProfile come "entrypoint".
	 * Mi interessa infatti sapere quali sono i valori tfidf per un certo tag. */ 

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
