package persistence.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
			 * tanto non Ž rilevante modificarla */
			//DELETE FROM CLUSTERS;
			
			String deleteQuery = this.prepareStatementForDelete();
			statement = connection.prepareStatement(deleteQuery);
//			statement.executeUpdate();
			System.out.println("deleting all clusters_sets rows: " + deleteQuery);
			
			/* visita l'albero, e per ogni nodo che incontri inserisci una tupla 
			 * nella tabella, costruita cos’: 
			 * 
			 * (id_cluster, 
			 * id_tag (pu— essere null per i cluster),
			 * similarity_value,  
			 * id_cluster_father (pu— essere null per la radice))
			 * 
			 * Ž cambiata la tabella: ora uso i nested sets, la tabella Ž fatta cos’: 
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
		
		/* qui devo mantenere l'id che Ž stato assegnato dal database alla tupla 
		 * inserita, che Ž il padre delle prossime tuple */
		
		System.out.println("visito il nodo: " + node.toString());
//		int fatherId = -1; 
		
		if (node.getFather() == null) {
			System.out.println("nodo root");
//			fatherId = 0;
		} else {
//			fatherId = node.getFather().getIdNode();	
		}
		
		/* nome del tag: se Ž un cluster, non ha senso metterlo. */
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
			System.out.println("saving node: " + node.getValue());
			statement = connection.prepareStatement(query);
			statement.setInt(1, node.getIdNode());
			statement.setInt(2, idTag);
			statement.setInt(3, node.getLeft());
			statement.setInt(4, node.getRight());			
			statement.setFloat(5, similarity);
			
			System.out.println("statement: " + statement.toString());
			
//			statement.executeUpdate();
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
		// TODO retrieve Tree from database
		Tree extractedTree = null;
		LinkedList<LinkedList<Node>> hierarchiesList = new LinkedList<LinkedList<Node>>();
		
		
		/* estrae tutte le liste degli antenati di ogni tag */
		for (RankedTag currentTag: tags) {
			LinkedList<Node> singleTagHierarchy = this.extractSingleTagHierarchy(currentTag);
			hierarchiesList.add(singleTagHierarchy);
		}
		
		/* algoritmo che ricostruisce un albero a partire da una lista di gerarchie */
		extractedTree = buildTreeFromHierarchies(hierarchiesList);
		
		return extractedTree;
	}
	
	private Tree buildTreeFromHierarchies(
			LinkedList<LinkedList<Node>> hierarchiesList) {
		Tree reconstructedTree = null;
		
		
		
		
		return reconstructedTree;
	}


	private LinkedList<Node> extractSingleTagHierarchy(RankedTag tag) {
		
		
		
		return null;
	}
	
	
	

}
