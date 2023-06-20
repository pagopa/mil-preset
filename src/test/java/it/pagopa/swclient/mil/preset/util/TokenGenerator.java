/**
 * 
 */
package it.pagopa.swclient.mil.preset.util;

import java.security.PrivateKey;
import java.util.Collections;
import java.util.HashSet;

import it.pagopa.swclient.mil.preset.bean.Role;
import org.eclipse.microprofile.jwt.Claims;

import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtSignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TokenGenerator {

	static final Logger logger = LoggerFactory.getLogger(TokenGenerator.class);

	private PrivateKey privateKey;

	private String keyId;

	public TokenGenerator(String keyId, PrivateKey privateKey) {
		this.keyId = keyId;
		this.privateKey = privateKey;
	}

	public String getToken(Role role) {
		
		String token = null;
		try {
			token = Jwt
					.issuer("http://wiremock-it:8080")
					.groups(new HashSet<>(Collections.singletonList(role.label)))
					.claim(Claims.sub.name(), "5254f087-1214-45cd-94ae-fda53c835197")
					.expiresIn(3600)
					.jws()
					.keyId(keyId)
					.sign(privateKey);
		} catch (JwtSignatureException e) {
			logger.error("Error while generating jwt token", e);
		}

		return token;
	}

}
