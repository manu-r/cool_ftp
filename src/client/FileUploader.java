package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import util.AppConstants;
import util.CoolFTP;

public class FileUploader {
	Socket socket;

	public FileUploader(Socket socket) {
		this.socket = socket;
	}

	public void upload(File file) {
		OutputStream outputStream = null;
		InputStream inputStream = null;
		FileInputStream fileInputStream = null;
		try {
			outputStream = this.socket.getOutputStream();
			inputStream = this.socket.getInputStream();
			fileInputStream = new FileInputStream(file);
			
			System.out.println("Sending UPLOAD");
			outputStream.write(AppConstants.CommandCode.UPLOAD);
			byte[] filename = ByteBuffer.allocate(AppConstants.FILE_NAME_LENGTH).put(file.getName().getBytes()).array();
			if(inputStream.read() == AppConstants.CommandCode.OK ) {
				outputStream.write(filename);
				System.out.println("Uploading file");
				new CoolFTP().sendFile(fileInputStream, outputStream, file.length(), AppConstants.DigestCode.NO_CHECK);
				if( inputStream.read() == AppConstants.CommandCode.ACCEPTED ) {
					System.out.println("Uploaded successfuly.");
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			try {
				outputStream.close();
				inputStream.close();
				fileInputStream.close();
				socket.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
}
