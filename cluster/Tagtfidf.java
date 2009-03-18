package cluster;

import java.util.Map;

public class Tagtfidf {
	
	/* Rappresentazione dei tags contenuti nella matrice C mxn (tagXurl) dove l'elemento 
	 * Cij Ž il numero di persone che hanno annotato la risorsa j con il tag i. 
	 * M invece (matrice delle associazioni - derivata da C) 
	 * Ž sempre mxn, ma si ispira al TF*IDF. 
	 * Mij = Cij * log(n/URLti) dove n Ž il numero di url (=il numero di colonne di C) 
	 * e URLti Ž il numero di url taggati con ti. Cos’ il tag Ž un vettore riga, 
	 * e l'url un vettore colonna. 
	 * 
	 * 
	 * TagTfidf: rappresenta il tag come mappa di chiave-valore: (url-tfidf) 
	 * forse mantiene solamente il tf nel tag, l'idf Ž calcolato al volo globalmente */

	
//	private Map<String, Map<RankedTag, Map<String, Double>>> userMatrix;
	private String tag;
	private Map<String, Double> tagUrlsMap;
	/* numero di risorse annotate con il tag attuale: Ž la lunghezza degli elementi 
	 * della mappa tagUrl? TODO */
	private int nt;
	
	public Tagtfidf(String tag, Map<String, Double> tagUrlsMapFromPersistence) {
		this.tag = tag;
		this.tagUrlsMap = tagUrlsMapFromPersistence;
		this.nt = this.tagUrlsMap.size();
	}
	
	
	/* coseno somiglianza tra due tags */
	/* occhio ai tipi */
	/* cosine similarity between tags */
	public static void cosSim(Tagtfidf tag1, Tagtfidf tag2) {
		
	}
	
	
	
	

	
}
