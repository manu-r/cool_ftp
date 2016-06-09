package util;


public interface AppConstants {
	int FILE_NAME_LENGTH = 256;
	String UPLOAD_PATH = "./Uploads/";
	int SERVER_PORT = 1122;
	String INET_ADDR = "127.0.0.1";
	int CHUNK_SIZE = 1048576;	//1 MB
	String DIGEST_ALGORITHM = "SHA";
	String DOWNLOAD_PATH = "./Downloads/";
	
	public interface Commands {
		String UPLOAD = "upload";
		String DOWNLOAD = "download";
		String EXIT = "exit";
		String SUPLOAD = "supload";
		String SDOWNLOAD = "sdownload";
		String LIST = "list";
		String HELP = "help";
	}
	
	public interface CommandCode {
		int UPLOAD = 100;
		int DOWNLOAD = 101;
		int ACCEPTED = 201;
		int OK = 202;
		int WRONGHASH = 300;
		int FILE_NOT_FOUND = 350;
	}
	
	public interface DigestCode {
		int SHA = 10;
		int MD5 = 20;
		int NO_CHECK = 0;
	}
}
