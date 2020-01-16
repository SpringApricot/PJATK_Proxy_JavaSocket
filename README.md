# Lightweight Java HTTP proxy
A proxy server in Java 8 using the Socket and ServerSocket classes. The principle is to only use those two classes from the java.net package. The server only supports HTTP (not HTTPS). It's a lightweight proxy, only supporting text files.

This server was creating as an assignemt at the Polish-Japanese Academy of Information Technology in Warsaw.
Year 2, course _SKJ_.

Student Joanna Juszczak, _s18344_.

## Assignemt requirements
- LIGHT version: HTTP proxy only supporting text files.
- HEAVY version: HTTP proxy supporting all types of media.
- Multithreading: support for handling multiple clients/connections at any given time.
- Filtering: provided a list of words, the proxy find and highlights those words.
- Caching: the proxy saves the recieved files to a cache. Asking for a file that has already been chached makes the proxy fetch it from the cache rathen than again send the request to the host.
- Settings: a simple settings file is to be provided as the argument at startup. It contains the proxy's port, the caching directory and the list of filtering words.
### Implemented features
LIGHT proxy, multithreading, filtering, caching, settings.
### Unimplemented features
HEAVY proxy: there is no support for anything but text files. Obviously that makes the proxy run in the LIGHT mode by default, despite the assignemt stating the the HEAVY mode should be the default one.

## Short design overview
Upon launching, a ProxyServer object is created. It parses the settings file and open a ServerSocket at the desired port. From this moment, the proxy listens on this port and opens a new thread for each incoming request - an object of type ProxyThread.

ProxyThread creates a HTTPRequest object with the data from the socket's stream. If the incoming request is valid (there is data coming from the stream), it is parsed and saved into the object (an exception handled by the ProxyThread object is thrown if the request is invalid).

After saving the request, ProxyThread checks if it's not a CONNECT request - if it is, the request is ignored, as HTTPS is not supported.
Then, if this request was not yet received, the request is forwarder to the target server and the response is saved and parsed in a HTTPResponse object. It is then saved to a file in cache. The name of the file is the hashcode of the request - this allows us to ommit the "/" and "." replacement in the requests and prevents generating files with names too long for the operating system.
Note that the request is ignored if it's Content-Type is not text/* - as mentioned above, only text is supported.

After finding the cached file (or receiving the response and saving it to ther cache), the file is read and forwarded to the browser. If the request was not forwarded to the server (thus the answer going to the browser is from the cache), "!!CACHED!!" is added to the beggining of the site's title. The streams and socket are then closed.
