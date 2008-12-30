package temp;

import java.util.HashSet;
import java.util.Set;

import model.ExpandedQuery;
import model.Nereau;
import model.RankedTag;
import model.User;

import org.json.JSONException;
import org.json.JSONObject;

public class ProvaJson {
	
	public static void main(String[] argv) {
		
		try {

			String jsonRequest = "{\"cmd\":\"expand\",\"args\":{\"username\":\"pizzonia\",\"query\":\"beers\"}}";
			
			JSONObject received = new JSONObject(jsonRequest);
			String command = received.getString("cmd");
			
			String query = "";
			String username = "";
			
			if(command.equals("expand")) {
				JSONObject args = received.getJSONObject("args");
				query = args.getString("query");
				username = args.getString("username");
			}
			
			System.out.println("query: " + query + ", username: " + username);
			
			Nereau nereau = Nereau.getInstance();
			
			Set<ExpandedQuery> expQueries = nereau.expandQuery(query, new User(username));
			
			JSONObject json = new JSONObject();
			String myString;
			json.put("code", "200");
			json.put("response", "ok");
			Set<JSONObject> eqset = new HashSet<JSONObject>();
			for(ExpandedQuery eq: expQueries) {
				JSONObject jsontags = new JSONObject();
				Set<JSONObject> tagset = new HashSet<JSONObject>();
				for(RankedTag rt: eq.getExpansionTags()) {
					JSONObject jsontag = new JSONObject();
					jsontag.put("tag", rt.getTag());
					jsontag.put("rank", rt.getRanking());
					tagset.add(jsontag);
				}
				jsontags.put("tags", tagset);
				JSONObject expquery = new JSONObject();
				expquery.put("tags", tagset);
				expquery.put("query", eq.toString());
				eqset.add(expquery);
			}
			json.put("results", eqset);
			myString = json.toString();

			System.out.println(myString);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}

}
