package cluster;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;


public class Tree {
	
	/* modella il clustering gerarchico, Ž l'albero che contiene i cluster e 
	 * i valori ai quali ogni cluster Ž stato accorpato in un nodo (=un cluster)*/

	private Node root;

	private IdGenerator nestedSetsIdGenerator;
	private IdGenerator idGenerator;

	public Tree(Node node) {
		this.setRoot(node);
	}

	public Tree() {
		// TODO Auto-generated constructor stub
	}

	public void setRoot(Node root) {
		this.root = root;
	}

	public Node getRoot() {
		return root;
	}
	
	public boolean contains(Node b) {
		boolean found = false;
		/* from root */
		Node currentNode = this.getRoot();
		/* examinate the list of root's children */
		found = (currentNode == b);
		if (!found) {
			found = currentNode.hasDescendant(b);
		}
		return found;
	}
	
	public List<Node> pathToRoot(Node child) {
		List<Node> path = null;
		/* list aggiunge in coda */
		/* aggiungi il nodo corrente */
		
		if (this.contains(child)) {
			path = new LinkedList<Node>();
			path.add(child);
			/* prosegui con il padre */
			Node actualFather = child.getFather();
			while (actualFather != null) {
				path.add(actualFather);
				actualFather = actualFather.getFather();
			}
		}
		
		return path;
	}
	/* TODO: occhio al null */
	public Tree getSubTree(Node child) {	
		if (this.contains(child)) {
			return new Tree(child);
		}
		else return null;
	}

	public Tree mergeTreesWithNewRoot(Tree firstTree, Node newRoot) {
		List<Node> oldTrees = new LinkedList<Node>();
		/* add children to the new root node */
		oldTrees.add(this.getRoot());
		oldTrees.add(firstTree.getRoot());
		newRoot.setChildren(oldTrees);	
		Tree mergedTree = new Tree(newRoot);
		return mergedTree;
	}

	public List<Node> getLeaves() {
		List<Node> leaves = new LinkedList<Node>(); 
		Node actualNode = this.getRoot();
		List<Node> children = actualNode.getChildren();
		if (actualNode.isLeaf()) {
			leaves.add(actualNode);
		} else {
			for(Node child : children) {
				Tree subTree = this.getSubTree(child);
				leaves.addAll(subTree.getLeaves());
			}
		}
		return leaves;
	}
	
	/* metodo duplicato, per ottenere le foglie a partire da un nodo diverso 
	 * dal root */
	
	public LinkedList<Node> getLeaves(Node node) {
		LinkedList<Node> leaves = new LinkedList<Node>(); 
		Node actualNode = node;
		if (actualNode.isLeaf()) {
			leaves.add(actualNode);
		} else {
			List<Node> children = actualNode.getChildren();
			for(Node child : children) {
				Tree subTree = this.getSubTree(child);
				leaves.addAll(subTree.getLeaves());
			}
		}
		return leaves;
	}
	
	
	/* restituisce il valore di similarity che si deve usare per 
	 * generare un cluster di qualit‡ buona. Con quale strategia? */
	double getCutSimilarity() {
		double similarity = 0.0;
		
		
		
		return similarity;
	}
	
	
	
	
	
	
	/* da un albero, restituisce il clustering esatto ottenuto tagliando 
	 * la gerarchia ad un certo valore di similarity */
	public Clustering cutTreeAtSimilarity(double similarity) {
		
		HashSet<HashSet<Node>> clustering = new HashSet<HashSet<Node>>();
		/* 
		 * TODO: clustering da un albero 
		 * avendo una gerarchia, devo decidere a che livello tagliare e
		 * ottenere il clustering effettivo dal quale accorpare poi i tag
		 * di ogni cluster. 
		 * Questa funzione deve restituire una lista di clusters (LinkedList<Node>). 
		 * In base alla similarity scelta, l'algoritmo scende nella gerarchia: 
		 * incontrer‡  dei nodi, ognuno con un valore di similarity. 
		 * Se il valore del nodo Ž < di quello scelto, prosegue
		 * altrimenti, crea una lista di nodi e ci mette le foglie del nodo 
		 * che sta visitando. 
		 * */
		
		visitNodeAndGetClustering(this.getRoot(), similarity, clustering);
		
		HashSet<HashSet<Node>> clustersSet = new HashSet<HashSet<Node>>();
		
		for (HashSet<Node> cluster: clustering) {
			HashSet setNode = new HashSet<Node>(cluster);
			clustersSet.add(setNode);
		}
		
		Clustering clusteringObject = new Clustering(clustersSet);

		
		return clusteringObject;
		
	}
	
