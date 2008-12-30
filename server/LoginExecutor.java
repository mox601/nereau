package server;

import model.User;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginExecutor extends CommandExecutor {

	public LoginExecutor(JSONObject args) {
		super(args);
	}
	
	@Override
	public String getJSONResponse() {
		
		String username = "", password = "";
		
		try {
			username = args.getString("username");
			password = args.getString("password");
		} catch (JSONException e) {
			e.printStackTrace();
		} 
		
		String JSONResponse = "";
		
		User user = this.nereau.authenticateUser(username, password);
		try {
		if(user==null)
			JSONResponse = this.createJSONStandardResponse(400,"wrong username and/or password").toString();
		else
			JSONResponse = this.createJSONLoginResponse(user);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return JSONResponse;
	}

	private String createJSONLoginResponse(User user) throws JSONException {
		
		JSONObject jsonResponse = this.createJSONStandardResponse(200, "ok");
		
		JSONObject jsonUser = new JSONObject();
		
		jsonUser.put("userid", user.getUserID());
		jsonUser.put("username", user.getUsername());
		jsonUser.put("firstname", user.getFirstName()==null ? "" : user.getFirstName());
		jsonUser.put("lastname", user.getLastName()==null ? "" : user.getLastName());
		jsonUser.put("email", user.getEmail()==null ? "" : user.getEmail());
		jsonUser.put("role", user.getRole());
		
		jsonResponse.put("results", jsonUser);
		
		return jsonResponse.toString();
		
	}

}
