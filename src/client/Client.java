package client;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import util.AppConstants;

public class Client {
	public static void main(String[] args) {
		String inetAddress = AppConstants.INET_ADDR;
		int port = AppConstants.SERVER_PORT;
		Socket socket = null;

		try {
			if (args.length == 2) {
				inetAddress = args[0];
				port = Integer.valueOf(args[1]);
			}
			System.out.printf("Connecting to %s:%d%n", inetAddress, port);
			socket = new Socket(inetAddress, port);

			Scanner scanner = new Scanner(System.in);
			String filename = "";
			String command;
			do {
				System.out.print("CoolFTP> ");
				command = scanner.next();
				command = command.toLowerCase();
				System.out.println("Command: " + command);
				switch (command) {
				case AppConstants.Commands.DOWNLOAD:
					System.out.println("Download command");
					if (scanner.hasNext()) {
						filename = scanner.next();
						System.out.println("File name: " + filename);
						if (socket.isClosed()) {
							socket = new Socket(inetAddress, port);
						}
						new FileDownloader(socket).download(filename, AppConstants.DOWNLOAD_PATH);
					}
					break;
				case AppConstants.Commands.UPLOAD:
					System.out.println("Upload command");
					if (scanner.hasNext()) {
						filename = scanner.next();
						System.out.println("File name: " + filename);
						File file = new File(filename);
						if (file.exists()) {
							if (socket.isClosed()) {
								socket = new Socket(inetAddress, port);
							}
							new FileUploader(socket).upload(file);
						} else {
							System.out.println("Cannot find the file.");
						}
					}
					break;
				case AppConstants.Commands.EXIT:
					scanner.close();
					System.exit(0);
				
				default:
					System.out.println("Invalid command. Enter HELP to get help.");
				}
			} while (true);

		} catch (IOException e) {
			System.err.println("Cannot connect to the specified host.");
			System.exit(1);
		}
	}
}
