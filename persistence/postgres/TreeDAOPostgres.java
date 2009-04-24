package persistence.postgres;

import cluster.Tree;
import persistence.PersistenceException;
import persistence.TreeDAO;

public class TreeDAOPostgres implements TreeDAO {

	@Override
	public void save(Tree clustering) throws PersistenceException {
		/* salva il clustering (sotto forma di Tree) sul database, 
		 * in una tabella che rappresenti la struttura gerarchica e
		 * che permetta quando ho tipo 5 tag di estrarre una gerarchia
		 * coerente con la gerarchia globale, ma limitata a quei 5 tags */
		
		/*  */
		
		
		
		
		
	}

}
