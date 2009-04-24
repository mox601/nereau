package persistence.postgres;

import java.util.logging.Logger;

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
		
		
		/**/
		Logger logger = LogHandler.getLogger(this.getClass().getName());
		logger.info("comincio la visita dell'albero ");
		visitAndSaveTree(root);
		
		
	}
	
	
	private void visitAndSaveTree(Node node) {
		
		/* qui devo mantenere l'id che Ž stato assegnato dal database alla tupla 
		 * inserita, che Ž il padre delle prossime tuple */
		
		System.out.println("visito il nodo: " + node.toString());
		for (Node child: node.getChildren()) {
			visitAndSaveTree(child);
		}
	}
	
	

}
