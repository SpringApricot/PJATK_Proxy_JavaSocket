package proxyServerPJATK;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class HTTPResponse {
	ArrayList<String> unparsedRequest = new ArrayList<>();
	String version;
	int statusCode;
	String phrase;
	HashMap<String, String> headers = new HashMap<String,String>();
	String body;
	
	//Reading in the response, parsing it, and reading body if content type is text.
	public HTTPResponse (BufferedReader input) throws IOException {
		String line;
		while (!(line = input.readLine()).equals("")) {
			unparsedRequest.add(line);
		}
		unparsedRequest.add("");
		parse();
		if(headers.containsKey("Content-Type") && headers.get("Content-Type").matches("text.*")) {
			body = new String();
			while (((line = input.readLine()) != null)) {
				body = body.concat(line + "\r\n");
			}
		}
	}
	
	//Parsing the response and headers for ease of access.
	public void parse() {
		//Parsing the response's first line.
		String[] topLineSplit = unparsedRequest.get(0).split(" ");
		version = topLineSplit[0];
		statusCode = Integer.parseInt(topLineSplit[1]);
		StringBuilder phraseBuilder = new StringBuilder(topLineSplit[2]);
		for (int i = 3; i < topLineSplit.length ; i++) {
			phraseBuilder.append(topLineSplit[i]);
			phraseBuilder.append(" ");
		}
		phrase = phraseBuilder.toString().trim();
		
		for (int i = 1; !((unparsedRequest.get(i)).equals("")); i++){
				String[] headerLineSplit = unparsedRequest.get(i).split(": ");
				headers.put(headerLineSplit[0], headerLineSplit[1]);
		}
	}
	
	//Sending response to the browser via the provided writer.
	public void send(PrintWriter writer) throws IOException {
		writer.println(version + " " + statusCode + " " + phrase);
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
