package proxyServerPJATK;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

public class HTTPResponse {
	ArrayList<String> unparsedRequest = new ArrayList<>();
	String version;
	int statusCode;
	String phrase;
	HashMap<String, String> headers = new HashMap<String,String>();
	String body;
	
	public HTTPResponse (BufferedReader input, InputStream stream) throws IOException {
		String line;
		while (!(line = input.readLine()).equals("")) {
			unparsedRequest.add(line);
		}
		unparsedRequest.add("");
		
		parse();
		
		if (headers.containsKey("Content-Length")) {
			try {
				int contentLength = Integer.parseInt(headers.get("Content-Length"));
				/*if (headers.get("Content-Encoding").equals("gzip")) {
					decodeBody(contentLength, stream);
				} else {*/
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
		}
	}
	
	private void decodeBody(int contentLength, InputStream stream) throws IOException {
		System.out.println("       >>>>>>>> DECODING");
		InputStream gzipBodyStream = new GZIPInputStream(stream);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] byteBuffer = new byte[4096];
		int tmp;
		while((tmp = gzipBodyStream.read(byteBuffer)) > 0) {
			outputStream.write(byteBuffer, 0, tmp);
		}
		
		body = new String(outputStream.toByteArray(), "UTF-8");
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
	
	private void decodeBodyV1(int contentLength) throws IOException {
		System.out.println("       >>>>>>>> DECODING");
		InputStream bodyInputStream = new ByteArrayInputStream(body.getBytes());
		InputStream gzipBodyStream = new GZIPInputStream(bodyInputStream);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] byteBuffer = new byte[4096];
		int tmp;
		while((tmp = gzipBodyStream.read(byteBuffer)) > 0) {
			outputStream.write(byteBuffer, 0, tmp);
		}
		
		body = new String(outputStream.toByteArray(), "UTF-8");
	}
}
