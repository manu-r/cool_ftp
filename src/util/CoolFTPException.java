package util;

public class CoolFTPException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	public CoolFTPException() {
		super("Unknown exception was raised.");
	}
	
	public CoolFTPException(String msg) {
		super(msg);
	}
}
