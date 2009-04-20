package cluster.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import cluster.Tag;
import cluster.Tagtfidf;

public class TagtfidfTest {
	
	private String blogTag;
	private HashMap<String, Integer> tagUrlsMap1;
	private String blogUrl1; 
	private String blogUrl2; 
	private String blogUrl3; 
	private Integer blogUrl1Freq;
	private Integer blogUrl2Freq;
	private Integer blogUrl3Freq;
	
	private Tagtfidf tag1;
//	private Tag tag1;
	
	String designTag; 
	private HashMap<String, Integer> tagUrlsMap2;
	private String designUrl1;
	private String designUrl2;
	private String designUrl3;
	private Integer designUrl1Freq;
	private Integer designUrl2Freq;
	private Integer designUrl3Freq;
	private Tagtfidf tag2; 

	
	
	@Before
	public void setUp() {
		blogTag = "blog";
		tagUrlsMap1 =  new HashMap<String, Integer>();
		tagUrlsMap2 =  new HashMap<String, Integer>();

		/* uso degli url che su delicious sono stati taggati con l'url blog, 
		 * e uso il numero totale delle annotazioni dell'url per rappresentare la
		 * frequenza con cui l'url é stato taggato con il tag in questione. 
		 * É solo per scopi di test. */
		blogUrl1 = "http://www.notcot.org/";
		blogUrl1Freq = new Integer(7200);
		tagUrlsMap1.put(blogUrl1, blogUrl1Freq);
		blogUrl2 = "http://www.noupe.com/";
		blogUrl2Freq = new Integer(3277);
		tagUrlsMap1.put(blogUrl2, blogUrl2Freq);
		blogUrl3 = "http://www.formfiftyfive.com/";
		blogUrl3Freq = new Integer(2500);
		tagUrlsMap1.put(blogUrl3, blogUrl3Freq);
		tag1 = new Tagtfidf(blogTag, tagUrlsMap1);
		/* ho inserito tutti i (chiave,valore) nella Map. 
		 * Ora il tag é rappresentato da 3 url, con la loro frequenza */
		
		
		
		/* rappresento un altro tag, e poi confronto la coseno somiglianza */
		/* devo mettere almeno 1 url in comune */
		designTag = "design";
		designUrl1 = "http://iamphotoshop.com/";
		designUrl1Freq = new Integer(395);
		tagUrlsMap2.put(designUrl1, designUrl1Freq);
		designUrl2 = "http://www.noupe.com/adobe/40-high-quality-adobe-fireworks-tutorials-and-resources.html";
		designUrl2Freq = new Integer(399);
		tagUrlsMap2.put(designUrl2, designUrl2Freq);
		designUrl3 = "http://www.formfiftyfive.com/";
		designUrl3Freq = new Integer(2000);
		tagUrlsMap2.put(designUrl3, designUrl3Freq);
		tag2 = new Tagtfidf(designTag, tagUrlsMap2);

		/*ho creato un tag design che ha in comune con il tag blog solo l'ultimo url */
		
		
	}

	
	

	@Test
	public void testTagtfidf() {
		
		/* check che esista il tag con il nome giusto*/
		assertEquals(blogTag, tag1.getTag());
		/* che la lista di tag sia uguale */
		assertEquals(tagUrlsMap1, tag1.getTagUrlsMap());
		
		/* e che i valori siano corretti per tutti i tag inseriti */
		assertEquals(blogUrl1Freq, tag1.getTagUrlsMap().get(blogUrl1)); 
		assertEquals(blogUrl2Freq, tag1.getTagUrlsMap().get(blogUrl2));
		assertEquals(blogUrl3Freq, tag1.getTagUrlsMap().get(blogUrl3));
		
		/* e che le occorrenze totali del tag siano la somma delle frequenze */
		Integer expectedTagFrequency = tagUrlsMap1.size();
		assertEquals(expectedTagFrequency, tag1.getTotalUrls()); 
	}
	
	
	@Test
	public void testAddUrlToTag() {
		/* aggiunge un'occorrenza di un sito noto */
		String oldUrl = "http://www.notcot.org/";
		// il tag deve esistere
		Integer oldFrequency =  tag1.getUrlFrequency(oldUrl);
		assertNotNull(oldFrequency);
		tag1.addUrlOccurrences(oldUrl, 1);
		assertEquals(new Integer(1 + oldFrequency), tag1.getUrlFrequency(oldUrl));
		
		/* aggiunge al tag un'occorrenza di un sito nuovo */
		String newUrl = "http://www.abkhfjdsohgos.it/";
		/* l'url aggiunto in questo caso non deve esistere prima, frequenza 0 */
		assertEquals((Integer) 0, tag1.getUrlFrequency(newUrl));
		tag1.addUrlOccurrency(newUrl);
		assertEquals(new Integer(1), tag1.getUrlFrequency(newUrl));
			
	}
	
	
	@Test
	public void testCosineSimilarity() {
		
	} 
	
	
	
