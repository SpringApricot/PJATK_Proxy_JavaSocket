package proxyServerPJATK;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

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
		
		public ProxyThread (Socket socket) {
            System.out.println("New client");
        	this.browserSocket = socket;
        }
		
		public void run() {
			try {
				//TODO
			} catch (Exception e) {
				//TODO
				System.out.println(e);
				e.printStackTrace();
			}
		}
	}
}
