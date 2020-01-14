package proxyServerPJATK;

import java.io.BufferedReader;
import java.io.IOException;
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
		
		//TODO
	}
	
	public HTTPResponse forward() throws UnknownHostException, IOException {
		//TODO
        return null;
	}
}
