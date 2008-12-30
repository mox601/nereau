package server;

import java.util.HashSet;
import java.util.Set;

import model.ExpandedQuery;
import model.Query;
import model.RankedTag;
import model.User;
import model.VisitedURL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SaveVisitedURLExecutor extends CommandExecutor {

	public SaveVisitedURLExecutor(JSONObject args) {
		super(args);
	}

	@Override
	public String getJSONResponse() {
		
		String queryString = "", userIDString = "", expandedQueryString = "", urlString = "";
		int userID = -1;
		Set<RankedTag> expansionTags = new HashSet<RankedTag>();
		try {
			queryString = args.getString("query");
			urlString = args.getString("url");
			userIDString = args.getString("userid");
			userID = Integer.parseInt(userIDString);
			expandedQueryString = args.getString("expandedquery");
			if(expandedQueryString!=null) {
				if(expandedQueryString.length()>0) {
					JSONArray tags = args.getJSONArray("tags");
					for(int i=0; i<tags.length(); i++) {
						JSONObject rankedtag = tags.getJSONObject(i);
						String tag = rankedtag.getString("tag");
						double rank = rankedtag.getDouble("rank");
						expansionTags.add(new RankedTag(tag,rank));
					}
				}
				else {
					expandedQueryString = null;
				}
			}
					
		} catch (JSONException e) {
			e.printStackTrace();
		} 
		User user = new User(userID);
		Query query = new Query(queryString);
		ExpandedQuery expQuery = null;
		if(expandedQueryString!=null) {
			expQuery = new ExpandedQuery(expandedQueryString,expansionTags);
		}
		VisitedURL vUrl = new VisitedURL(urlString,query,expQuery);
		nereau.saveVisitedURL(vUrl, user);
		
		//E IN UNA RIGA CREO' IL MONDO!
		UpdateUserModelThread updateUM = new UpdateUserModelThread(nereau,user);
		Thread updateThread = new Thread(updateUM);
		//nereau.updateUserModel(user);
		updateThread.start();
		
		String JSONResponse = "";
		try {
			JSONResponse = this.createJSONStandardResponse(200,"ok").toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return JSONResponse;
		
	}

}
