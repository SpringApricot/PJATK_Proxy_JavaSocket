//A simple text-only HTTP proxy server.
//Created for an assignment for the SKJ course at PJATK in Warsaw, Poland.
//Author: Joanna Juszczak (s18344).
//Created 2020.

package proxyServerPJATK;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HTTPResponse {
	String version;
	int statusCode;
	String phrase;
	HashMap<String, String> headers = new HashMap<String,String>();
	String body;
	
	//Reading in the response, parsing it, and reading body if content type is text.
	public HTTPResponse (BufferedReader input) throws IOException {
		//Reading in the response and headers.
		ArrayList<String> unparsedRequest = new ArrayList<>();
		
		String line;
		while (!(line = input.readLine()).equals("")) {
			unparsedRequest.add(line);
		}
		
		//Parsing the read data into the object.
		parse(unparsedRequest);
		
		//Reading in the body if there is content of type text.
		if(headers.containsKey("Content-Type") && headers.get("Content-Type").matches("text.*")) {
			body = new String();
			while (((line = input.readLine()) != null)) {
				body = body.concat(line + "\r\n");
			}
		}
	}
	
	//Marking the words from a given list in the response's body.
	public void markSearchedWords(List<String> words) {
		for (String word : words) {
			body = body.replaceAll(word, "<mark><font color=\"red\">" + word + "</font></mark>");
		}
	}
	
	//Parsing the response and headers for ease of access.
	public void parse(ArrayList<String> unparsedRequest) {
		//Splitting the response's first line.
		String[] topLineSplit = unparsedRequest.get(0).split(" ");
		
		//Saving the data from the response's top line.
		version = topLineSplit[0];
		statusCode = Integer.parseInt(topLineSplit[1]);
		
		//Saving the phrase from the top line to the object - it may contain more than one word.
		StringBuilder phraseBuilder = new StringBuilder(topLineSplit[2]);
		for (int i = 3; i < topLineSplit.length ; i++) {
			phraseBuilder.append(topLineSplit[i]);
			phraseBuilder.append(" ");
		}
		phrase = phraseBuilder.toString().trim();
		
		//Saving the headers to the object.
		for (int i = 1; i < unparsedRequest.size(); i++){
				String[] headerLineSplit = unparsedRequest.get(i).split(": ");
				headers.put(headerLineSplit[0], headerLineSplit[1]);
		}
	}
	
	//Sending response via the provided writer.
	public void send(PrintWriter writer) throws IOException {
		//Sending the top line.
		writer.println(version + " " + statusCode + " " + phrase);
		writer.flush();
		
		//Sending the headers.
		headers.forEach((k, v) -> {
			writer.println(k + ": " + v);
			writer.flush();
		});
		
		//Inserting an empty line after the headers.
		writer.println();
		writer.flush();
		
		//Sending the body, if there is one.
		if (body != null) {
			writer.println(body);
			writer.flush();
		}
	}
}
