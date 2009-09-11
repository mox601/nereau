package cluster;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/* class Node = Cluster */
public class Node {
	
	private Node father;
	private List<Node> children;
	private String value; 
	//ranking del RankedTag corrispondente che il nodo rappresenta
	private double ranking;
	/* valore di somiglianza in corrispondenza del quale Ž stato creato il nodo (cluster) */
	private Float similarity;
	
	private LinkedList<Node> hierarchy;
	
	/* mi serve un id fittizio. ora ho anche left e right */
	private int idNode;
	
	/* id per la rappresentazione sul database con nestedsets */
	private int left;
	/**
	 * @return the left
	 */
	public int getLeft() {
		return left;
	}

	/**
	 * @param left the left to set
	 */
	public void setLeft(int left) {
		this.left = left;
	}

	/**
	 * @return the right
	 */
	public int getRight() {
		return right;
	}

	/**
	 * @param right the right to set
	 */
	public void setRight(int right) {
		this.right = right;
	}

	private int right;
	
	/**
	 * @return the idNode
	 */
	public int getIdNode() {
		return idNode;
	}

	/**
	 * @param idNode the idNode to set
	 */
	public void setIdNode(int idNode) {
		this.idNode = idNode;
	}

	private Tagtfidf centroid;
	public Node(String value) {
		this.value = value; 
		this.children = new LinkedList<Node>();
		
	}
	
	public Node(String value, Float similarity) {
		this.value = value;
		this.similarity = similarity;
		this.children = new LinkedList<Node>();

	}
	
	public Node(String value, Tagtfidf tagCentroid) {
		this.value = value;
//		this.similarity = similarity;
		this.centroid = tagCentroid;
		this.children = new LinkedList<Node>();
	}
	
	public Node(LinkedList<Node> clustersToMerge, double similarity) {
		/* gli passo solo i tag di cui deve calcolare la media */
		List<Tagtfidf> tagsToMerge = new LinkedList<Tagtfidf>();
		Float sim = new Float(similarity);
		this.setSimilarity(sim);
		
		/* devo settare gli ancestors 
		 * se ho passato due nodi che HANNO la gerarchia, 
		 * restituisco UNA gerarchia del nodo padre */
		
		if (clustersToMerge.getFirst().getHierarchy() != null && 
				clustersToMerge.getLast().getHierarchy() != null) {
			LinkedList<Node> mergedHierarchies = 
				this.mergeHierarchies(clustersToMerge.getFirst(), clustersToMerge.getLast());
			this.setHierarchy(mergedHierarchies);
		}
	
//		Node mergedClusterNode = new Node(value, tagCentroid);
		this.setChildren(clustersToMerge);
		/* questo nodo Ž il padre per tutti i cluster fusi */
		Iterator<Node> childrenIterator = this.getChildren().listIterator();
		/* il valore del nodo Ž calcolato sulla base del valore che 
		 * ha il Tagtfidf creato come fusione */
		
		while (childrenIterator.hasNext()) {
			Node currentChild = childrenIterator.next();
			/* ogni nodo deve consocere l'id del padre, per il salvataggio 
			 * sul database. 
			 * basta che ogni nodo conosca il suo id, 
			 * poi lo chiedo al father quale id ha.  */
			currentChild.setFather(this);
			/* aggiungi i tag da fondere alla lista */
			tagsToMerge.add(currentChild.getCentroid());
		}
		this.centroid = new Tagtfidf(tagsToMerge);
		
		/* valore preso dal Tagtfidf */
		this.value = this.centroid.getTag();
//		this.value = bufferValue.toString();
//		this.value = this.value.substring(0, this.value.length() - 1);
		
		
		/* l'id di un nodo lo si assegna quando si costruisce il clustering. 
		 * quando il clustering Ž completo, si assegnano gli id */
	}
	
	
	
	public static Node calculateFirstAncestor(LinkedList<Node> couple) {
		Node firstAncestor = null;
		
		Iterator<Node> firstIterator = couple.getFirst().getHierarchy().descendingIterator();

		/* parte dall'ultimo nodo della gerarchia del nodo 1 e cerca un nodo 
		 * uguale nella gerarchia del nodo 2 
		 * quando lo trova, mette quale valore di similarity? 
		 * quello del nodo nella gerarchia */
		
		boolean found = false;
		
		//se cerco l'ancestor di un nodo e la root, non trova mai l'antenato 
			
		
//		System.out.println("searching ancestor of: " + couple.getFirst().getValue()
//				+ " and " + couple.getLast().getValue());
		
		
//		System.out.println("with 1st hierarchy: " + couple.getFirst().getHierarchy().toString());
//		System.out.println("with 2nd hierarchy: " + couple.getLast().getHierarchy().toString());
		
		while (firstIterator.hasNext() && !found) {
			Iterator<Node> secondIterator = couple.getLast().getHierarchy().descendingIterator();
			Node a = firstIterator.next();
			
			while (secondIterator.hasNext() && !found) {
				Node b = secondIterator.next();
				//se i due nodi in lista sono uguali, 
				if ((b.getLeft() == a.getLeft()) && 
						(b.getRight() == a.getRight()) && 
						(b.getIdNode() == a.getIdNode()) ) {
					firstAncestor = b;
					found = true;
				} 
			} // secondIterator
			
		}// firstIterator
		
		return firstAncestor;
	}
	
	

