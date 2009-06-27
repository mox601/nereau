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
		
		String queryString = "", userIDString = "", expandedQueryString = "", urlString = "", expansionTypeString = "";
		int userID = -1;
		int expansionType = -1;
		Set<RankedTag> expansionTags = new HashSet<RankedTag>();
		try {
			
			System.out.println("args received from php: " + args.toString());
			queryString = args.getString("query");
			urlString = args.getString("url");
			userIDString = args.getString("userid");
			userID = Integer.parseInt(userIDString);
			expandedQueryString = args.getString("expandedquery");
			
//			System.out.println("expanded query: " + expandedQueryString);
			
			expansionTypeString = args.getString("expansion_type");
//			expansionType = Integer.parseInt(expansionTypeString);
			
			if(expandedQueryString!=null) {
				if(expandedQueryString.length()>0) {
					JSONArray tags = args.getJSONArray("tags");					
					for(int i=0; i<tags.length(); i++) {
						JSONObject rankedtag = tags.getJSONObject(i);
						String tag = rankedtag.getString("tag");
						double rank = rankedtag.getDouble("rank");
						expansionTags.add(new RankedTag(tag,rank));
						
						System.out.println("rtags: " + new RankedTag(tag,rank).toString());
					}
				}
				else {
					expandedQueryString = null;
				}
			}
					
		} catch (JSONException e) {
			e.printStackTrace();
		} 
		
//		System.out.println(queryString + " " + urlString + " " + userIDString 
//				+ " " + userID + " " + expandedQueryString + " " + expansionType);

		
		User user = new User(userID);
		Query query = new Query(queryString);
		ExpandedQuery expQuery = null;
		if(expandedQueryString!=null) {
			expQuery = new ExpandedQuery(expandedQueryString, expansionTags);
			System.out.println("exp query: " + expQuery.toString());
		}
		
		
//		VisitedURL vUrl = new VisitedURL(urlString,query,expQuery);
		
		/* devo prima tradurre il tipo di espansione in intero, come sta sul database */
	
		/* ORENDO!, hard coded...  */
		if (expansionTypeString.equals("no_expansion")) {
			expansionType = 1;
		} else {
			if(expansionTypeString.equals("co-occurrences")) {
				expansionType = 2;
			} else {
				if(expansionTypeString.equals("tag_clustering")) {
					expansionType = 3;
				}
				
			}
		}
		
		
		VisitedURL vUrl = new VisitedURL(urlString, query, expQuery, expansionType);
		
		System.out.println("built VisitedURL: " + vUrl.toString());
		
		/* qui salva l'url visitato, tenendo traccia della query fatta dall'utente 
		 * e della query espansa (se Ž stato cliccato su un url nella finestra delle 
		 * query espanse) */
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
