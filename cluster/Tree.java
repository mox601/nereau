package cluster;
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
	public LinkedList<LinkedList<Node>> cutTreeAtSimilarity(double similarity) {
		// restituisce una linkedlist di linked list di node
		LinkedList<LinkedList<Node>> clustering = new LinkedList<LinkedList<Node>>();
		
		/* 
		 * TODO: clutering da un albero 
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
		
		return clustering;
		
	}
	
	void visitNodeAndGetClustering(Node node, double similarity, 
			LinkedList<LinkedList<Node>> clustering) {
		/* visita in preordine */
		
		if (node.getSimilarity().doubleValue() < similarity) {
			
			/* se Ž una foglia, con similarity piœ bassa di quella 
			 * specificata, comunque aggiungi la lista col nodo singolo */
			if (node.isLeaf()) {
				LinkedList<Node> leaf = new LinkedList<Node>();
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
			System.out.println("similarity corrente: " + node.getSimilarity());
			LinkedList<Node> leaves = this.getLeaves(node);
			clustering.add(leaves);
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
	
}
