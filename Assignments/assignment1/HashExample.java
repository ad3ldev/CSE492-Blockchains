
import java.security.MessageDigest;

public class HashExample {
	private static void trial(byte a) throws Exception {
		byte[] input = new byte[] { a };
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(input);
		byte[] digest = md.digest();
		System.out.print(String.format("%X: ", a));
		System.out.println("Digest of input is " + toHex(digest));
	}

	private static String toHex(byte[] in) {
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < in.length; i++) {
			int val = 0xFF & in[i];
			hexString.append(String.format("%02X", val));
		}
		return hexString.toString();
	}

	public static void main(String args[]) throws Exception {
		// This code calculates the SHA-256 hash of a single byte with value 0.
		byte a = 0;
		for (a = 0; a < 10; a++) {
			trial(a);
		}
	}
}
