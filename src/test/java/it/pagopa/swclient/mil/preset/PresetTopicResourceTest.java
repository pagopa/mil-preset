package it.pagopa.swclient.mil.preset;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;
import io.smallrye.reactive.messaging.kafka.companion.ProducerTask;
import it.pagopa.swclient.mil.preset.bean.Notice;
import it.pagopa.swclient.mil.preset.bean.PaymentTransaction;
import it.pagopa.swclient.mil.preset.bean.Preset;
import it.pagopa.swclient.mil.preset.dao.PresetRepository;
import it.pagopa.swclient.mil.preset.dao.PresetsEntity;
import it.pagopa.swclient.mil.preset.resource.PresetTopicResource;
import it.pagopa.swclient.mil.preset.utils.DateUtils;
import it.pagopa.swclient.mil.preset.utils.PresetSerializer;


@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTestResource(KafkaCompanionResource.class)
class PresetTopicResourceTest{
//
	@InjectKafkaCompanion 
    KafkaCompanion companion;
	
//	@Inject
//    @Channel("presets")
//    Emitter<PaymentTransaction> emitter;
	
	@InjectMock
	PresetRepository presetRepository;
	
    @Test
    void preset_200() {
		
		final String timestamp = DateUtils.getAndFormatCurrentDate();
		PresetsEntity presetEntity = new PresetsEntity();
		presetEntity.setId("77457c64-0870-407a-b2cb-0f948b04fb9a");
		presetEntity.setPresetId("77457c64-0870-407a-b2cb-0f948b04fb9a");
		presetEntity.setCreationTimestamp(timestamp);
		presetEntity.setNoticeNumber("485564829563528563");
		presetEntity.setNoticeTaxCode("15376371009");
		presetEntity.setOperationType(OperationType.PAYMENT_NOTICE.name());
		presetEntity.setPaTaxCode("15376371009");
		presetEntity.setStatus(PresetStatus.TO_EXECUTE.name());
		presetEntity.setStatusTimestamp(timestamp);
		presetEntity.setSubscriberId("csl0kq");
    	
		Mockito
		.when(presetRepository.list(Mockito.any(String.class), Mockito.any(Map.class)))
		.thenReturn(Uni.createFrom().item(presetEntity));
    	
    	
//    	CompletionStage<Void> ack = emitter.send(getPaymentTransaction());
    	PresetSerializer s = new PresetSerializer();
    	ProducerTask producerTask = companion.produce(byte[].class).usingGenerator(i -> new ProducerRecord<>("presets",s.serialize("presets", getPaymentTransaction())),2);
    	 
    	 
    	
//    	ProducerTask producerTask = companion.produce(byte[].class).usingGenerator(i -> new ProducerRecord<>("presets",SerializationUtils.serialize(getPaymentTransaction())),1);
    	long count = producerTask.awaitCompletion().count();
    	
    	System.out.println(">[" + count + "]<");	
    	
//    	System.out.println(">>>>b");
//    	ConsumerTask<String, byte[]> consumerTask = companion.consume(byte[].class).fromTopics("preset-result", 1);
//    	System.out.println(">>>>c");
//    	ConsumerRecord<String, byte[]> lastRecord = consumerTask.awaitCompletion().getLastRecord();
//    	
//    	System.out.println(">>>>d " + lastRecord.topic());
    	
    	
    	
    }

/*
    @Test
    void preset_200() {
    	
    	  CompletionStage<Void> ack = testEmitter.send(getPaymentTransaction());
    	System.out.println(">>>>a");
    	
//    	ProducerTask producerTask = companion.produce(byte[].class).usingGenerator(i -> new ProducerRecord<>("presets",SerializationUtils.serialize(getPaymentTransaction())),1);   
//    	long count = producerTask.awaitCompletion().count();
//    	System.out.println(">[" + count + "]<");
    	
//    	System.out.println(">>>>b");
//    	ConsumerTask<String, byte[]> consumerTask = companion.consume(byte[].class).fromTopics("presets", 1);
//    	ConsumerRecord<String, byte[]> lastRecord = consumerTask.awaitCompletion().getLastRecord();
//    	
//    	System.out.println(">>>>c " + new String(lastRecord.value()));

    	
    	
//    	  System.out.println(">>>>");
//          ConsumerTask<String, String> orders = companion.consumeStrings().fromTopics("presets", 10); 
//          orders.awaitCompletion();
//          System.out.println(">>>><<<<");
//          assertEquals(10, orders.count());
          
//    	ProducerRecord<String, PaymentTransaction> p = new ProducerRecord<String, PaymentTransaction>("presets", getPaymentTransaction());
//    	companion.produce(p.getClass());
//    	
//    	companion.topics().create("presets", 1);
//    	
//    	companion.registerSerde(PaymentTransaction.class, new PaymentTransaction()));
//    	companion.produce(getPaymentTransaction().getClass());
    }
    */
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
