package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import util.AppConstants;
import util.CoolFTP;

public class ConnectionHandler extends Thread {
	Socket socket;

	public ConnectionHandler(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		InputStream inputStream;
		OutputStream outputStream = null;
		try {
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();

			int command = inputStream.read();
			switch (command) {
			case AppConstants.CommandCode.DOWNLOAD:
				System.out.println("Received DOWNLOAD");
				outputStream.write(AppConstants.CommandCode.OK);
				System.out.println("Sending OK");
				String file_name = new BufferedReader(new InputStreamReader(inputStream)).readLine();
				System.out.println("Received filename: " +file_name);
				File file = new File(AppConstants.UPLOAD_PATH + file_name);
				if (file.exists()) {
					InputStream fileInputStream = new FileInputStream(file);
					outputStream.write(AppConstants.CommandCode.ACCEPTED);
					System.out.println("Sending ACCEPTED");
					System.out.println("Sending file");
					new CoolFTP().sendFile(fileInputStream, outputStream, file.length(), AppConstants.DigestCode.NO_CHECK);
				}
				break;
			case AppConstants.CommandCode.UPLOAD:
				System.out.println("Received UPLOAD");
				System.out.println("Sending OK");
				outputStream.write(AppConstants.CommandCode.OK);
				byte[] filename = new byte[AppConstants.FILE_NAME_LENGTH];
				inputStream.read(filename);
				
				File outputFile = new File(AppConstants.UPLOAD_PATH +new String(filename).trim());
				System.out.println("Filename: " +outputFile.getName());
				if(!outputFile.exists()) {
					outputFile.createNewFile();
				}
				FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
				System.out.println("Receiving file");
				int returnCode = new CoolFTP().receiveFile(fileOutputStream, inputStream);
				if ( returnCode == AppConstants.CommandCode.OK) {
					System.out.println("Sending ACCEPTED");
					outputStream.write(AppConstants.CommandCode.ACCEPTED);
				} else {
					outputStream.write(returnCode);
					System.err.println("Didn't upload: " +returnCode);
				}
				outputStream.flush();
				fileOutputStream.close();
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
