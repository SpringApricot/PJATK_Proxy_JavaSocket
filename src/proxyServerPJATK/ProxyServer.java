package proxyServerPJATK;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class ProxyServer {
	ServerSocket serverSocket;
	int port;
	
	//TODO:
	//Deal with exceptions nicely.
	
	//Static methods
	public static void main(String[] args) throws IOException {
		ProxyServer server = new ProxyServer(5555);
        server.start();
    }
	
	//Dynamic methods
	public ProxyServer(int port) {
		this.port = port;
	}
	
	public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        while (true) {
        	new ProxyThread(serverSocket.accept()).start();
        }
    }
	
	//Thread private class
	private static class ProxyThread extends Thread {
		Socket browserSocket;
		BufferedReader browserFrom;
		BufferedWriter browserTo;
		
		public ProxyThread (Socket socket) {
            System.out.println("New client");
        	this.browserSocket = socket;
        }
		
		public void run() {
			try {
				browserFrom = new BufferedReader(new InputStreamReader(browserSocket.getInputStream()));
				browserTo = new BufferedWriter(new OutputStreamWriter(browserSocket.getOutputStream()));
				
				String topLine;
				if((topLine = browserFrom.readLine()) == null) {
					System.out.println("Invlaid request!");
					return;
				}
				
				System.out.println(topLine);
				String[] topLineSplit = topLine.split(" ");
				
				//Parsing host from topline
				if(topLineSplit[1].substring(0,4).equals("http")) {
					topLineSplit[1] = topLineSplit[1].split("://")[1];
				}
				if(topLineSplit[1].matches(".*/")) {
					topLineSplit[1] = topLineSplit[1].substring(0, topLineSplit[1].length()-1);
				}
				
				//Filtering out https
				if(topLineSplit[0].contentEquals("CONNECT")) {
					System.out.println("HTTPS requests are not supported!");
					return;
				}
				
				//Creating server socket
				Socket targetSocket = new Socket(topLineSplit[1], 80);
				BufferedReader targetFrom = new BufferedReader(new InputStreamReader(targetSocket.getInputStream()));
				BufferedWriter targetTo = new BufferedWriter(new OutputStreamWriter(targetSocket.getOutputStream()));
				
				dp("sending topline");
				targetTo.write(topLine);
				String outcomingLine;
				
				while((outcomingLine = browserFrom.readLine()) != null && outcomingLine.length() > 0) {
					dp("sending");
					dp(outcomingLine);
					if(outcomingLine.matches("Connection:.*")) {
						outcomingLine = "Connection: close";
					}
					targetTo.write(outcomingLine);
					targetTo.flush();
				}
				
				targetTo.flush();

				dp("======================================flushed");
				
				String incomingLine;
				
				File fileToCache = new File("test.txt");

				if(!fileToCache.exists()){
					fileToCache.createNewFile();
				}

				// Create Buffered output stream to write to cached copy of file
				BufferedWriter fileToCacheBW = new BufferedWriter(new FileWriter(fileToCache));
				
				while((incomingLine = targetFrom.readLine()) != null) {
					dp("receiving");
					dp(incomingLine);
					incomingLine = incomingLine + "\r\n";
					fileToCacheBW.write(incomingLine);
					browserTo.write(incomingLine);
				}
				
				browserTo.flush();
				
				dp("++++++++++++++++++++++++++++++++++++++++flushed");
				
				targetFrom.close();
				targetTo.close();
				browserTo.close();
				browserFrom.close();
				fileToCacheBW.close();
				
			} catch (UnknownHostException e) {
				System.out.println("UnknownHostException");
			} catch (Exception e) {
				//TODO
				System.out.println(e);
				e.printStackTrace();
			}
		}
		
		//DEBUG
		
		public void justPrint(BufferedReader reader) throws IOException {
			String line;
			while((line = reader.readLine()) != null) {
				System.out.println(line);
			}
		}
		
		public void dp(String text) {
			System.out.println(text);
		}
	}
}
