package server;

import org.json.JSONException;
import org.json.JSONObject;

public class ErrorExecutor extends CommandExecutor {

	public ErrorExecutor(JSONObject args) {
		super(args);
	}

	@Override
	public String getJSONResponse() {
		String JSONResponse = "";
		try {
			JSONResponse = this.createJSONStandardResponse(400, "wrong input").toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return JSONResponse;
	}

}
