/**
 * 
 */
package it.pagopa.swclient.mil.preset.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

import org.eclipse.microprofile.jwt.Claims;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;

import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtSignatureException;

public class TokenGenerator {
	/**
	 * 
	 */
	private TokenGenerator() {
	}


	private final static String PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\r\n"
			+ "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCWK8UjyoHgPTLa\r\n"
			+ "PLQJ8SoXLLjpHSjtLxMqmzHnFscqhTVVaDpCRCb6e3Ii/WniQTWw8RA7vf4djz4H\r\n"
			+ "OzvlfBFNgvUGZHXDwnmGaNVaNzpHYFMEYBhE8VGGiveSkzqeLZI+Y02G6sQAfDtN\r\n"
			+ "qqzM/l5QX8X34oQFaTBW1r49nftvCpITiwJvWyhkWtXP9RP8sXi1im5Vi3dhupOh\r\n"
			+ "nelk5n0BfajUYIbfHA6ORzjHRbt7NtBl0L2J+0/FUdHyKs6KMlFGNw8O0Dq88qnM\r\n"
			+ "uXoLJiewhg9332W3DFMeOveel+//cvDnRsCRtPgd4sXFPHh+UShkso7+DRsChXa6\r\n"
			+ "oGGQD3GdAgMBAAECggEAAjfTSZwMHwvIXIDZB+yP+pemg4ryt84iMlbofclQV8hv\r\n"
			+ "6TsI4UGwcbKxFOM5VSYxbNOisb80qasb929gixsyBjsQ8284bhPJR7r0q8h1C+jY\r\n"
			+ "URA6S4pk8d/LmFakXwG9Tz6YPo3pJziuh48lzkFTk0xW2Dp4SLwtAptZY/+ZXyJ6\r\n"
			+ "96QXDrZKSSM99Jh9s7a0ST66WoxSS0UC51ak+Keb0KJ1jz4bIJ2C3r4rYlSu4hHB\r\n"
			+ "Y73GfkWORtQuyUDa9yDOem0/z0nr6pp+pBSXPLHADsqvZiIhxD/O0Xk5I6/zVHB3\r\n"
			+ "zuoQqLERk0WvA8FXz2o8AYwcQRY2g30eX9kU4uDQAQKBgQDmf7KGImUGitsEPepF\r\n"
			+ "KH5yLWYWqghHx6wfV+fdbBxoqn9WlwcQ7JbynIiVx8MX8/1lLCCe8v41ypu/eLtP\r\n"
			+ "iY1ev2IKdrUStvYRSsFigRkuPHUo1ajsGHQd+ucTDf58mn7kRLW1JGMeGxo/t32B\r\n"
			+ "m96Af6AiPWPEJuVfgGV0iwg+HQKBgQCmyPzL9M2rhYZn1AozRUguvlpmJHU2DpqS\r\n"
			+ "34Q+7x2Ghf7MgBUhqE0t3FAOxEC7IYBwHmeYOvFR8ZkVRKNF4gbnF9RtLdz0DMEG\r\n"
			+ "5qsMnvJUSQbNB1yVjUCnDAtElqiFRlQ/k0LgYkjKDY7LfciZl9uJRl0OSYeX/qG2\r\n"
			+ "tRW09tOpgQKBgBSGkpM3RN/MRayfBtmZvYjVWh3yjkI2GbHA1jj1g6IebLB9SnfL\r\n"
			+ "WbXJErCj1U+wvoPf5hfBc7m+jRgD3Eo86YXibQyZfY5pFIh9q7Ll5CQl5hj4zc4Y\r\n"
			+ "b16sFR+xQ1Q9Pcd+BuBWmSz5JOE/qcF869dthgkGhnfVLt/OQzqZluZRAoGAXQ09\r\n"
			+ "nT0TkmKIvlza5Af/YbTqEpq8mlBDhTYXPlWCD4+qvMWpBII1rSSBtftgcgca9XLB\r\n"
			+ "MXmRMbqtQeRtg4u7dishZVh1MeP7vbHsNLppUQT9Ol6lFPsd2xUpJDc6BkFat62d\r\n"
			+ "Xjr3iWNPC9E9nhPPdCNBv7reX7q81obpeXFMXgECgYEAmk2Qlus3OV0tfoNRqNpe\r\n"
			+ "Mb0teduf2+h3xaI1XDIzPVtZF35ELY/RkAHlmWRT4PCdR0zXDidE67L6XdJyecSt\r\n"
			+ "FdOUH8z5qUraVVebRFvJqf/oGsXc4+ex1ZKUTbY0wqY1y9E39yvB3MaTmZFuuqk8\r\n"
			+ "f3cg+fr8aou7pr9SHhJlZCU=\r\n"
			+ "-----END PRIVATE KEY-----";
	
	public static String generate(String group) {
		
		String token = null;
		try {
			token = Jwt.issuer("https://test") 
				       .groups(new HashSet<>(Arrays.asList(group))) 
				       .claim(Claims.sub.name(), "5254f087-1214-45cd-94ae-fda53c835197")
				       .expiresIn(900000000)
				       .sign(getPrivateKey());
		} catch (JwtSignatureException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return token;
	}
	
	
	private static PrivateKey getPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		StringBuilder pkcs8Lines = new StringBuilder();
        BufferedReader rdr = new BufferedReader(new StringReader(PRIVATE_KEY));
        String line;
        while ((line = rdr.readLine()) != null) {
            pkcs8Lines.append(line);
        }
        
        // Remove the "BEGIN" and "END" lines, as well as any whitespace
        
        String pkcs8Pem = pkcs8Lines.toString();
        pkcs8Pem = pkcs8Pem.replace("-----BEGIN PRIVATE KEY-----", "");
        pkcs8Pem = pkcs8Pem.replace("-----END PRIVATE KEY-----", "");
        pkcs8Pem = pkcs8Pem.replaceAll("\\s+","");
        
        // Base64 decode the result
        
        byte [] pkcs8EncodedBytes = Base64.getDecoder().decode(pkcs8Pem);
        
        // extract the private key
        
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8EncodedBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privKey = kf.generatePrivate(keySpec);
        
        return privKey;
	}
	
	private static PublicKey getPublicKey(PrivateKey privKey) {
		
	    RSAPrivateCrtKey privk = (RSAPrivateCrtKey)privKey;

	    RSAPublicKeySpec publicKeySpec = new java.security.spec.RSAPublicKeySpec(privk.getModulus(), privk.getPublicExponent());

	    KeyFactory keyFactory;
	    PublicKey myPublicKey = null;
		try {
			keyFactory = KeyFactory.getInstance("RSA");
			myPublicKey = keyFactory.generatePublic(publicKeySpec);
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return myPublicKey;
	}
	
	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
	

//		PrivateKey pk = getPrivateKey();
////		 RSAPrivateCrtKey privk = (RSAPrivateCrtKey)pk;
//		PublicKey pu = getPublicKey(pk);
//		// Convert to JWK format
//		JWK jwk = new RSAKey.Builder((RSAPublicKey)pu)
//		    .privateKey((RSAPrivateKey)pk)
//		    .keyUse(KeyUse.SIGNATURE)
//		    .keyID(UUID.randomUUID().toString())
//		    .issueTime(new Date())
//		    .build();
		System.out.println(generate("SlavePos"));
	}
}
