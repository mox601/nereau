package cluster;

import java.util.Map;

public class Tagtfidf {
	
	/* Rappresentazione dei tags contenuti nella matrice C mxn (tagXurl) dove l'elemento 
	 * Cij é il numero di persone che hanno annotato la risorsa j con il tag i. 
	 * M invece (matrice delle associazioni - derivata da C) 
	 * é sempre mxn, ma si ispira al TF*IDF. 
	 * Mij = Cij * log(n/URLti) dove n é il numero di url (=il numero di colonne di C) 
	 * e URLti é il numero di url taggati con ti. Cosí il tag é un vettore riga, 
	 * e l'url un vettore colonna. 
	 * 
	 * 
	 * TagTfidf: rappresenta il tag come mappa di chiave-valore: (url-tfidf) 
	 * forse mantiene solamente il tf nel tag, l'idf é calcolato al volo globalmente */

	// le Map<K, V> accettano solo istanze di classi come K e V
//	private Map<String, Map<RankedTag, Map<String, Double>>> userMatrix;
	private String tag;
	private Map<String, Integer> tagUrlsMap;
	/* numero di risorse annotate con il tag attuale: é la lunghezza degli elementi 
	 * della mappa tagUrl? TODO */
	private Integer totalUrls;
	
	public Tagtfidf(String tag, Map<String, Integer> tagUrlsMap) {
		this.tag = tag;
		this.tagUrlsMap = tagUrlsMap;
		this.totalUrls = new Integer(this.tagUrlsMap.size());
	}
	
	/* confronto tra due tag */
	public double compareToTag(Tagtfidf tagToCompare) {
		double cosineSimilarity = 0;
		return cosineSimilarity;
	}
	
	/* occhio al tipo di ritorno, vorrei fosse rappresentativo. 
	 * TRUE/FALSE? */
	/* TODO: ho levato il parametro Integer frequency:  
	 * perché dovrei poter specificare un valore per l'occorrenza dell'url? */
	public boolean addUrlOccurrency(String url) {
		boolean success = false;
		/* verifica se l'url esiste: 
		 * se esiste, aggiungi un'occorrenza, 
		 * altrimenti fai una nuova chiave e aggiungila alla mappa */
		/* cerca subito di estrarre il valore vecchio associato all'url */
		Integer oldValue = this.tagUrlsMap.get(url);
		if (oldValue == null) {
			// aggiungo un'occorrenza del tag per l'url (inesistente) specificato 
			this.tagUrlsMap.put(url, 1);
			// aumento di 1 totalUrls, contiene un url in piú
			this.totalUrls = this.totalUrls + 1;
			success = true;
		} else {
			/* aggiorno il valore esistente, aggiungendo un'occorrenza */
			this.tagUrlsMap.put(url, oldValue + 1);
			success = true; 
		}
		
//		if (this.tagUrlsMap.containsKey(url)) {
//			double newValue = this.tagUrlsMap.get(url).doubleValue() + 1;
//			this.tagUrlsMap.put(url, newValue);
//		}
		
		return success;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/* restituisce la frequenza di un url di un tag */
	public Integer getUrlFrequency(String url) {
		return this.getTagUrlsMap().get(url);
		
	} 
	

	/* calcola la coseno somiglianza tra tag corrente e tag parametro */
	public Double cosineSimilarity(Tagtfidf tag) {
		Double cosineValue = 0.0; 
		/* per ogni tag posso creare un vettore che contiene 0 se nel vettore corrente 
		 * non é presente una chiave che invece c'é nell'altro 
		 * 
		 *  123
		 *  456
		 * result: 
		 * 123000
		 * 000456
		 * (anche in ordine diverso)
		 * Poi calcolo la coseno somiglianza tra questi due vettori. 
		 * 
		 * */
		
		
		
		return cosineValue;
	}
	
	

	/**
	 * @return the tag
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * @param tag the tag to set
	 */
	public void setTag(String tag) {
		this.tag = tag;
	}

	/**
	 * @return the tagUrlsMap
	 */
	public Map<String, Integer> getTagUrlsMap() {
		return tagUrlsMap;
	}

	/**
	 * @param tagUrlsMap the tagUrlsMap to set
	 */
	public void setTagUrlsMap(Map<String, Integer> tagUrlsMap) {
		this.tagUrlsMap = tagUrlsMap;
	}

	/**
	 * @return the totalUrls
	 */
	public Integer getTotalUrls() {
		return totalUrls;
	}

	/**
	 * @param totalUrls the totalUrls to set
	 */
// non deve essere possibile settare il numero degli url dall'esterno
//	public void setTotalUrls(int totalUrls) {
//		this.totalUrls = totalUrls;
//	}

	

	
}
