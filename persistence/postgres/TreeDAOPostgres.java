package persistence.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import model.RankedTag;

import cluster.Node;
import cluster.Tree;
import persistence.PersistenceException;
import java.sql.SQLException;
import persistence.TreeDAO;
import util.LogHandler;

public class TreeDAOPostgres implements TreeDAO {




	/* da cambiare, devo salvare una struttura di nested sets */
	@Override
	public void save(Tree clustering) throws PersistenceException {
		/* salva il clustering (sotto forma di Tree) sul database, 
		 * in una tabella che rappresenti la struttura gerarchica e
		 * che permetta quando ho tipo 5 tag di estrarre una gerarchia
		 * coerente con la gerarchia globale, ma limitata a quei 5 tags */
		
		/* deve essere una sola transazione atomica, altrimenti 
		 * potrebbero esserci accessi alla tabella clusters 
		 * quando si trova in uno stato inconsistente */
		
		Node root = clustering.getRoot();
		
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		logger.info("comincio la visita dell'albero e lo salvo");
				
		DataSource dataSource = DataSource.getInstance();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = null;
		
		try {
			/* cancella tutte le tuple della tabella clusters_sets, 
			 * tanto non é rilevante modificarla */
			//DELETE FROM CLUSTERS;
			
			String deleteQuery = this.prepareStatementForDelete();
			statement = connection.prepareStatement(deleteQuery);
			statement.executeUpdate();
			logger.info("deleting all clusters_sets rows: " + deleteQuery);
			
			/* visita l'albero, e per ogni nodo che incontri inserisci una tupla 
			 * nella tabella, costruita cosí: 
			 * 
			 * (id_cluster, 
			 * id_tag (puó essere null per i cluster),
			 * similarity_value,  
			 * id_cluster_father (puó essere null per la radice))
			 * 
			 * é cambiata la tabella: ora uso i nested sets, la tabella é fatta cosí: 
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

		/* qui devo mantenere l'id che é stato assegnato dal database alla tupla 
		 * inserita, che é il padre delle prossime tuple */
		
		logger.info("visito il nodo: " + node.toString());
//		int fatherId = -1; 
		
		if (node.getFather() == null) {
			System.out.println("nodo root");
//			fatherId = 0;
		} else {
//			fatherId = node.getFather().getIdNode();	
		}
		
		/* nome del tag: se é un cluster, non ha senso metterlo. */
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
			logger.info("saving node: " + node.getValue());
			statement = connection.prepareStatement(query);
			statement.setInt(1, node.getIdNode());
			statement.setInt(2, idTag);
			statement.setInt(3, node.getLeft());
			statement.setInt(4, node.getRight());			
			statement.setFloat(5, similarity);
			
			logger.info("statement: " + statement.toString());
			
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
		logger.info("query: \n" + query);
		return query.toString();
	}

	
	
	private static String SQL_INSERT_CLUSTER = 
		"INSERT INTO clusters_sets values (" +
		"?, ?, ?, ?, ?);";
	

	private static String SQL_DELETE_CLUSTER = 
	"DELETE FROM CLUSTERS_SETS;";

	
	
	@Override
	public Tree retrieve(List<RankedTag> tags) throws PersistenceException {
		/* da una lista di rankedtags - quelli scelti da nereau vecchio - 
		 * estrae una gerarchia dal database con solo quei tag.  */
		//viene usato durante l'espansione
		Tree extractedTree = null;
		LinkedList<LinkedList<Node>> hierarchiesList = new LinkedList<LinkedList<Node>>();
		
		/* estrae tutte le liste degli antenati di ogni tag */
		for (RankedTag currentTag: tags) {
			LinkedList<Node> singleTagHierarchy = this.extractSingleTagHierarchy(currentTag);
			/* l'ultimo nodo nella gerarchia é il nodo del rankedtag che si sta calcolando */
			hierarchiesList.add(singleTagHierarchy);
		}
		extractedTree = buildTreeFromHierarchies(hierarchiesList);	
		return extractedTree;
	}
	
	private Tree buildTreeFromHierarchies(
			LinkedList<LinkedList<Node>> hierarchiesList) {
		Tree reconstructedTree = null;
		
		/* algoritmo che ricostruisce un albero a partire da una lista di gerarchie */
		while (hierarchiesList.size() != 1) {
			/* 1 - cerco la coppia (lista) di RankedTags che ha un ancestor 
			 * con la similarity piú alta di tutti gli altri */
			LinkedList<Node> nodesToMerge = findHighestSimilarityAncestor(hierarchiesList);
			
			/* 2 - fondo i RankedTag che ho scelto in un nodo con la similarity trovata */
			
			/* 3 - rimuovo dalla lista delle gerarchie tutte le gerarchie dei tag da rimuovere */

			/* 4 - proseguo finché non ho un solo elemento nella lista delle gerarchie */
			
		}
		
		
		return reconstructedTree;
	}


	private LinkedList<Node> findHighestSimilarityAncestor(
			LinkedList<LinkedList<Node>> hierarchiesList) {
		LinkedList<Node> nodes = null;
		
		return nodes;
		
	}


	private LinkedList<Node> extractSingleTagHierarchy(RankedTag tag) throws PersistenceException {
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		logger.info("extracting ancestors of tag: " + tag.getTag());
		LinkedList<Node> ancestors = new LinkedList<Node>();
		/* faccio una query ed estraggo tutti gli antenati del tag, 
		 * prima estraendo l'id nella tabella tags */
		
		DataSource dataSource = DataSource.getInstance();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = null;
		
		try {
			String query = prepareStatementForExtraction();
			statement = connection.prepareStatement(query);
			statement.setString(1, tag.getTag());			
			logger.info("statement: " + statement.toString());
			
			ResultSet result = statement.executeQuery();
			while(result.next()) {
				/* nel result set ho le ultime 5 colonne che contengono i dati 
				 * di ogni nodo della gerarchia, ordinati per ...? */
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
				
				logger.info("found ancestor with value: " + nodeValue + " left: " + 
						currentAncestor.getLeft() + " right: " + currentAncestor.getRight());
				
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
	
	
	/* é un cross join: ho le prime colonne a sx che sono il tag selezionato 
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
		"WHERE tag = ?);";
	

}
