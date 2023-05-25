/**
 * 
 */
package it.pagopa.swclient.mil.preset.utils;

import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;
import it.pagopa.swclient.mil.preset.bean.PaymentTransaction;

public class PresetSerializer extends ObjectMapperSerializer<PaymentTransaction>{
	public PresetSerializer() {
        super();  
        }
}
