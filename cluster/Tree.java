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