	void visitNodeAndGetClustering(Node node, double similarity, 
			HashSet<HashSet<Node>> clustering) {
		/* visita in preordine */
		
		if (node.getSimilarity().doubleValue() < similarity) {
			
			/* se Ž una foglia, con similarity piœ bassa di quella 
			 * specificata, comunque aggiungi la lista col nodo singolo */
			if (node.isLeaf()) {
				HashSet<Node> leaf = new HashSet<Node>();
				leaf.add(node);
				clustering.add(leaf);
			}
			
			/* passa a visitare i figli, 
			 * questo nodo Ž troppo generale per la similarity scelta.  */
			for (Node child: node.getChildren()) {
				visitNodeAndGetClustering(child, similarity, clustering);
			}
		} else {
			/* la similarity Ž = o > di quella scelta, 
			 * costruisco UNA SOLA (?) lista con le foglie di questo nodo */
			/* TODO: caso limite, se similarity Ž = a quella attuale. */
//			System.out.println("similarity corrente: " + node.getSimilarity());
			LinkedList<Node> leavesList = this.getLeaves(node);
			HashSet<Node> leavesSet = new HashSet(leavesList);
			clustering.add(leavesSet);
		}
		return;
	}
	
		
	public String toString() {
		String treeString = "";
		/* visita in preordine e stampa i nodi */
		StringBuffer buffer = new StringBuffer();
		visitNodeAndPrintToString(root, 0, buffer);
		treeString = buffer.toString();
		return treeString;
	}

	private void visitNodeAndPrintToString(Node node, int level, StringBuffer buffer) {
		StringBuffer indentBuffer = new StringBuffer();
		
		if (node.getFather() != null) {
			indentBuffer.append("\n");
			for (int i = 0; i <= level; i++) {
				indentBuffer.append("	");
			}	
		}
		
		/* stampo il contenuto del nodo */
		
//		String nodeString = indentBuffer.toString() + node.getValue() + " {" + node.getLeft() + ", " + 
//							node.getRight() +  "}";
		
		buffer.append(indentBuffer.toString() + node.toString());
		
		for (Node child: node.getChildren()) {
			visitNodeAndPrintToString(child, level + 1, buffer);
		}
		
	}
	
	
	
	public void assignIds() {
		/* assegna gli id a tutti i nodi dell'albero visitandolo in preordine */
		nestedSetsIdGenerator = new IdGenerator();
		idGenerator = new IdGenerator();		
		int startingNestedSetsId = nestedSetsIdGenerator.getId();
		int rowId = idGenerator.getId();
		Node startingNode = this.getRoot();
		this.visitAndAssignIds(startingNode, rowId, startingNestedSetsId);
		
	}

	/* visita in preordine e assegnazione degli id. ora si assegnano secondo 
	 * la struttura del database nested sets */
	private void visitAndAssignIds(Node node, int rowId, int nestedSetId) {
		//vecchio id
		node.setIdNode(rowId);
		System.out.println("id " + rowId + " assegnato al nodo: " + node.getValue());
		// left 
		node.setLeft(nestedSetId);
		for (Node child: node.getChildren()) {
			visitAndAssignIds(child, idGenerator.getId(), nestedSetsIdGenerator.getId());
		}
		//right dopo aver assegnato id a tutto il sotto albero 
		node.setRight(nestedSetsIdGenerator.getId());
		System.out.println("nodo: " + node.getValue() + " left: " + node.getLeft() + " right: " + node.getRight());
		
	}

	public Clustering calculateClusteringByMean() {
		/* itera da similarity = 1 a similarity = 0.1, 
		 * calcola per ogni clustering ottenuto un valore, 
		 * combinazione lineare di dimensioni, media e varianza di tutti i clusters */
		
		HashSet<HashSet<Node>> clustering = new HashSet<HashSet<Node>>();
		double similarity = 1.0;
		HashSet<Clustering> clusteringsSet = new HashSet<Clustering>();
		
		//calcola tutti i clustering possibili
		while (similarity > 0.0) {	
			Clustering actualClustering = this.cutTreeAtSimilarity(similarity);
			if (!clusteringsSet.contains(actualClustering)) {
				clusteringsSet.add(actualClustering);
			}			
			similarity = similarity - 0.15; 
		}
		
		double maxDevStandard = Double.MAX_VALUE;
		
		Clustering optimalClustering = new Clustering();
		/* ricerca il clustering ottimo, con minima deviazione standard */
		for (Clustering actualClustering: clusteringsSet) {
			System.out.print("esamino clustering: \n" + actualClustering.toString());
			System.out.println("che ha devStandard = " + actualClustering.getDevStandard());
			System.out.println("e media di nodi per clusters = " + actualClustering.getNodesPerClusters());
			if (actualClustering.getDevStandard() < maxDevStandard) {
				maxDevStandard = actualClustering.getDevStandard();
				optimalClustering = actualClustering;
			}
		
		}
		
//		System.out.println("clustering ottimo by mean: ");
//		System.out.println(optimalClustering.toString() + " " + optimalClustering.getDevStandard());
		
		Clustering clusteringObj = null;
		
		
		return optimalClustering;
	}
	
}
