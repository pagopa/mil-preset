package it.pagopa.swclient.mil.preset.util;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.stream.Stream;

import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.junit.jupiter.params.provider.Arguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.pagopa.swclient.mil.preset.ErrorCode;
import jakarta.ws.rs.core.Response;

public class TestUtils {

    static final Logger logger = LoggerFactory.getLogger(TestUtils.class);
    private static final boolean IS_POS 	= true;
    private static final boolean IS_NOT_POS = false;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private TestUtils() {}

    public static <T> T getClonedObject(T objectToClone, Class<T> clazz) {
        try {
            return objectMapper.readValue(objectMapper.writeValueAsString(objectToClone), clazz);
        } catch (JsonProcessingException e) {
            logger.error("Error while cloning object {}", objectToClone);
            return objectToClone;
        }
    }

    public static Stream<Arguments> provideHeaderValidationErrorCases() {
        return Stream.of(
                // RequestId null
                Arguments.of(removeAndGet(PresetTestData.getMilHeaders(IS_POS, true), "RequestId"), it.pagopa.swclient.mil.ErrorCode.REQUEST_ID_MUST_NOT_BE_NULL ),
                // RequestId invalid regex
                Arguments.of(putAndGet(PresetTestData.getMilHeaders(IS_POS, true), "RequestId", "dmmmm0d654e6-97da-4848-b568-99fedccb642ba"), it.pagopa.swclient.mil.ErrorCode.REQUEST_ID_MUST_MATCH_REGEXP ),
                // Version longer than max size
                Arguments.of(putAndGet(PresetTestData.getMilHeaders(false, true), "Version", "1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okayokayokayokayokayokayokayokay"), it.pagopa.swclient.mil.ErrorCode.VERSION_SIZE_MUST_BE_AT_MOST_MAX ),
             // Version invalid regex
                Arguments.of(putAndGet(PresetTestData.getMilHeaders(IS_POS, true), "Version", ".1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay"), it.pagopa.swclient.mil.ErrorCode.VERSION_MUST_MATCH_REGEXP )
        );
    }
    
    
    public static Stream<Arguments> provideAllHeaderValidationErrorCases() {
        return Stream.of(
                // RequestId null
                Arguments.of(removeAndGet(PresetTestData.getMilHeaders(IS_POS, true), "RequestId"), it.pagopa.swclient.mil.ErrorCode.REQUEST_ID_MUST_NOT_BE_NULL ),
                // RequestId invalid regex
                Arguments.of(putAndGet(PresetTestData.getMilHeaders(IS_POS, true), "RequestId", "dmmmm0d654e6-97da-4848-b568-99fedccb642ba"), it.pagopa.swclient.mil.ErrorCode.REQUEST_ID_MUST_MATCH_REGEXP ),
                // Version longer than max size
                Arguments.of(putAndGet(PresetTestData.getMilHeaders(IS_POS, true), "Version", "1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okayokayokayokayokayokayokayokay"), it.pagopa.swclient.mil.ErrorCode.VERSION_SIZE_MUST_BE_AT_MOST_MAX ),
                // Version invalid regex
                Arguments.of(putAndGet(PresetTestData.getMilHeaders(IS_POS, true), "Version", ".1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay"), it.pagopa.swclient.mil.ErrorCode.VERSION_MUST_MATCH_REGEXP ),
                // AcquirerId null
                Arguments.of(removeAndGet(PresetTestData.getMilHeaders(IS_POS, true), "AcquirerId"), it.pagopa.swclient.mil.ErrorCode.ACQUIRER_ID_MUST_NOT_BE_NULL ),
                // AcquirerId invalid regex
                Arguments.of(putAndGet(PresetTestData.getMilHeaders(IS_POS, true), "AcquirerId", "45856bb25"), it.pagopa.swclient.mil.ErrorCode.ACQUIRER_ID_MUST_MATCH_REGEXP ),
                // Channel null
                Arguments.of(removeAndGet(PresetTestData.getMilHeaders(IS_POS, true), "Channel"), it.pagopa.swclient.mil.ErrorCode.CHANNEL_MUST_NOT_BE_NULL ),
                // Channel invalid regex
                Arguments.of(putAndGet(PresetTestData.getMilHeaders(IS_POS, true), "Channel", "ATOM"), it.pagopa.swclient.mil.ErrorCode.CHANNEL_MUST_MATCH_REGEXP ),
                // TerminalId null
                Arguments.of(removeAndGet(PresetTestData.getMilHeaders(IS_POS, true), "TerminalId"), it.pagopa.swclient.mil.ErrorCode.TERMINAL_ID_MUST_NOT_BE_NULL ),
                // TerminalId invalid regex
                Arguments.of(putAndGet(PresetTestData.getMilHeaders(IS_POS, true), "TerminalId", "0aB9wXyZ0029DDDsno9"), it.pagopa.swclient.mil.ErrorCode.TERMINAL_ID_MUST_MATCH_REGEXP ),
                // Merchant invalid regex
                Arguments.of(putAndGet(PresetTestData.getMilHeaders(IS_POS, true), "MerchantId", "0aB9wXyZ00_29DDDsno9"), it.pagopa.swclient.mil.ErrorCode.MERCHANT_ID_MUST_MATCH_REGEXP ),
                // Merchant null if pos
                Arguments.of(removeAndGet(PresetTestData.getMilHeaders(IS_POS, true), "MerchantId"), it.pagopa.swclient.mil.ErrorCode.MERCHANT_ID_MUST_NOT_BE_NULL_FOR_POS )
        );
    }
    
