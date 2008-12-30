package model.usermodel.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.htmlparser.Parser;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.TextExtractingVisitor;

import util.LogHandler;
import util.Stemmer;


public class URLParser {
	
	public List<String> parse(File doc, Stemmer stemmer) throws FileNotFoundException, ParserException {
		
		Scanner scanner = new Scanner(doc);
		StringBuffer textInPage = new StringBuffer();
		while(scanner.hasNext())
			textInPage.append(scanner.next() + ' ');
		
		return this.parse(textInPage.toString(), stemmer);
		
	}
	
	public List<String> parse(URLConnection urlConnection, Stemmer stemmer) throws ParserException {
		
		Parser parser = new Parser(urlConnection);
		
		//1: html-parse (using external libraries)
		TextExtractingVisitor visitor = new ContentExtractingVisitor();
		try {

			parser.visitAllNodesWith(visitor);
		} catch (ParserException e) {
			throw new ParserException();
		}
		
		String textInPage = visitor.getExtractedText();
		
		return this.parse(textInPage, stemmer);
		
	}
	
	public List<String> parse(String textInPage, Stemmer stemmer) throws ParserException {
		
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		

		logger.info("il testo è stato estratto. ora ci si lavora sopra...");
		
		//2: normalize extracted text
		Pattern pattern = Pattern.compile("\\W+");
		Scanner scanner = new Scanner(textInPage);
		scanner.useDelimiter(pattern);
		List<String> words = new LinkedList<String>();
		while(scanner.hasNext()) {
			String nextWord = scanner.next().toLowerCase();
			words.add(nextWord);
		}
		
		logger.info("è stata creata la lista di termini.");
		
		//3: remove stop words
		Set<String> stopwords = StopWordsHandler.getStopWords();
		words.removeAll(stopwords);
		
		logger.info("sono state rimosse le stopwords.");
		
		//4: stem remaining words
		List<String> stemmedWords = stemmer.stem(words);
		
		//5: return result
		return stemmedWords;
	}

}
