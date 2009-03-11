package temp;

import java.util.Set;

import model.ExpandedQuery;
import model.User;
import model.queryexpansion.QueryExpansionFacade;

public class ProvaEspansioni {
	
	public static void main(String[] args) {
		/*
		if(args.length!=2) {
			System.out.println("[]");
			System.exit(1);
		}
		*/
		User user;
		String queryString;
		
		if(args.length!=2) {
			user = new User("iddio");
			queryString = "madonna mp3 pop";
		}
		
		else {
			user = new User(args[0]);
			queryString = args[1];
		}
		
		QueryExpansionFacade qef = QueryExpansionFacade.getInstance();
		
		Set<ExpandedQuery> expQueries = qef.expandQuery(queryString, user);
		
		/* posso fare una seconda prova di espansione, usando il metodo nuovo */
		/* Set<ExpandedQuery> newExpQueries = qef.newExpandQuery(queryString, user) */
		
		
		
		
		for(ExpandedQuery  eq: expQueries)
			System.out.println(eq.toString() + " for tags: " + eq.getExpansionTags());
		
		
		
		
		
		
	}

}
