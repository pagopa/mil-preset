package it.pagopa.swclient.mil.preset.it;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;
import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.TestProfile;
import it.pagopa.swclient.mil.preset.PresetStatus;
import it.pagopa.swclient.mil.preset.bean.Notice;
import it.pagopa.swclient.mil.preset.bean.PaymentTransaction;
import it.pagopa.swclient.mil.preset.bean.PaymentTransactionStatus;
import it.pagopa.swclient.mil.preset.bean.Preset;
import it.pagopa.swclient.mil.preset.bean.PresetOperation;
import it.pagopa.swclient.mil.preset.dao.PresetEntity;
import it.pagopa.swclient.mil.preset.util.PresetTestData;
import it.pagopa.swclient.mil.preset.utils.DateUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@QuarkusIntegrationTest
@TestProfile(IntegrationTestProfile.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PresetTopicResourceTestIT implements DevServicesContext.ContextAware {

	static final Logger logger = LoggerFactory.getLogger(PresetTopicResourceTestIT.class);

	DevServicesContext devServicesContext;

	KafkaProducer<String, PaymentTransaction> paymentTransactionProducer;

	CodecRegistry pojoCodecRegistry;

	MongoClient mongoClient;

	@Override
	public void setIntegrationTestContext(DevServicesContext devServicesContext) {
		this.devServicesContext = devServicesContext;
	}

	@BeforeAll
	void createTestData() {

		// initialize mongo client
		pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
				CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));

		String mongoExposedPort = devServicesContext.devServicesProperties().get("test.mongo.exposed-port");
		mongoClient = MongoClients.create("mongodb://127.0.0.1:" + mongoExposedPort);

		// initialize kafka producer
		Properties kafkaConfig = new Properties();
		kafkaConfig.put("bootstrap.servers", devServicesContext.devServicesProperties().get("test.kafka.bootstrap-server"));
		kafkaConfig.put("security.protocol", "SASL_PLAINTEXT");
		kafkaConfig.put("sasl.mechanism","SCRAM-SHA-256");
		kafkaConfig.put("sasl.jaas.config","org.apache.kafka.common.security.scram.ScramLoginModule required username=\"testuser\" password=\"testuser\";");
		kafkaConfig.put("linger.ms", 1);

		paymentTransactionProducer = new KafkaProducer<>(kafkaConfig, new StringSerializer(), new ObjectMapperSerializer<>());
	}

	@Test
	void consume_close_ok() {

		String presetId = UUID.randomUUID().toString();

		PaymentTransaction paymentTransaction = PresetTestData.getPaymentTransaction(
				PaymentTransactionStatus.PENDING,
				PresetTestData.getPosHeaders(true, true),
				PresetTestData.getPreset(presetId, "y46tr3"),
				1);

		PresetEntity presetEntity = PresetTestData.getPresetEntity(presetId, "y46tr3");

		mongoClient.getDatabase("mil")
				.getCollection("presets", PresetEntity.class)
				.withCodecRegistry(pojoCodecRegistry)
				.insertOne(presetEntity);

		String currentTimestamp = DateUtils.getCurrentTimestamp();

		paymentTransactionProducer.send(new ProducerRecord<>("presets", paymentTransaction));

		Awaitility
				.with().pollInterval(10, TimeUnit.SECONDS)
				.and().timeout(Duration.of(30, ChronoUnit.SECONDS))
				.await().until(() -> {
					PresetOperation presetOperation = getPresetOperation(presetId);
					//return presetOperation.getStatusTimestamp().compareTo(currentTimestamp) > 0;
					return presetOperation.getStatus().equals(PresetStatus.EXECUTED.name());
				});

		checkDatabaseData(presetId, PresetStatus.EXECUTED, paymentTransaction);

	}

	private PresetOperation getPresetOperation(String presetId) {

		MongoCollection<PresetEntity> collection = mongoClient.getDatabase("mil")
				.getCollection("presets", PresetEntity.class)
				.withCodecRegistry(pojoCodecRegistry);

		Bson filter = Filters.in("_id", presetId);
		FindIterable<PresetEntity> documents  = collection.find(filter);

		try (MongoCursor<PresetEntity> iterator = documents.iterator()) {
			Assertions.assertTrue(iterator.hasNext());
			PresetEntity presetEntity = iterator.next();
			logger.info("Found preset operation entry on DB: {}", presetEntity.presetOperation);
			return presetEntity.presetOperation;
		}
	}

	private void checkDatabaseData(String presetId, PresetStatus transactionStatus, PaymentTransaction paymentTransaction) {

		PresetOperation presetOperation = getPresetOperation(presetId);

		Assertions.assertEquals(PresetStatus.EXECUTED.name(), presetOperation.getStatus());

	}

	@AfterAll
	void tearDown() {

		try {
			mongoClient.close();
		} catch (Exception e){
			logger.error("Error while closing mongo client", e);
		}
		try {
			paymentTransactionProducer.close();
		} catch (Exception e){
			logger.error("Error while closing kafka producer", e);
		}

	}

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
