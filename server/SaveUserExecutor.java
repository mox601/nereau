package server;

import org.json.JSONException;
import org.json.JSONObject;

public class SaveUserExecutor extends CommandExecutor {

	public SaveUserExecutor(JSONObject args) {
		super(args);
	}

	@Override
	public String getJSONResponse() {
		
		String username = "", password = "";
		String firstName = "", lastName = "", email = "";
		
		try {
			username = args.getString("username");
			password = args.getString("password");
			firstName = args.getString("firstname");
			lastName = args.getString("lastname");
			email = args.getString("email");
		} catch (JSONException e) {
			e.printStackTrace();
		} 
		
		String JSONResponse = "";
		
		boolean result = 
			this.nereau.saveUser(username,password, firstName, lastName, email);
		try {
		if(!result)
			JSONResponse = this.createJSONStandardResponse(400,"username already exists").toString();
		else
			JSONResponse = this.createJSONStandardResponse(200,"ok").toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return JSONResponse;
		
	}

}
