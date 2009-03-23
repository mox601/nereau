package cluster.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

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
		 * frequenza con cui l'url Ž stato taggato con il tag in questione. 
		 * ƒ solo per scopi di test. */
		blogUrl1 = "http://www.notcot.org/";
		blogUrl1Freq = new Integer(7200);
		tagUrlsMap1.put(blogUrl1, blogUrl1Freq);
		blogUrl2 = "http://www.noupe.com/";
		blogUrl2Freq = new Integer(3277);
		tagUrlsMap1.put(blogUrl2, blogUrl2Freq);
		blogUrl3 = "http://www.formfiftyfive.com/";
		blogUrl3Freq = new Integer(1929);
		tagUrlsMap1.put(blogUrl3, blogUrl3Freq);
		tag1 = new Tagtfidf(blogTag, tagUrlsMap1);
		/* ho inserito tutti i (chiave,valore) nella Map. 
		 * Ora il tag Ž rappresentato da 3 url, con la loro frequenza */
		
		
		
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
		designUrl3Freq = new Integer(1345);
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
		tag1.addUrlOccurrency(oldUrl);
		assertEquals(new Integer(1 + oldFrequency), tag1.getUrlFrequency(oldUrl));

		
		
		/* aggiunge al tag un'occorrenza di un sito nuovo */
		String newUrl = "http://www.abkhfjdsohgos.it/";
		/* l'url aggiunto in questo caso non deve esistere prima */
		assertNull(tag1.getUrlFrequency(newUrl));
		tag1.addUrlOccurrency(newUrl);
		assertEquals(new Integer(1), tag1.getUrlFrequency(newUrl));
			
	}
	
	
	@Test
	public void testCosineSimilarity() {
		/* confronto i due tag blog (tag1) e design(tag2) */
		
		/* la coseno somiglianza Ž simmetrica */
		Double cosSim12 = tag1.compareToTag(tag2);
		Double cosSim21 = tag2.compareToTag(tag1);
		assertTrue(cosSim12.equals(cosSim21));
		
		
	} 
	
	@Test
	public void testModule() {
		System.out.println("tag1 module " + tag1.getModule());
	}
	
	
	

	@Test
	public void testCompareToTag() {
//		fail("Not yet implemented");
	}

}
