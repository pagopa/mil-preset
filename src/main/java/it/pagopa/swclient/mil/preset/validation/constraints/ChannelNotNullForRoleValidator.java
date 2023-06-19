/**
 * 
 */
package it.pagopa.swclient.mil.preset.validation.constraints;

import org.eclipse.microprofile.jwt.JsonWebToken;

import it.pagopa.swclient.mil.preset.bean.UnsubscribeHeaders;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;

public class ChannelNotNullForRoleValidator implements ConstraintValidator<ChannelNotNullForRole, UnsubscribeHeaders> {

	@Inject
    JsonWebToken jwt;

	List<String> roles;

	@Override
	public void initialize(ChannelNotNullForRole constraintAnnotation) {
		roles = Arrays.stream(constraintAnnotation.roles()).map(r -> r.label).toList();
	}
	
	@Override
	public boolean isValid(UnsubscribeHeaders unsubscribeHeaders, ConstraintValidatorContext context) {
		String channel = unsubscribeHeaders.getChannel();
		return (jwt.getGroups().stream().noneMatch(roles::contains) || channel != null);
		
	}
}
