/**
 * The main class
 * Start three server side threads for three ports to continuously accept client connections
 *
 * @author Dongyang Yao (dongyang.yao@rutgers.edu)
 */

import java.io.*;
import java.util.*;
import java.net.*;

public class MapChatServer {

    // The port number for receiving request from clients
    public static final int PORT_REQUEST = 4444;

    // The port number for relaying a request from a client to another or several other clients 
    public static final int PORT_PUSH = 5555;

    // The port number for transfering large file, e.g., image
    public static final int PORT_TRANSFER = 6666;

    // Database
    public static final String DATABASE_NAME = "MapChat";

    public static final String USER_NAME = "root";

    public static final String PASSWORD = "easy";

    public static final String URL = "jdbc:mysql://localhost:3306/";

    public static final String DRIVER = "com.mysql.jdbc.Driver";

    // Chatting group table
    public static Hashtable<String, Group> groupTable;

    // Client connection table
    public static Hashtable<String, Link> linkTable;

    // Client threads in push port
    public static ArrayList<ClientThread_push> pushes_threads;

    public static void main(String[] args) {
        groupTable = new Hashtable<String, Group>();
	linkTable = new Hashtable<String, Link>();
        pushes_threads = new ArrayList<ClientThread_push>();

	// Start request port server thread
        ServerThread serverThread_request = new ServerThread(PORT_REQUEST);
        (new Thread(serverThread_request)).start();

	// Start push port server thread
        ServerThread serverThread_push = new ServerThread(PORT_PUSH);
        (new Thread(serverThread_push)).start();

	// Start transfer server thread
        ServerThread serverThread_transfer = new ServerThread(PORT_TRANSFER);
        (new Thread(serverThread_transfer)).start();
    }

    /**
     * Copy bytes from input stream to output stream
     * @param in input stream
     * @param out output stream
     * @param length number of bytes
     */
    public static void copy(InputStream in, OutputStream out, long length) throws IOException {
        byte[] buf = new byte[8192];
        int len = 0;
        while (length > 0) {
            len = in.read(buf);
            out.write(buf, 0, len);
            length -= len;
        }
    }

    /**
     * Add a new push thread by IP
     * @param ip the ip address sequence
     * @param clientThread the ClientThread_push reference
     * @return if it is in push threads
     */
    public static boolean addPushThreadByIP(String[] ip, ClientThread_push clientThread) {
	boolean isInThreads = false;

	// Check if the ip is already in the push threads
	for (int i = 0; i < pushes_threads.size(); i++) {
	    if (pushes_threads.get(i).ip.equals(ip[0])) {
		System.out.println("already in push threads");
		pushes_threads.set(i, clientThread);
		isInThreads = true;
	    }
	}

	if (!isInThreads) {
	    pushes_threads.add(clientThread);
	}
	
	return isInThreads;
    }

    /**
     * Get the push thread reference by IP
     * @param ip the ip address of client
     * @return null if not exists
     */
    public static ClientThread_push findPushThreadByIP(String[] ip) {
	ClientThread_push push = null;
	for (ClientThread_push p : pushes_threads) {
	    if (p.ip.equals(ip[0])) {
		push = p;
	    }
	}
	return push;
    }

    /**
     * Send a command to all other group members
     * By pushing the command to their push thread buffers
     * @param group the group reference
     * @param identification the requesting client's name
     * @param command the message to be sent
     * @return the requesting client's link reference
     */
    public static Link sendToAllGroupMembers(Group group, String identification, String command) {
	Link self = null;
	
	for (Link l : group.links) {
	    String member_name = l.name;
	    if (!(member_name.equals(identification))) {
		System.out.println("send to: " + member_name);

		String[] ip = l.client.getRemoteSocketAddress().toString().split(":");

		ClientThread_push push = findPushThreadByIP(ip);

		if (push != null) {
		    push.pushes.add(command);
		}

	    } else {
		self = l;
	    }
	}

	return self;
    }

}