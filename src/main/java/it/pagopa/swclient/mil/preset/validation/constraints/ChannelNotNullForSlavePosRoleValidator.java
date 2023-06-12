/**
 * 
 */
package it.pagopa.swclient.mil.preset.validation.constraints;

import org.eclipse.microprofile.jwt.JsonWebToken;

import it.pagopa.swclient.mil.preset.bean.UnsubscribeHeaders;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ChannelNotNullForSlavePosRoleValidator implements ConstraintValidator<ChannelNotNullForSlavePosRole, UnsubscribeHeaders> {
	/**
	 * @see jakarta.validation.ConstraintValidator#isValid(Object, ConstraintValidatorContext)
	 */
	
	@Inject
    JsonWebToken jwt;
	
	@Override
	public boolean isValid(UnsubscribeHeaders unsubscriberHeader, ConstraintValidatorContext context) {
		
		String channel = unsubscriberHeader.getChannel();
		return (jwt.getGroups().contains("SlavePos") && channel != null) || (jwt.getGroups().contains("InstitutionPortal") );
		
	}
}
