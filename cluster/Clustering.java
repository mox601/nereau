package cluster;

import java.util.HashSet;

public class Clustering {
	private HashSet<HashSet<Node>> clustering;
	private double varianza;
	private int clusters;
	private double nodesPerClusters;
	private int totalNodes;
	private double devStandard;	
	
	
	
	/* TODO: due cluster sono uguali se: 
	 * hanno stesso numero di clusters
	 */
	
//	public boolean equals(Clustering clusteringTwo) {
//		boolean equals = true;
//		
//		for (HashSet<Node> cluster: clustering) {
//			if (!clusteringTwo.getClustering().contains(cluster)) {
//				equals = false;
//			}
//			
//		}
//		
//		
//		return equals;
//		
//	}
	
	/**
	 * @return the devStandard
	 */
	public double getDevStandard() {
		return devStandard;
	}


	/**
	 * @param devStandard the devStandard to set
	 */
	public void setDevStandard(double devStandard) {
		this.devStandard = devStandard;
	}

	public Clustering() {
		this.clustering = new HashSet<HashSet<Node>>();
		this.clusters = clustering.size();
		this.totalNodes = 0;
	}

	public Clustering(HashSet<HashSet<Node>> setOfClusters) {
		this.clustering = setOfClusters;
		this.clusters = clustering.size();
		
		this.totalNodes = 0;
		for(HashSet<Node> cluster: clustering){
			this.totalNodes += cluster.size();
		}
		
		this.nodesPerClusters  = this.totalNodes / this.clusters;
		
		double distance = 0.0;
		
		for (HashSet<Node> cluster: clustering) {
			//calcolo la distanza dalla media, poi al quadrato
			//e sommo in un accumulatore
			distance += Math.pow(cluster.size() - this.nodesPerClusters, 2);
		}
		//divido per il numero di clusters e ottengo la varianza
		this.varianza = distance / this.nodesPerClusters;
		//radice quadrata ed ho la deviazione standard
		this.devStandard = Math.sqrt(varianza);
//		System.out.println("var calcolata: " + this.varianza);
//		System.out.println("var calcolata: " + this.devStandard);

		
	}
	
	public String toString() {
		String clusterString = "";
		StringBuffer buffer = new StringBuffer();
		for(HashSet<Node> cluster: this.getClustering()) {
			buffer.append("< ");
			for(Node node: cluster) {
				buffer.append(node.toString() + ", ");
			}
			buffer.append("> \n");
		}
		clusterString = buffer.toString();
		return clusterString;
	}


	/**
	 * @return the clustering
	 */
	public HashSet<HashSet<Node>> getClustering() {
		return clustering;
	}


	/**
	 * @param clustering the clustering to set
	 */
	public void setClustering(HashSet<HashSet<Node>> clustering) {
		this.clustering = clustering;
	}


	/**
	 * @return the varianza
	 */
	public double getVarianza() {
		return varianza;
	}


	/**
	 * @param varianza the varianza to set
	 */
	public void setVarianza(double varianza) {
		this.varianza = varianza;
	}


	/**
	 * @return the clusters
	 */
	public int getClusters() {
		return clusters;
	}


	/**
	 * @param clusters the clusters to set
	 */
	public void setClusters(int clusters) {
		this.clusters = clusters;
	}


	/**
	 * @return the nodesPerClusters
	 */
	public double getNodesPerClusters() {
		return nodesPerClusters;
	}


	/**
	 * @param nodesPerClusters the nodesPerClusters to set
	 */
	public void setNodesPerClusters(double nodesPerClusters) {
		this.nodesPerClusters = nodesPerClusters;
	}


	/**
	 * @return the totalNodes
	 */
	public int getTotalNodes() {
		return totalNodes;
	}


	/**
	 * @param totalNodes the totalNodes to set
	 */
	public void setTotalNodes(int totalNodes) {
		this.totalNodes = totalNodes;
	}

}
