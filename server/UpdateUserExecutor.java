package server;

import model.User;

import org.json.JSONException;
import org.json.JSONObject;

public class UpdateUserExecutor extends CommandExecutor {

	public UpdateUserExecutor(JSONObject args) {
		super(args);
	}

	@Override
	public String getJSONResponse() {

		String oldPassword = "", newPassword = "";
		String firstName = "", lastName = "", email = "";
		int userId = -1;
		User oldUser = null;
		
		try {
			//username = args.getString("username");
			oldPassword = args.getString("oldpassword");
			if(oldPassword==null)
				oldPassword="";
			newPassword = args.getString("newpassword");
			firstName = args.getString("firstname");
			lastName = args.getString("lastname");
			email = args.getString("email");
			userId = args.getInt("userid");
		} catch (JSONException e) {
			e.printStackTrace();
		} 
		
		
		String JSONResponse = "";
		
		oldUser = this.nereau.retrieveUser(userId);
		
		if(newPassword!=null) {
			if(newPassword.length()>=1) {
				oldUser = this.nereau.retrieveUser(userId);
				if(!oldUser.getPassword().equals(oldPassword)) {
					try {
						JSONResponse = this.createJSONStandardResponse(400,"wrong password").toString();
					} catch (JSONException e) {
						e.printStackTrace();
					}
					return JSONResponse;
				}
				oldUser.setPassword(newPassword);
			}
		}


		oldUser.setFirstName(firstName);
		oldUser.setLastName(lastName);
		oldUser.setEmail(email);
		
		//newUser = new User(username,newPassword,firstName,lastName,email,0,userId);
	
		boolean result = 
			this.nereau.updateUser(oldUser);
		try {
		if(!result)
			JSONResponse = this.createJSONStandardResponse(400,"wrong data").toString();
		else
			JSONResponse = this.createJSONStandardResponse(200,"ok").toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return JSONResponse;
		
		
	}

}
