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

public class TerminalIdNotNullForRoleValidator implements ConstraintValidator<TerminalIdNotNullForRole, UnsubscribeHeaders> {

	@Inject
    JsonWebToken jwt;

	List<String> roles;

	@Override
	public void initialize(TerminalIdNotNullForRole constraintAnnotation) {
		roles = Arrays.stream(constraintAnnotation.roles()).map(r -> r.label).toList();
	}
	
	@Override
	public boolean isValid(UnsubscribeHeaders unsubscribeHeaders, ConstraintValidatorContext context) {
		String terminalId = unsubscribeHeaders.getTerminalId();
		return (jwt.getGroups().stream().noneMatch(roles::contains) || terminalId != null);
		
	}
}
