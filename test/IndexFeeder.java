package test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;

public class IndexFeeder {
	
	public static void createIndex() throws CorruptIndexException, LockObtainFailedException, IOException {
		
		File indexDir = new File(TestParams.index_dir);
		File dataDir = new File(TestParams.data_dir);
		File noiseDir = new File(TestParams.noise_dir);
		
		IndexWriter writer = new IndexWriter(indexDir, new StemmingAnalyzer(), true);
		writer.setUseCompoundFile(true);
		
		
		File[] testDirs = dataDir.listFiles();
		
		for(File testDir: testDirs) {
			
			String testName = testDir.getName();
			
			if(!testDir.isDirectory())
				continue;
			
			for(File tagGroupDir: testDir.listFiles()) {
				
				if(!tagGroupDir.isDirectory())
					continue;
				
				String tagGroup = tagGroupDir.getName();
				File testSetDir = new File(tagGroupDir,TestParams.test_dir);
				
				//per ogni documento nella directory
				for(File fsDoc: testSetDir.listFiles()) {
					
					//crea un documento e setta i Field da indicizzare o meno
					Document doc = new Document();
					doc.add(new Field("contents", new FileReader(fsDoc)));
					doc.add(new Field("filename", fsDoc.getName(), Field.Store.YES, Field.Index.NO));
					doc.add(new Field("test", testName, Field.Store.YES, Field.Index.NO));
					doc.add(new Field("taggroup", tagGroup, Field.Store.YES, Field.Index.NO));
					writer.addDocument(doc);
					
				}
				
				System.out.println(tagGroupDir.getAbsolutePath() + " added...");
				
			}
			
		}
		
		
		File[] noiseDirs = noiseDir.listFiles();
		
		for(File dir: noiseDirs) {
			
			if(!dir.isDirectory())
				continue;
			
			for(File noiseDoc: dir.listFiles()) {
				
				Document doc = new Document();
				doc.add(new Field("contents", new FileReader(noiseDoc)));
				doc.add(new Field("filename", noiseDoc.getName(), Field.Store.YES, Field.Index.NO));
				doc.add(new Field("test", dir.getName(), Field.Store.YES, Field.Index.NO));
				doc.add(new Field("taggroup", "(noise)", Field.Store.YES, Field.Index.NO));
				writer.addDocument(doc);
				
			}
			
			System.out.println(dir.getAbsolutePath() + " added...");
			
		}
		
		writer.optimize();
		writer.close();
		
	}
	
	
	
	public static void search(String q) throws IOException, ParseException {

		Directory fsDir = FSDirectory.getDirectory(new File(TestParams.index_dir));
		IndexSearcher is = new IndexSearcher(fsDir);
		QueryParser parser = new QueryParser("contents", new StemmingAnalyzer());
		Query query = parser.parse(q);
		
		long start = new Date().getTime();
		Hits hits = is.search(query);
		long end = new Date().getTime();
		
		System.out.println("Found " + hits.length() + " document" + (hits.length()==1 ? "" : "s") 
				+ " (in " + (end - start) + " milliseconds) matching query '" + query + "':");
		
		Iterator hi = hits.iterator();
		while(hi.hasNext()) {
			Hit hit = (Hit)hi.next();
			System.out.println("(" + hit.getDocument().get("taggroup") + ") " + hit.getDocument().get("filename"));
		}
		
	}




	public static void main(String[] args) throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {
		
		
		//costruisce l'index per le cartelle nei file dei parametri
		createIndex();
		
		//search("cancer");
		
	}
	
}
