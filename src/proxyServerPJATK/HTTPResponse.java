package proxyServerPJATK;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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
	
	byte[] byteBuffer;
	
	public HTTPResponse (BufferedReader input, InputStream stream) throws IOException {
		byteBuffer = new byte[16384];
		int n;
		
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		
		while ((n = stream.read(byteBuffer, 0, byteBuffer.length)) != -1) {
			byteStream.write(byteBuffer, 0, n);
		}
		
		byte[] byteData = byteStream.toByteArray();
		
		String fullData = byteData.toString();
		
		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!                 " + byteData);
		
		
		
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
