package proxyServerPJATK;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class HTTPResponse {
	ArrayList<String> unparsedRequest = new ArrayList<>();
	String version;
	int statusCode;
	String phrase;
	HashMap<String, String> headers = new HashMap<String,String>();
	String body;
	
	public HTTPResponse (BufferedReader input) throws IOException {
		//TODO
	}

	public void parse() {
		//TODO
	}
	
	public void send() throws IOException {
		//TODO
	}
}
