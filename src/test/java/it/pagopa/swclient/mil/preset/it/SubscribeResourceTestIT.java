package it.pagopa.swclient.mil.preset.it;


import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import it.pagopa.swclient.mil.preset.ErrorCode;
import it.pagopa.swclient.mil.preset.bean.SubscriberRequest;
import it.pagopa.swclient.mil.preset.bean.SubscriberResponse;
import it.pagopa.swclient.mil.preset.resource.MongoTestResource;
import it.pagopa.swclient.mil.preset.resource.SubscribeResource;


@QuarkusIntegrationTest
@QuarkusTestResource(value=MongoTestResource.class,restrictToAnnotatedClass = true)
@TestHTTPEndpoint(SubscribeResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SubscribeResourceTestIT {
	final static String SESSION_ID		= "a6a666e6-97da-4848-b568-99fedccb642c";
	final static String API_VERSION		= "1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay";
	final static String PA_TAX_CODE		= "15376371009";
	final static String SUBSCRIBER_ID	= "a25tr0";
	
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
		commonHeaders.put("Channel", "POS");
		commonHeaders.put("TerminalId", "0aB9wXyZ");
		commonHeaders.put("SessionId", SESSION_ID);
		commonHeaders.put("MerchantId", "4585625");
	}
	
//	
//	/* ****  get Subscribers **** */
//	@Test
//	void getSubscribers_200() {
//		
//
//		Response response = given()
//				.contentType(ContentType.JSON)
//				.headers(presetHeaders)
//				.and()
//				.when()
//				.get("/" + PA_TAX_CODE)
//				.then()
//				.extract()
//				.response();
//			
//        Assertions.assertEquals(200, response.statusCode());
//        
//        Assertions.assertNotNull(response.jsonPath().getJsonObject("subscribers"));
//        List<SubscriberResponse> res = response.jsonPath().getList("subscribers", SubscriberResponse.class);
//        System.out.println("RESPONSE " + res);
//        System.out.println("RESPONSE size " + res.size());
//        Assertions.assertNotNull(res.get(0).getChannel());
//        Assertions.assertNotNull(res.get(0).getMerchantId());
//        Assertions.assertNotNull(res.get(0).getTerminalId());
//        Assertions.assertNotNull(res.get(0).getPaTaxCode());
//        Assertions.assertNotNull(res.get(0).getSubscriberId());
//        Assertions.assertNotNull(res.get(0).getLabel());
//        Assertions.assertNotNull(res.get(0).getSubscriptionTimestamp());
//        Assertions.assertNotNull(res.get(0).getLastUsageTimestamp());
//	}
//	
//	@Test
//	void getSubscribers_200_emptyListOfSubribers() {
//
//		Response response = given()
//				.contentType(ContentType.JSON)
//				.headers(presetHeaders)
//				.and()
//				.when()
//				.get("/" + "00000000000")
//				.then()
//				.extract()
//				.response();
//			
//        Assertions.assertEquals(200, response.statusCode());
//        
//        Assertions.assertNotNull(response.jsonPath().getJsonObject("subscribers"));
//        List<SubscriberResponse> res = response.jsonPath().getList("subscribers", SubscriberResponse.class);
//        
//        Assertions.assertEquals(0,res.size());
//      
//	}
//	
//	/* **** unsubscribe **** */
//	@Test
//	void unsubscriber_200() {
//
//		Response response = given()
//				.contentType(ContentType.JSON)
//				.headers(commonHeaders)
//				.and()
//				.when()
//				.delete("/11111111111/a25tr0")
//				.then()
//				.extract()
//				.response();
//			
//        Assertions.assertEquals(204, response.statusCode());
//        Assertions.assertEquals(0,response.body().asString().length());
//	}
//	
//	@Test
//	void unsubscriber_404() {
//		
//		Response response = given()
//				.contentType(ContentType.JSON)
//				.headers(commonHeaders)
//				.and()
//				.when()
//				.delete("/" + PA_TAX_CODE + "/a00aa0")
//				.then()
//				.extract()
//				.response();
//			
//        Assertions.assertEquals(404, response.statusCode());
//        Assertions.assertEquals(0,response.body().asString().length());
//	}
//	
//	/* **** subscribe **** */
//	@Test
//	void subscribe_200() {
//		
//		SubscriberRequest request = new SubscriberRequest();
//		request.setPaTaxCode("34576371029");
//		request.setLabel("Reception POS");
//		
//		Response response = given()
//				.contentType(ContentType.JSON)
//				.headers(commonHeaders)
//				.body(request)
//				.and()
//				.when()
//				.post()
//				.then()
//				.extract()
//				.response();
//			
//        Assertions.assertEquals(201, response.statusCode());
//        Assertions.assertEquals(0,response.body().asString().length());
//        Assertions.assertNotNull(response.getHeader(SubscribeResource.LOCATION));
//	}
//	@Test
//	void subscribe_409() {
//
//		SubscriberRequest request = new SubscriberRequest();
//		request.setPaTaxCode("15376371009");
//		request.setLabel("Reception POS");
//		
//		Response response = given()
//				.contentType(ContentType.JSON)
//				.headers(commonHeaders)
//				.body(request)
//				.and()
//				.when()
//				.post()
//				.then()
//				.extract()
//				.response();
//			
//        Assertions.assertEquals(409, response.statusCode());
//        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_CONFLICT_TERMINAL_IN_DB));
//        Assertions.assertNotNull(response.getHeader(SubscribeResource.LOCATION));
//	}
}
