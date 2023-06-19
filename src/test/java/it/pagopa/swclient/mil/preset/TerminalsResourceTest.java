package it.pagopa.swclient.mil.preset;

import static io.restassured.RestAssured.given;
import io.quarkus.test.security.TestSecurity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.preset.bean.SubscribeRequest;
import it.pagopa.swclient.mil.preset.bean.Subscriber;
import it.pagopa.swclient.mil.preset.dao.SubscriberEntity;
import it.pagopa.swclient.mil.preset.dao.SubscriberRepository;
import it.pagopa.swclient.mil.preset.resource.TerminalsResource;


@QuarkusTest
@TestHTTPEndpoint(TerminalsResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TerminalsResourceTest {

    final static String SESSION_ID = "a6a666e6-97da-4848-b568-99fedccb642c";
    final static String API_VERSION = "1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay";
    final static String PA_TAX_CODE = "15376371009";
    final static String SUBSCRIBER_ID = "y46tr3";

    @InjectMock
    SubscriberRepository subscriberRepository;

    Map<String, String> commonHeaders;
    Map<String, String> presetHeaders;

    SubscriberEntity subscriberEntity;

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

    /* ****  get Subscribers **** */
    @Test
    @TestSecurity(user = "userJwt", roles = {"InstitutionPortal"})
    void getSubscribers_200() {

        Mockito
                .when(subscriberRepository.list(Mockito.anyString(), Mockito.any(Object[].class)))
                .thenReturn(Uni.createFrom().item(List.of(subscriberEntity)));

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(presetHeaders)
				.and()
				.pathParam("paTaxCode", PA_TAX_CODE)
                .when()
                .get("/{paTaxCode}")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(200, response.statusCode());

        Assertions.assertNotNull(response.jsonPath().getJsonObject("subscribers"));
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));

        List<Subscriber> subscriberList = response.jsonPath().getList("subscribers", Subscriber.class);
		Assertions.assertEquals(subscriberEntity.subscriber.getAcquirerId(), subscriberList.get(0).getAcquirerId());
        Assertions.assertEquals(subscriberEntity.subscriber.getChannel(), subscriberList.get(0).getChannel());
        Assertions.assertNotNull(subscriberEntity.subscriber.getMerchantId(), subscriberList.get(0).getMerchantId());
        Assertions.assertNotNull(subscriberEntity.subscriber.getTerminalId(), subscriberList.get(0).getTerminalId());
        Assertions.assertNotNull(subscriberEntity.subscriber.getPaTaxCode(), subscriberList.get(0).getPaTaxCode());
        Assertions.assertNotNull(subscriberEntity.subscriber.getSubscriberId(), subscriberList.get(0).getSubscriberId());
        Assertions.assertNotNull(subscriberEntity.subscriber.getLabel(), subscriberList.get(0).getLabel());
        Assertions.assertNotNull(subscriberEntity.subscriber.getSubscriptionTimestamp(), subscriberList.get(0).getSubscriptionTimestamp());
        Assertions.assertNotNull(subscriberEntity.subscriber.getLastUsageTimestamp(), subscriberList.get(0).getLastUsageTimestamp());

		// check mongo panache repository integration
		ArgumentCaptor<String> captorQuery = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Object[]> captorArguments = ArgumentCaptor.forClass(Object[].class);

		Mockito.verify(subscriberRepository).list(captorQuery.capture(), captorArguments.capture());
		Assertions.assertEquals(1, captorArguments.getValue().length);
		Assertions.assertEquals(PA_TAX_CODE, captorArguments.getValue()[0].toString());

    }

    @Test
    @TestSecurity(user = "userJwt", roles = {"InstitutionPortal"})
    void getSubscribers_200_noSubscribers() {

		Mockito
				.when(subscriberRepository.list(Mockito.anyString(), Mockito.any(Object[].class)))
				.thenReturn(Uni.createFrom().item(List.of()));

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(presetHeaders)
				.and()
				.pathParam("paTaxCode", PA_TAX_CODE)
				.when()
				.get("/{paTaxCode}")
				.then()
				.extract()
				.response();

        Assertions.assertEquals(200, response.statusCode());
		Assertions.assertNull(response.jsonPath().getJsonObject("errors"));

        Assertions.assertNotNull(response.jsonPath().getJsonObject("subscribers"));
        List<Subscriber> subscriberList = response.jsonPath().getList("subscribers", Subscriber.class);

        Assertions.assertEquals(0, subscriberList.size());

    }

    @TestSecurity(user = "userJwt", roles = {"InstitutionPortal"})
 	@ParameterizedTest
 	@MethodSource("it.pagopa.swclient.mil.preset.util.TestUtils#provideHeaderValidationErrorCases")
    void getSubscribers_400_invalidHeaders(Map<String, String> invalidHeaders, String errorCode)  {
    	Response response = given()
				.contentType(ContentType.JSON)
				.headers(invalidHeaders)
				.and()
				.pathParam("paTaxCode", PA_TAX_CODE)
				.when()
				.get("/{paTaxCode}")
				.then()
				.extract()
				.response();
    	Assertions.assertEquals(400, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(errorCode));
		Assertions.assertNull(response.jsonPath().getList("subscribers"));
    }
    
 	@ParameterizedTest
  	@MethodSource("it.pagopa.swclient.mil.preset.util.TestUtils#providePaTaxCodeValidationErrorCases")
    @TestSecurity(user = "userJwt", roles = {"InstitutionPortal"})
    void getSubscribers_400_invalidPathParams(String paTaxCode, String errorCode) {
 		Response response = given()
				.contentType(ContentType.JSON)
				.headers(presetHeaders)
				.and()
				.pathParam("paTaxCode", paTaxCode)
				.when()
				.get("/{paTaxCode}")
				.then()
				.extract()
				.response();
    	Assertions.assertEquals(400, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(errorCode));
		Assertions.assertNull(response.jsonPath().getList("subscribers"));
 	}


    @Test
    @TestSecurity(user = "userJwt", roles = {"InstitutionPortal"})
    void getSubscribers_500_dbError_listSubscribers() {

        Mockito
				.when(subscriberRepository.list(Mockito.anyString(), Mockito.any(Object[].class)))
                .thenReturn(Uni.createFrom().failure(new TimeoutException()));

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(presetHeaders)
				.and()
				.pathParam("paTaxCode", PA_TAX_CODE)
				.when()
				.get("/{paTaxCode}")
				.then()
				.extract()
				.response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_READING_DATA_FROM_DB));
        Assertions.assertNull(response.jsonPath().getJsonObject("subscribers"));

    }

    /* **** unsubscribe **** */
    @Test
    @TestSecurity(user = "userJwt", roles = {"SlavePos"})
    void unsubscribe_200() {

        Mockito
                .when(subscriberRepository.delete(Mockito.any(String.class), Mockito.anyMap()))
                .thenReturn(Uni.createFrom().item(1L));

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(commonHeaders)
                .and()
				.pathParam("paTaxCode", PA_TAX_CODE)
				.pathParam("subscriberId", SUBSCRIBER_ID)
                .when()
                .delete("/{paTaxCode}/{subscriberId}")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(204, response.statusCode());
        Assertions.assertEquals(0, response.body().asString().length());

		// check mongo panache repository integration
		ArgumentCaptor<String> captorQuery = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Map<String, Object>> captorArguments = ArgumentCaptor.forClass(Map.class);

		Mockito.verify(subscriberRepository).delete(captorQuery.capture(), captorArguments.capture());
		Assertions.assertEquals(2, captorArguments.getValue().size());
		Assertions.assertEquals(PA_TAX_CODE, captorArguments.getValue().get("paTaxCode"));
		Assertions.assertEquals(SUBSCRIBER_ID, captorArguments.getValue().get("subscriberId"));

    }

 	@ParameterizedTest
  	@MethodSource("it.pagopa.swclient.mil.preset.util.TestUtils#providePaTaxCodeSubscriberIdValidationErrorCases")
    @TestSecurity(user = "userJwt", roles = {"SlavePos"})
    void unsubscribe_400_invalidPathParams(String paTaxCode, String subscriberId, String errorCode) {
 		Response response = given()
                .contentType(ContentType.JSON)
                .headers(commonHeaders)
                .and()
				  .pathParam("paTaxCode", paTaxCode)
				  .pathParam("subscriberId", subscriberId)
                .when()
                .delete("/{paTaxCode}/{subscriberId}")
                .then()
                .extract()
                .response();
		Assertions.assertEquals(400, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(errorCode));
  }
    
    @ParameterizedTest
 	@MethodSource("it.pagopa.swclient.mil.preset.util.TestUtils#provideAllHeaderValidationErrorCases")
 	@TestSecurity(user = "userJwt", roles = {"SlavePos"})
    void unsubscribe_400_invalidHeaders_SlavePos(Map<String, String> invalidHeaders, String errorCode)  {
    	  Response response = given()
                  .contentType(ContentType.JSON)
                  .headers(invalidHeaders)
                  .and()
  				  .pathParam("paTaxCode", PA_TAX_CODE)
  				  .pathParam("subscriberId", SUBSCRIBER_ID)
                  .when()
                  .delete("/{paTaxCode}/{subscriberId}")
                  .then()
                  .extract()
                  .response();
  		Assertions.assertEquals(400, response.statusCode());
  		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
  		Assertions.assertTrue(response.jsonPath().getList("errors").contains(errorCode));
    }

    @ParameterizedTest
 	@MethodSource("it.pagopa.swclient.mil.preset.util.TestUtils#provideAllInstitutionPortalHeaderValidationErrorCases")
 	@TestSecurity(user = "userJwt", roles = {"InstitutionPortal"})
    void unsubscribe_400_invalidHeaders_InstitutionPortal(Map<String, String> invalidHeaders, String errorCode)  {
    	  Response response = given()
                  .contentType(ContentType.JSON)
                  .headers(invalidHeaders)
                  .and()
  				  .pathParam("paTaxCode", PA_TAX_CODE)
  				  .pathParam("subscriberId", SUBSCRIBER_ID)
                  .when()
                  .delete("/{paTaxCode}/{subscriberId}")
                  .then()
                  .extract()
                  .response();
  		Assertions.assertEquals(400, response.statusCode());
  		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
  		Assertions.assertTrue(response.jsonPath().getList("errors").contains(errorCode));
    }
    
    @Test
    @TestSecurity(user = "userJwt", roles = {"SlavePos"})
    void unsubscribe_404_unknownSubscriberId() {

        Mockito
				.when(subscriberRepository.delete(Mockito.any(String.class), Mockito.anyMap()))
                .thenReturn(Uni.createFrom().item(0L));

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.pathParam("paTaxCode", PA_TAX_CODE)
				.pathParam("subscriberId", SUBSCRIBER_ID)
				.when()
				.delete("/{paTaxCode}/{subscriberId}")
				.then()
				.extract()
				.response();

        Assertions.assertEquals(404, response.statusCode());
        Assertions.assertEquals(0, response.body().asString().length());
    }

    @Test
    @TestSecurity(user = "userJwt", roles = {"SlavePos"})
    void unsubscribe_500_dbError_deleteSubscriber() {

        Mockito
				.when(subscriberRepository.delete(Mockito.any(String.class), Mockito.anyMap()))
                .thenReturn(Uni.createFrom().failure(new TimeoutException()));

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(commonHeaders)
				.and()
				.pathParam("paTaxCode", PA_TAX_CODE)
				.pathParam("subscriberId", SUBSCRIBER_ID)
				.when()
				.delete("/{paTaxCode}/{subscriberId}")
				.then()
				.extract()
				.response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_WRITING_DATA_IN_DB));
    }

    /* **** subscribe **** */
    @Test
    @TestSecurity(user = "userJwt", roles = {"SlavePos"})
    void subscribe_200() {

        SubscribeRequest request = new SubscribeRequest();
        request.setPaTaxCode("15376371009");
        request.setLabel("Reception POS");

        Mockito
                .when(subscriberRepository.list(Mockito.any(String.class), Mockito.anyMap()))
                .thenReturn(Uni.createFrom().item(List.of()));

        Mockito
                .when(subscriberRepository.persist(Mockito.any(SubscriberEntity.class)))
                .then(i -> Uni.createFrom().item(i.getArgument(0, SubscriberEntity.class)));

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
        Assertions.assertEquals(0, response.body().asString().length());


        // check mongo panache repository integration
        ArgumentCaptor<String> captorFindQuery = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> captorFindArguments = ArgumentCaptor.forClass(Map.class);

        Mockito.verify(subscriberRepository).list(captorFindQuery.capture(), captorFindArguments.capture());
        Assertions.assertEquals(5, captorFindArguments.getValue().size());
        Assertions.assertEquals(commonHeaders.get("AcquirerId"), captorFindArguments.getValue().get("acquirerId"));
        Assertions.assertEquals(commonHeaders.get("Channel"), captorFindArguments.getValue().get("channel"));
        Assertions.assertEquals(commonHeaders.get("MerchantId"), captorFindArguments.getValue().get("merchantId"));
        Assertions.assertEquals(commonHeaders.get("TerminalId"), captorFindArguments.getValue().get("terminalId"));
        Assertions.assertEquals(request.getPaTaxCode(), captorFindArguments.getValue().get("paTaxCode"));

        ArgumentCaptor<SubscriberEntity> captorPersistEntity = ArgumentCaptor.forClass(SubscriberEntity.class);

        Mockito.verify(subscriberRepository).persist(captorPersistEntity.capture());
        Assertions.assertNotNull(captorPersistEntity.getValue());
        Subscriber persistedSubscriber = captorPersistEntity.getValue().subscriber;
        Assertions.assertEquals(commonHeaders.get("AcquirerId"), persistedSubscriber.getAcquirerId());
        Assertions.assertEquals(commonHeaders.get("Channel"), persistedSubscriber.getChannel());
        Assertions.assertEquals(commonHeaders.get("MerchantId"), persistedSubscriber.getMerchantId());
        Assertions.assertEquals(commonHeaders.get("TerminalId"), persistedSubscriber.getTerminalId());
        Assertions.assertEquals(request.getPaTaxCode(), persistedSubscriber.getPaTaxCode());
        Assertions.assertNotNull(persistedSubscriber.getSubscriberId());
        Assertions.assertEquals(request.getLabel(), persistedSubscriber.getLabel());
        Assertions.assertNotNull(persistedSubscriber.getSubscriptionTimestamp());
        Assertions.assertNotNull(persistedSubscriber.getLastUsageTimestamp());

        Assertions.assertTrue(response.getHeader("Location") != null &&
                response.getHeader("Location").endsWith("/terminals/" + PA_TAX_CODE + "/" + persistedSubscriber.getSubscriberId()));

    }

    @ParameterizedTest
   	@MethodSource("it.pagopa.swclient.mil.preset.util.TestUtils#provideAllHeaderValidationErrorCases")
   	@TestSecurity(user = "userJwt", roles = {"SlavePos"})
    void subscribe_400_invalidHeadersSlavePos(Map<String, String> invalidHeaders, String errorCode)  {
        
    	SubscribeRequest request = new SubscribeRequest();
        request.setPaTaxCode("15376371009");
        request.setLabel("Reception POS");
        
    	Response response = given()
                .contentType(ContentType.JSON)
                .headers(invalidHeaders)
                .body(request)
                .and()
                .when()
                .post()
                .then()
                .extract()
                .response();
		Assertions.assertEquals(400, response.statusCode());
		Assertions.assertEquals(1, response.jsonPath().getList("errors").size());
		Assertions.assertTrue(response.jsonPath().getList("errors").contains(errorCode));
    }

    @Test
    @TestSecurity(user = "userJwt", roles = {"SlavePos"})
    void subscribe_409_subscriberAlreadyExists() {

        SubscribeRequest request = new SubscribeRequest();
        request.setPaTaxCode("15376371009");
        request.setLabel("Reception POS");

        Mockito
                .when(subscriberRepository.list(Mockito.any(String.class), Mockito.anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(subscriberEntity)));

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
        Assertions.assertEquals(0, response.body().asString().length());
        Assertions.assertTrue(response.getHeader("Location") != null &&
                response.getHeader("Location").endsWith("/terminals/" + PA_TAX_CODE + "/" + SUBSCRIBER_ID));

    }

    @Test
    @TestSecurity(user = "userJwt", roles = {"SlavePos"})
    void subscribe_500_dbError_listSubscribers() {

        SubscribeRequest request = new SubscribeRequest();
        request.setPaTaxCode("15376371009");
        request.setLabel("Reception POS");

        Mockito
                .when(subscriberRepository.list(Mockito.any(String.class), Mockito.anyMap()))
                .thenReturn(Uni.createFrom().failure(new TimeoutException()));

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
        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_READING_DATA_FROM_DB));

    }

    @Test
    @TestSecurity(user = "userJwt", roles = {"SlavePos"})
    void subscribe_500_dbError_persistSubscriber() {

        SubscribeRequest request = new SubscribeRequest();
        request.setPaTaxCode("15376371009");
        request.setLabel("Reception POS");

        Mockito
                .when(subscriberRepository.list(Mockito.any(String.class), Mockito.anyMap()))
                .thenReturn(Uni.createFrom().item(new ArrayList<>()));

        Mockito
                .when(subscriberRepository.persist(Mockito.any(SubscriberEntity.class)))
                .thenReturn(Uni.createFrom().failure(new TimeoutException()));

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
        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_WRITING_DATA_IN_DB));

    }
}
