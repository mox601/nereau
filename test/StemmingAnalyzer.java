package test;

import java.io.Reader;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class StemmingAnalyzer extends Analyzer {
    
	public final TokenStream tokenStream(String fieldName, Reader reader) {
        
		return new PorterStemFilter(new StandardTokenizer(reader));
      
	}
}