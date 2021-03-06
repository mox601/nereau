package persistence.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import model.RankedTag;

import cluster.ClusterCombinator;
import cluster.Node;
import cluster.Tagtfidf;
import cluster.Tree;
import persistence.PersistenceException;
import java.sql.SQLException;
import persistence.TreeDAO;
import util.LogHandler;

public class TreeDAOPostgres implements TreeDAO {



	/* � l'antenato trovato durante la ricerca dell'antenato comune 
	 * con similarity pi� alta. diventa il padre della coppia di nodi */
	private Node ancestorFound;



	/* da cambiare, devo salvare una struttura di nested sets */
	@Override
	public void save(Tree clustering) throws PersistenceException {
		/* salva il clustering (sotto forma di Tree) sul database, 
		 * in una tabella che rappresenti la struttura gerarchica
		 */
		
		/* deve essere una sola transazione atomica, altrimenti 
		 * potrebbero esserci accessi alla tabella clusters 
		 * quando si trova in uno stato inconsistente 
		 * SET TRANSACTION */
		
		Node root = clustering.getRoot();
		
		Logger logger = LogHandler.getLogger(this.getClass().getName());
//		logger.info("comincio la visita dell'albero e lo salvo");
				
		DataSource dataSource = DataSource.getInstance();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = null;
		try {
			/* cancella tutte le tuple della tabella clusters_sets, 
			 * tanto non � rilevante modificarla */
			//DELETE FROM CLUSTERS;
			
			String deleteQuery = this.prepareStatementForDelete();
			statement = connection.prepareStatement(deleteQuery);
			statement.executeUpdate();
//			logger.info("DELETED all clusters_sets rows: " + deleteQuery);
			
			/* visita l'albero, e per ogni nodo che incontri inserisci una tupla.  
			 * la tabella � fatta cos�: 
			 * (id, idtag, left, right, similarity)
			 * 
			 * */
			
			visitAndSaveSubTree(root, dataSource, connection, statement);	
		}
		catch (PersistenceException e) {
			throw new PersistenceException(e.getMessage());
		} catch (SQLException e) {
			// per l'execute update
			e.printStackTrace();
		}
		finally {
			dataSource.close(statement);
			dataSource.close(connection);
		}
		

	}
	
	
	private String prepareStatementForDelete() {
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		StringBuffer query = new StringBuffer();
		query.append(SQL_DELETE_CLUSTER);
		logger.info("deleting clusters_sets table content: " + query);
		return query.toString();
		}


	private void visitAndSaveSubTree(Node node, DataSource dataSource, Connection connection, PreparedStatement statement) throws PersistenceException {
		Logger logger = LogHandler.getLogger(this.getClass().getName());

		/* qui devo mantenere l'id che � stato assegnato dal database alla tupla 
		 * inserita, che � il padre delle prossime tuple */
		
//		logger.info("visito il nodo: " + node.toString());
//		int fatherId = -1; 
		
		if (node.getFather() == null) {
//			System.out.println("nodo root");
//			fatherId = 0;
		} else {
//			fatherId = node.getFather().getIdNode();	
		}
		
		/* nome del tag: se � un cluster, non ha senso metterlo. */
		String tagName = "-";
		int idTag = -1;
		Float similarity;
		
		if (node.isLeaf()) {
			tagName = node.getCentroid().getTag();
			/* devo estrarre l'id che il tag ha sul database */
			TagDAOPostgres tagHandler = new TagDAOPostgres();
			try {
				idTag = tagHandler.retrieveTagId(tagName);
			} catch (PersistenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// la somiglianza non ha senso se visito le foglie
			similarity = new Float(1.0);			
		} else {
			similarity = new Float(node.getSimilarity());
		} 
		
		try {
			String query = prepareStatementForInsert();
			/* query per salvare i dati del nodo e l'id del padre */	
//			logger.info("saving node: " + node.getValue());
			statement = connection.prepareStatement(query);
			statement.setInt(1, node.getIdNode());
			statement.setInt(2, idTag);
			statement.setInt(3, node.getLeft());
			statement.setInt(4, node.getRight());			
			statement.setFloat(5, similarity);
			
//			logger.info("statement: " + statement.toString());
			
			statement.executeUpdate();
		}
		
		catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		}
		
		
		for (Node child: node.getChildren()) {
			visitAndSaveSubTree(child, dataSource, connection, statement);
		}
	}

	private String prepareStatementForInsert() {
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		StringBuffer query = new StringBuffer();
		query.append(SQL_INSERT_CLUSTER);
//		logger.info("query: \n" + query);
		return query.toString();
	}

	
	
	private static String SQL_INSERT_CLUSTER = 
		"INSERT INTO clusters_sets values (" +
		"?, ?, ?, ?, ?);";
	

