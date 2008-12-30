package util;

import java.util.logging.*;



public class LogHandler {
	
	private static Handler general_instance;
	private static Handler expansion_instance;
	//private static Handler persistence_instance;
	
	public static Logger getLogger(String loggerName) {
		Logger logger = null;
		if(loggerName.startsWith("persistence")) {
			logger = Logger.getLogger("persistence_logger");
			/*if(persistence_instance==null) {
				try {
					persistence_instance = new FileHandler(ParameterHandler.PERSISTENCE_LOG_FILE_PATH);
					logger.addHandler(persistence_instance);
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}//*/
		} //model.queryexpansion.QueryExpander expand
		else if(loggerName.startsWith("model.queryexpansion.QueryExpander")) {
			logger = Logger.getLogger("expansion_logger");
			if(expansion_instance==null) {
				try {
					//expansion_instance = new FileHandler(ParameterHandler.EXPANSION_LOG_FILE_PATH);
					//logger.addHandler(expansion_instance);
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		else {
			logger = Logger.getLogger("general_logger");
			if(general_instance==null) {
				try {
					//general_instance = new FileHandler(ParameterHandler.GENERAL_LOG_FILE_PATH);
					//logger.addHandler(general_instance);
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		logger.setLevel(Level.INFO);
		
		return logger;
	}

}
