package cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import util.LogHandler;

public class Tagtfidf extends Tag {
	
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
	//cambio di rappresentazione: da Integer a Double
	private Map<String, Double> tagUrlsMap;
	/* numero di risorse annotate con il tag attuale: é la lunghezza degli elementi 
	 * della mappa tagUrl? */
	private Integer totalUrls;
	
	public Tagtfidf(String tag, Map<String, Double> tagUrlsMap) {
		this.tag = tag;
		this.tagUrlsMap = tagUrlsMap;
		this.totalUrls = new Integer(this.tagUrlsMap.size());
	}
	
	public Tagtfidf(String tagValue) {
		this.tag = tagValue;
		this.tagUrlsMap = new HashMap<String, Double>();
		this.totalUrls = new Integer(this.tagUrlsMap.size());
	}
	
	
	
	
	
	public Tagtfidf(List<Tagtfidf> tagsToMerge) {
	/* costruisce un tag medio con la lista di tagsToMerge. 
	 * É il centroide dei tag passati nella lista. */
		int numTags = tagsToMerge.size();
		this.tagUrlsMap = new HashMap<String, Double>();
		/* prendo tutte le chiavi che compaiono in tutti i tag (senza ripetizioni) */
		ArrayList<String> tagsKeys = this.getAllKeys(tagsToMerge);
		this.totalUrls = tagsKeys.size();
		/* iterando su queste, estrae i valori di tutti i tags corrispondenti alla chiave 
		 * richiesta: li somma tra loro e aggiunge la nuova (chiave, val1+...+valn) */
		StringBuffer sb = new StringBuffer();
		for (String key : tagsKeys) {
			/* estrai tutti i valori corrispondenti a questa chiave e sommali tra loro 
			 * al termine aggiungi la nuova (key,sumValue) */
			Iterator<Tagtfidf> it = tagsToMerge.iterator();
			Double accumulatore = 0.0;
			while(it.hasNext()) {
				Tagtfidf currentTag = it.next();
				if (currentTag.getTagUrlsMap().containsKey(key)) {
					accumulatore = accumulatore + currentTag.getValue(key);
				}
			}
			
			/* aggiungo la chiave con il nuovo valore, diviso per il numero di tags
			 * che sto fondendo */
			this.tagUrlsMap.put(key, accumulatore / numTags);
		} // ciclo for
		
		/* costruisco il nome, iterando di nuovo */	
		for (Tagtfidf tag: tagsToMerge) {
			sb.append(tag.getTag()).append("/");
		}
		
		this.tag = sb.toString();
		/* rimuovo l'ultimo carattere, il / che avevo scritto come separatore */
		this.tag = this.tag.substring(0, this.tag.length() - 1);		
	}
	
	/* metodo duplicato? 
	 * quando non é presente un url, restituisce 0 */
	public Double getValue(String url) {
		Double value = null;
		if (this.getTagUrlsMap().containsKey(url)) {
			value = this.getTagUrlsMap().get(url);
		}
		return value;
	}
	
	/* restituisce tutte le chiavi dei clusters */
	private ArrayList<String> getAllKeys(List<Tagtfidf> tags) {
		ArrayList<String> keys;
		Set<String> setKeys = new HashSet<String>();
		/* itera su tutti i tags *
		 * per ognuno, estrai le sue chiavi */
		Iterator<Tagtfidf> it = tags.iterator();
		while (it.hasNext()) {
//			Tagtfidf currentTag = it.next();
			setKeys.addAll(it.next().getKeys());
		}
		
		/* non devo considerare i duplicati dalla lista, quindi uso un set 
		 * e poi lo ritrasformo in arraylist */
		keys = new ArrayList<String>(setKeys);
		return keys;
	}
	
	/* 
	 * confronto tra due tag usando la coseno somiglianza */
	public Double compareToTag(Tagtfidf tagToCompare) {
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		logger.info("comparing tag: " + this.getTag() + " to tag: " + tagToCompare.getTag());
		Double cosineSimilarity = 0.0;
		Double numeratore = 0.0;
		Iterator<Entry<String, Double>> iterator1 = this.getTagUrlsMap().entrySet().iterator();
		
		while (iterator1.hasNext()) {
			Map.Entry<String, Double> keyValue1 = (Entry<String, Double>) iterator1.next();
			Double value2 = tagToCompare.getUrlFrequency(keyValue1.getKey());
//			System.out.println("chiave " + keyValue1.getKey() + " valore " + keyValue1.getValue());
			
			/* fai il prodotto e aggiungi all'accumulatore */			
			if (value2 != 0.0) {
//				System.out.println("keyfound: " + keyValue1.getKey() + " value: " + keyValue1.getValue());
				numeratore = numeratore + (keyValue1.getValue() * value2);			
			}
				/* se nel secondo tag l'url cercato non é presente: 
				 * avrebbe come occorrenza 0 e quindi si moltiplicherebbe il 
				 * primo valore per 0: non é necessario aggiungere un caso else */	
		}
		
		/* Modulo (sqrt della somma di tutti i valori tf) 
		 * dei due vettori Tagtfidf 
		 * se uso tfidf cambia il risultato? no */
		/* Modulo tag 1 */
		Double moduloTag1 = this.getModule();
		/* Modulo tag 2 */
		Double moduloTag2 = tagToCompare.getModule();
			
		Double denominatore = moduloTag1 * moduloTag2;		
//		System.out.println("denominatore: " + denominatore);
		
		cosineSimilarity = numeratore / denominatore;
		
		logger.info("calculated similarity: " + cosineSimilarity);
		
		return cosineSimilarity;
	}
	
	
	public String toString() {
		String tagRepresentation;
		StringBuffer sBuffer = new StringBuffer();
		
		sBuffer.append(getTag() + " :" );
		
		sBuffer.append(" {");
		for (String key: this.getKeys()) {
			Double freq = this.getUrlFrequency(key);
			sBuffer.append(" " + key + ": " + freq + " -");
		}
		sBuffer.append(" }");
		
		tagRepresentation = sBuffer.toString();
		return tagRepresentation;
	}
	
	
	public Double getModule() {
		Double module = 0.0;
		/* somma tutti i quadrati degli elementi della mappa. */
		Collection<Double> values = this.getTagUrlsMap().values();
		
		for (Double value: values) {
			module = module + Math.pow(value, 2);
//			System.out.println(module.intValue());	
		}
		module = Math.sqrt(module.doubleValue());
		return module;
	}
	

