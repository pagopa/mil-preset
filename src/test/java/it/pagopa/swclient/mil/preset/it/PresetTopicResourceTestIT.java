package it.pagopa.swclient.mil.preset.it;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import it.pagopa.swclient.mil.preset.bean.Notice;
import it.pagopa.swclient.mil.preset.bean.PaymentTransaction;
import it.pagopa.swclient.mil.preset.bean.Preset;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.List;

@QuarkusIntegrationTest
//@QuarkusTestResource(value=MongoTestResource.class,restrictToAnnotatedClass = true)
//@TestHTTPEndpoint(PresetsResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@QuarkusTestResource(KafkaCompanionResource.class)
class PresetTopicResourceTestIT {
//	@InjectKafkaCompanion
 //   KafkaCompanion companion;
	
//	@Test
//    void preset_200() {
//
//    	ProducerTask producerTask = companion.produce(byte[].class).usingGenerator(i -> new ProducerRecord<>("presets",SerializationUtils.serialize(getPaymentTransaction())),1);
//    	long count = producerTask.awaitCompletion().count();
//    	System.out.println(">[" + count + "]<");
//    	System.out.println(">>>>a");
//  }
	
	private PaymentTransaction getPaymentTransaction() {
    	PaymentTransaction transaction = new PaymentTransaction();
    	transaction.setTransactionId("517a4216840E461fB011036A0fd134E1");
    	transaction.setAcquirerId("4585625");
    	transaction.setChannel("POS");
    	transaction.setMerchantId("28405fHfk73x88D");
    	transaction.setTerminalId("0aB9wXyZ");
    	transaction.setInsertTimestamp("2023-04-11T16:20:34");
    	
    	Notice notice = new Notice();
    	notice.setPaymentToken("648fhg36s95jfg7DS");
    	notice.setPaTaxCode("15376371009");
    	notice.setNoticeNumber("485564829563528563");
    	notice.setAmount(Long.valueOf("12345"));
    	notice.setDescription("Health ticket for chest x-ray");
    	notice.setCompany("ASL Roma 2");
    	notice.setOffice("Office RoMA");
    	List<Notice> notices = new ArrayList<>();
    	notices.add(notice);
    	
    	Preset preset = new Preset();
    	preset.setPaTaxCode("15376371009");
    	preset.setSubscriberId("csl0kq");
    	preset.setPresetId("77457c64-0870-407a-b2cb-0f948b04fb9a");
    	transaction.setPreset(preset);
    	
    	transaction.setNotices(notices);
    	
    	transaction.setTotalAmount(1000L);
    	transaction.setFee(Long.valueOf("50"));
    	transaction.setStatus("SSS");
    	transaction.setPaymentMethod("CASH");
    	transaction.setPaymentTimestamp("2023-05-21T09:29:34.526");
    	transaction.setCloseTimestamp("2023-05-21T10:29:34.526");
    	return transaction;
    }
    
}
