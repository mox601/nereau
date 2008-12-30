package model;

import java.util.Set;

public class NereauCommandLine {
	
	private static String EXPAND = "expand";
	private static String SAVE = "save";
	private static String UPDATE = "update";
	private static String UPDATE_ALL = "update_all";
	private static Nereau nereau = Nereau.getInstance();
	
	public static void main(String[] args) {
		
		if(args.length==0) {
			System.out.println("nereau: missing operands.");
			System.exit(1);
		}
		
		//expand
		if(args[0].equals(EXPAND)) {
			handleExpand(args);
		}
		
		//save
		if(args[0].equals(SAVE)) {
			handleSave(args);
		}
		
		//update
		if(args[0].equals(UPDATE)) {
			handleUpdate(args);
		}
		
		//update_all
		if(args[0].equals(UPDATE_ALL)) {
			handleUpdateAll(args);
		}
		
		
	}

	private static void handleUpdateAll(String[] args) {
		// TODO Auto-generated method stub
		
	}

	private static void handleUpdate(String[] args) {
		// TODO Auto-generated method stub
		
	}

	private static void handleSave(String[] args) {
		// TODO Auto-generated method stub
		if(args.length<3) {
			System.out.println("usage: save <url> <query> {-e <expquery> <rankedtaglist>}");
			System.exit(1);
		}
	}

	private static void handleExpand(String[] args) {
		
		if(args.length!=3) {
			System.out.println("usage: expand <query> <username>");
			System.exit(1);
		}
		
		String queryString = args[1];
		String username = args[2];
		User user = new User(username);
		
		Set<ExpandedQuery> expQueries = nereau.expandQuery(queryString, user);
		
		for(ExpandedQuery expQuery: expQueries)
			System.out.println("'" + expQuery.toString() + "'," 
					+ expQuery.getExpansionTags() + "\n");
		
	}

}
