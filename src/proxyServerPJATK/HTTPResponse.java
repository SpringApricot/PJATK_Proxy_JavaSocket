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
	byte[] imageBody;
	
	public HTTPResponse (BufferedReader input, InputStream stream) throws IOException {
		String line;
		while (!(line = input.readLine()).equals("")) {
			unparsedRequest.add(line);
		}
		unparsedRequest.add("");
		
		parse();
		
		if(headers.containsKey("Content-Type") && headers.get("Content-Type").matches("text.*")) {
			//Reading text body
			body = new String();
			while (((line = input.readLine()) != null)) {
				System.out.println( "_O_O_O_O_O_O_O_O_O_O_O     " + line);
				body = body.concat(line + "\r\n");
			}
		} else if (headers.containsKey("Content-Type") && headers.get("Content-Type").matches("image.*")) {
			//Reading image body
			System.out.println("!!!!!!READING IMAGE!!!!!!!");
			int contentLength = Integer.parseInt(headers.get("Content-Length"));
			imageBody = new byte[contentLength];
			stream.read(imageBody, 0, contentLength);
			System.out.println(imageBody);
		}
		/*
		if (headers.containsKey("Content-Length")) {
			try {
				int contentLength = Integer.parseInt(headers.get("Content-Length"));
				/*if (headers.get("Content-Encoding").equals("gzip")) {
					decodeBody(contentLength, stream);
				} else {*\/
					char[] buffer = new char[contentLength];
					input.read(buffer, 0, contentLength);
					body = new String(buffer);
					body = body.trim();
				//}
				unparsedRequest.add(body);
			} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
			}
		}*/
	}
	
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
		
		//Reading all the headers to the hashmap - until I meet the empty line that indicates end of headers, start of the body.
				String unparsedLine;
				for (int i = 1; !((unparsedLine = unparsedRequest.get(i)).equals("")); i++){
					String[] headerLineSplit = unparsedRequest.get(i).split(": ");
					headers.put(headerLineSplit[0], headerLineSplit[1]);
				}
	}
	
	public void send(PrintWriter writer, OutputStream stream) throws IOException {
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
		} else if (imageBody != null) {
			stream.write(imageBody);
		}
	}
	
	//=====================DEBUG=====================
	
	public void printHeader() {
		System.out.println(unparsedRequest.get(0));
	}
	
	public String getHeader() {
		return unparsedRequest.get(0);
	}
	
	public String getBody() {
		return body;
	}
	
	public void printHeaders(String prefix) {
		headers.forEach((k, v) -> {
			System.out.print(prefix + k);
			System.out.print(": ");
			System.out.println(v);
		});
	}
}
