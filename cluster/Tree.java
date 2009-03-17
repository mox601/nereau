package cluster;
import java.util.LinkedList;
import java.util.List;


public class Tree {
	
	/* modella il clustering gerarchico, Ž l'albero che contiene i cluster e 
	 * i valori ai quali ogni cluster Ž stato accorpato in un nodo (=un cluster)*/

	private Node root;

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
	
}
