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
