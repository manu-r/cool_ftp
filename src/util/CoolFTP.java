package util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class CoolFTP {
	private UpdateListener updateListener;
	private float updateRate;

	public CoolFTP() {
	}

	private byte[] prepareHeader(InputStream inputStream, int digestType, long payloadSize) {
		byte[] header;
		byte[] bPayloadSize = new byte[Long.BYTES]; 
		bPayloadSize = ByteBuffer.allocate(Long.BYTES).putLong(payloadSize).array();
		if (digestType == AppConstants.DigestCode.NO_CHECK) {
			header = new byte[Long.BYTES + 1];
			System.arraycopy(bPayloadSize, 0, header, 0, Long.BYTES);			
			header[Long.BYTES] = AppConstants.DigestCode.NO_CHECK;			
			return header;
		}
		byte[] checksum = calculateChecksum(inputStream, digestType);
		header = new byte[Long.BYTES + checksum.length + 1];
		System.arraycopy(bPayloadSize, 0, header, 0, Long.BYTES);
		header[Long.BYTES] = (byte) checksum.length;
		System.arraycopy(checksum, 0, header, Long.BYTES + 1, checksum.length);

		return header;
	}

	private byte[] calculateChecksum(InputStream inputStream, int digestType) {
		try {
			MessageDigest md = MessageDigest.getInstance(getDigestString(digestType));
			byte[] chunk = new byte[AppConstants.CHUNK_SIZE];
			int length = 0;
			while ((length = inputStream.read(chunk)) != -1) {
				md.update(chunk, 0, length);
			}
			return md.digest();
		} catch (IOException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setUpdateRate(float rate) {
		this.updateRate = rate;
	}

	public int receiveFile(OutputStream outputStream, InputStream inputStream) {
		try {
			byte[] fileSize = new byte[Long.BYTES];
			if(inputStream.read(fileSize) == -1) {
				throw new EOFException();
			}
			Long payloadSize = ByteBuffer.wrap(fileSize).getLong();
			
			int digestType = inputStream.read();
			if (digestType != AppConstants.DigestCode.NO_CHECK) {
				MessageDigest md = MessageDigest.getInstance(getDigestString(digestType));
				byte[] actualDigest = new byte[md.getDigestLength()];
				inputStream.read(actualDigest);
				byte[] receivedDigest = writeFromStream(inputStream, outputStream, AppConstants.CHUNK_SIZE, payloadSize, digestType);
				if (Arrays.equals(receivedDigest, actualDigest)) {
					return AppConstants.CommandCode.OK;
				} else
					return AppConstants.CommandCode.WRONGHASH;
			}
			writeFromStream(inputStream, outputStream, AppConstants.CHUNK_SIZE, payloadSize, digestType);
			return AppConstants.CommandCode.OK;
		} catch (IOException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			return -1;
		}
	}

	public void sendFile(InputStream inputStream, OutputStream outputStream, long payloadSize, int digestType) {
		byte[] header;
		header = prepareHeader(inputStream, digestType, payloadSize);
		try {
			outputStream.write(header);
			writeToStream(inputStream, outputStream, AppConstants.CHUNK_SIZE, payloadSize);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setUpdateListener(UpdateListener listener) {
		this.updateListener = listener;
	}

	private void writeToStream(InputStream input, OutputStream output, int chunkSize, long payloadSize) throws IOException {
		float updatePercent = 0;
		float lastUpdate = updatePercent;
		long readCount = 0;
		byte[] chunk = new byte[chunkSize];
		int length;
		while (readCount < payloadSize) {
			length = input.read(chunk);
			output.write(chunk, 0, length);
			readCount += length;
			updatePercent = readCount;
			if (updatePercent - lastUpdate >= this.updateRate) {
				callOnUpdate(updatePercent);
				lastUpdate = updatePercent;
			}
		}
		output.flush();
	}

	private byte[] writeFromStream(InputStream input, OutputStream output, int chunkSize, long payloadSize, int digestType)
			throws IOException {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA");
			if (digestType != AppConstants.DigestCode.NO_CHECK) {
				md = MessageDigest.getInstance(getDigestString(digestType));
			}
			float updatePercent = 0;
			float lastUpdate = updatePercent;
			int length;
			long readCount = 0;
			byte[] chunk = new byte[chunkSize];
			while (readCount < payloadSize) {
				length = input.read(chunk);
				output.write(chunk, 0, length);
				readCount += length;
				md.update(chunk, 0, length);
				updatePercent = readCount;
				if (updatePercent - lastUpdate >= this.updateRate) {
					callOnUpdate(updatePercent);
					lastUpdate = updatePercent;
				}
			}
			output.flush();
			if (digestType != AppConstants.DigestCode.NO_CHECK) {
				return md.digest();
			}
			return new byte[0];
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void callOnUpdate(float updatePercent) {
		if (this.updateListener != null) {
			this.updateListener.onUpdate(updatePercent);
		}
	}

	private String getDigestString(int digest) {
		switch (digest) {
		case AppConstants.DigestCode.SHA:
			return "SHA";
		case AppConstants.DigestCode.MD5:
			return "MD5";
		default:
			return "SHA";
		}
	}
}
