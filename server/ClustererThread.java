package server;

import persistence.PersistenceException;
import persistence.postgres.TreeDAOPostgres;
import cluster.ClusterBuilder;
import cluster.Tree;

public class ClustererThread implements Runnable {
	
	private TreeDAOPostgres treeHandler;
	private ClusterBuilder clusterer;

	@Override
	public void run() {

		/* questo thread aspetta un tot di tempo e poi fa il clustering gerarchico  
		 * di tutti i tag contenuti nel db */
		
		
		
		
		while (true) {
			System.out.println("Eseguo il clustering dei tag e salvo sul database");
			
			ClusterBuilder clusterer = this.getClusterBuilder();
			/* retrieve tags and build the singleton clusters to be clustered */
			clusterer.retrieveAllTagsFromDatabase();
			clusterer.buildClusters();
			Tree actualClustering = clusterer.getActualClustering();
			
			try {
				this.treeHandler.save(actualClustering);
			} catch (PersistenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			
			
			
			
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
	}

}
