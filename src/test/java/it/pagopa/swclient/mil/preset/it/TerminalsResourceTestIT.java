package it.pagopa.swclient.mil.preset.it;

import com.mongodb.client.MongoClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import it.pagopa.swclient.mil.preset.bean.SubscribeRequest;
import it.pagopa.swclient.mil.preset.bean.Subscriber;
import it.pagopa.swclient.mil.preset.resource.TerminalsResource;
import it.pagopa.swclient.mil.preset.util.PresetTestData;
import it.pagopa.swclient.mil.preset.util.TokenGenerator;
import org.apache.commons.lang3.StringUtils;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

@QuarkusIntegrationTest
@TestProfile(IntegrationTestProfile.class)
@TestHTTPEndpoint(TerminalsResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TerminalsResourceTestIT {

    static final Logger logger = LoggerFactory.getLogger(TerminalsResourceTestIT.class);

    MongoClient mongoClient;

    Map<String, String> posHeaders;
    Map<String, String> institutionPortalHeaders;

    String bearerInstitutionPortal;
    String bearerSlavePos;

    Map<String, String> subscriberMap;

    @BeforeAll
    void createTestObjects() {

        posHeaders = PresetTestData.getPosHeaders(true, true);
        institutionPortalHeaders = PresetTestData.getInstitutionPortalHeaders();

        bearerInstitutionPortal = /*"Bearer " +*/ TokenGenerator.generate("InstitutionPortal");
        bearerSlavePos = /*"Bearer " ++*/ TokenGenerator.generate("SlavePos");

        subscriberMap = new HashMap<>();
    }

    @AfterAll
    void destroyTestObjects() {

        try {
            mongoClient.close();
        } catch (Exception e) {
            logger.error("Error while closing mongo client", e);
        }

    }

    @Test
    @Order(4)
    void getSubscribers_200() {

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(institutionPortalHeaders)
                .and()
                .auth()
                .oauth2(bearerInstitutionPortal)
                .and()
                .pathParam("paTaxCode", PresetTestData.PA_TAX_CODE)
                .when()
                .get("/{paTaxCode}")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(200, response.statusCode());

        Assertions.assertNotNull(response.jsonPath().getJsonObject("subscribers"));
        List<Subscriber> subscriberlist = response.jsonPath().getList("subscribers", Subscriber.class);

        Assertions.assertEquals(2, subscriberlist.size());

        for (Subscriber subscriber : subscriberlist) {
            Assertions.assertNotNull(subscriber.getSubscriberId());
            Assertions.assertEquals(posHeaders.get("Channel"), subscriber.getChannel());
            Assertions.assertEquals(posHeaders.get("MerchantId"), subscriber.getMerchantId());
            Assertions.assertEquals(subscriberMap.get(subscriber.getSubscriberId()), subscriber.getTerminalId());
            Assertions.assertEquals(PresetTestData.PA_TAX_CODE, subscriber.getPaTaxCode());
            Assertions.assertTrue(StringUtils.startsWith(subscriber.getLabel(), "Reception POS"));
            Assertions.assertNotNull(subscriber.getSubscriptionTimestamp());
            Assertions.assertNotNull(subscriber.getLastUsageTimestamp());
        }

    }

    @Test
    @Order(7)
    void getSubscribers_200_noSubscribers() {

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(institutionPortalHeaders)
                .and()
                .auth()
                .oauth2(bearerInstitutionPortal)
                .and()
                .pathParam("paTaxCode", "00000000000")
                .when()
                .get("/{paTaxCode}")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(200, response.statusCode());

        Assertions.assertNotNull(response.jsonPath().getJsonObject("subscribers"));
        List<Subscriber> res = response.jsonPath().getList("subscribers", Subscriber.class);

        Assertions.assertEquals(0, res.size());

    }

    @Test
    @Order(5)
    void unsubscribe_200_slavePos() {

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(posHeaders)
                .and()
                .auth()
                .oauth2(bearerSlavePos)
                .and()
                .pathParam("paTaxCode", PresetTestData.PA_TAX_CODE)
                .pathParam("subscriberId", subscriberMap.keySet().toArray()[0])
                .when()
                .delete("/{paTaxCode}/{subscriberId}")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(204, response.statusCode());
        Assertions.assertEquals(0, response.body().asString().length());
    }

    @Test
    @Order(6)
    void unsubscribe_200_institutionPortal() {

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(institutionPortalHeaders)
                .and()
                .auth()
                .oauth2(bearerInstitutionPortal)
                .and()
                .pathParam("paTaxCode", PresetTestData.PA_TAX_CODE)
                .pathParam("subscriberId", subscriberMap.keySet().toArray()[1])
                .when()
                .delete("/{paTaxCode}/{subscriberId}")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(204, response.statusCode());
        Assertions.assertEquals(0, response.body().asString().length());

    }

    @Test
    @Order(8)
    void unsubscribe_404() {

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(posHeaders)
                .and()
                .auth()
                .oauth2(bearerSlavePos)
                .and()
                .pathParam("paTaxCode", PresetTestData.PA_TAX_CODE)
                .pathParam("subscriberId", "a00aa0")
                .when()
                .delete("/{paTaxCode}/{subscriberId}")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(404, response.statusCode());
        Assertions.assertEquals(0, response.body().asString().length());
    }

    @Test
    @Order(1)
    void subscribe_200_slavePos1() {

        SubscribeRequest request = new SubscribeRequest();
        request.setPaTaxCode(PresetTestData.PA_TAX_CODE);
        request.setLabel("Reception POS 1");

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(posHeaders)
                .and()
                .auth()
                .oauth2(bearerSlavePos)
                .and()
                .body(request)
                .when()
                .post()
                .then()
                .extract()
                .response();

        Assertions.assertEquals(201, response.statusCode());
        Assertions.assertEquals(0, response.body().asString().length());
        final String locationPath = "/terminals/" + PresetTestData.PA_TAX_CODE + "/";
        Assertions.assertTrue(response.getHeader("Location") != null && response.getHeader("Location").contains(locationPath));

        String[] locationParts = response.getHeader("Location").split("/");
        String subscriberId = locationParts[locationParts.length - 1];
        logger.info("Created subscriberId {}", subscriberId);

        subscriberMap.put(subscriberId, posHeaders.get("TerminalId"));

    }

    @Test
    @Order(2)
    void subscribe_200_slavePos2() {

        SubscribeRequest request = new SubscribeRequest();
        request.setPaTaxCode(PresetTestData.PA_TAX_CODE);
        request.setLabel("Reception POS 2");

        Map<String, String> headerMap = PresetTestData.getPosHeaders(true, true);
        headerMap.put("TerminalId", "1aB9wXyZ");

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(headerMap)
                .and()
                .auth()
                .oauth2(bearerSlavePos)
                .and()
                .body(request)
                .when()
                .post()
                .then()
                .extract()
                .response();

        Assertions.assertEquals(201, response.statusCode());
        Assertions.assertEquals(0, response.body().asString().length());
        final String locationPath = "/terminals/" + PresetTestData.PA_TAX_CODE + "/";
        Assertions.assertTrue(response.getHeader("Location") != null && response.getHeader("Location").contains(locationPath));

        String[] locationParts = response.getHeader("Location").split("/");
        String subscriberId = locationParts[locationParts.length - 1];
        logger.info("Created subscriberId {}", subscriberId);

        subscriberMap.put(subscriberId, headerMap.get("TerminalId"));

    }

    @Test
    @Order(3)
    void subscribe_409() {

        SubscribeRequest request = new SubscribeRequest();
        request.setPaTaxCode(PresetTestData.PA_TAX_CODE);
        request.setLabel("Reception POS");

        Response response = given()
                .contentType(ContentType.JSON)
                .headers(posHeaders)
                .and()
                .auth()
                .oauth2(bearerSlavePos)
                .and()
                .body(request)
                .when()
                .post()
                .then()
                .extract()
                .response();

        Assertions.assertEquals(409, response.statusCode());
        final String locationPath = "/terminals/" + PresetTestData.PA_TAX_CODE + "/";
        Assertions.assertTrue(response.getHeader("Location") != null && response.getHeader("Location").contains(locationPath));

        String[] locationParts = response.getHeader("Location").split("/");
        String subscriberId = locationParts[locationParts.length - 1];
        logger.info("SubscriberId {} already exists for POS", subscriberId);

    }

}