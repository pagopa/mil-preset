package it.pagopa.swclient.mil.preset.it;

import com.google.common.collect.ImmutableList;
import io.quarkus.test.junit.QuarkusTestProfile;
import it.pagopa.swclient.mil.preset.resource.MongoTestResource;
import it.pagopa.swclient.mil.preset.resource.RedpandaTestResource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntegrationTestProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {

        Map<String, String> configOverrides = new HashMap<>();

        configOverrides.put("preset.quarkus-log-level", "DEBUG");
        configOverrides.put("preset.app-log-level", "DEBUG");

        return configOverrides;
    }

    @Override
    public List<TestResourceEntry> testResources() {
        return ImmutableList.of(
                new TestResourceEntry(MongoTestResource.class),
                new TestResourceEntry(RedpandaTestResource.class)
        );
    }

    @Override
    public boolean disableGlobalTestResources() {
        return true;
    }

}