	/* Override dei metodi equals e hashCode per il confronto tra oggetti */
	// TODO: cambiare se cambia la definizione dell'oggetto
	/* fonde le gerarchie dei due nodi */
	
	private LinkedList<Node> mergeHierarchies(Node first, Node second) {
		LinkedList<Node> mergedAncestorsList = new LinkedList<Node>();
		
		/* cerco a partire dal penultimo elemento di first il primo nodo in 
		 * comune con gli elementi di second 
		 * */
		
		//dalla fine!!
		Iterator<Node> firstIterator = first.hierarchy.descendingIterator();
		boolean found = false;
		
		while (firstIterator.hasNext() && found != true) {
			Node ancestorA = firstIterator.next();	
			int toIndex = second.hierarchy.size() - 1;
			Iterator<Node> secondIterator = second.hierarchy.descendingIterator();
			while (secondIterator.hasNext() && found != true) {
				Node ancestorB = secondIterator.next();
				if ( (ancestorA.left == ancestorB.left) && 
						(ancestorA.right == ancestorB.right) ) {
					/* rappresentano lo stesso antenato, il nodo per cui
					 * sto creando la lista degli antenati
					 * creo una sottolista dall'inizio fino a questo elemento, 
					 * che si trova a distanza toIndex */
					found = true;
					mergedAncestorsList = (LinkedList<Node>) ancestorB.getHierarchy().subList(0, toIndex);
//					System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
//					System.out.println("ho trovato l'antenato comune - id: " + 
//							ancestorA.idNode + " left: " + ancestorA.left + " right: " + ancestorA.right);
				}
				/* diminuisco il counter per calcolare l'indice della seconda lista */
				toIndex = toIndex - 1;
			} // second iterator
			
		} // first iterator
		
		
		return mergedAncestorsList;
	}

	private Tagtfidf calculateAverageTag(List<Node> clustersToMerge) {		
		List<Tagtfidf> tagsToMerge = new LinkedList<Tagtfidf>();
		
		/* aggiungi i tagtfidf in una lista */
		Iterator<Node> clustersIterator = clustersToMerge.iterator();
		while (clustersIterator.hasNext()) {
			tagsToMerge.add(clustersIterator.next().getCentroid());
		}
		Tagtfidf centroid = new Tagtfidf(tagsToMerge);
				
		return centroid;
		
	}
	

	
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
		String sets = "{" + this.getLeft() + ", "+ this.getRight() + "}";
		String id = String.valueOf(this.getIdNode());
		String description = this.getValue() + " (sim: " + 
							 this.getSimilarity() + ") " + id + " " + sets;
		return description;
	}
	
	
	
	/* pseudo code per il merge dei nodi (si parte da cluster con un solo tag e si 
	 * prosegue per i cluster composti da piœ tag) 
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
	 * 		// 1 e 2 per esempio) Ž <= similarity, non lo considero.
	 * 		// Se Ž maggiore, allora lo prendo e fondo i cluster 1 e 2 nel cluster X
	 * 		// con valore di mergeSimilarity = similarity, aggiornando il valore del 
	 * 		// centroide di X come somma di tutt i tag che include.   
	 * 		
	 * 		}
	 * 
	 * 	//diminuisci somiglianza e prosegui
	 * 	similarity = similarity - 0.15;
	 * 	
	 * }
	 * */
	
	

	public boolean hasSibling(Node c) {
		return this.getFather().getChildren().contains(c);
		
	}


	public boolean isLeaf() {
		return this.children.isEmpty();
	}

	public void setCentroid(Tagtfidf centroid) {
		this.centroid = centroid;
	}

	public Tagtfidf getCentroid() {
		return centroid;
	}

	/**
	 * @return the hierarchy
	 */
	public LinkedList<Node> getHierarchy() {
		return hierarchy;
	}

	/**
	 * @param hierarchy the hierarchy to set
	 */
	public void setHierarchy(LinkedList<Node> hierarchy) {
		this.hierarchy = hierarchy;
	}

	public double getRanking() {
		return this.ranking;
	}

	public void setRanking(double ranking) {
		this.ranking = ranking;
		
	}


	
	
	

}