	private static String SQL_DELETE_CLUSTER = 
	"DELETE FROM CLUSTERS_SETS;";

	
	
	@Override
	public Tree retrieve(List<RankedTag> tags) throws PersistenceException {
		/* da una lista di rankedtags con i valori pesati - quelli scelti da nereau vecchio - 
		 * estrae una gerarchia dal database con solo quei tag.  */
		//viene usato durante l'espansione
		//FIXME: se gli passo solo un rankedTag??
		Logger logger = LogHandler.getLogger(this.getClass().getName());		
		
		Tree extractedTree = null;
		LinkedList<LinkedList<Node>> hierarchiesList = new LinkedList<LinkedList<Node>>();
		
		/* estrae tutte le liste degli antenati di ogni tag */
		for (RankedTag currentTag: tags) {
			LinkedList<Node> singleTagHierarchy = this.extractSingleTagHierarchy(currentTag);

//			logger.info("ottengo la lista di antenati del tag: " + currentTag.getTag() + ": \n" + singleTagHierarchy);
//			System.out.println("ho estratto gli antenati del tag: " + currentTag.getTag());
			//OK, ho estratto gerarchie solo di Tags!
			
			/* l'ultimo nodo nella gerarchia � il nodo del rankedtag che si sta calcolando */
			if (singleTagHierarchy.size() != 0) {
//				System.out.println("aggiungo il tag singolo in coda: " + singleTagHierarchy);
				hierarchiesList.add(singleTagHierarchy);
			}
		}
		
		if (hierarchiesList.size() == 0) {
			// l'albero sar� vuoto
			extractedTree = new Tree();
		} else {
			extractedTree = buildTreeFromHierarchies(hierarchiesList);	
		}
		
		return extractedTree;
	}
	
	
	public void deleteClustering() throws PersistenceException {
		Logger logger = LogHandler.getLogger(this.getClass().getName());		
		
		DataSource dataSource = DataSource.getInstance();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = null;
		
		try {
			statement = connection.prepareStatement(SQL_DELETE_ALL_CLUSTERS_SETS);
			statement.executeUpdate();

		} catch (SQLException e){
			throw new PersistenceException(e.getMessage());
		}
	}


	private Tree buildTreeFromHierarchies(LinkedList<LinkedList<Node>> hierarchiesList) {
		
		/* algoritmo che ricostruisce un albero a partire da una lista di gerarchie */
		Logger logger = LogHandler.getLogger(this.getClass().getName());		
		LinkedList<Node> nodesList = new LinkedList<Node>();
		
		/* devo avere un nodo per ogni gerarchia: sono le foglie dell'albero che
		 * devo ricostruire */
//		logger.info("costruisco un oggetto Node per ogni gerarchia");
		for (LinkedList<Node> hierarchy: hierarchiesList) {
			/* costruisco un nodo per ogni gerarchia, 
			 * assegnandogli la gerarchia stessa come attributo */
//			Node currentNode = new Node(hierarchy.getLast().getValue());
			Node currentNode = hierarchy.getLast();
			currentNode.setHierarchy(hierarchy);
			/* aggiungo alla lista dei Node da fondere per 
			 * la costruzione del cluster */
//			currentNode.setLeft(hierarchy.getLast().getLeft());
//			currentNode.setRight(hierarchy.getLast().getRight());
//			currentNode.setIdNode(hierarchy.getLast().getIdNode());
//			currentNode.setSimilarity(hierarchy.getLast().getSimilarity());
//			logger.info("from list to Node: " + currentNode.toString());
			nodesList.add(currentNode);
		}
		
		
		logger.info("Algoritmo per la ricostruzione dell'albero ridotto");
		
		while (nodesList.size() > 1) {
			/* algoritmo per la ricostruzione */
			/* 1 - cerco la coppia (lista) di Nodes che ha un ancestor (comune)
			 * con la similarity pi� alta di tutti gli altri */
//			logger.info("cerco l'ancestor con similarity pi� alta di tutti");
			LinkedList<Node> nodesToMerge = findNodesWithHighestAncestorSimilarity(nodesList);

			/* 2 - fondo i Node che ho scelto in un nodo con la similarity trovata */
			//la similarity � presa dall'antenato comune
			/* la gerarchia del nodo fuso � calcolata dalle due gerarchie dei nodi */
//			Node mergedNode = new Node(nodesToMerge, similarity);
			Node mergedNode = this.ancestorFound;
			mergedNode.setChildren(nodesToMerge);
			for (Node node: nodesToMerge) {
				node.setFather(mergedNode);
			}
	
			// l' ho gi� calcolata con il costruttore del Node			
//			LinkedList<Node> mergedHierarchy = mergeNodesHierarchies(nodesToMerge);
//			mergedNode.setHierarchy(mergedHierarchy);

			/* 3 - rimuovo dalla lista dei Node tutti i Node vecchi che ora ho fuso */
			nodesList.removeAll(nodesToMerge);
			/* 4 - aggiungo il Node fusione nei nodesToMerge */ 
			nodesList.add(mergedNode);
			logger.info("aggiunto alla lista il nodo: " + mergedNode);
			logger.info("che ha i figli: " + mergedNode.getChildren().toString());
			/* 5 - proseguo finch� non ho UN solo elemento nella lista dei Node */
			
		}
		
		logger.info("ricostruzione dell'albero ridotto terminata");

		Tree reconstructedTree = new Tree(nodesList.getFirst());
		return reconstructedTree;
	}

