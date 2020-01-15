package proxyServerPJATK;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

public class HTTPRequest {
	ArrayList<String> unparsedRequest = new ArrayList<>();
	String method;
	String address;
	String version;
	HashMap<String, String> headers = new HashMap<String,String>();
	String body;
	
	public HTTPRequest (BufferedReader input) throws IOException {
		String line;
		
		//Check for invalid input.
		if ((line = input.readLine()) == null) {
			System.out.println("!!! BAD REQUEST !!!");
			return;
		}
		
		while (!(line).equals("")) {
			unparsedRequest.add(line);
			line = input.readLine();
		}
		unparsedRequest.add("");
		
		parse();
		
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
		
		headers.put("Accept-Encoding", "identity");
	}
	
	public HTTPResponse forward() throws UnknownHostException, IOException {
		Socket targetSocket = new Socket(headers.get("Host"), 80);
		InputStream targetInStream = targetSocket.getInputStream();
		BufferedReader targetIn = new BufferedReader(new InputStreamReader((targetInStream)));
        PrintWriter targetOut = new PrintWriter(targetSocket.getOutputStream());
        
        //Send data to targetOut here.
        send(targetOut);
        HTTPResponse response = new HTTPResponse(targetIn, targetInStream);
        
        targetOut.close();
		targetIn.close();
		targetSocket.close();
        
        return response;
	}
	
	public void parse() {
		//Saving the data stored in the request line to the object.
		String[] topLineSplit = unparsedRequest.get(0).split(" ");
		method = topLineSplit[0];
		address = topLineSplit[1];
		version = topLineSplit[2];
		
		//Reading all the headers to the hashmap - until I meet the empty line that indicates end of headers, start of the body.
		String unparsedLine;
		for (int i = 1; !((unparsedLine = unparsedRequest.get(i)).equals("")); i++){
			String[] headerLineSplit = unparsedRequest.get(i).split(": ");
			headers.put(headerLineSplit[0], headerLineSplit[1]);
		}
	}
	
	public void send(PrintWriter writer) throws IOException {
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
	
	//=====================DEBUG=====================
	
	public void sendUnparsed(PrintWriter writer) throws IOException {
		for (int i = 0; i < unparsedRequest.size(); i++) {
			String unparsedLine = unparsedRequest.get(i);
			if(unparsedLine.equals("Accept-Encoding: gzip, deflate")) {
				unparsedRequest.remove(i);
				unparsedRequest.add(i, "Accept-Encoding: identity");
			}
		}
		
		for (String entry : unparsedRequest) {
			writer.println(entry);
			writer.flush();
		}
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder(method);
		
		result.append(" ");
		result.append(address);
		result.append(" ");
		result.append(version);
		
		headers.forEach((k, v) -> {
			result.append(System.lineSeparator());
			result.append(k);
			result.append(": ");
			result.append(v);
		});
		
		result.append(System.lineSeparator());
		
		if (body != null) {
			result.append(System.lineSeparator());
			result.append(body);
		}
		
		return result.toString();
	}
	
	public void printHeader() {
		System.out.println(unparsedRequest.get(0));
	}
	
	public String getHeader() {
		return unparsedRequest.get(0);
	}
}
