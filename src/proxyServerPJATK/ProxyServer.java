package proxyServerPJATK;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ProxyServer {
	ServerSocket serverSocket;
	int port;
	List<String> words = new ArrayList<String>();
	String cachePath;
	
	//TODO:
	//Deal with exceptions nicely.
	
	//==Static methods==
	public static void main(String[] args) throws IOException {
		System.out.println("Settings file: " + args[0]);
		ProxyServer server = new ProxyServer(args[0]);
        server.start();
    }
	
	//==Dynamic methods==
	//Constructor
	private ProxyServer(String settingsPath) throws IOException {
		readSettings(settingsPath);
	}
	
	//Read in the settings from the settings file.
		private void readSettings(String path) throws IOException {
			HashMap<String, String> rawSettings = new HashMap<String, String>();
			File settingsFile = new File(path);
			BufferedReader reader = new BufferedReader(new FileReader(settingsFile));
			
			//Reading settings from the file in a raw form.
			String line;
			String[] lineSplit;
			while((line = reader.readLine()) != null) {
				lineSplit = line.split("=");
				rawSettings.put(lineSplit[0], lineSplit[1]);
			}
			
			//Saving the raw settings to appropriate variables.
			port = Integer.parseInt(rawSettings.get("PROXY_PORT"));
			cachePath = rawSettings.get("CACHE_DIR");
			words = Arrays.asList(rawSettings.get("WORDS").split(";"));
		}
	
	//Main server part - listener. Launched after the initial setup.
	private void start() throws IOException {
        serverSocket = new ServerSocket(port);
        //Listening for incoming requests.
        while (true) {
        	new ProxyThread(serverSocket.accept()).start();
        }
    }
	
	//Thread private class
	private class ProxyThread extends Thread {
		Socket browserSocket;
		
		public ProxyThread (Socket socket) {
            System.out.println("New client");
        	this.browserSocket = socket;
        }
		
		public void run() {
			try {
				BufferedReader browserIn = new BufferedReader(new InputStreamReader((browserSocket.getInputStream())));
	            PrintWriter browserOut = new PrintWriter(browserSocket.getOutputStream());
	            
	            HTTPRequest currentRequest = new HTTPRequest(browserIn, words);
	            
	            //Only supporting HTTP, not HTTPS
	            if(currentRequest.method.equals("GET")) {
	            	HTTPResponse response = currentRequest.forward();
	            	response.send(browserOut);
	            }
	            
	            browserIn.close();
            	browserOut.close();
				browserSocket.close();
			} catch (Exception e) {
				//TODO
				System.out.println(e);
				e.printStackTrace();
			}
		}
	}
}
