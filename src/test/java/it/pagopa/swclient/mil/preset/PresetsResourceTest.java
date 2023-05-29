package it.pagopa.swclient.mil.preset;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
import io.quarkus.panache.common.Sort;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import it.pagopa.swclient.mil.preset.bean.CreatePresetRequest;
import it.pagopa.swclient.mil.preset.bean.Notice;
import it.pagopa.swclient.mil.preset.bean.PaymentTransaction;
import it.pagopa.swclient.mil.preset.bean.PresetOperation;
import it.pagopa.swclient.mil.preset.bean.Subscriber;
import it.pagopa.swclient.mil.preset.dao.PresetEntity;
import it.pagopa.swclient.mil.preset.dao.PresetRepository;
import it.pagopa.swclient.mil.preset.dao.SubscriberEntity;
import it.pagopa.swclient.mil.preset.dao.SubscriberRepository;
import it.pagopa.swclient.mil.preset.resource.PresetsResource;
import it.pagopa.swclient.mil.preset.util.TestUtils;
import it.pagopa.swclient.mil.preset.utils.DateUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static io.restassured.RestAssured.given;


@QuarkusTest
@TestHTTPEndpoint(PresetsResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PresetsResourceTest {

    static final Logger logger = LoggerFactory.getLogger(PresetsResourceTest.class);

    final static String SESSION_ID = "a6a666e6-97da-4848-b568-99fedccb642c";
    final static String API_VERSION = "1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay";
    final static String PA_TAX_CODE = "15376371009";
    final static String SUBSCRIBER_ID = "y46tr3";

    @InjectMock
    SubscriberRepository subscriberRepository;

    @InjectMock
    PresetRepository presetRepository;

    Map<String, String> commonHeaders;
    Map<String, String> presetHeaders;

    SubscriberEntity subscriberEntity;

    PresetEntity presetEntity;

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

        final String timestamp = DateUtils.getCurrentTimestamp();
        PresetOperation presetOperation = new PresetOperation();
        presetOperation.setPresetId("77457c64-0870-407a-b2cb-0f948b04fb9a");
        presetOperation.setCreationTimestamp(timestamp);
        presetOperation.setNoticeNumber("485564829563528563");
        presetOperation.setNoticeTaxCode("15376371009");
        presetOperation.setOperationType(OperationType.PAYMENT_NOTICE.name());
        presetOperation.setPaTaxCode(PA_TAX_CODE);
        presetOperation.setStatus(PresetStatus.TO_EXECUTE.name());
        presetOperation.setStatusTimestamp(timestamp);
        presetOperation.setSubscriberId(SUBSCRIBER_ID);

        presetEntity = new PresetEntity();
        presetEntity.id = "77457c64-0870-407a-b2cb-0f948b04fb9a";
        presetEntity.presetOperation = presetOperation;



    }


    /* **** preset **** */
    @Test
    void createPreset_201() {

        Mockito
                .when(subscriberRepository.list(Mockito.any(String.class), Mockito.anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(TestUtils.getClonedObject(subscriberEntity, SubscriberEntity.class))));

        Mockito
                .when(subscriberRepository.update(Mockito.any(SubscriberEntity.class)))
                .then(i-> Uni.createFrom().item(i.getArgument(0, SubscriberEntity.class)));

        Mockito
                .when(presetRepository.persist(Mockito.any(PresetEntity.class)))
                .then(i-> Uni.createFrom().item(i.getArgument(0, PresetEntity.class)));

        CreatePresetRequest request = new CreatePresetRequest();
        request.setNoticeNumber("485564829563528563");
        request.setNoticeTaxCode("15376371009");
        request.setOperationType(OperationType.PAYMENT_NOTICE.name());
        request.setPaTaxCode("15376371009");
        request.setSubscriberId(SUBSCRIBER_ID);

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(presetHeaders)
                .body(request)
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
        Assertions.assertEquals(2, captorFindArguments.getValue().size());
        Assertions.assertEquals(request.getNoticeTaxCode(), captorFindArguments.getValue().get("paTaxCode"));
        Assertions.assertEquals(request.getSubscriberId(), captorFindArguments.getValue().get("subscriberId"));

        ArgumentCaptor<SubscriberEntity> captorUpdateEntity = ArgumentCaptor.forClass(SubscriberEntity.class);

        Mockito.verify(subscriberRepository).update(captorUpdateEntity.capture());
        Assertions.assertNotNull(captorUpdateEntity.getValue());
        Subscriber updatedSubscriber = captorUpdateEntity.getValue().subscriber;
        Assertions.assertEquals(subscriberEntity.subscriber.getAcquirerId(), updatedSubscriber.getAcquirerId());
        Assertions.assertEquals(subscriberEntity.subscriber.getChannel(), updatedSubscriber.getChannel());
        Assertions.assertEquals(subscriberEntity.subscriber.getMerchantId(), updatedSubscriber.getMerchantId());
        Assertions.assertEquals(subscriberEntity.subscriber.getTerminalId(), updatedSubscriber.getTerminalId());
        Assertions.assertEquals(subscriberEntity.subscriber.getPaTaxCode(), updatedSubscriber.getPaTaxCode());
        Assertions.assertEquals(subscriberEntity.subscriber.getSubscriberId(), updatedSubscriber.getSubscriberId());
        Assertions.assertEquals(subscriberEntity.subscriber.getLabel(), updatedSubscriber.getLabel());
        Assertions.assertEquals(subscriberEntity.subscriber.getSubscriptionTimestamp(), updatedSubscriber.getSubscriptionTimestamp());
        Assertions.assertNotEquals(subscriberEntity.subscriber.getLastUsageTimestamp(), updatedSubscriber.getLastUsageTimestamp());

        ArgumentCaptor<PresetEntity> captorPersistEntity = ArgumentCaptor.forClass(PresetEntity.class);

        Mockito.verify(presetRepository).persist(captorPersistEntity.capture());
        Assertions.assertNotNull(captorPersistEntity.getValue());
        PresetOperation presetOperation = captorPersistEntity.getValue().presetOperation;
        Assertions.assertEquals(request.getOperationType(), presetOperation.getOperationType());
        Assertions.assertNotNull(presetOperation.getPresetId());
        Assertions.assertEquals(request.getPaTaxCode(), presetOperation.getPaTaxCode());
        Assertions.assertEquals(request.getSubscriberId(), presetOperation.getSubscriberId());
        Assertions.assertNotNull(presetOperation.getCreationTimestamp());
        Assertions.assertEquals(PresetStatus.TO_EXECUTE.name(), presetOperation.getStatus());
        Assertions.assertNotNull(presetOperation.getStatusTimestamp());
        Assertions.assertEquals(request.getNoticeTaxCode(), presetOperation.getNoticeTaxCode());
        Assertions.assertEquals(request.getNoticeNumber(), presetOperation.getNoticeNumber());
        Assertions.assertNull(presetOperation.getStatusDetails());

        logger.debug("Generated preset id -> {}", presetOperation.getPresetId());
        String locationSuffix = "/presets/" + PA_TAX_CODE + "/" + SUBSCRIBER_ID + "/" + presetOperation.getPresetId();
        Assertions.assertTrue(response.getHeader("Location") != null && response.getHeader("Location").endsWith(locationSuffix));

    }


    @Test
    void createPreset_200_dbError_updateSubscriber() {

        Mockito
                .when(subscriberRepository.list(Mockito.any(String.class), Mockito.anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(subscriberEntity)));

        Mockito
                .when(subscriberRepository.update(Mockito.any(SubscriberEntity.class)))
                .thenReturn(Uni.createFrom().failure(new TimeoutException()));

        Mockito
                .when(presetRepository.persist(Mockito.any(PresetEntity.class)))
                .then(i-> Uni.createFrom().item(i.getArgument(0, PresetEntity.class)));

        CreatePresetRequest request = new CreatePresetRequest();
        request.setNoticeNumber("485564829563528563");
        request.setNoticeTaxCode("15376371009");
        request.setOperationType(OperationType.PAYMENT_NOTICE.name());
        request.setPaTaxCode("15376371009");
        request.setSubscriberId(SUBSCRIBER_ID);

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(presetHeaders)
                .body(request)
                .when()
                .post()
                .then()
                .extract()
                .response();

        Assertions.assertEquals(201, response.statusCode());
        Assertions.assertEquals(0, response.body().asString().length());

        ArgumentCaptor<PresetEntity> captorPersistEntity = ArgumentCaptor.forClass(PresetEntity.class);
        Mockito.verify(presetRepository).persist(captorPersistEntity.capture());
        Assertions.assertNotNull(captorPersistEntity.getValue());
        PresetOperation presetOperation = captorPersistEntity.getValue().presetOperation;
        logger.debug("Generated preset id -> {}", presetOperation.getPresetId());
        String locationSuffix = "/presets/" + PA_TAX_CODE + "/" + SUBSCRIBER_ID + "/" + presetOperation.getPresetId();
        Assertions.assertTrue(response.getHeader("Location") != null && response.getHeader("Location").endsWith(locationSuffix));

    }

    // TODO add header validation

    // TODO add request validation

    @Test
    void createPreset_400_subscriberNotFound() {

        Mockito
                .when(subscriberRepository.list(Mockito.any(String.class), Mockito.anyMap()))
                .thenReturn(Uni.createFrom().item(List.of()));

        CreatePresetRequest request = new CreatePresetRequest();
        request.setNoticeNumber("485564829563528563");
        request.setNoticeTaxCode("15376371009");
        request.setOperationType("PAYMENT_NOTICE");
        request.setPaTaxCode("15376371009");
        request.setSubscriberId("x46tr3");

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(presetHeaders)
                .body(request)
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
    void createPreset_500_dbError_listSubscribers() {

        Mockito
                .when(subscriberRepository.list(Mockito.any(String.class), Mockito.anyMap()))
                .thenReturn(Uni.createFrom().failure(new TimeoutException()));

        CreatePresetRequest request = new CreatePresetRequest();
        request.setNoticeNumber("485564829563528563");
        request.setNoticeTaxCode("15376371009");
        request.setOperationType("PAYMENT_NOTICE");
        request.setPaTaxCode("15376371009");
        request.setSubscriberId("x46tr3");

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(presetHeaders)
                .body(request)
                .when()
                .post()
                .then()
                .extract()
                .response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_READING_DATA_FROM_DB));
        Assertions.assertNull(response.getHeader("Location"));
    }


    @Test
    void createPreset_500_dbError_persistPreset() {

        Mockito
                .when(subscriberRepository.list(Mockito.any(String.class), Mockito.anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(subscriberEntity)));

        Mockito
                .when(subscriberRepository.update(Mockito.any(SubscriberEntity.class)))
                .thenReturn(Uni.createFrom().item(subscriberEntity));

        Mockito
                .when(presetRepository.persist(Mockito.any(PresetEntity.class)))
                .thenReturn(Uni.createFrom().failure(new TimeoutException()));

        CreatePresetRequest request = new CreatePresetRequest();
        request.setNoticeNumber("485564829563528563");
        request.setNoticeTaxCode("15376371009");
        request.setOperationType("PAYMENT_NOTICE");
        request.setPaTaxCode("15376371009");
        request.setSubscriberId("x46tr3");

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(presetHeaders)
                .body(request)
                .when()
                .post()
                .then()
                .extract()
                .response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_WRITING_DATA_IN_DB));
        Assertions.assertNull(response.getHeader("Location"));

    }


    @Test
    void getPresets_200() {

        final String timestamp = DateUtils.getCurrentTimestamp();

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

        PresetOperation presetOperation = new PresetOperation();
        presetOperation.setPresetId("77457c64-0870-407a-b2cb-0f948b04fb9a");
        presetOperation.setCreationTimestamp(timestamp);
        presetOperation.setNoticeNumber("485564829563528563");
        presetOperation.setNoticeTaxCode("15376371009");
        presetOperation.setOperationType(OperationType.PAYMENT_NOTICE.name());
        presetOperation.setPaTaxCode("15376371009");
        presetOperation.setStatus(PresetStatus.EXECUTED.name());
        presetOperation.setStatusTimestamp(timestamp);
        presetOperation.setSubscriberId("x46tr3");
        presetOperation.setStatusDetails(statusDetails);

        PresetEntity presetEntity = new PresetEntity();
        presetEntity.id = "77457c64-0870-407a-b2cb-0f948b04fb9a";
        presetEntity.presetOperation = presetOperation;

        Mockito
                .when(presetRepository.list(Mockito.any(String.class), Mockito.anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(presetEntity)));

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(presetHeaders)
                .pathParam("paTaxCode", PA_TAX_CODE)
                .pathParam("subscriberId", SUBSCRIBER_ID)
                .when()
                .get("/{paTaxCode}/{subscriberId}")
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
        List<Notice> noticesResponse = statDetails.getNotices();
        Assertions.assertNotNull(noticesResponse.get(0).getAmount());
        Assertions.assertNotNull(noticesResponse.get(0).getCompany());
        Assertions.assertNotNull(noticesResponse.get(0).getDescription());
        Assertions.assertNotNull(noticesResponse.get(0).getNoticeNumber());
        Assertions.assertNotNull(noticesResponse.get(0).getOffice());
        Assertions.assertNotNull(noticesResponse.get(0).getPaTaxCode());
        Assertions.assertNotNull(noticesResponse.get(0).getPaymentToken());

        // check mongo panache repository integration
        ArgumentCaptor<String> captorFindQuery = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> captorFindArguments = ArgumentCaptor.forClass(Map.class);

        Mockito.verify(presetRepository).list(captorFindQuery.capture(), captorFindArguments.capture());
        Assertions.assertEquals(2, captorFindArguments.getValue().size());
        Assertions.assertEquals(PA_TAX_CODE, captorFindArguments.getValue().get("paTaxCode"));
        Assertions.assertEquals(SUBSCRIBER_ID, captorFindArguments.getValue().get("subscriberId"));
    }

    @Test
    void getPresets_200_emptyPresets() {

        Mockito
                .when(presetRepository.list(Mockito.any(String.class), Mockito.anyMap()))
                .thenReturn(Uni.createFrom().item(List.of()));

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(presetHeaders)
                .pathParam("paTaxCode", PA_TAX_CODE)
                .pathParam("subscriberId", SUBSCRIBER_ID)
                .when()
                .get("/{paTaxCode}/{subscriberId}")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(200, response.statusCode());

        Assertions.assertNotNull(response.jsonPath().getJsonObject("presets"));
        List<PresetOperation> arr = response.jsonPath().getList("presets", PresetOperation.class);
        Assertions.assertEquals(0, arr.size());

    }

    // TODO add header validation

    @Test
    void getPresets_500_dbError_listPresets() {

        Mockito
                .when(presetRepository.list(Mockito.any(String.class), Mockito.anyMap()))
                .thenReturn(Uni.createFrom().failure(new TimeoutException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(presetHeaders)
                .pathParam("paTaxCode", PA_TAX_CODE)
                .pathParam("subscriberId", SUBSCRIBER_ID)
                .when()
                .get("/{paTaxCode}/{subscriberId}")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_READING_DATA_FROM_DB));
        Assertions.assertNull(response.getHeader("Location"));

    }

    @Test
    void getLastPresetsOperation_200() {

        final String timestamp = DateUtils.getCurrentTimestamp();

        PresetOperation presetOperation = new PresetOperation();
        presetOperation.setPresetId("77457c64-0870-407a-b2cb-0f948b04fb9a");
        presetOperation.setCreationTimestamp(timestamp);
        presetOperation.setNoticeNumber("485564829563528563");
        presetOperation.setNoticeTaxCode("15376371009");
        presetOperation.setOperationType(OperationType.PAYMENT_NOTICE.name());
        presetOperation.setPaTaxCode("15376371009");
        presetOperation.setStatus(PresetStatus.EXECUTED.name());
        presetOperation.setStatusTimestamp(timestamp);
        presetOperation.setSubscriberId("x46tr3");
        presetOperation.setStatusDetails(null);

        PresetEntity presetEntity = new PresetEntity();
        presetEntity.id = "77457c64-0870-407a-b2cb-0f948b04fb9a";
        presetEntity.presetOperation = presetOperation;

        ReactivePanacheQuery<PresetEntity> reactivePanacheQuery = Mockito.mock(ReactivePanacheQuery.class);
        Mockito.when(presetRepository.find(Mockito.anyString(), Mockito.any(Sort.class), Mockito.anyMap())).thenReturn(reactivePanacheQuery);
        Mockito.when(reactivePanacheQuery.firstResult()).thenReturn(Uni.createFrom().item(presetEntity));

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(commonHeaders)
                .pathParam("paTaxCode", PA_TAX_CODE)
                .pathParam("subscriberId", SUBSCRIBER_ID)
                .when()
                .get("/{paTaxCode}/{subscriberId}/last_to_execute")
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
        Assertions.assertNull(response.jsonPath().getString("statusDetails"));

        // TODO add check find parameters

    }

    // TODO add header validation

    @Test
    void getLastPresetsOperation_404_presetNotFound() {

        ReactivePanacheQuery<PresetEntity> reactivePanacheQuery = Mockito.mock(ReactivePanacheQuery.class);
        Mockito.when(presetRepository.find(Mockito.anyString(), Mockito.any(Sort.class), Mockito.anyMap())).thenReturn(reactivePanacheQuery);
        Mockito.when(reactivePanacheQuery.firstResult()).thenReturn(Uni.createFrom().nullItem());

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(commonHeaders)
                .pathParam("paTaxCode", PA_TAX_CODE)
                .pathParam("subscriberId", SUBSCRIBER_ID)
                .when()
                .get("/{paTaxCode}/{subscriberId}/last_to_execute")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(404, response.statusCode());
        Assertions.assertEquals(0, response.body().asString().length());

    }

    @Test
    void getLastPresetsOperation_500_dbError_findPreset() {

        ReactivePanacheQuery<PresetEntity> reactivePanacheQuery = Mockito.mock(ReactivePanacheQuery.class);
        Mockito.when(presetRepository.find(Mockito.anyString(), Mockito.any(Sort.class), Mockito.anyMap())).thenReturn(reactivePanacheQuery);
        Mockito.when(reactivePanacheQuery.firstResult()).thenReturn(Uni.createFrom().failure(new TimeoutException()));

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(commonHeaders)
                .pathParam("paTaxCode", PA_TAX_CODE)
                .pathParam("subscriberId", SUBSCRIBER_ID)
                .when()
                .get("/{paTaxCode}/{subscriberId}/last_to_execute")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertTrue(response.jsonPath().getList("errors").contains(ErrorCode.ERROR_READING_DATA_FROM_DB));

    }
}
