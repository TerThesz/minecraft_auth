package authentication;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class Hash {
    private static byte[] getHash(String input) throws NoSuchAlgorithmException
    { 
        MessageDigest md = MessageDigest.getInstance("SHA-256"); 
        return md.digest(input.getBytes(StandardCharsets.UTF_8)); 
    }
    
    private static String toHexString(byte[] hash)
    {
        BigInteger number = new BigInteger(1, hash); 
        StringBuilder hexString = new StringBuilder(number.toString(16)); 
  
        while (hexString.length() < 32) 
        { 
            hexString.insert(0, '0'); 
        } 
  
        return hexString.toString(); 
    }
  
    public static String hash(String message)
    { 
        try {
        	return toHexString(getHash(message));
        }
        catch (NoSuchAlgorithmException e) { 
            e.printStackTrace();
        } 
        
        return null;
    }
    
	public static String salt() {
	    int leftLimit = 97;
	    int rightLimit = 122;
	    int targetStringLength = 5;
	    Random random = new Random();

	    return random.ints(leftLimit, rightLimit + 1)
	      .limit(targetStringLength)
	      .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
	      .toString();
	}
}