    public static Stream<Arguments> provideAllInstitutionPortalHeaderValidationErrorCases() {
        return Stream.of(
                // RequestId null
                Arguments.of(removeAndGet(PresetTestData.getMilHeaders(IS_POS, true), "RequestId"), it.pagopa.swclient.mil.ErrorCode.REQUEST_ID_MUST_NOT_BE_NULL ),
                // RequestId invalid regex
                Arguments.of(putAndGet(PresetTestData.getMilHeaders(IS_POS, true), "RequestId", "dmmmm0d654e6-97da-4848-b568-99fedccb642ba"), it.pagopa.swclient.mil.ErrorCode.REQUEST_ID_MUST_MATCH_REGEXP ),
                // Version longer than max size
                Arguments.of(putAndGet(PresetTestData.getMilHeaders(IS_POS, true), "Version", "1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okayokayokayokayokayokayokayokay"), it.pagopa.swclient.mil.ErrorCode.VERSION_SIZE_MUST_BE_AT_MOST_MAX ),
                // Version invalid regex
                Arguments.of(putAndGet(PresetTestData.getMilHeaders(IS_POS, true), "Version", ".1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay"), it.pagopa.swclient.mil.ErrorCode.VERSION_MUST_MATCH_REGEXP ),
                // Merchant invalid regex
                Arguments.of(putAndGet(PresetTestData.getMilHeaders(IS_POS, true), "MerchantId", "0aB9wXyZ00_29DDDsno9"), it.pagopa.swclient.mil.ErrorCode.MERCHANT_ID_MUST_MATCH_REGEXP ),
                // Merchant null if pos
                Arguments.of(removeAndGet(PresetTestData.getMilHeaders(IS_POS, true), "MerchantId"), it.pagopa.swclient.mil.ErrorCode.MERCHANT_ID_MUST_NOT_BE_NULL_FOR_POS )
        );
    }

    public static Stream<Arguments> providePaTaxCodeSubscriberIdValidationErrorCases() {

        return Stream.of(
                Arguments.of("15376371009","abcde", ErrorCode.SUBSCRIBER_ID_MUST_MATCH_REGEXP),
                Arguments.of("22314","x46tr3", ErrorCode.PA_TAX_CODE_MUST_MATCH_REGEXP)
        );
    }
    
    public static Stream<Arguments> providePaTaxCodeValidationErrorCases() {

        return Stream.of(
                Arguments.of("22314", ErrorCode.PA_TAX_CODE_MUST_MATCH_REGEXP)
        );
    }

    public static Stream<Arguments> provideActivateRequestValidationErrorCases() {

        return Stream.of(
                Arguments.of(setAndGet(PresetTestData.getCreatePresetRequest(), "operationType", null), ErrorCode.OPERATION_TYPE_MUST_NOT_BE_NULL),
                Arguments.of(setAndGet(PresetTestData.getCreatePresetRequest(), "operationType", "TEST_PAYMENT"), ErrorCode.OPERATION_TYPE_MUST_MATCH_REGEXP),
                Arguments.of(setAndGet(PresetTestData.getCreatePresetRequest(), "paTaxCode", null), ErrorCode.PA_TAX_CODE_MUST_NOT_BE_NULL),
                Arguments.of(setAndGet(PresetTestData.getCreatePresetRequest(), "paTaxCode", "000"), ErrorCode.PA_TAX_CODE_MUST_MATCH_REGEXP),
                Arguments.of(setAndGet(PresetTestData.getCreatePresetRequest(), "subscriberId", null), ErrorCode.SUBSCRIBER_ID_MUST_NOT_BE_NULL),
                Arguments.of(setAndGet(PresetTestData.getCreatePresetRequest(), "subscriberId", "000"), ErrorCode.SUBSCRIBER_ID_MUST_MATCH_REGEXP),
                Arguments.of(setAndGet(PresetTestData.getCreatePresetRequest(), "noticeTaxCode", null), ErrorCode.NOTICE_TAX_CODE_MUST_NOT_BE_NULL),
                Arguments.of(setAndGet(PresetTestData.getCreatePresetRequest(), "noticeTaxCode", "0000"), ErrorCode.NOTICE_TAX_CODE_MUST_MATCH_REGEXP),
                Arguments.of(setAndGet(PresetTestData.getCreatePresetRequest(), "noticeNumber", null), ErrorCode.NOTICE_NUMBER_MUST_NOT_BE_NULL),
                Arguments.of(setAndGet(PresetTestData.getCreatePresetRequest(), "noticeNumber", "0000"), ErrorCode.NOTICE_NUMBER_MUST_MATCH_REGEXP)
                
        );
    }

    public static ClientWebApplicationException getExceptionWithEntity(int statusCode) {
        return new ClientWebApplicationException(Response.status(statusCode).entity("").build());
    }

    private static JsonParser getJsonParser() {
        JsonParser jsonParser = null;
        try {
            jsonParser = new JsonFactory().createParser("{}");
        } catch (IOException ignored) {
        }
        return jsonParser;
    }

    private static <K, V> Map<K, V> removeAndGet(Map<K, V> map, K key) {
        map.remove(key);
        return map;
    }
    private static <K, V> Map<K, V> putAndGet(Map<K, V> map, K key, V value) {
        map.put(key, value);
        return map;
    }

    private static <T, V> T setAndGet(T object, String propertyName, V propertyValue) {

        try {
            PropertyDescriptor desc = new PropertyDescriptor(propertyName, object.getClass());
            Method setter = desc.getWriteMethod();
            setter.invoke(object, propertyValue);
        }
        catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        return object;
    }



}
