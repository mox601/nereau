package server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import org.json.JSONException;
import org.json.JSONObject;

public class NereauCommandServer implements Runnable {

	private static final String EXPAND = "expand";
	private static final String SAVE_VISITED_URL = "savevisitedurl";
	private static final String UPDATE = "update";
	private static final String UPDATE_ALL = "update_all";
	private static final String LOGIN = "login";
	private static final String SAVE_USER = "saveuser";
	private static final String UPDATE_USER = "updateuser";
	
	private Socket clientSocket;
    private DataInputStream is;
    private BufferedReader br;
    private PrintStream os;
    private CommandExecutor executor;
	
	public NereauCommandServer(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	@Override
	public void run() {
		
	    String line = "";
	    
        try {
			is = new DataInputStream(clientSocket.getInputStream());
	        br = new BufferedReader(new InputStreamReader(is));
	        os = new PrintStream(clientSocket.getOutputStream());
	        line = br.readLine();
	        
	        if(line!=null) {
	        	System.out.println("received: " + line);
	        	System.out.flush();
		        line.trim();
	        }
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String command = "";
		JSONObject args = new JSONObject();
		try {
			JSONObject received = new JSONObject(line);
			command = received.getString("cmd");
			args = received.getJSONObject("args");
		} catch (JSONException e1) {
			System.out.println("JSONException: " + e1.getMessage());
		}
		
		//expand
		if(command.equals(EXPAND))
			executor = new ExpandExecutor(args);

		//save
		else if(command.equals(SAVE_VISITED_URL))
			executor = new SaveVisitedURLExecutor(args);

		//update
		else if(command.equals(UPDATE))
			executor = new UpdateExecutor(args);
		
		//update_all
		else if(command.equals(UPDATE_ALL))
			executor = new UpdateAllExecutor(args);
		
		//login
		else if(command.equals(LOGIN))
			executor = new LoginExecutor(args);
		
		//save user
		else if(command.equals(SAVE_USER))
			executor = new SaveUserExecutor(args);
		
		//save user
		else if(command.equals(UPDATE_USER))
			executor = new UpdateUserExecutor(args);
			
		//not recognized
		else
			executor = new ErrorExecutor(args);

		
		String response = executor.getJSONResponse();
		os.print(response);
		System.out.println("response: " + response);
		
		try {
			br.close();
			os.close();
			is.close();
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

}
