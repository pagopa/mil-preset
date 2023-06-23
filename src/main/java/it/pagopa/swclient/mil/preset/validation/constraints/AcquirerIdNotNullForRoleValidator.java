package it.pagopa.swclient.mil.preset.validation.constraints;

import it.pagopa.swclient.mil.preset.bean.UnsubscribeHeaders;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Arrays;
import java.util.List;

public class AcquirerIdNotNullForRoleValidator implements ConstraintValidator<AcquirerIdNotNullForRole, UnsubscribeHeaders> {

	@Inject
	JsonWebToken jwt;

	List<String> roles;

	@Override
	public void initialize(AcquirerIdNotNullForRole constraintAnnotation) {
		roles = Arrays.stream(constraintAnnotation.roles()).map(r -> r.label).toList();
	}

	@Override
	public boolean isValid(UnsubscribeHeaders unsubscribeHeaders, ConstraintValidatorContext context) {
		String acquirerId = unsubscribeHeaders.getAcquirerId();
		return (jwt.getGroups().stream().noneMatch(roles::contains) || acquirerId != null);
	}
}