	@Test
	public void testModule() {
		Double expectedModule = 0.0;		
		Iterator<Integer> it = tagUrlsMap1.values().iterator();
		while (it.hasNext()) {
			Integer value = (Integer) it.next();
			expectedModule = expectedModule + Math.pow(value, 2); 
		}
		expectedModule = Math.sqrt(expectedModule);
		assertEquals(expectedModule, tag1.getModule());
		
	}
	
	
	

	@Test
	public void testCompareToTag() {
		/* confronto i due tag blog (tag1) e design(tag2) */
		
		/* la coseno somiglianza é simmetrica */
		Double cosSim12 = tag1.compareToTag(tag2);
		Double cosSim21 = tag2.compareToTag(tag1);
		
//		System.out.println("somiglianza: " + cosSim12);
		assertTrue(cosSim12.equals(cosSim21));	
		
	}
	
	
	
	
	
	@Test
	public void testCompareToTagBig() {
		/* crea due tag con molti url, in numero random */
		
		String firstTagString = "tag_one";
		Map<String, Integer> firstTagUrlsMap = new HashMap<String, Integer>();
		String secondTagString = "tag_two";
		Map<String, Integer> secondTagUrlsMap = new HashMap<String, Integer>();
		
		int numberOfUrls = (int) (Math.random() * 100);
//		System.out.println("numero di url 1: " + numberOfUrls);
		for (int i = 0; i < numberOfUrls; i++) {

			String number = Integer.toString(i);
			String key = "a".concat(number);
//			System.out.println(url);
			
			int urlTagFrequency = (int) (Math.random() * 100);
			firstTagUrlsMap.put(key, urlTagFrequency);
			
			if (i % 2 == 0) {
				/*aggiungi la stessa chiave con diverso valore all'altro tag */
				secondTagUrlsMap.put(key, urlTagFrequency + (int) Math.random() * 40);
			}

		}
		
		Tagtfidf firstTag = new Tagtfidf(firstTagString, firstTagUrlsMap);
		Tagtfidf secondTag = new Tagtfidf(secondTagString, secondTagUrlsMap);
		
		
		/* la coseno somiglianza é simmetrica */
		Double cosSim12 = firstTag.compareToTag(secondTag);
		Double cosSim21 = secondTag.compareToTag(firstTag);
		
//		System.out.println("somiglianza: " + cosSim12);
		assertTrue(cosSim12.equals(cosSim21));	
	
	}
	
	
	@Test
	public void testGetKeys() {
		ArrayList<String> keys = tag1.getKeys();
		ArrayList<String> expectedKeys = new ArrayList<String>();
		expectedKeys.add(blogUrl1);
		expectedKeys.add(blogUrl2);
		expectedKeys.add(blogUrl3);
		assertTrue(keys.containsAll(expectedKeys));
	}
	
	
	
	
	@Test
	public void constructorTagAverageFromList() {
		List<Tagtfidf> tagsToMerge = new LinkedList<Tagtfidf>();
		
		tagsToMerge.add(tag1);
		tagsToMerge.add(tag2);
		Tagtfidf mergedTags = new Tagtfidf(tagsToMerge);
		
		/* il tag merged deve contenere chiavi con i valori che sono la media dei 
		 * valori corrispondenti alle chiavi sommate */
		mergedTags.getKeys();
		Set<String> mergedKeys = new HashSet<String>(tag1.getKeys());
		mergedKeys.addAll(tag2.getKeys());
//		ArrayList<String> expectedKeyList = new ArrayList<String>(mergedKeys);
		Set<String> actualKeySet = new HashSet<String>(mergedTags.getKeys());
		assertEquals(mergedKeys, actualKeySet);
		
		for (String mergedKey: mergedKeys) {
			Integer freq1 = tag1.getUrlFrequency(mergedKey);
			Integer freq2 = tag2.getUrlFrequency(mergedKey);
			Integer averageFreq = (freq1 + freq2) / tagsToMerge.size();
			/* verifica che la somma delle occorrenze sia corretta nel 
			 * tag fusione: cioé la media! 
			 * TODO: allora passo a double 
			 * per rappresentare le occorrenze? sono possibili dei valori con 
			 * la virgola!! */
			assertEquals(mergedTags.getUrlFrequency(mergedKey), averageFreq);
		}
		
	}
	
	
	

}
