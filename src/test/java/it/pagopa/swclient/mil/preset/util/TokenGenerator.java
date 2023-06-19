/**
 * 
 */
package it.pagopa.swclient.mil.preset.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;

import org.eclipse.microprofile.jwt.Claims;

import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtSignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TokenGenerator {

	static final Logger logger = LoggerFactory.getLogger(TokenGenerator.class);

	private TokenGenerator() {
	}

	private final static String PRIVATE_KEY = """
			-----BEGIN PRIVATE KEY-----
			MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCWK8UjyoHgPTLa
			PLQJ8SoXLLjpHSjtLxMqmzHnFscqhTVVaDpCRCb6e3Ii/WniQTWw8RA7vf4djz4H
			OzvlfBFNgvUGZHXDwnmGaNVaNzpHYFMEYBhE8VGGiveSkzqeLZI+Y02G6sQAfDtN
			qqzM/l5QX8X34oQFaTBW1r49nftvCpITiwJvWyhkWtXP9RP8sXi1im5Vi3dhupOh
			nelk5n0BfajUYIbfHA6ORzjHRbt7NtBl0L2J+0/FUdHyKs6KMlFGNw8O0Dq88qnM
			uXoLJiewhg9332W3DFMeOveel+//cvDnRsCRtPgd4sXFPHh+UShkso7+DRsChXa6
			oGGQD3GdAgMBAAECggEAAjfTSZwMHwvIXIDZB+yP+pemg4ryt84iMlbofclQV8hv
			6TsI4UGwcbKxFOM5VSYxbNOisb80qasb929gixsyBjsQ8284bhPJR7r0q8h1C+jY
			URA6S4pk8d/LmFakXwG9Tz6YPo3pJziuh48lzkFTk0xW2Dp4SLwtAptZY/+ZXyJ6
			96QXDrZKSSM99Jh9s7a0ST66WoxSS0UC51ak+Keb0KJ1jz4bIJ2C3r4rYlSu4hHB
			Y73GfkWORtQuyUDa9yDOem0/z0nr6pp+pBSXPLHADsqvZiIhxD/O0Xk5I6/zVHB3
			zuoQqLERk0WvA8FXz2o8AYwcQRY2g30eX9kU4uDQAQKBgQDmf7KGImUGitsEPepF
			KH5yLWYWqghHx6wfV+fdbBxoqn9WlwcQ7JbynIiVx8MX8/1lLCCe8v41ypu/eLtP
			iY1ev2IKdrUStvYRSsFigRkuPHUo1ajsGHQd+ucTDf58mn7kRLW1JGMeGxo/t32B
			m96Af6AiPWPEJuVfgGV0iwg+HQKBgQCmyPzL9M2rhYZn1AozRUguvlpmJHU2DpqS
			34Q+7x2Ghf7MgBUhqE0t3FAOxEC7IYBwHmeYOvFR8ZkVRKNF4gbnF9RtLdz0DMEG
			5qsMnvJUSQbNB1yVjUCnDAtElqiFRlQ/k0LgYkjKDY7LfciZl9uJRl0OSYeX/qG2
			tRW09tOpgQKBgBSGkpM3RN/MRayfBtmZvYjVWh3yjkI2GbHA1jj1g6IebLB9SnfL
			WbXJErCj1U+wvoPf5hfBc7m+jRgD3Eo86YXibQyZfY5pFIh9q7Ll5CQl5hj4zc4Y
			b16sFR+xQ1Q9Pcd+BuBWmSz5JOE/qcF869dthgkGhnfVLt/OQzqZluZRAoGAXQ09
			nT0TkmKIvlza5Af/YbTqEpq8mlBDhTYXPlWCD4+qvMWpBII1rSSBtftgcgca9XLB
			MXmRMbqtQeRtg4u7dishZVh1MeP7vbHsNLppUQT9Ol6lFPsd2xUpJDc6BkFat62d
			Xjr3iWNPC9E9nhPPdCNBv7reX7q81obpeXFMXgECgYEAmk2Qlus3OV0tfoNRqNpe
			Mb0teduf2+h3xaI1XDIzPVtZF35ELY/RkAHlmWRT4PCdR0zXDidE67L6XdJyecSt
			FdOUH8z5qUraVVebRFvJqf/oGsXc4+ex1ZKUTbY0wqY1y9E39yvB3MaTmZFuuqk8
			f3cg+fr8aou7pr9SHhJlZCU=
			-----END PRIVATE KEY-----""";
	
	public static String generate(String group) {
		
		String token = null;
		try {
			token = Jwt.issuer("https://test") 
				       .groups(new HashSet<>(Collections.singletonList(group)))
				       .claim(Claims.sub.name(), "5254f087-1214-45cd-94ae-fda53c835197")
				       .expiresIn(900000000)
				       .sign(getPrivateKey());
		} catch (JwtSignatureException | NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
			logger.error("Error while generating jwt token", e);
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

		return kf.generatePrivate(keySpec);
	}
	
	private static PublicKey getPublicKey(PrivateKey privateKey) {
		
	    RSAPrivateCrtKey rsaPrivateKey = (RSAPrivateCrtKey)privateKey;

	    RSAPublicKeySpec publicKeySpec = new java.security.spec.RSAPublicKeySpec(rsaPrivateKey.getModulus(), rsaPrivateKey.getPublicExponent());

	    KeyFactory keyFactory;
	    PublicKey myPublicKey = null;
		try {
			keyFactory = KeyFactory.getInstance("RSA");
			myPublicKey = keyFactory.generatePublic(publicKeySpec);
			
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			logger.error("Error while generating public key", e);
		}

		return myPublicKey;
	}

}
