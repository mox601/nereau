package server;

import java.io.*;
import java.net.*;

public class MainServer {
    
	public static void main(String args[]) {
		
		if(args.length!=1) {
			System.out.println("usage: server <port number>");
			System.exit(1);
			
		}
		
        ServerSocket echoServer = null;
        Socket clientSocket = null;

        try {
           echoServer = new ServerSocket(Integer.parseInt(args[0]));
        }
        catch (IOException e) {
           System.out.println(e);
        }   
        
        /* prima di lanciare il server in ascolto delle connessioni JSON, 
         * ora posso lanciare un thread che si occupa di effettuare il clustering 
         * di tutti i tag nel database */
        
        Runnable clustererThread = new ClustererThread();
        
        Thread clusterer = new Thread(clustererThread);
        clusterer.start();
        

    try {
    	while(true) {
           clientSocket = echoServer.accept();
           System.out.println("connection accepted: " + 
        		   clientSocket.getInetAddress().getHostName() + 
        		   clientSocket.getInetAddress().getAddress().toString());
           Runnable serverLogic = new NereauCommandServer(clientSocket);
           Thread t = new Thread(serverLogic);
           t.start();
    	}
       }   
    catch (IOException e) {
           System.out.println(e);
        }
    
  
    
    
    
    
    }
}

