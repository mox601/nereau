package persistence.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.logging.Logger;

import model.RankedTag;

import cluster.Node;
import cluster.Tree;
import persistence.PersistenceException;
import persistence.TreeDAO;
import util.LogHandler;

public class TreeDAOPostgres implements TreeDAO {

	@Override
	public void save(Tree clustering) throws PersistenceException {
		/* salva il clustering (sotto forma di Tree) sul database, 
		 * in una tabella che rappresenti la struttura gerarchica e
		 * che permetta quando ho tipo 5 tag di estrarre una gerarchia
		 * coerente con la gerarchia globale, ma limitata a quei 5 tags */
		
		
		/* deve essere una sola transazione atomica, altrimenti 
		 * potrebbero esserci accessi alla tabella clusters 
		 * quando si trova in uno stato inconsistente */
		
		/* cancella tutte le tuple dell tabella clusters, 
		 * tanto non Ž rilevante modificarla */
		//DELETE FROM CLUSTERS;
		
		
		/* visita l'albero, e per ogni nodo che incontri inserisci una tupla 
		 * nella tabella, costruita cos’: 
		 * 
		 * (id_cluster, 
		 * id_tag (pu— essere null per i cluster),
		 * similarity_value,  
		 * id_cluster_father (pu— essere null per la radice))
		 * 
		 * */
		
		Node root = clustering.getRoot();
		
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		logger.info("comincio la visita dell'albero e lo salvo");
		
		
		
		DataSource dataSource = DataSource.getInstance();
		Connection connection = dataSource.getConnection();
		PreparedStatement statement = null;
	
		visitAndSaveSubTree(root);
		
		
	}
	
	
	private void visitAndSaveSubTree(Node node) {
		
		/* qui devo mantenere l'id che Ž stato assegnato dal database alla tupla 
		 * inserita, che Ž il padre delle prossime tuple */
		
		System.out.println("visito il nodo: " + node.toString());
		int fatherId = -1; 
		
		if (node.getFather() == null) {
			System.out.println("nodo root");
			fatherId = 0;
		} else {
			fatherId = node.getFather().getIdNode();	
		}
		
		/* nome del tag: se Ž un cluster, non ha senso metterlo. */
		String tagName = "";
		if (node.isLeaf()) {
			tagName = node.getCentroid().getTag();
			
		} else {
			tagName = "-";
		}
	
		/* query per salvare i dati del nodo e l'id del padre */	
		System.out.println("saving node: ");
		System.out.println("INSERT INTO clusters values(" +
				node.getIdNode() + ", " +
				tagName + ", " +
				node.getSimilarity() + ", " + 
				fatherId + ")");
	
		for (Node child: node.getChildren()) {
			visitAndSaveSubTree(child);
		}
	}


	@Override
	public Tree retrieve(List<RankedTag> tags) throws PersistenceException {
		/* da una lista di rankedtags - quelli scelti da nereau vecchio - 
		 * estrae una gerarchia dal database con solo quei tag.  */
		/* Ž meglio farlo qui oppure estrarre tutta la gerarchia e poi 
		 * tagliarla in memoria? */
		//viene usato durante l'espansione
		// TODO retrieve Tree from database
		return null;
	}
	
	

}
