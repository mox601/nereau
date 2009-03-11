package model;

import java.util.HashMap;
import java.util.Map;

import persistence.GlobalProfileDAO;
import persistence.GlobalProfileModelDAO;
import persistence.postgres.GlobalProfileDAOPostgres;
import persistence.postgres.GlobalProfileModelDAOPostgres;

public class GlobalProfileModel {

	private GlobalProfile globalProfile;
	private GlobalProfileModelDAO globalProfileModelHandler;
	private GlobalProfileDAO globalProfileHandler;
	private HashMap<String, Map<RankedTag, Map<String, Double>>> globalMatrix;

	public GlobalProfileModel(GlobalProfile globalProfile) {
		this.globalProfile = globalProfile;
		this.globalProfileModelHandler = new GlobalProfileModelDAOPostgres();
		this.globalProfileHandler = new GlobalProfileDAOPostgres();
		
		/* matrice che contiene i valori di tf(*idf) di tag-url */
		this.globalMatrix = new HashMap<String, Map<RankedTag, Map<String, Double>>>();
	}

}
