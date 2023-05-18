/**
 * 
 */
package it.pagopa.swclient.mil.preset.validation.constraints;

import it.pagopa.swclient.mil.bean.Channel;
import it.pagopa.swclient.mil.preset.bean.UnsubscriberHeaders;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MerchantIdNotNullForPosValidator implements ConstraintValidator<MerchantIdNotNullForPos, UnsubscriberHeaders> {
	/**
	 * @see jakarta.validation.ConstraintValidator#isValid(Object, ConstraintValidatorContext)
	 */
	@Override
	public boolean isValid(UnsubscriberHeaders unsubscriberHeader, ConstraintValidatorContext context) {
		String channel = unsubscriberHeader.getChannel();
		String merchantId = unsubscriberHeader.getMerchantId();
		return !(channel != null && channel.equals(Channel.POS) && merchantId == null);
	}
}
