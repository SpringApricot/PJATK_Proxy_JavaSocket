//A simple text-only HTTP proxy server.
//Created for an assignment for the SKJ course at PJATK in Warsaw, Poland.
//Author: Joanna Juszczak (s18344).
//Created 2020.

package proxyServerPJATK;

public class InvalidRequestException extends Exception {
	public InvalidRequestException(String errorMsg) {
		super(errorMsg);
	}
}
