package cluster;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public class Node {
	
	private Node father;
	private List<Node> children;
	// gli alberi e i nodi mi servono per modellare l'algoritmo di clustering
	private String value; 
	/* per l'algoritmo di clustering */
	/* valore di somiglianza in corrispondenza del quale � stato creato il nodo (cluster) */
	private Float similarity;
	
	/* contiene all'inizio solo un tag, poi i nodi si fondono e contengono sempre pi� 
	 * tags, fino a contenerli tutti */
	private List<Tagtfidf> clusterTags; 
	
	public Node(String value) {
		this.value = value; 
		this.children = new LinkedList<Node>();
		
	}
	
	public Node(String value, float similarity) {
		this.value = value;
		this.similarity = similarity;
	}
	
	
	/* Override dei metodi equals e hashCode per il confronto tra oggetti */
	// TODO: cambiare se cambia la definizione dell'oggetto
	
	public boolean equals(Object obj) {
		
		if (this == obj) return true;
		if((obj == null) || (obj.getClass() != this.getClass())) return false;
		Node nodeToCompare = (Node)obj;
		return value == nodeToCompare.getValue() && (this.similarity == nodeToCompare.getSimilarity()); 
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
	
	
	public void setSimilarity(Float similarity) {
		this.similarity = similarity;
		
	}
	
	public Float getSimilarity() {
		return this.similarity;
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

	
	public String toString() {
		/* returns a string description of node */
		String description = this.value + "-" + this.getSimilarity();
		return description;
	}
	
	
	
	/* pseudo code per il merge dei nodi (si parte da cluster con un solo tag e si 
	 * prosegue per i cluster composti da pi� tag) 
	 * 
	 * (init: similarity = 1.0; actualClusters)
	 * 
	 * Tree mergeClusters(List<Tree> actualClusters, Float similarity) {
	 *		// posso fare a meno di una delle due condizioni?
	 * 	if ((similarity == 0.0) || (actualClusters.length() == 1)) {
	 * 		//termina l'algoritmo, restituisci il primo (e unico) elemento di actualClusters
	 * 		return actualClusters.first();
	 * 	} else {
	 * 		//calcola la distanza tra tutti i cluster presenti in actualCluster
	 * 		// se sono 4: dist12, dist13, dist14, dist23, dist24, dist34. 
	 * 		// tutte le permutazioni, insomma. 
	 * 		// se il valore di somiglianza che ho ottenuto (tra i cluster 
	 * 		// 1 e 2 per esempio) � <= similarity, non lo considero.
	 * 		// Se � maggiore, allora lo prendo e fondo i cluster 1 e 2 nel cluster X
	 * 		// con valore di mergeSimilarity = similarity, aggiornando il valore del 
	 * 		// centroide di X come somma di tutt i tag che include.   
	 * 		
	 * 		}
	 * 
	 * 	//diminuisci somiglianza e prosegui
	 * 	similarity = similarity - 0.15;
	 * 	
	 * }
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * */
	
	

	public boolean hasSibling(Node c) {
		return this.getFather().getChildren().contains(c);
		
	}


	public boolean isLeaf() {
		return this.children.isEmpty();
	}


	
	
	

}
