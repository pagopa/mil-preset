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

import io.quarkus.panache.common.Sort;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.preset.bean.Notice;
import it.pagopa.swclient.mil.preset.bean.PaymentTransaction;
import it.pagopa.swclient.mil.preset.bean.PresetRequest;
import it.pagopa.swclient.mil.preset.bean.PresetResponse;
import it.pagopa.swclient.mil.preset.dao.PresetRepository;
import it.pagopa.swclient.mil.preset.dao.PresetsEntity;
import it.pagopa.swclient.mil.preset.dao.SubscriberEntity;
import it.pagopa.swclient.mil.preset.dao.SubscriberRepository;
import it.pagopa.swclient.mil.preset.resource.PresetsResource;
import it.pagopa.swclient.mil.preset.resource.SubscribeResource;
import it.pagopa.swclient.mil.preset.utils.DateUtils;
import jakarta.ws.rs.InternalServerErrorException;


@QuarkusTest
@TestHTTPEndpoint(PresetsResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PresetResourceTest {
	
	final static String SESSION_ID		= "a6a666e6-97da-4848-b568-99fedccb642c";
	final static String API_VERSION		= "1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay";
	final static String PA_TAX_CODE		= "15376371009";
	final static String SUBSCRIBER_ID	= "y46tr3";
	
	@InjectMock
	SubscriberRepository subscriberRepository;
	
	@InjectMock
	PresetRepository presetRepository;
	
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
	
	/* **** preset **** */
	@Test
	void createPreset_201() {
		
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
			.when(subscriberRepository.list(Mockito.any(String.class), Mockito.any(Map.class)))
			.thenReturn(Uni.createFrom().item(listOfEntities));
		
		
		Mockito
		.when(subscriberRepository.update(Mockito.any(SubscriberEntity.class)))
		.thenReturn(Uni.createFrom().item(subscriberEntity));
		
		final String timestamp = DateUtils.getAndFormatCurrentDate();
		PresetsEntity presetEntity = new PresetsEntity();
		presetEntity.setId("77457c64-0870-407a-b2cb-0f948b04fb9a");
		presetEntity.setPresetId("77457c64-0870-407a-b2cb-0f948b04fb9a");
		presetEntity.setCreationTimestamp(timestamp);
		presetEntity.setNoticeNumber("485564829563528563");
		presetEntity.setNoticeTaxCode("15376371009");
		presetEntity.setOperationType(OperationType.PAYMENT_NOTICE.name());
		presetEntity.setPaTaxCode(subscriberEntity.getPaTaxCode());
		presetEntity.setStatus(PresetStatus.TO_EXECUTE.name());
		presetEntity.setStatusTimestamp(timestamp);
		presetEntity.setSubscriberId(subscriberEntity.getSubscriberId());
		
		Mockito
		.when(presetRepository.persist(Mockito.any(PresetsEntity.class)))
		.thenReturn(Uni.createFrom().item(presetEntity));
		
		PresetRequest request = new PresetRequest();
		request.setNoticeNumber("485564829563528563");
		request.setNoticeTaxCode("15376371009");
		request.setOperationType("PAYMENT_NOTICE");
		request.setPaTaxCode("15376371009");
		request.setSubscriberId("x46tr3");
		
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
        
        Assertions.assertNotNull(response.getHeader(SubscribeResource.LOCATION));
	}
	
	@Test
	void createPreset_400_subscriberNotFound() {
		
		Mockito
			.when(subscriberRepository.list(Mockito.any(String.class), Mockito.any(Map.class)))
			.thenReturn(Uni.createFrom().item(List.of()));
		
		
		PresetRequest request = new PresetRequest();
		request.setNoticeNumber("485564829563528563");
		request.setNoticeTaxCode("15376371009");
		request.setOperationType("PAYMENT_NOTICE");
		request.setPaTaxCode("15376371009");
		request.setSubscriberId("x46tr3");
		
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
        
        Assertions.assertNull(response.getHeader(SubscribeResource.LOCATION));
        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_SUBSCRIBER_NOT_FOUND));
	}
	
	@Test
	void createPreset_500_datadaseErrorFindSubscriber() {
		
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
			.when(subscriberRepository.list(Mockito.any(String.class), Mockito.any(Map.class)))
			.thenReturn(Uni.createFrom().failure(new InternalServerErrorException()));
		
		PresetRequest request = new PresetRequest();
		request.setNoticeNumber("485564829563528563");
		request.setNoticeTaxCode("15376371009");
		request.setOperationType("PAYMENT_NOTICE");
		request.setPaTaxCode("15376371009");
		request.setSubscriberId("x46tr3");
		
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
			
        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_COMMUNICATION_MONGO_DB));
        Assertions.assertNull(response.getHeader(SubscribeResource.LOCATION));
	}
	
	
	
	@Test
	void createPreset_500_datadaseErrorInsertingPreset() {
		
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
			.when(subscriberRepository.list(Mockito.any(String.class), Mockito.any(Map.class)))
			.thenReturn(Uni.createFrom().item(listOfEntities));
		
		
		Mockito
		.when(subscriberRepository.update(Mockito.any(SubscriberEntity.class)))
		.thenReturn(Uni.createFrom().item(subscriberEntity));
		
		Mockito
		.when(presetRepository.persist(Mockito.any(PresetsEntity.class)))
		.thenReturn(Uni.createFrom().failure(new InternalServerErrorException()));
		
		PresetRequest request = new PresetRequest();
		request.setNoticeNumber("485564829563528563");
		request.setNoticeTaxCode("15376371009");
		request.setOperationType("PAYMENT_NOTICE");
		request.setPaTaxCode("15376371009");
		request.setSubscriberId("x46tr3");
		
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
			
        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_STORING_TERMINAL_IN_DB));
        Assertions.assertNull(response.getHeader(SubscribeResource.LOCATION));
	}
	
	@Test
	void getPresets_200() {
		
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
		presetEntity.setSubscriberId("x46tr3");
		
        Notice notice = new Notice();
        notice.setPaymentToken("648fhg36s95jfg7DS");
        notice.setPaTaxCode(PA_TAX_CODE);
        notice.setNoticeNumber("485564829563528563");
        notice.setAmount(12345L);
        notice.setDescription("Test payment notice");
        notice.setCompany("Test company");
        notice.setOffice("Test office");
        
        PaymentTransaction statusDetails = new PaymentTransaction();
        statusDetails.setTransactionId("517a4216840E461fB011036A0fd134E1");
        statusDetails.setAcquirerId("4585625");
        statusDetails.setChannel("POS");
        statusDetails.setMerchantId("28405fHfk73x88D");
        statusDetails.setTerminalId("0aB9wXyZ");
        statusDetails.setInsertTimestamp(timestamp);
        List<Notice> notices = new ArrayList<>();
        notices.add(notice);
        statusDetails.setNotices(notices);
        statusDetails.setTotalAmount(notices.stream().map(Notice::getAmount).reduce(Long::sum).orElse(0L));

        statusDetails.setFee(100L);
        statusDetails.setStatus("PRE_CLOSE");
        
        presetEntity.setStatusDetails(statusDetails);
        
        List<PresetsEntity> listOfPresetsEntity = new ArrayList<>();
        listOfPresetsEntity.add(presetEntity);
        
		Mockito
			.when(presetRepository.list(Mockito.any(String.class), Mockito.any(Map.class)))
			.thenReturn(Uni.createFrom().item(listOfPresetsEntity));
		
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
        List<PresetResponse> arr = response.jsonPath().getList("presets", PresetResponse.class);
        
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
		presetEntity.setSubscriberId("x46tr3");
		
        Notice notice = new Notice();
        notice.setPaymentToken("648fhg36s95jfg7DS");
        notice.setPaTaxCode(PA_TAX_CODE);
        notice.setNoticeNumber("485564829563528563");
        notice.setAmount(12345L);
        notice.setDescription("Test payment notice");
        notice.setCompany("Test company");
        notice.setOffice("Test office");
        
        PaymentTransaction statusDetails = new PaymentTransaction();
        statusDetails.setTransactionId("517a4216840E461fB011036A0fd134E1");
        statusDetails.setAcquirerId("4585625");
        statusDetails.setChannel("POS");
        statusDetails.setMerchantId("28405fHfk73x88D");
        statusDetails.setTerminalId("0aB9wXyZ");
        statusDetails.setInsertTimestamp(timestamp);
        List<Notice> notices = new ArrayList<>();
        notices.add(notice);
        statusDetails.setNotices(notices);
        statusDetails.setTotalAmount(notices.stream().map(Notice::getAmount).reduce(Long::sum).orElse(0L));

        statusDetails.setFee(100L);
        statusDetails.setStatus("PRE_CLOSE");
        
        presetEntity.setStatusDetails(statusDetails);
        
        List<PresetsEntity> listOfPresetsEntity = new ArrayList<>();
//        listOfPresetsEntity.add(presetEntity);
        
		Mockito
			.when(presetRepository.list(Mockito.any(String.class), Mockito.any(Map.class)))
			.thenReturn(Uni.createFrom().item(listOfPresetsEntity));
		
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
        List<PresetResponse> arr = response.jsonPath().getList("presets", PresetResponse.class);
        
        Assertions.assertEquals(0,arr.size());
	}
	
	@Test
	void getLastPreset_200() {
		
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
		presetEntity.setSubscriberId("x46tr3");
		
        Notice notice = new Notice();
        notice.setPaymentToken("648fhg36s95jfg7DS");
        notice.setPaTaxCode(PA_TAX_CODE);
        notice.setNoticeNumber("485564829563528563");
        notice.setAmount(12345L);
        notice.setDescription("Test payment notice");
        notice.setCompany("Test company");
        notice.setOffice("Test office");
        
        PaymentTransaction statusDetails = new PaymentTransaction();
        statusDetails.setTransactionId("517a4216840E461fB011036A0fd134E1");
        statusDetails.setAcquirerId("4585625");
        statusDetails.setChannel("POS");
        statusDetails.setMerchantId("28405fHfk73x88D");
        statusDetails.setTerminalId("0aB9wXyZ");
        statusDetails.setInsertTimestamp(timestamp);
        List<Notice> notices = new ArrayList<>();
        notices.add(notice);
        statusDetails.setNotices(notices);
        statusDetails.setTotalAmount(notices.stream().map(Notice::getAmount).reduce(Long::sum).orElse(0L));

        statusDetails.setFee(100L);
        statusDetails.setStatus("PRE_CLOSE");
        
        presetEntity.setStatusDetails(statusDetails);
        
        List<PresetsEntity> listOfPresetsEntity = new ArrayList<>();
        listOfPresetsEntity.add(presetEntity);
        
		Mockito
			.when(presetRepository.list(Mockito.any(String.class), Mockito.any(Sort.class),Mockito.any(Map.class)))
			.thenReturn(Uni.createFrom().item(listOfPresetsEntity));
		
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
        PaymentTransaction statDetails = response.jsonPath().getObject("statusDetails", PaymentTransaction.class);
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
	void getLastPreset_404_presetNotFound() {
		
        List<PresetsEntity> listOfPresetsEntity = new ArrayList<>();
        
		Mockito
			.when(presetRepository.list(Mockito.any(String.class), Mockito.any(Sort.class),Mockito.any(Map.class)))
			.thenReturn(Uni.createFrom().item(listOfPresetsEntity));
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.when()
				.get("/15376371009/x46tr3/last_to_execute")
				.then()
				.extract()
				.response();
			
        Assertions.assertEquals(404, response.statusCode());
        
        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_PRESET_OPERATION_NOT_FOUND));
        
        Assertions.assertNull(response.jsonPath().getString("creationTimestamp"));
        Assertions.assertNull(response.jsonPath().getString("noticeNumber"));
        Assertions.assertNull(response.jsonPath().getString("noticeTaxCode"));
        Assertions.assertNull(response.jsonPath().getString("operationType"));
        Assertions.assertNull(response.jsonPath().getString("paTaxCode"));
        Assertions.assertNull(response.jsonPath().getString("presetId"));
        Assertions.assertNull(response.jsonPath().getString("status"));
        Assertions.assertNull(response.jsonPath().getString("statusTimestamp"));
        Assertions.assertNull(response.jsonPath().getString("subscriberId"));
        Assertions.assertNull(response.jsonPath().getObject("statusDetails", PaymentTransaction.class));
	}
	
	@Test
	void getLastPreset_500() {
        
		Mockito
			.when(presetRepository.list(Mockito.any(String.class), Mockito.any(Sort.class),Mockito.any(Map.class)))
			.thenReturn(Uni.createFrom().failure(new InternalServerErrorException()));
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.when()
				.get("/15376371009/x46tr3/last_to_execute")
				.then()
				.extract()
				.response();
			
        Assertions.assertEquals(500, response.statusCode());
        
        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_COMMUNICATION_MONGO_DB));
        
        Assertions.assertNull(response.jsonPath().getString("creationTimestamp"));
        Assertions.assertNull(response.jsonPath().getString("noticeNumber"));
        Assertions.assertNull(response.jsonPath().getString("noticeTaxCode"));
        Assertions.assertNull(response.jsonPath().getString("operationType"));
        Assertions.assertNull(response.jsonPath().getString("paTaxCode"));
        Assertions.assertNull(response.jsonPath().getString("presetId"));
        Assertions.assertNull(response.jsonPath().getString("status"));
        Assertions.assertNull(response.jsonPath().getString("statusTimestamp"));
        Assertions.assertNull(response.jsonPath().getString("subscriberId"));
        Assertions.assertNull(response.jsonPath().getObject("statusDetails", PaymentTransaction.class));
       
	}
}
