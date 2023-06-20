package it.pagopa.swclient.mil.preset.resource;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import org.apache.commons.io.IOUtils;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import com.google.common.collect.ImmutableMap;

import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;


public class WiremockTestResource implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {

    private static final Logger logger = LoggerFactory.getLogger(WiremockTestResource.class);
    private static final String WIREMOCK_NETWORK_ALIAS = "wiremock-it";

    private GenericContainer<?> wiremockContainer;

    private DevServicesContext devServicesContext;

    @Override
    public int order() {
        return 1;
    }

    public void setIntegrationTestContext(DevServicesContext devServicesContext) {
        this.devServicesContext = devServicesContext;
    }

    @Override
    public Map<String, String> start() {

        logger.info("Starting WireMock container...");

        wiremockContainer = new GenericContainer<>(DockerImageName.parse("wiremock/wiremock:latest"))
                .withNetwork(getNetwork())
                .withNetworkAliases(WIREMOCK_NETWORK_ALIAS)
                //.withNetworkMode(devServicesContext.containerNetworkId().get())
                .waitingFor(Wait.forListeningPort())
                .withExposedPorts(8080);

        //wiremockContainer.withLogConsumer(new Slf4jLogConsumer(logger));
        wiremockContainer.setCommand("--verbose --local-response-templating");
        wiremockContainer.withFileSystemBind("./src/test/resources/it/wiremock/mappings", "/home/wiremock/mappings");
        wiremockContainer.withFileSystemBind("./target/generated-idp-files", "/home/wiremock/__files");

        wiremockContainer.start();

        final Integer exposedPort = wiremockContainer.getMappedPort(8080);
        devServicesContext.devServicesProperties().put("test.wiremock.exposed-port", exposedPort.toString());

        // Pass the configuration to the application under test
        return ImmutableMap.of(
                "jwt-publickey-location", "http://" + WIREMOCK_NETWORK_ALIAS + ":8080/jwks.json"
        );

    }


    // create a "fake" network using the same id as the one that will be used by Quarkus
    // using the network is the only way to make the withNetworkAliases work
    private Network getNetwork() {
        logger.info("devServicesContext.containerNetworkId() -> " + devServicesContext.containerNetworkId());
        return new Network() {
            @Override
            public String getId() {
                return devServicesContext.containerNetworkId().orElse(null);
            }

            @Override
            public void close() {

            }

            @Override
            public Statement apply(Statement statement, Description description) {
                return null;
            }
        };
    }

    @Override
    public void stop() {
        // Stop the needed container(s)
        if (wiremockContainer != null) {
            logger.info("Stopping WireMock container...");
            wiremockContainer.stop();
            logger.info("WireMock container stopped!");
        }
    }
}