	/* da una lista di nodi, trova nodi con 
	 * il valore di similarity dell'antenato pi� alta */

	private LinkedList<Node> findNodesWithHighestAncestorSimilarity(
			LinkedList<Node> nodesList) {
		Logger logger = LogHandler.getLogger(this.getClass().getName());

		LinkedList<Node> twoNodes = new LinkedList<Node>();
		
		//modifica anche un attributo dell'oggetto, che contiene l'antenato 
		//comune che deve diventare il padre della coppia di nodi
		this.ancestorFound = null;
				
		/* ottengo tutte le combinazioni di coppie tra i nodi della lista */
		ClusterCombinator nodesCombinator= new ClusterCombinator(nodesList);
		LinkedList<LinkedList<Node>> combinations = nodesCombinator.getClusterCombinations();
		
//		System.out.println("combinazioni di coppie tra nodi: " + combinations);
		
		/* occhio a >=0.0 o > 0.0 */
		
//		for(int i=0;i<combinations.size();i++) {
//			System.out.println("combinazione " + i + ": " + combinations.get(i));
//		}
		
		/* per ogni coppia trovo il valore del primo nodo antenato 
		 * e verifico se supera il valore massimo trovato finora */
		
		double maxSimilarity = -1.0;
	
		for (LinkedList<Node> couple: combinations) {
//			System.out.println("ricerca antenato della coppia di nodi: \n" +  
//					 couple.toString() + "\n" + "con gerarchie: \n" + 
//					 couple.getFirst().getHierarchy() + " e \n" + 
//					 couple.getLast().getHierarchy());
			
			double actualSimilarity = 0.0;

//			logger.info("cerco l'ancestor dei nodi: " + 
//					couple.getFirst().toString() + " e " + 
//					couple.getLast().toString());

			Node ancestor = Node.calculateFirstAncestor(couple);
			
//			System.out.println("found ancestor: " + ancestor);
			
			if (ancestor != null) {
				actualSimilarity = ancestor.getSimilarity().doubleValue();
//				logger.info("found ancestor: " + ancestor.toString() 
//						+ " sim: " + actualSimilarity);

				if (actualSimilarity > maxSimilarity) {
				/* aggiorno l'antenato, la sua similarity trovata e la coppia di nodi 
				 * a cui corrisponde l'antenato con similarity pi� alta */
					
	//				logger.info("found ancestor with similarity " + actualSimilarity +
	//						", HIGHER than = " + maxSimilarity);
	//				logger.info("ancestor with highest sim: " + ancestor.getIdNode() +
	//						"{ "+ ancestor.getLeft() + ", " + ancestor.getRight() + "}" + 
	//						" similarity: " + actualSimilarity);
					maxSimilarity = actualSimilarity;
					twoNodes.clear();
					twoNodes.addAll(couple);
					this.ancestorFound = ancestor;
//
//					logger.info("found ancestor with MAX similarity: " 
//							+ ancestor.toString() 
//							+ " sim: " + actualSimilarity);
//					
//					logger.info("found ancestor with MAX similarity: " 
//							+ this.ancestorFound.toString() 
//							+ " sim: " + actualSimilarity);
					

				}	


			}


		} //for combinations

			
		/* cerco l'indice dell'ancestor nella gerarchia di uno dei due 
		 * nodi per costruire la sua gerarchia di ancestors */

		
//		Node newNode = new Node(actualMergingCouple, similarityValue);			

		
		LinkedList<Node> ancestorHierarchy = new LinkedList<Node>();
		int ancestorIndex = 0;
		int i = 0;
		for (Node node: twoNodes.getFirst().getHierarchy()) {
			
			if ((node.getLeft() == ancestorFound.getLeft()) && 
					(node.getRight() == ancestorFound.getRight()) && 
					(node.getIdNode() == ancestorFound.getIdNode()) ) {
				ancestorIndex = i;
			}

			i++;
		}

//		logger.info("MAX similarity ancestor's index in hierarchy: " + ancestorIndex);
		/* costruisco la gerarchia dell'ancestor trovato */

		if(ancestorIndex > 0) {
			ancestorHierarchy.addAll(twoNodes.getFirst().getHierarchy().subList(0, ancestorIndex));
			this.ancestorFound.setHierarchy(ancestorHierarchy);
		} else {//FIXED!
			//caso limite in cui l'indice dell'ancestor � 0, restituisco 
			//proprio quell'elemento
			ancestorHierarchy.add(twoNodes.getFirst().getHierarchy().getFirst());
			this.ancestorFound.setHierarchy(ancestorHierarchy);
		}

		
		
		
		return twoNodes;
	}
	
	
	
	
	
	
	
	
	
	
	
	

