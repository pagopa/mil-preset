package it.pagopa.swclient.mil.preset.it;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import io.quarkus.test.junit.QuarkusTestProfile;
import it.pagopa.swclient.mil.preset.resource.IDPTestResource;
import it.pagopa.swclient.mil.preset.resource.KafkaTestResource;
import it.pagopa.swclient.mil.preset.resource.MongoTestResource;
import it.pagopa.swclient.mil.preset.resource.WiremockTestResource;

public class IntegrationTestProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {

        Map<String, String> configOverrides = new HashMap<>();

        configOverrides.put("preset.quarkus-log-level", "INFO");
        configOverrides.put("preset.app-log-level", "DEBUG");
        configOverrides.put("preset.location.base-url", "https://mil-d-apim.azure-api.net/mil-payment-notice");

        return configOverrides;
    }

    @Override
    public List<TestResourceEntry> testResources() {
        return ImmutableList.of(
                new TestResourceEntry(IDPTestResource.class),
                new TestResourceEntry(MongoTestResource.class),
                new TestResourceEntry(KafkaTestResource.class),
                new TestResourceEntry(WiremockTestResource.class)
        );
    }

    @Override
    public boolean disableGlobalTestResources() {
        return true;
    }

}