	/* occhio al tipo di ritorno, vorrei fosse rappresentativo. 
	 * TRUE/FALSE? */
	/* TODO: ho levato il parametro Integer frequency:  
	 * perché dovrei poter specificare un valore per l'occorrenza dell'url? 
	 * perché quando costruisco il Tag estraendolo dal database ha senso... */
	/* DEPRECATED , use addUrlOccurrences(String, int) instead */
	public boolean addUrlOccurrency(String url) {
		boolean success = false;
		/* verifica se l'url esiste: 
		 * se esiste, aggiungi un'occorrenza, 
		 * altrimenti fai una nuova chiave e aggiungila alla mappa */
		Double oldValue = null; 
		oldValue = this.tagUrlsMap.get(url);
		if (oldValue == 0.0) {
			// aggiungo un'occorrenza del tag per l'url (nuovo) specificato 
			this.tagUrlsMap.put(url, 1.0);
			// aumento di 1 totalUrls, contiene un url in piú
			this.totalUrls = this.totalUrls + 1;
			success = true;
		} else {
			/* aggiorno il valore esistente, aggiungendo un'occorrenza */
			/* TODO: uhm... */
			this.tagUrlsMap.put(url, oldValue + 1.0);
//			System.out.print("tag " + this.getTag());
//			System.out.println(" chiave inserita: " + url + " valore: " + this.tagUrlsMap.get(url));
			success = true; 
		}
	
		return success;
	}
	
	
	/* altro metodo, utile nella costruzione degli url quando li 
	 * estraggo dal database 
	 * TODO: test addurlOccurrences*/
	public boolean addUrlOccurrences(String url, Double freq) {
		boolean success = false;
		/* verifica se l'url esiste: 
		 * se esiste, aggiungi un'occorrenza, 
		 * altrimenti fai una nuova chiave e aggiungila alla mappa */
		Double oldValue = null; 
		oldValue = this.getUrlFrequency(url);
		if (oldValue == 0.0) {
			// aggiungo le occorrenze del tag per l'url (nuovo) specificato 
			this.tagUrlsMap.put(url, freq);
			this.totalUrls = this.totalUrls + 1;
			success = true;
		} else {
			/* put rimpiazza la (chiave, valore) se la chiave giá esisteva */
			this.tagUrlsMap.put(url, oldValue + freq);
//			System.out.print("tag " + this.getTag());
//			System.out.println(" chiave inserita: " + url + " valore: " + this.tagUrlsMap.get(url));
			success = true; 
		}
		return success;
	}
	
	/* restituisce la frequenza di un url di un tag. 
	 * se non é presente l'url, restituisce 0 */
	public Double getUrlFrequency(String url) {
		Double frequency = 0.0;
		if (this.getTagUrlsMap().containsKey(url)) {
			frequency =  this.getTagUrlsMap().get(url);
		}
		return frequency;
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
	public Map<String, Double> getTagUrlsMap() {
		return tagUrlsMap;
	}

	/**
	 * @param tagUrlsMap the tagUrlsMap to set
	 */
	public void setTagUrlsMap(Map<String, Double> tagUrlsMap) {
		this.tagUrlsMap = tagUrlsMap;
	}

	/**
	 * @return the totalUrls
	 */
	public Integer getTotalUrls() {
		return totalUrls;
	}

	public ArrayList<String> getKeys() {
		ArrayList<String> keyList = new ArrayList<String>();
		Collection<String> keys = this.getTagUrlsMap().keySet();
		Iterator<String> it = keys.iterator();
		while (it.hasNext()) {
			keyList.add(it.next());
		}
		return keyList;
		
	}

	


	/**
	 * @param totalUrls the totalUrls to set
	 */
// non deve essere possibile settare il numero degli url dall'esterno
//	public void setTotalUrls(int totalUrls) {
//		this.totalUrls = totalUrls;
//	}

	
}
