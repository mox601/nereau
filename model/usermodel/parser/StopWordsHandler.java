package model.usermodel.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import util.LogHandler;
import util.ParameterHandler;


public abstract class StopWordsHandler {
	
	private static Set<String> stopWords;

	public static Set<String> getStopWords() {
		
		Logger logger = LogHandler.getLogger(StopWordsHandler.class.getName());
		
		if(stopWords==null) {
			stopWords = new HashSet<String>();
			String path = ParameterHandler.STOPWORDS_FILE_PATH;
			File file = new File(path);
			Scanner scanner = null;
			try {
				scanner = new Scanner(file);
			}
			catch(FileNotFoundException e) {
				logger.info("ATTENZIONE: errore nell'accesso al file");
				return stopWords;
			}
			Pattern pattern = Pattern.compile("\\W+");
			scanner.useDelimiter(pattern);
			while(scanner.hasNext()) {
				stopWords.add(scanner.next());
			}
		}
		return stopWords;
	}

}
