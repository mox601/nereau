package cluster;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;


public class Tree {

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
		/* examinate list of root's children */
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
	
}
