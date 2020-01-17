//A simple text-only HTTP proxy server.
//Created for an assignment for the SKJ course at PJATK in Warsaw, Poland.
//Author: Joanna Juszczak (s18344).
//Created 2020.

package proxyServerPJATK;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class HTTPRequest {
	ArrayList<String> unparsedRequest = new ArrayList<>();
	String method;
	String address;
	String version;
	HashMap<String, String> headers = new HashMap<String,String>();
	String body;
	
	public HTTPRequest (BufferedReader input) throws IOException, InvalidRequestException {
		
		String line;
		
		//Check for invalid input.
		if ((line = input.readLine()) == null) {
			throw new InvalidRequestException("Invalid request");
		}
		
		//Reading in the top line and the headers.
		while (!(line).equals("")) {
			unparsedRequest.add(line);
			line = input.readLine();
		}
		unparsedRequest.add("");
		
		//Parsing the request's data into the object.
		parse();
		
		//Reading the body, if there is any.
		if (headers.containsKey("Content-Length")) {
			try {
				int contentLength = Integer.parseInt(headers.get("Content-Length"));
				char[] buffer = new char[contentLength];
				input.read(buffer, 0, contentLength);
				body = new String(buffer);
				unparsedRequest.add(body);
			} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
			}
		}
		
		//Overwriting two headers: to avoid gziped content and to facilitate reading the response's body.
		headers.put("Accept-Encoding", "identity");
		headers.put("Connection", "close");
	}
	
	public HTTPResponse forward() throws IOException {
		Socket targetSocket = new Socket(headers.get("Host"), 80);
		BufferedReader targetIn = new BufferedReader(new InputStreamReader((targetSocket.getInputStream())));
        PrintWriter targetOut = new PrintWriter(targetSocket.getOutputStream());
        
        //Send data to targetOut here, create a response object to read in and store the target server's response.
        send(targetOut);
        HTTPResponse response = new HTTPResponse(targetIn);
        
        targetOut.close();
		targetIn.close();
		targetSocket.close();
        
		//Returning the response object that can send the server's response.
        return response;
	}
	
	//Parsing the request and headers for ease of access.
	public void parse() {
		//Saving the data stored in the request line to the object.
		String[] topLineSplit = unparsedRequest.get(0).split(" ");
		method = topLineSplit[0];
		address = topLineSplit[1];
		version = topLineSplit[2];
		
		//Reading all the headers to the hashmap - until I meet the empty line that indicates end of headers, start of the body.
		String unparsedLine;
		for (int i = 1; !((unparsedLine = unparsedRequest.get(i)).equals("")); i++){
			String[] headerLineSplit = unparsedLine.split(": ");
			headers.put(headerLineSplit[0], headerLineSplit[1]);
		}
	}
	
	//Sending the request to the target server via the provided writer.
	public void send(PrintWriter writer) {
		writer.println(method + " " + address + " " + version);
		writer.flush();
		
		headers.forEach((k, v) -> {
			writer.println(k + ": " + v);
			writer.flush();
		});
		
		writer.println();
		writer.flush();
		
		if (body != null) {
			writer.println(body);
			writer.flush();
		}
	}
}
