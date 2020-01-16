//A simple text-only HTTP proxy server.
//Created for an assignment for the SKJ course at PJATK in Warsaw, Poland.
//Author: Joanna Juszczak (s18344).
//Created 2020.

package proxyServerPJATK;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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
import java.util.Scanner;

public class ProxyServer {
	ServerSocket serverSocket;
	int port;
	List<String> words = new ArrayList<String>();
	String cachePath;
	
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
		cachePath = rawSettings.get("CACHE_DIR").replaceAll("\"", ""); //Removing the surrounding "" for use in File's constructor.
		words = Arrays.asList(rawSettings.get("WORDS").split(";"));
	}
	
	//Main server part - listener. Launched after the initial setup.
	private void start() {
		try {
			serverSocket = new ServerSocket(port);
			//Listening for incoming requests.
			while (true) {
				new ProxyThread(serverSocket.accept()).start();
			}
		} catch (IOException e) {
			System.out.println("Exception when initializibng serverSocket, please launch again. Consider providing a different port.");
		}
	}
	
	//Thread private class
	private class ProxyThread extends Thread {
		Socket browserSocket;
		
		public ProxyThread (Socket socket) {
			System.out.println("> New client");
			this.browserSocket = socket;
		}
		
		public void run() {
			try {
				
				BufferedReader browserIn = new BufferedReader(new InputStreamReader((browserSocket.getInputStream())));
				PrintWriter browserOut = new PrintWriter(browserSocket.getOutputStream());
				
				//Receiving the request from the browser.
				HTTPRequest currentRequest = new HTTPRequest(browserIn);
				System.out.println("Request received: " + currentRequest.method + " " + currentRequest.address);
				
				//Only supporting HTTP, not HTTPS
				if(currentRequest.method.equals("GET")) {
					
					File cacheFile = findInCache(currentRequest.address);
					
					//If the file is not cached, send the request to the target server and save the response to a cache.
					if(!cacheFile.exists()) {
						//Sending the request to the target server.
						HTTPResponse response = currentRequest.forward();
						
						//Only storing (and handling) the response if it contains a text file.
						if (response.headers.containsKey("Content-Type") && response.headers.get("Content-Type").matches("text.*")) {
							cacheFile.createNewFile();
							//Highlighting words from the provided list if the response is a html document.
							if (response.headers.get("Content-Type").matches("text/html.*")) {
								response.markSearchedWords(words);
							}
							
							//Sending the response to the cache file.
							PrintWriter fileWriter = new PrintWriter(cacheFile);
							response.send(fileWriter);
							fileWriter.close();
						}
					}
					
					//If the site was already cached, we just reads the file.
					//If the site was not cached and the response content was text, it is now cached and can be read and sent to the browser.
					//If it was not text, it is not cached and the request will be ignored.
					if (cacheFile.exists()) {
						//Read the response from cache and send it to the browser.
						//The file exists in cache only if it's a text.
						Scanner fileScanner = new Scanner(cacheFile);
						String line;
						while(fileScanner.hasNextLine()) {
							line = fileScanner.nextLine();
							browserOut.println(line);
							browserOut.flush();
						}
					}
				}
				
				//Closing the communication with the browser.
				browserIn.close();
				browserOut.close();
				browserSocket.close();
			} catch (InvalidRequestException e) {
				System.out.println(e.getMessage());
			} catch (FileNotFoundException e) { //This should never occur as we're using the file only after checking if it exists.
				System.out.println("Cache file requested, but not found!");
			} catch (IOException e) {
				System.out.println("In/out exception!");
				System.out.println("Message: " + e.getMessage());
			}
		}
		
		private File findInCache(String request) {
			//Hashing the request for shorter unique file names.
			int hash = request.hashCode();
			return new File(cachePath + "\\" + hash + ".txt");
		}
	}
}
