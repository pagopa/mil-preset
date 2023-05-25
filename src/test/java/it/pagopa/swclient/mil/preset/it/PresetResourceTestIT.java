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
import it.pagopa.swclient.mil.preset.bean.Notice;
import it.pagopa.swclient.mil.preset.bean.PaymentTransaction;
import it.pagopa.swclient.mil.preset.bean.PresetRequest;
import it.pagopa.swclient.mil.preset.bean.PresetResponse;
import it.pagopa.swclient.mil.preset.resource.MongoTestResource;
import it.pagopa.swclient.mil.preset.resource.PresetsResource;
import it.pagopa.swclient.mil.preset.resource.SubscribeResource;

@QuarkusIntegrationTest
@QuarkusTestResource(value=MongoTestResource.class,restrictToAnnotatedClass = true)
@TestHTTPEndpoint(PresetsResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PresetResourceTestIT {
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
	
//	@Test
//	void createPreset_201() {
//		
//		PresetRequest request = new PresetRequest();
//		request.setNoticeNumber("485564829563528563");
//		request.setNoticeTaxCode("15376371009");
//		request.setOperationType("PAYMENT_NOTICE");
//		request.setPaTaxCode("15376371009");
//		request.setSubscriberId("x46tr4");
//		
//		Response response = given()
//				.contentType(ContentType.JSON)
//				.headers(presetHeaders)
//				.body(request)
//				.and()
//				.when()
//				.post()
//				.then()
//				.extract()
//				.response();
//			
//        Assertions.assertEquals(201, response.statusCode());
//        
//        Assertions.assertNotNull(response.getHeader(SubscribeResource.LOCATION));
//	}
//	
//	@Test
//	void createPreset_400_subscriberNotFound() {
//		
//		PresetRequest request = new PresetRequest();
//		request.setNoticeNumber("485564829563528563");
//		request.setNoticeTaxCode("15376371009");
//		request.setOperationType("PAYMENT_NOTICE");
//		request.setPaTaxCode("15376371111");
//		request.setSubscriberId("x46tr4");
//		
//		Response response = given()
//				.contentType(ContentType.JSON)
//				.headers(presetHeaders)
//				.body(request)
//				.and()
//				.when()
//				.post()
//				.then()
//				.extract()
//				.response();
//			
//        Assertions.assertEquals(400, response.statusCode());
//        
//        Assertions.assertNull(response.getHeader(SubscribeResource.LOCATION));
//        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_SUBSCRIBER_NOT_FOUND));
//	}
//	@Test
//	void getPresets_200() {
//		Response response = given()
//				.contentType(ContentType.JSON)
//				.headers(presetHeaders)
//				.and()
//				.when()
//				.get("/15376371009/x46tr3")
//				.then()
//				.extract()
//				.response();
//			
//        Assertions.assertEquals(200, response.statusCode());
//        
//        Assertions.assertNotNull(response.jsonPath().getJsonObject("presets"));
//        List<PresetResponse> arr = response.jsonPath().getList("presets", PresetResponse.class);
//        
//        Assertions.assertNotNull(arr.get(0).getCreationTimestamp());
//        Assertions.assertNotNull(arr.get(0).getNoticeNumber());
//        
//        Assertions.assertNotNull(arr.get(0).getNoticeTaxCode());
//        Assertions.assertNotNull(arr.get(0).getOperationType());
//        Assertions.assertNotNull(arr.get(0).getPaTaxCode());
//        Assertions.assertNotNull(arr.get(0).getPresetId());
//        Assertions.assertNotNull(arr.get(0).getStatus());
//        Assertions.assertNotNull(arr.get(0).getStatusTimestamp());
//        Assertions.assertNotNull(arr.get(0).getSubscriberId());
//        PaymentTransaction statDetails = arr.get(0).getStatusDetails();
//        Assertions.assertNotNull(statDetails.getAcquirerId());
//        Assertions.assertNotNull(statDetails.getChannel());
//        Assertions.assertNotNull(statDetails.getInsertTimestamp());
//        Assertions.assertNotNull(statDetails.getAcquirerId());
//        Assertions.assertNotNull(statDetails.getStatus());
//        Assertions.assertNotNull(statDetails.getNotices());
//        List<Notice>  noticesResponse = statDetails.getNotices();
//        Assertions.assertNotNull(noticesResponse.get(0).getAmount());
//        Assertions.assertNotNull(noticesResponse.get(0).getCompany());
//        Assertions.assertNotNull(noticesResponse.get(0).getDescription());
//        Assertions.assertNotNull(noticesResponse.get(0).getNoticeNumber());
//        Assertions.assertNotNull(noticesResponse.get(0).getOffice());
//        Assertions.assertNotNull(noticesResponse.get(0).getPaTaxCode());
//        Assertions.assertNotNull(noticesResponse.get(0).getPaymentToken());
//	}
//	
//	@Test
//	void getPresets_200_emptyPreset() {
//		
//		Response response = given()
//				.contentType(ContentType.JSON)
//				.headers(presetHeaders)
//				.and()
//				.when()
//				.get("/15376371009/46t000")
//				.then()
//				.extract()
//				.response();
//			
//        Assertions.assertEquals(200, response.statusCode());
//        
//        Assertions.assertNotNull(response.jsonPath().getJsonObject("presets"));
//        List<PresetResponse> arr = response.jsonPath().getList("presets", PresetResponse.class);
//        
//        Assertions.assertEquals(0,arr.size());
//	}
//	
//	@Test
//	void getLastPreset_200() {
//
//		Response response = given()
//				.contentType(ContentType.JSON)
//				.headers(commonHeaders)
//				.and()
//				.when()
//				.get("/15376371009/x46tr3/last_to_execute")
//				.then()
//				.extract()
//				.response();
//			
//        Assertions.assertEquals(200, response.statusCode());
//        
//        Assertions.assertNotNull(response.jsonPath().getString("creationTimestamp"));
//        Assertions.assertNotNull(response.jsonPath().getString("noticeNumber"));
//        Assertions.assertNotNull(response.jsonPath().getString("noticeTaxCode"));
//        Assertions.assertNotNull(response.jsonPath().getString("operationType"));
//        Assertions.assertNotNull(response.jsonPath().getString("paTaxCode"));
//        Assertions.assertNotNull(response.jsonPath().getString("presetId"));
//        Assertions.assertNotNull(response.jsonPath().getString("status"));
//        Assertions.assertNotNull(response.jsonPath().getString("statusTimestamp"));
//        Assertions.assertNotNull(response.jsonPath().getString("subscriberId"));
//        System.out.println(">>>>>" + response.jsonPath());
//        Assertions.assertNotNull(response.jsonPath().getJsonObject("statusDetails"));
//        PaymentTransaction statDetails = response.jsonPath().getObject("statusDetails", PaymentTransaction.class);
//        Assertions.assertNotNull(statDetails.getChannel());
//        Assertions.assertNotNull(statDetails.getInsertTimestamp());
//        Assertions.assertNotNull(statDetails.getAcquirerId());
//        Assertions.assertNotNull(statDetails.getStatus());
//        Assertions.assertNotNull(statDetails.getNotices());
//        List<Notice>  noticesResponse = statDetails.getNotices();
//        Assertions.assertNotNull(noticesResponse.get(0).getAmount());
//        Assertions.assertNotNull(noticesResponse.get(0).getCompany());
//        Assertions.assertNotNull(noticesResponse.get(0).getDescription());
//        Assertions.assertNotNull(noticesResponse.get(0).getNoticeNumber());
//        Assertions.assertNotNull(noticesResponse.get(0).getOffice());
//        Assertions.assertNotNull(noticesResponse.get(0).getPaTaxCode());
//        Assertions.assertNotNull(noticesResponse.get(0).getPaymentToken());
//	}
	
}
