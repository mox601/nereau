package cluster;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public class Node {
	
	private Node father;
	private List<Node> children;
	// TODO: cambiare in generic o in un tipo che mi serve
	// gli alberi e i nodi mi servono per modellare l'algoritmo di clustering
	private String value; 
	
	
	public Node(String value) {
		this.value = value; 
		this.children = new LinkedList<Node>();
		
	}
	
	
	/* Override dei metodi equals e hashCode per il confronto tra oggetti */
	// TODO: cambiare se cambia la definizione dell'oggetto
	
	public boolean equals(Object obj) {
		
		if (this == obj) return true;
		if((obj == null) || (obj.getClass() != this.getClass())) return false;
		Node nodeToCompare = (Node)obj;
		return value == nodeToCompare.getValue(); 
		// && (data == test.data || (data != null && data.equals(test.data)));		
	}
	
	public int hashCode() {
		return this.getValue().hashCode();
	}
	
	
	
	
	public boolean addChild(Node newChild) {
		newChild.setFather(this);
		return this.children.add(newChild);
	} 
	
	// TODO
	public boolean removeChild(Node toRemove) {
		
		return false; 
	}
	
	
	
	
	public void setChildren(List<Node> children) {
		this.children = children;
	}
	public List<Node> getChildren() {
		return children;
	}
	public void setFather(Node father) {
		this.father = father;
	}
	public Node getFather() {
		return father;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getValue() {
		return value;
	}
	
	
	public boolean hasFather(Node father) {
		return this.getFather() == father;
		
	}
	

	public boolean hasChild(Node child) {
		return this.getChildren().contains(child);
	}
	
	public boolean hasDescendant(Node descendant) {
		boolean found = false;
		found = this.getChildren().contains(descendant);
		if(!found) {
			Iterator<Node> childIterator = this.getChildren().iterator();
			while(childIterator.hasNext() && !found) {
				found = childIterator.next().hasDescendant(descendant);
			} 

		}
		return found;
	}
	
	
	/* verifica se un nodo A ha, tra gli antenati, un nodo B */
	public boolean hasAncestor(Node ancestor) {
		boolean found = false; 
		Node currentNode = this;
		
		while(!found || (currentNode.getFather() == null)) {
			Node currentFather = currentNode.getFather();
			/* TODO: redefine equivalence between Nodes? */
			if (currentFather == ancestor) {
				found = true; 
			} else {
				currentNode = currentFather;
			}
		}
		return found;
	}


	public boolean hasSibling(Node c) {
		return this.getFather().getChildren().contains(c);
		
	}


	public boolean isLeaf() {
		return this.children.isEmpty();
	}


	
	
	

}
