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
	private Map<String, Integer> tagUrlsMap;
	/* numero di risorse annotate con il tag attuale: é la lunghezza degli elementi 
	 * della mappa tagUrl? */
	private Integer totalUrls;
	
	public Tagtfidf(String tag, Map<String, Integer> tagUrlsMap) {
		this.tag = tag;
		this.tagUrlsMap = tagUrlsMap;
		this.totalUrls = new Integer(this.tagUrlsMap.size());
	}
	
	public Tagtfidf(List<Tagtfidf> tagsToMerge) {
		
	/* costruisce un tag average con i tagsToMerge */
		
		this.tagUrlsMap = new HashMap<String, Integer>();
		
		/* prendo tutte le chiavi che compaiono in tutti i tag (senza ripetizioni) */
		ArrayList<String> tagsKeys = this.getAllKeys(tagsToMerge);
		/* iterando su queste, estrae i valori di tutti i tags corrispondenti alla chiave 
		 * richiesta: li somma tra loro e aggiunge la nuova (chiave, val1+...+valn) */
		StringBuffer sb = new StringBuffer();

		for (String key : tagsKeys) {
			/* estrai tutti i valori corrispondenti a questa chiave e sommali tra loro 
			 * al termine aggiungi la nuova (key,sumValue) */
			Iterator<Tagtfidf> it = tagsToMerge.iterator();
			Integer accumulatore = 0;
			while(it.hasNext()) {
				Tagtfidf currentTag = it.next();
				
				if (currentTag.getTagUrlsMap().containsKey(key)) {
					accumulatore = accumulatore + currentTag.getValue(key);
				}
			}
			/* aggiungo la chiave con il nuovo valore */
			this.tagUrlsMap.put(key, accumulatore);
		} // ciclo for
		
		/* costruisco il nome, iterando di nuovo */
		
		Iterator<Tagtfidf> itNome = tagsToMerge.iterator();
		while (itNome.hasNext()) {
			sb.append(itNome.next().getTag()).append("/");
		}
		
		this.tag = sb.toString();
		/* rimuovo l'ultimo carattere, il / che avevo scritto come separatore */
		this.tag = this.tag.substring(0, this.tag.length() - 1);		
	}
	
	/* TODO: gestisci l'eccezione */
	public Integer getValue(String url) {
		Integer value = null;
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
			Tagtfidf currentTag = it.next();
			setKeys.addAll(currentTag.getKeys());
		}
		
		/* non devo considerare i duplicati dalla lista, é meglio se uso un set 
		 * e poi lo ritrasformo in arraylist */
		keys = new ArrayList<String>(setKeys);
		return keys;
	}

	/*AAAAARRHRGGGGGGGGGGGHHHHHHHHHHH!!!!!!!!!!!!!!
	 * la somiglianza si calcola tra due cluster!!!!!!!!!!!! 
	 * é utile quando si deve applicare il clustering gerarchico!! */
	
	/* confronto tra due tag usando la coseno somiglianza */
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
		Integer oldValue = null; 
		oldValue = this.tagUrlsMap.get(url);
		if (oldValue == null) {
			// aggiungo un'occorrenza del tag per l'url (nuovo) specificato 
			this.tagUrlsMap.put(url, 1);
			// aumento di 1 totalUrls, contiene un url in piú
			this.totalUrls = this.totalUrls + 1;
			success = true;
		} else {
			/* aggiorno il valore esistente, aggiungendo un'occorrenza */
			/* TODO: uhm... */
			this.tagUrlsMap.put(url, oldValue + 1);
			System.out.print("tag " + this.getTag());
			System.out.println(" chiave inserita: " + url + " valore: " + this.tagUrlsMap.get(url));
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
