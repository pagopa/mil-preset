package it.pagopa.swclient.mil.preset;

import static io.restassured.RestAssured.given;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.preset.bean.SubscriberRequest;
import it.pagopa.swclient.mil.preset.bean.SubscriberResponse;
import it.pagopa.swclient.mil.preset.dao.SubscriberEntity;
import it.pagopa.swclient.mil.preset.dao.SubscriberRepository;
import it.pagopa.swclient.mil.preset.resource.SubscribeResource;
import jakarta.ws.rs.InternalServerErrorException;


@QuarkusTest
@TestHTTPEndpoint(SubscribeResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SubscribeResourceTest {
	
	final static String SESSION_ID		= "a6a666e6-97da-4848-b568-99fedccb642c";
	final static String API_VERSION		= "1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay";
	final static String PA_TAX_CODE		= "15376371009";
	final static String SUBSCRIBER_ID	= "y46tr3";
	
	@InjectMock
	SubscriberRepository subscriberRepository;
	
	Map<String, String> commonHeaders;
	Map<String, String> presetHeaders;
	
	@BeforeAll
	void createTestObjects() {
		presetHeaders = new HashMap<>();
		presetHeaders.put("RequestId", "d0d654e6-97da-4848-b568-99fedccb642b");
		presetHeaders.put("Version", API_VERSION);
		
		commonHeaders = new HashMap<>();
		commonHeaders.put("RequestId", "d0d654e6-97da-4848-b568-99fedccb642b");
		commonHeaders.put("Version", API_VERSION);
		commonHeaders.put("AcquirerId", "4585625");
		commonHeaders.put("Channel", "ATM");
		commonHeaders.put("TerminalId", "0aB9wXyZ");
		commonHeaders.put("SessionId", SESSION_ID);
	}
	
	/* ****  get Subscribers **** */
	@Test
	void getSubscribers_200() {
		
		SubscriberEntity subscriberEntity = new SubscriberEntity();
		subscriberEntity.setAcquirerId("4585625");
		subscriberEntity.setChannel("POS");
		subscriberEntity.setLabel("Reception POS");
		subscriberEntity.setLastUsageTimestamp("2023-05-08T10:55:57");
		subscriberEntity.setMerchantId("28405fHfk73x88D");
		subscriberEntity.setPaTaxCode("15376371009");
		subscriberEntity.setSubscriberId("x46tr3");
		subscriberEntity.setSubscriptionTimestamp("2023-05-05T09:31:33");
		subscriberEntity.setTerminalId("0aB9wXyZ");
		List<SubscriberEntity> listOfEntities = new ArrayList<>();
		listOfEntities.add(subscriberEntity);
		
		Mockito
			.when(subscriberRepository.list("paTaxCode",PA_TAX_CODE))
			.thenReturn(Uni.createFrom().item(listOfEntities));
		
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
        List<SubscriberResponse> res = response.jsonPath().getList("subscribers", SubscriberResponse.class);
        
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
		
		List<SubscriberEntity> listOfEntities = new ArrayList<>();
		
		Mockito
			.when(subscriberRepository.list("paTaxCode",""))
			.thenReturn(Uni.createFrom().item(listOfEntities));
		
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
        List<SubscriberResponse> res = response.jsonPath().getList("subscribers", SubscriberResponse.class);
        
        Assertions.assertEquals(0,res.size());
      
	}
 
	@Test
	void getSubscribers_500_exceptionMongoDb() {
		
		Mockito
			.when(subscriberRepository.list("paTaxCode",PA_TAX_CODE))
			.thenReturn(Uni.createFrom().failure(new InternalServerErrorException()));
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(presetHeaders)
				.and()
				.when()
				.get("/" + PA_TAX_CODE)
				.then()
				.extract()
				.response();
			
        Assertions.assertEquals(500, response.statusCode());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_COMMUNICATION_MONGO_DB));
		Assertions.assertNull(response.jsonPath().getJsonObject("subcribers"));
	}
 
	/* **** unsubscribe **** */
	@Test
	void unsubscriber_200() {
		
		Mockito
			.when(subscriberRepository.delete(Mockito.any(String.class),Mockito.any(Map.class)))
			.thenReturn(Uni.createFrom().item(1L));
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.when()
				.delete("/" + PA_TAX_CODE + "/" + SUBSCRIBER_ID)
				.then()
				.extract()
				.response();
			
        Assertions.assertEquals(204, response.statusCode());
        Assertions.assertEquals(0,response.body().asString().length());
	}
	
	@Test
	void unsubscriber_404() {
		
		Mockito
			.when(subscriberRepository.delete(Mockito.any(String.class),Mockito.any(Map.class)))
			.thenReturn(Uni.createFrom().item(0L));
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.when()
				.delete("/" + PA_TAX_CODE + "/" + SUBSCRIBER_ID)
				.then()
				.extract()
				.response();
			
        Assertions.assertEquals(404, response.statusCode());
        Assertions.assertEquals(0,response.body().asString().length());
	}
	
	@Test
	void unsubscriber_500() {
		
		Mockito
			.when(subscriberRepository.delete(Mockito.any(String.class),Mockito.any(Map.class)))
			.thenReturn(Uni.createFrom().failure(new InternalServerErrorException()));
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.when()
				.delete("/" + PA_TAX_CODE + "/" + SUBSCRIBER_ID)
				.then()
				.extract()
				.response();
			
        Assertions.assertEquals(500, response.statusCode());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_COMMUNICATION_MONGO_DB));
	}
	
	/* **** subscribe **** */
	@Test
	void subscribe_200() {
		SubscriberEntity entity = new SubscriberEntity();
		entity.setAcquirerId("4585625");
		entity.setChannel("POS");
		entity.setLabel("Reception POS");
		entity.setLastUsageTimestamp("2023-05-15T12:08:58.392");
		entity.setSubscriptionTimestamp("2023-05-15T12:08:58.392");
		entity.setMerchantId("28405fHfk73x88D");
		entity.setPaTaxCode("15376371009");
		entity.setTerminalId("0aB9wXyZ");
		
		SubscriberRequest request = new SubscriberRequest();
		request.setPaTaxCode("15376371009");
		request.setLabel("Reception POS");
		
		Mockito
			.when(subscriberRepository.persist(Mockito.any(SubscriberEntity.class)))
			.thenReturn(Uni.createFrom().item(entity));
		
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
        Assertions.assertNotNull(response.getHeader(SubscribeResource.LOCATION));
	}
	
	@Test
	void subscribe_409() {
		SubscriberEntity entity = new SubscriberEntity();
		entity.setAcquirerId("4585625");
		entity.setChannel("POS");
		entity.setLabel("Reception POS");
		entity.setLastUsageTimestamp("2023-05-15T12:08:58.392");
		entity.setSubscriptionTimestamp("2023-05-15T12:08:58.392");
		entity.setMerchantId("28405fHfk73x88D");
		entity.setPaTaxCode("15376371009");
		entity.setTerminalId("0aB9wXyZ");
		entity.setSubscriberId("2Or8Jw");
		
		SubscriberRequest request = new SubscriberRequest();
		request.setPaTaxCode("15376371009");
		request.setLabel("Reception POS");
		
		Mockito
		.when(subscriberRepository.list(Mockito.any(String.class), Mockito.any(Map.class)))
		.thenReturn(Uni.createFrom().item(List.of(entity)));
		
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
        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_CONFLICT_TERMINAL_IN_DB));
        Assertions.assertNotNull(response.getHeader(SubscribeResource.LOCATION));
	}
	
	
	@Test
	void subscribe_500() {
		
		SubscriberRequest request = new SubscriberRequest();
		request.setPaTaxCode("15376371009");
		request.setLabel("Reception POS");
		
		Mockito
		.when(subscriberRepository.list(Mockito.any(String.class), Mockito.any(Map.class)))
		.thenReturn(Uni.createFrom().item(new ArrayList()));
		
		Mockito
			.when(subscriberRepository.persist(Mockito.any(SubscriberEntity.class)))
			.thenReturn(Uni.createFrom().failure(new InternalServerErrorException()));
		
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
			
        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_STORING_TERMINAL_IN_DB));
	}
}
