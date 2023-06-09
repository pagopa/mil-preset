/**
 * 
 */
package it.pagopa.swclient.mil.preset.utils;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import it.pagopa.swclient.mil.preset.bean.PaymentTransaction;

public class PresetDeserializer  extends ObjectMapperDeserializer<PaymentTransaction> {
    public PresetDeserializer() {
        super(PaymentTransaction.class);
    }
}
