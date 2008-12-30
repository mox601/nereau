package util;

import model.RankedTag;

public class ParameterHandler {
	
	//persistence
	public static final String DATABASE_CONFIG = "files/database_config";
	
	public static final String DB_URI = "jdbc:postgresql://localhost/nereau_backup";
	public static final String DB_USERNAME = "postgres";
	public static final String DB_PASSWORD = "prova";

	//logging
	public static final String GENERAL_LOG_FILE_PATH = "logs/" + System.currentTimeMillis() + ".log";
	public static final String PERSISTENCE_LOG_FILE_PATH = "logs/" + System.currentTimeMillis() + "-persistence.log";
	public static final String EXPANSION_LOG_FILE_PATH = "/home/olbion/test/expanded_queries.log";
	
	//null tag
	public static final RankedTag NULL_TAG = new RankedTag("no_tag",1.0);

	//query expansion
	public static final int MAX_EXPANSION_TERMS = 2;
	public static final int MAX_EXPANSION_TAGS = 5;
	public static final double MIN_EXPANSIONTAGS_RELATIVE_VALUE = 0.3;
	public static final int MAX_QUERY_TERMS = 6;
	public static final double MIN_QUERYEXPANDER_ABSOLUTE_VALUE = 0.4;
	public static final double MIN_QUERYEXPANDER_RELATIVE_VALUE = 0.5;
	public static final boolean LIMIT_EXPANSION_TERMS = true;

	//user model
	public static boolean MAX_OCCURRENCE_VALUE_FOR_QUERY_TERMS = false;
	public static boolean MAX_FOR_QUERY_TERMS_ONLY_IF_PRESENT = false;
	public static final double QUERY_TERMS_WEIGHT_FOR_MAX = 0.2;
	public static final int MAX_KEYWORDS = 20;
	public static final double SUBURL_RELEVANCE_COEFFICIENT = 0.9;
	public static final double MIN_TAGFINDER_RELATIVE_VALUE = 0.4;
	public static final String STOPWORDS_FILE_PATH = "files/stopwords";
	public static final int URL_TIMEOUT = 5000;

	//old tests
	public static final String TEST_QUERY_STRING = "amazon";
	public static final String TEST2_PATH = "temp/test2.html";


}
