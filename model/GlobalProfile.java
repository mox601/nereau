package model;

public class GlobalProfile {
	
	private GlobalProfileModel globalProfileModel; 
	
	/* il global profile � un profilo condiviso da tutti gli utenti. 
	 * potrebbe essere superfluo, contiene solo il globalprofilemodel
	 * contiene il Profile Model, che contiene 
	 * tutti gli oggetti Tagtfidf del sistema, che vengono aggiornati mano 
	 * mano che gli utenti cliccano sui siti. 
	 * Quando si clicca su un sito infatti, i tag di delicious associati a questo url 
	 * vengono salvati nel GlobalProfile come "entrypoint".
	 * Mi interessa infatti sapere quali sono i valori tfidf per un certo tag. 
	 *   
	 * Come dati privati, contiene: 
	 * data dell ultimo aggiornamento
	 * numero di tag totali
	 * 
	 * costruttore + get e set degli attributi privati
	 * */
	
	public GlobalProfile() {
		this.globalProfileModel = new GlobalProfileModel(this);
	}
	

}
