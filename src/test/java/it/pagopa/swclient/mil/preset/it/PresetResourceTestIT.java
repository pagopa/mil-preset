package it.pagopa.swclient.mil.preset.it;

import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;

import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import it.pagopa.swclient.mil.preset.ErrorCode;
import it.pagopa.swclient.mil.preset.bean.CreatePresetRequest;
import it.pagopa.swclient.mil.preset.bean.Notice;
import it.pagopa.swclient.mil.preset.bean.PaymentTransaction;
import it.pagopa.swclient.mil.preset.bean.PaymentTransactionStatus;
import it.pagopa.swclient.mil.preset.bean.PresetOperation;
import it.pagopa.swclient.mil.preset.bean.Subscriber;
import it.pagopa.swclient.mil.preset.dao.PresetEntity;
import it.pagopa.swclient.mil.preset.dao.SubscriberEntity;
import it.pagopa.swclient.mil.preset.resource.PresetsResource;
import it.pagopa.swclient.mil.preset.util.PresetTestData;
import it.pagopa.swclient.mil.preset.util.SubscriberTestData;

@QuarkusIntegrationTest
@TestProfile(IntegrationTestProfile.class)
@TestHTTPEndpoint(PresetsResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PresetResourceTestIT {
	
	static final Logger logger = LoggerFactory.getLogger(PresetResourceTestIT.class);
	
	final static String SESSION_ID		= "a6a666e6-97da-4848-b568-99fedccb642c";
	final static String API_VERSION		= "1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay";
	final static String PA_TAX_CODE		= "15376371009";
	final static String SUBSCRIBER_ID	= "a25tr0";
	
	DevServicesContext devServicesContext;
	
	Map<String, String> validMilHeaders;
	
	MongoClient mongoClient;
	
	CodecRegistry pojoCodecRegistry;
	
	Map<String, String> commonHeaders;
	Map<String, String> presetHeaders;
	
	SubscriberEntity subscriberEntity;
	
	@BeforeAll
	void createTestObjects() {
		validMilHeaders = PresetTestData.getMilHeaders(true, true);
		
		// initialize mongo client
		pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
				CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));

		String mongoExposedPort = devServicesContext.devServicesProperties().get("test.mongo.exposed-port");
		mongoClient = MongoClients.create("mongodb://127.0.0.1:" + mongoExposedPort);
		
		List<String> subscriberIdList = List.of(
				SubscriberTestData.SUBCRIBER_FOUND,
				SubscriberTestData.SUBCRIBER_NOT_FOUND
		);
		
		List<SubscriberEntity> subscriberEntityEntities = subscriberIdList.stream()
				.map(subId -> SubscriberTestData.getSubscribers(subId))
				.toList();

		
		MongoCollection<SubscriberEntity> collection = mongoClient.getDatabase("mil")
				.getCollection("subscribers", SubscriberEntity.class)
				.withCodecRegistry(pojoCodecRegistry);

		collection.insertMany(subscriberEntityEntities);
		
		
		MongoCollection<PresetEntity> collectionPreset = mongoClient.getDatabase("mil")
				.getCollection("presets", PresetEntity.class)
				.withCodecRegistry(pojoCodecRegistry);

		final String presetId = UUID.randomUUID().toString();
		
		PresetEntity presetEntity = PresetTestData.getPresetEntity("77457c64-0870-407a-b2cb-0f948b04fb9a","x46tr3");
		PaymentTransaction paymentTransaction = PresetTestData.getPaymentTransaction(
				PaymentTransactionStatus.PENDING,
				PresetTestData.getMilHeaders(true, true),
				PresetTestData.getPreset(presetId, "x46tr3"),
				1);
		presetEntity.presetOperation.setStatusDetails(paymentTransaction);
		collectionPreset.insertOne(presetEntity);
		
		presetHeaders = new HashMap<>();
		presetHeaders.put("RequestId", "d0d654e6-97da-4848-b568-99fedccb642b");
		presetHeaders.put("Version", API_VERSION);
		
		commonHeaders = new HashMap<>();
		commonHeaders.put("RequestId", "d0d654e6-97da-4848-b568-99fedccb642b");
		commonHeaders.put("Version", API_VERSION);
		commonHeaders.put("AcquirerId", "4585625");
		commonHeaders.put("Channel", "POS");
		commonHeaders.put("TerminalId", "0aB9wXyZ");
		commonHeaders.put("SessionId", SESSION_ID);
		commonHeaders.put("MerchantId", "4585625");
		
        Subscriber subscriber = new Subscriber();
        subscriber.setAcquirerId("4585625");
        subscriber.setChannel("POS");
        subscriber.setLabel("Reception POS");
        subscriber.setLastUsageTimestamp("2023-05-08T10:55:57");
        subscriber.setMerchantId("28405fHfk73x88D");
        subscriber.setPaTaxCode("15376371009");
        subscriber.setSubscriberId(SUBSCRIBER_ID);
        subscriber.setSubscriptionTimestamp("2023-05-05T09:31:33");
        subscriber.setTerminalId("0aB9wXyZ");
		
        subscriberEntity = new SubscriberEntity();
        subscriberEntity.id = SUBSCRIBER_ID;
        subscriberEntity.subscriber = subscriber;

	}
	
	@AfterAll
	void destroyTestObjects() {

		try {
			mongoClient.close();
		} catch (Exception e){
			logger.error("Error while closing mongo client", e);
		}

	}

	
	@Test
	void createPreset_201() {
		
		CreatePresetRequest request = new CreatePresetRequest();
		request.setNoticeNumber("485564829563528563");
		request.setNoticeTaxCode("15376371009");
		request.setOperationType("PAYMENT_NOTICE");
		request.setPaTaxCode("15376371009");
		request.setSubscriberId("x46tr0");
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(presetHeaders)
				.body(request)
				.and()
				.when()
				.post()
				.then()
				.extract()
				.response();
			
        Assertions.assertEquals(201, response.statusCode());
        Assertions.assertEquals(0, response.body().asString().length());


        final String locationPath =  "/presets/" + PA_TAX_CODE + "/" + SubscriberTestData.SUBCRIBER_FOUND + "/";
        Assertions.assertTrue(response.getHeader("Location") != null && response.getHeader("Location").contains(locationPath));
	}
	
	@Test
	void createPreset_400_subscriberNotFound() {
		
		CreatePresetRequest request = new CreatePresetRequest();
		request.setNoticeNumber("485564829563528563");
		request.setNoticeTaxCode("15376371009");
		request.setOperationType("PAYMENT_NOTICE");
		request.setPaTaxCode("15376371111");
		request.setSubscriberId("x46tr4");
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(presetHeaders)
				.body(request)
				.and()
				.when()
				.post()
				.then()
				.extract()
				.response();
			
        Assertions.assertEquals(400, response.statusCode());
        
        Assertions.assertNull(response.getHeader("Location"));
        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.SUBSCRIBER_NOT_FOUND));
	}
	
	@Test
	void getPresets_200() {
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(presetHeaders)
				.and()
				.when()
				.get("/15376371009/x46tr3")
				.then()
				.extract()
				.response();
			
        Assertions.assertEquals(200, response.statusCode());
        
        Assertions.assertNotNull(response.jsonPath().getJsonObject("presets"));
        
        List<PresetOperation> arr = response.jsonPath().getList("presets", PresetOperation.class);
        
        Assertions.assertNotNull(arr.get(0).getCreationTimestamp());
        Assertions.assertNotNull(arr.get(0).getNoticeNumber());
        
        Assertions.assertNotNull(arr.get(0).getNoticeTaxCode());
        Assertions.assertNotNull(arr.get(0).getOperationType());
        Assertions.assertNotNull(arr.get(0).getPaTaxCode());
        Assertions.assertNotNull(arr.get(0).getPresetId());
        Assertions.assertNotNull(arr.get(0).getStatus());
        Assertions.assertNotNull(arr.get(0).getStatusTimestamp());
        Assertions.assertNotNull(arr.get(0).getSubscriberId());
        PaymentTransaction statDetails = arr.get(0).getStatusDetails();
        Assertions.assertNotNull(statDetails.getAcquirerId());
        Assertions.assertNotNull(statDetails.getChannel());
        Assertions.assertNotNull(statDetails.getInsertTimestamp());
        Assertions.assertNotNull(statDetails.getAcquirerId());
        Assertions.assertNotNull(statDetails.getStatus());
        Assertions.assertNotNull(statDetails.getNotices());
        List<Notice>  noticesResponse = statDetails.getNotices();
        Assertions.assertNotNull(noticesResponse.get(0).getAmount());
        Assertions.assertNotNull(noticesResponse.get(0).getCompany());
        Assertions.assertNotNull(noticesResponse.get(0).getDescription());
        Assertions.assertNotNull(noticesResponse.get(0).getNoticeNumber());
        Assertions.assertNotNull(noticesResponse.get(0).getOffice());
        Assertions.assertNotNull(noticesResponse.get(0).getPaTaxCode());
        Assertions.assertNotNull(noticesResponse.get(0).getPaymentToken());
	}
	
	@Test
	void getPresets_200_emptyPreset() {
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(presetHeaders)
				.and()
				.when()
				.get("/15376371009/46t000")
				.then()
				.extract()
				.response();
			
        Assertions.assertEquals(200, response.statusCode());
        
        Assertions.assertNotNull(response.jsonPath().getJsonObject("presets"));
        List<PresetOperation> arr = response.jsonPath().getList("presets", PresetOperation.class);
        
        Assertions.assertEquals(0,arr.size());
	}
	
	@Test
	void getLastPreset_200() {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.when()
				.get("/15376371009/x46tr3/last_to_execute")
				.then()
				.extract()
				.response();
			
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertNotNull(response.jsonPath().getString("creationTimestamp"));
        Assertions.assertNotNull(response.jsonPath().getString("noticeNumber"));
        Assertions.assertNotNull(response.jsonPath().getString("noticeTaxCode"));
        Assertions.assertNotNull(response.jsonPath().getString("operationType"));
        Assertions.assertNotNull(response.jsonPath().getString("paTaxCode"));
        Assertions.assertNotNull(response.jsonPath().getString("presetId"));
        Assertions.assertNotNull(response.jsonPath().getString("status"));
        Assertions.assertNotNull(response.jsonPath().getString("statusTimestamp"));
        Assertions.assertNotNull(response.jsonPath().getString("subscriberId"));
        Assertions.assertNotNull(response.jsonPath().getJsonObject("statusDetails"));
        PaymentTransaction statDetails = response.jsonPath().getObject("statusDetails", PaymentTransaction.class);
        Assertions.assertNotNull(statDetails.getChannel());
        Assertions.assertNotNull(statDetails.getInsertTimestamp());
        Assertions.assertNotNull(statDetails.getAcquirerId());
        Assertions.assertNotNull(statDetails.getStatus());
        Assertions.assertNotNull(statDetails.getNotices());
        List<Notice>  noticesResponse = statDetails.getNotices();
        Assertions.assertNotNull(noticesResponse.get(0).getAmount());
        Assertions.assertNotNull(noticesResponse.get(0).getCompany());
        Assertions.assertNotNull(noticesResponse.get(0).getDescription());
        Assertions.assertNotNull(noticesResponse.get(0).getNoticeNumber());
        Assertions.assertNotNull(noticesResponse.get(0).getOffice());
        Assertions.assertNotNull(noticesResponse.get(0).getPaTaxCode());
        Assertions.assertNotNull(noticesResponse.get(0).getPaymentToken());
	}
	
}