	/* l'ultimo elemento Node deve anche contenere una rappresentazione tfidf del tag */
	private LinkedList<Node> extractSingleTagHierarchy(RankedTag tag) throws PersistenceException {
		Logger logger = LogHandler.getLogger(this.getClass().getName());
//		logger.info("extracting ancestors of tag: " + tag.getTag());
		LinkedList<Node> ancestors = new LinkedList<Node>();
		/* faccio una query ed estraggo tutti gli antenati del tag, 
		 * prima estraendo l'id nella tabella tags */
		
		/* li restituisce in ordine farlocco, perch�? 
		 * li ordino con la query sql */
		
		DataSource dataSource = DataSource.getInstance();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = null;
		
		try {
			String query = prepareStatementForExtraction();
			statement = connection.prepareStatement(query);
			statement.setString(1, tag.getTag());			
//			logger.info("statement: " + statement.toString());
			
			ResultSet result = statement.executeQuery();
			while(result.next()) {
				/* nel result set ho le ultime 5 colonne che contengono i dati 
				 * di ogni nodo della gerarchia */
				//devo estrarre id, left, right, similarity
				int nodeId = result.getInt(6);
				int left = result.getInt(8);
				int right = result.getInt(9);
				float similarity = result.getFloat(10);
				String nodeValue = String.valueOf(nodeId);
				Node currentAncestor = new Node(nodeValue, similarity);
				//setta gli id del nodo
				currentAncestor.setIdNode(nodeId);
				currentAncestor.setLeft(left);
				currentAncestor.setRight(right);
				currentAncestor.setSimilarity(similarity);
//				logger.info("found ancestor with value: " + nodeValue + " sim: " + 
//						currentAncestor.getSimilarity() + 
//						" {" + currentAncestor.getLeft() + ", " + currentAncestor.getRight() + "}");
				
				//ho trovato l'ultimo nodo foglia, quello da cui ha origine questa gerarchia
				if ((left + 1) == right) {
					logger.info("foglia: " + tag.getTag());
					currentAncestor.setValue(tag.getTag());
					//e il ranking?
					
					//devo estrarre i dati relativi al tfidf del tag foglia.
					
					Tagtfidf tagRepresentation = new Tagtfidf();
					tagRepresentation.setTag(tag.getTag());
					tagRepresentation.extractTfidfFromDatabase();
					currentAncestor.setCentroid(tagRepresentation);
					//aggiungo al node un attributo ranking, del tag che rappresenta. 
					//Solo se � una foglia
					currentAncestor.setRanking(tag.getRanking());
//					logger.info("aggiungo il RankedTag "+ tag.getTag()
//							+ " e il ranking " + tag.getRanking());
					
				}
				ancestors.add(currentAncestor);
				
			}
		}
		
		catch (SQLException e) {
			throw new PersistenceException(e.getMessage());
		}
		
		return ancestors;
	}


	private String prepareStatementForExtraction() {
		StringBuffer query = new StringBuffer();
		query.append(SQL_EXTRACT_CLUSTER_HIERARCHY);		
		return query.toString();
	}
	
	
	/* � un cross join: ho le prime colonne a sx che sono il tag selezionato 
	 * e quelle a dx sono la gerarchia */
	/* ripreso da http://www.intelligententerprise.com/001020/celko.jhtml
	 * */
	private static String SQL_EXTRACT_CLUSTER_HIERARCHY = 
		"SELECT * " +
		"FROM clusters_sets AS C1, clusters_sets AS C2 " +
		"WHERE C1.left BETWEEN C2.left AND C2.right " +
		"AND C1.idtag = " +
		"(SELECT id " +
		"FROM tags " +
		"WHERE tag = ?) ORDER BY C2.left;";

	private static String SQL_DELETE_ALL_CLUSTERS_SETS =
		"DELETE from clusters_sets;";

	
	

}

