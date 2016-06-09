package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import util.AppConstants;

public class Server {
	private static ServerSocket serverSocket;
	private static int port;

	public static void main(String[] args) {
		try {
			port = AppConstants.SERVER_PORT;
			if( args.length == 1) {
				port = Integer.getInteger(args[0]);
			}
			serverSocket = new ServerSocket(port);
			
			System.out.println("Server started at port" +serverSocket.getLocalPort());
			while( true ) {
				Socket socket = serverSocket.accept();
				new ConnectionHandler(socket).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
