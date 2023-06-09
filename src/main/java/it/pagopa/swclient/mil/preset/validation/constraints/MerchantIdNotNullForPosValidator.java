/**
 * 
 */
package it.pagopa.swclient.mil.preset.validation.constraints;

import it.pagopa.swclient.mil.bean.Channel;
import it.pagopa.swclient.mil.preset.bean.UnsubscribeHeaders;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MerchantIdNotNullForPosValidator implements ConstraintValidator<MerchantIdNotNullForPos, UnsubscribeHeaders> {
	/**
	 * @see jakarta.validation.ConstraintValidator#isValid(Object, ConstraintValidatorContext)
	 */
	@Override
	public boolean isValid(UnsubscribeHeaders unsubscriberHeader, ConstraintValidatorContext context) {
		String channel = unsubscriberHeader.getChannel();
		String merchantId = unsubscriberHeader.getMerchantId();
		return !(channel != null && channel.equals(Channel.POS) && merchantId == null);
	}
}
