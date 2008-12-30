package server;

import model.Nereau;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class CommandExecutor {
	
	protected JSONObject args;
	protected Nereau nereau;
	
	public CommandExecutor() {
		
	}
	
	public CommandExecutor(JSONObject args) {
		this.args = args;
		this.nereau = Nereau.getInstance();
	}
	
	public abstract String getJSONResponse();
	
	protected JSONObject createJSONStandardResponse(int code, String response) throws JSONException {
		
		JSONObject jsonResponse = new JSONObject();

		jsonResponse.put("code", "" + code + "");
		jsonResponse.put("response", response);
		
		return jsonResponse;
		
	}
	
	

}
