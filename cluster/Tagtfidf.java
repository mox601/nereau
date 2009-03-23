package cluster;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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
	public Double compareToTag(Tagtfidf tagToCompare) {
		Double cosineSimilarity = 0.0;
		
		Double numeratore = 0.0;
		
		Iterator<Entry<String, Integer>> iterator1 = this.getTagUrlsMap().entrySet().iterator();
		
		while (iterator1.hasNext()) {
			Map.Entry<String, Integer> keyValue1 = (Entry<String, Integer>) iterator1.next();
			Integer value2 = null;
			value2 = tagToCompare.getUrlFrequency(keyValue1.getKey());
			
			/* fai il prodotto e aggiungi all'accumulatore */			
			if (value2 != null) {
//				System.out.println("keyfound: " + keyValue1.getKey() + " value: " + keyValue1.getValue());
				numeratore = numeratore + (keyValue1.getValue() * value2);			
			}
				/* se nel secondo tag l'url cercato non é presente: 
				 * avrebbe come occorrenza 0 e quindi si moltiplicherebbe il 
				 * primo valore per 0: non é necessario aggiungere un caso else */	
		}
		
			
		
		/* Modulo (sqrt della somma di tutti i valori tf) 
		 * dei due vettori Tagtfidf 
		 * TODO: se uso tfidf cambia il risultato? */
		/* Modulo tag 1 */
		Double moduloTag1 = this.getModule();
		/* Modulo tag 2 */
		Double moduloTag2 = tagToCompare.getModule();
		
		
		Double denominatore = moduloTag1 * moduloTag2;		
//		System.out.println("denominatore: " + denominatore);
		
		cosineSimilarity = numeratore / denominatore;
		
		return cosineSimilarity;
	}
	
	
	public Double getModule() {
		Double module = 0.0;
		/* somma tutti i quadrati degli elementi della mappa. */
		Collection<Integer> values = this.getTagUrlsMap().values();
		
		Iterator<Integer> it = values.iterator();
		while (it.hasNext()) {
			Integer value = (Integer) it.next();
			module = module + Math.pow(value, 2);
//			System.out.println(module.intValue());
		}
		module = Math.sqrt(module.doubleValue());
		return module;
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
	
	/*AAAAARRHRGGGGGGGGGGGHHHHHHHHHHH!!!!!!!!!!!!!!
	 * la somiglianza si calcola tra due cluster!!!!!!!!!!!! 
	 * é utile quando si deve applicare il clustering gerarchico!! */
	/* calcola la coseno somiglianza tra tag corrente e tag parametro */
	public Double cosineSimilarity(Tagtfidf tag) {
		Double cosineValue = 0.0; 
		/* per ogni tag posso creare un vettore che contiene 0 se nel vettore corrente 
		 * non é presente una chiave-valore che invece c'é nell'altro
		 *  
		 * a1 b1 c1
		 * d1 e1 f1
		 * risultato: 
		 * a1 b1 c1 00 00 00
		 * 00 00 00 d1 e1 f1
		 * Poi calcolo la coseno somiglianza tra questi due vettori.
		 * 
		 *  oppure: 
		 *  a1 b1 c1
		 *  b1 d1 a1
		 *  risultato: 
		 *  a1 b1 c1 00
		 *  a1 b1 00 d1
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
