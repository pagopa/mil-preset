/**
 * 
 */
package it.pagopa.swclient.mil.preset.validation.constraints;

import org.eclipse.microprofile.jwt.JsonWebToken;

import it.pagopa.swclient.mil.preset.bean.UnsubscribeHeaders;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AcquirerIdNotNullForSlavePosRoleValidator implements ConstraintValidator<AcquirerIdNotNullForSlavePosRole, UnsubscribeHeaders> {
	/**
	 * @see jakarta.validation.ConstraintValidator#isValid(Object, ConstraintValidatorContext)
	 */
	
	@Inject
    JsonWebToken jwt;
	
	@Override
	public boolean isValid(UnsubscribeHeaders unsubscriberHeader, ConstraintValidatorContext context) {
		
		String acquirerId = unsubscriberHeader.getAcquirerId();
		return (jwt.getGroups().contains("SlavePos") && acquirerId != null) || (jwt.getGroups().contains("InstitutionPortal") );
		
	}
}
