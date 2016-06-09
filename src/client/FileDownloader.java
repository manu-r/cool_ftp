package client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import util.AppConstants;
import util.CoolFTP;

public class FileDownloader {	
	Socket socket;
	
	public FileDownloader(Socket socket) {
		this.socket = socket;
	}
	

	public void download(String filename, String downloadPath) {
		try {
			OutputStream outputStream = this.socket.getOutputStream();
			InputStream inputStream = this.socket.getInputStream();
			outputStream.write(AppConstants.CommandCode.DOWNLOAD);
			if(inputStream.read() == AppConstants.CommandCode.OK) {
				System.out.println("Received OK; Writing filename.");
				byte[] bFilename = ByteBuffer.allocate(AppConstants.FILE_NAME_LENGTH).put(filename.getBytes()).array();
				outputStream.write(bFilename);	
			}
			switch(inputStream.read()) {
			case AppConstants.CommandCode.ACCEPTED:
				File file = new File(downloadPath + filename);
				if(!file.exists()) {
					file.createNewFile();
				}
				System.out.println("Downloading...");
				if( new CoolFTP().
						receiveFile(
								new FileOutputStream(file), 
								socket.getInputStream()) 
								== AppConstants.CommandCode.OK ) {
					System.out.println("File downloaded!");
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
