package it.pagopa.swclient.mil.preset.it;

import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import it.pagopa.swclient.mil.preset.bean.SubscribeRequest;
import it.pagopa.swclient.mil.preset.bean.Subscriber;
import it.pagopa.swclient.mil.preset.dao.SubscriberEntity;
import it.pagopa.swclient.mil.preset.resource.TerminalsResource;
import it.pagopa.swclient.mil.preset.util.SubscriberTestData;


@QuarkusIntegrationTest
@TestProfile(IntegrationTestProfile.class)
@TestHTTPEndpoint(TerminalsResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SubscribeResourceTestIT {
	static final Logger logger = LoggerFactory.getLogger(SubscribeResourceTestIT.class);
	
	final static String SESSION_ID		= "a6a666e6-97da-4848-b568-99fedccb642c";
	final static String API_VERSION		= "1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay";
	final static String PA_TAX_CODE		= "15376371009";
	final static String SUBSCRIBER_ID	= "a25tr0";
	
	DevServicesContext devServicesContext;
	
	MongoClient mongoClient;
	
	CodecRegistry pojoCodecRegistry;
	
	Map<String, String> commonHeaders;
	Map<String, String> presetHeaders;
	
	@BeforeAll
	void createTestObjects() {
		
		// initialize mongo client
		pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
				CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));

		String mongoExposedPort = devServicesContext.devServicesProperties().get("test.mongo.exposed-port");
		mongoClient = MongoClients.create("mongodb://127.0.0.1:" + mongoExposedPort);
		
		List<String> subscriberIdList = List.of(
				
				SubscriberTestData.UNSUBCRIBE
		);
		List<SubscriberEntity> subscriberEntityEntities = subscriberIdList.stream()
				.map(subId -> SubscriberTestData.getSubscribers(subId))
				.toList();

		
		MongoCollection<SubscriberEntity> collection = mongoClient.getDatabase("mil")
				.getCollection("subscribers", SubscriberEntity.class)
				.withCodecRegistry(pojoCodecRegistry);

		collection.insertMany(subscriberEntityEntities);
		
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
		commonHeaders.put("MerchantId", "23533");
	}
	
	
	@AfterAll
	void destroyTestObjects() {

		try {
			mongoClient.close();
		} catch (Exception e){
			logger.error("Error while closing mongo client", e);
		}

	}

	
	/* ****  get Subscribers **** */
	@Test
	void getSubscribers_200() {
		

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(presetHeaders)
				.and()
				.when()
				.get("/" + PA_TAX_CODE)
				.then()
				.extract()
				.response();
			
        Assertions.assertEquals(200, response.statusCode());
        
        Assertions.assertNotNull(response.jsonPath().getJsonObject("subscribers"));
        List<Subscriber> res = response.jsonPath().getList("subscribers", Subscriber.class);
        Assertions.assertNotNull(res.get(0).getChannel());
        Assertions.assertNotNull(res.get(0).getMerchantId());
        Assertions.assertNotNull(res.get(0).getTerminalId());
        Assertions.assertNotNull(res.get(0).getPaTaxCode());
        Assertions.assertNotNull(res.get(0).getSubscriberId());
        Assertions.assertNotNull(res.get(0).getLabel());
        Assertions.assertNotNull(res.get(0).getSubscriptionTimestamp());
        Assertions.assertNotNull(res.get(0).getLastUsageTimestamp());
	}
	
	@Test
	void getSubscribers_200_emptyListOfSubribers() {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(presetHeaders)
				.and()
				.when()
				.get("/" + "00000000000")
				.then()
				.extract()
				.response();
			
        Assertions.assertEquals(200, response.statusCode());
        
        Assertions.assertNotNull(response.jsonPath().getJsonObject("subscribers"));
        List<Subscriber> res = response.jsonPath().getList("subscribers", Subscriber.class);
        
        Assertions.assertEquals(0,res.size());
      
	}
	
	/* **** unsubscribe **** */
	@Test
	void unsubscriber_200() {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.when()
				.delete("/15376371009/" + SubscriberTestData.UNSUBCRIBE)
				.then()
				.extract()
				.response();
			
        Assertions.assertEquals(204, response.statusCode());
        Assertions.assertEquals(0,response.body().asString().length());
	}
	
	@Test
	void unsubscriber_404() {
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.when()
				.delete("/" + PA_TAX_CODE + "/a00aa0")
				.then()
				.extract()
				.response();
			
        Assertions.assertEquals(404, response.statusCode());
        Assertions.assertEquals(0,response.body().asString().length());
	}
	
	/* **** subscribe **** */
	@Test
	void subscribe_200() {
		
		SubscribeRequest request = new SubscribeRequest();
		request.setPaTaxCode("34576371029");
		request.setLabel("Reception POS");
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.body(request)
				.and()
				.when()
				.post()
				.then()
				.extract()
				.response();
			
        Assertions.assertEquals(201, response.statusCode());
        Assertions.assertEquals(0,response.body().asString().length());
      final String locationPath =  "/terminals/34576371029/" ;
      Assertions.assertTrue(response.getHeader("Location") != null && response.getHeader("Location").contains(locationPath));
	}
	@Test
	void subscribe_409() {

		SubscribeRequest request = new SubscribeRequest();
		request.setPaTaxCode("15376371009");
		request.setLabel("Reception POS");
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.body(request)
				.and()
				.when()
				.post()
				.then()
				.extract()
				.response();
			
        Assertions.assertEquals(409, response.statusCode());
        final String locationPath =  "/terminals/15376371009/" ;
        Assertions.assertTrue(response.getHeader("Location") != null && response.getHeader("Location").contains(locationPath));
	}
}
