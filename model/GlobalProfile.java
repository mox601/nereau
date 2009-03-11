package model;

public class GlobalProfile {
	
	private GlobalProfileModel profileModel; 

	/* attributi privati che descrivono il modello? 
	 * ultimo aggiornamento
	 * numero di utenti rappresentati nel sistema
	 * numero di tag totali
	 * */
	
	public GlobalProfile() {
		this.profileModel = new GlobalProfileModel(this);		
	}

	public GlobalProfileModel getProfileModel() {
		return profileModel;
	} 

}
