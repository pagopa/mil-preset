package it.pagopa.swclient.mil.preset.it;

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
import it.pagopa.swclient.mil.preset.bean.PaymentTransaction;
import it.pagopa.swclient.mil.preset.bean.PresetOperation;
import it.pagopa.swclient.mil.preset.bean.Role;
import it.pagopa.swclient.mil.preset.dao.SubscriberEntity;
import it.pagopa.swclient.mil.preset.resource.InjectTokenGenerator;
import it.pagopa.swclient.mil.preset.resource.PresetsResource;
import it.pagopa.swclient.mil.preset.util.PresetTestData;
import it.pagopa.swclient.mil.preset.util.TokenGenerator;
import org.apache.commons.lang3.RandomStringUtils;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

@QuarkusIntegrationTest
@TestProfile(IntegrationTestProfile.class)
@TestHTTPEndpoint(PresetsResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PresetsResourceTestIT implements DevServicesContext.ContextAware {

	static final Logger logger = LoggerFactory.getLogger(PresetsResourceTestIT.class);
	public static final String NOTICE_NUMBER = "485564829563528563";
	public static final String OPERATION_PAYMENT_NOTICE = "PAYMENT_NOTICE";

	@InjectTokenGenerator
	TokenGenerator tokenGenerator;

	DevServicesContext devServicesContext;

	MongoClient mongoClient;
	
	CodecRegistry pojoCodecRegistry;

	Map<String, String> posHeaders;
	Map<String, String> institutionPortalHeaders;
	
	String subscriberId;
	String presetId;

	@Override
	public void setIntegrationTestContext(DevServicesContext devServicesContext) {
		this.devServicesContext = devServicesContext;
	}

	@BeforeAll
	void createTestObjects() {

		posHeaders = PresetTestData.getPosHeaders(true, true);
		institutionPortalHeaders = PresetTestData.getInstitutionPortalHeaders();

		subscriberId = RandomStringUtils.random(6, 0, 0, true, true, null, new SecureRandom()).toLowerCase();
		SubscriberEntity subscriberEntity = PresetTestData.getSubscriberEntity(subscriberId, PresetTestData.PA_TAX_CODE, posHeaders);

		// initialize mongo client
		pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
				CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));

		String mongoExposedPort = devServicesContext.devServicesProperties().get("test.mongo.exposed-port");
		mongoClient = MongoClients.create("mongodb://127.0.0.1:" + mongoExposedPort);

		MongoCollection<SubscriberEntity> subscribersCollection = mongoClient.getDatabase("mil")
				.getCollection("subscribers", SubscriberEntity.class)
				.withCodecRegistry(pojoCodecRegistry);

		subscribersCollection.drop();
		subscribersCollection.insertMany(List.of(subscriberEntity));

		MongoCollection<SubscriberEntity> presetsCollection = mongoClient.getDatabase("mil")
				.getCollection("presets", SubscriberEntity.class)
				.withCodecRegistry(pojoCodecRegistry);

		presetsCollection.drop();

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
	@Order(3)
	void createPreset_201() {
		
		CreatePresetRequest request = new CreatePresetRequest();
		request.setNoticeNumber(NOTICE_NUMBER);
		request.setNoticeTaxCode(PresetTestData.PA_TAX_CODE);
		request.setOperationType(OPERATION_PAYMENT_NOTICE);
		request.setPaTaxCode(PresetTestData.PA_TAX_CODE);
		request.setSubscriberId(subscriberId);

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(institutionPortalHeaders)
				.and()
				.auth()
				.oauth2(tokenGenerator.getToken(Role.INSTITUTION_PORTAL))
				.and()
				.body(request)
				.when()
				.post()
				.then()
				.extract()
				.response();
			
        Assertions.assertEquals(201, response.statusCode());
        Assertions.assertEquals(0, response.body().asString().length());

        final String locationPath =  "/presets/" + PresetTestData.PA_TAX_CODE + "/" + subscriberId + "/";
        Assertions.assertTrue(response.getHeader("Location") != null && response.getHeader("Location").contains(locationPath));

		String[] locationParts = response.getHeader("Location").split("/");
		presetId = locationParts[locationParts.length - 1];
		logger.info("Created presetId {}", presetId);
	}
	
	@Test
	@Order(4)
	void createPreset_400_subscriberNotFound() {
		
		CreatePresetRequest request = new CreatePresetRequest();
		request.setNoticeNumber(NOTICE_NUMBER);
		request.setNoticeTaxCode(PresetTestData.PA_TAX_CODE);
		request.setOperationType(OPERATION_PAYMENT_NOTICE);
		request.setPaTaxCode(PresetTestData.PA_TAX_CODE);
		request.setSubscriberId("test12");

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(institutionPortalHeaders)
				.and()
				.auth()
				.oauth2(tokenGenerator.getToken(Role.INSTITUTION_PORTAL))
				.and()
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
	@Order(5)
	void getPresets_200() {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(institutionPortalHeaders)
				.and()
				.auth()
				.oauth2(tokenGenerator.getToken(Role.INSTITUTION_PORTAL))
				.and()
				.pathParam("paTaxCode", PresetTestData.PA_TAX_CODE)
				.pathParam("subscriberId", subscriberId)
				.when()
				.get("/{paTaxCode}/{subscriberId}")
				.then()
				.extract()
				.response();
			
        Assertions.assertEquals(200, response.statusCode());
        
        Assertions.assertNotNull(response.jsonPath().getJsonObject("presets"));
        List<PresetOperation> presets = response.jsonPath().getList("presets", PresetOperation.class);

		Assertions.assertEquals(1, presets.size());

		for (PresetOperation presetOperation: presets) {

			Assertions.assertEquals(subscriberId, presetOperation.getSubscriberId());

			Assertions.assertEquals(NOTICE_NUMBER, presetOperation.getNoticeNumber());
			Assertions.assertEquals(PresetTestData.PA_TAX_CODE, presetOperation.getNoticeTaxCode());
			Assertions.assertEquals(OPERATION_PAYMENT_NOTICE, presetOperation.getOperationType());
			Assertions.assertEquals(PresetTestData.PA_TAX_CODE, presetOperation.getPaTaxCode());
			Assertions.assertEquals(presetId, presetOperation.getPresetId());
			Assertions.assertEquals("TO_EXECUTE", presetOperation.getStatus());
			Assertions.assertNotNull(presetOperation.getCreationTimestamp());
			Assertions.assertNotNull(presetOperation.getStatusTimestamp());

			PaymentTransaction statusDetails = presetOperation.getStatusDetails();
			Assertions.assertNull(statusDetails);
		}
        

	}
	
	@Test
	@Order(1)
	void getPresets_200_emptyPreset() {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(institutionPortalHeaders)
				.and()
				.auth()
				.oauth2(tokenGenerator.getToken(Role.INSTITUTION_PORTAL))
				.and()
				.pathParam("paTaxCode", PresetTestData.PA_TAX_CODE)
				.pathParam("subscriberId", subscriberId)
				.when()
				.get("/{paTaxCode}/{subscriberId}")
				.then()
				.extract()
				.response();
			
        Assertions.assertEquals(200, response.statusCode());
        
        Assertions.assertNotNull(response.jsonPath().getJsonObject("presets"));
        List<PresetOperation> presets = response.jsonPath().getList("presets", PresetOperation.class);
        Assertions.assertEquals(0, presets.size());

	}
	
	@Test
	@Order(6)
	void getLastPresetsOperation_200() {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(posHeaders)
				.and()
				.auth()
				.oauth2(tokenGenerator.getToken(Role.SLAVE_POS))
				.and()
				.pathParam("paTaxCode", PresetTestData.PA_TAX_CODE)
				.pathParam("subscriberId", subscriberId)
				.when()
				.get("/{paTaxCode}/{subscriberId}/last_to_execute")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(200, response.statusCode());

		Assertions.assertEquals(subscriberId, response.jsonPath().getString("subscriberId"));

		Assertions.assertEquals(NOTICE_NUMBER, response.jsonPath().getString("noticeNumber"));
		Assertions.assertEquals(PresetTestData.PA_TAX_CODE, response.jsonPath().getString("paTaxCode"));
		Assertions.assertEquals(OPERATION_PAYMENT_NOTICE, response.jsonPath().getString("operationType"));
		Assertions.assertEquals(PresetTestData.PA_TAX_CODE, response.jsonPath().getString("noticeTaxCode"));
		Assertions.assertEquals(presetId, response.jsonPath().getString("presetId"));
		Assertions.assertEquals("TO_EXECUTE", response.jsonPath().getString("status"));
		Assertions.assertNotNull(response.jsonPath().getString("statusTimestamp"));
		Assertions.assertNotNull(response.jsonPath().getString("creationTimestamp"));

		Assertions.assertNull(response.jsonPath().getJsonObject("statusDetails"));
	}

	@Test
	@Order(2)
	void getLastPresetsOperation_404_presetNotFound() {

		Response response = given()
				.contentType(ContentType.JSON)
				.headers(posHeaders)
				.and()
				.auth()
				.oauth2(tokenGenerator.getToken(Role.SLAVE_POS))
				.and()
				.pathParam("paTaxCode", PresetTestData.PA_TAX_CODE)
				.pathParam("subscriberId", subscriberId)
				.when()
				.get("/{paTaxCode}/{subscriberId}/last_to_execute")
				.then()
				.extract()
				.response();

		Assertions.assertEquals(404, response.statusCode());
		Assertions.assertEquals(0, response.body().asString().length());

	}
	
}
