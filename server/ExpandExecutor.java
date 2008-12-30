package server;

import java.util.Set;

import model.ExpandedQuery;
import model.RankedTag;
import model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ExpandExecutor extends CommandExecutor {

	public ExpandExecutor(JSONObject args) {
		super(args);
	}

	@Override
	public String getJSONResponse() {
		
		String queryString = "", userIDString = "";
		int userID = -1;
		try {
			queryString = args.getString("query");
			userIDString = args.getString("userid");
			userID = Integer.parseInt(userIDString);
			System.out.println("userID: " + userID);
		} catch (JSONException e) {
			e.printStackTrace();
		} 
		User user = new User(userID);
		Set<ExpandedQuery> expQueries = nereau.expandQuery(queryString, user);
		
		String JSONResponse = "";
		try {
			if(expQueries==null)
				JSONResponse = this.createJSONStandardResponse(500,"database access error").toString();
			else if(expQueries.isEmpty())
				JSONResponse = this.createJSONStandardResponse(400,"unexpanded query").toString();
			else
				JSONResponse = this.createJSONExpandResponse(expQueries);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return JSONResponse;
		
	}

	private String createJSONExpandResponse(Set<ExpandedQuery> expQueries) throws JSONException {
	
		JSONObject jsonResponse = this.createJSONStandardResponse(200, "ok");
		
		JSONArray jsonExpQuerySet = new JSONArray();
		
		for(ExpandedQuery eq: expQueries) {
			
			JSONObject jsonExpQuery = new JSONObject();
			JSONArray jsonRankedTagSet = new JSONArray();
			
			for(RankedTag rt: eq.getExpansionTags()) {
				JSONObject jsonRankedTag = new JSONObject();
				jsonRankedTag.put("tag", rt.getTag());
				jsonRankedTag.put("rank", rt.getRanking());
				jsonRankedTagSet.put(jsonRankedTag);
			}

			jsonExpQuery.put("tags", jsonRankedTagSet);
			jsonExpQuery.put("query", eq.toString());
			
			jsonExpQuerySet.put(jsonExpQuery);
			
		}
		
		jsonResponse.put("results", jsonExpQuerySet);
		
		return jsonResponse.toString();
		
	}

}
