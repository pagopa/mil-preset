/**
 * 
 */
package it.pagopa.swclient.mil.preset.validation.constraints;

import it.pagopa.swclient.mil.bean.Channel;
import it.pagopa.swclient.mil.preset.bean.UnsubscribeHeaders;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Arrays;
import java.util.List;

public class MerchantIdNotNullForRoleValidator implements ConstraintValidator<MerchantIdNotNullForRole, UnsubscribeHeaders> {

	@Inject
	JsonWebToken jwt;

	List<String> roles;

	@Override
	public void initialize(MerchantIdNotNullForRole constraintAnnotation) {
		roles = Arrays.stream(constraintAnnotation.roles()).map(r -> r.label).toList();
	}

	@Override
	public boolean isValid(UnsubscribeHeaders unsubscribeHeaders, ConstraintValidatorContext context) {
		String channel = unsubscribeHeaders.getChannel();
		String merchantId = unsubscribeHeaders.getMerchantId();
		return (jwt.getGroups().stream().noneMatch(roles::contains) || !StringUtils.equals(channel, Channel.POS) || merchantId != null);
	}
}
