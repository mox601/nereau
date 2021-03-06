package server;

import java.util.logging.Logger;

import persistence.PersistenceException;
import persistence.postgres.TreeDAOPostgres;
import util.LogHandler;
import cluster.ClusterBuilder;
import cluster.Tree;

public class ClustererThread implements Runnable {
	
	private TreeDAOPostgres treeHandler;
	private ClusterBuilder clusterer;

	@Override
	public void run() {
		
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		/* questo thread aspetta un tot di tempo e poi fa il clustering gerarchico  
		 * di tutti i tag contenuti nel db */
	
		while (true) {
			
			logger.info("INIZIO DEL CLUSTERING DEI TAG E SALVATAGGIO");
			
			ClusterBuilder clusterer = this.clusterer.getInstance();
			/* retrieve tags and build the singleton clusters to be clustered */
			clusterer.retrieveAllTagsFromDatabase();
			clusterer.buildClusters();
//			Tree actualClustering = clusterer.getActualClustering();
			
			//stampo il clustering ottenuto
//			logger.info("clustering ottenuto: " + clusterer.actualClusteringToString());


			if (clusterer.getActualClustering() != null) {
				logger.info("SAVING CLUSTERING ON DATABASE");
				clusterer.saveActualClustering();
			}

			logger.info("FINE DEL CLUSTERING DEI TAG E SALVATAGGIO SUL DATABASE");

			
			try {
				int pause = 40000;
				logger.info("waiting " + pause + "msec");
				Thread.sleep(pause);
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
	}

}
