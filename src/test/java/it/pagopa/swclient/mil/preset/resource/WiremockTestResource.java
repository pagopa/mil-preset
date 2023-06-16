package it.pagopa.swclient.mil.preset.resource;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

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
                .waitingFor(Wait.forListeningPort());

        wiremockContainer.withLogConsumer(new Slf4jLogConsumer(logger));
        wiremockContainer.setCommand("--verbose --local-response-templating");
        wiremockContainer.withFileSystemBind("./src/test/resources/it/wiremock", "/home/wiremock");

        wiremockContainer.start();
        
        
        final String wiremockEndpoint = "http://" + WIREMOCK_NETWORK_ALIAS + ":" + 8080;

        
        
        Path path = Paths.get("./src/test/postman/Preset.postman_collection.json");
        Charset charset = StandardCharsets.UTF_8;

        String content;
		try {
			content = new String(Files.readAllBytes(path), charset);
	        content = content.replaceAll("<token_url>", wiremockEndpoint);
	        Files.write(path, content.getBytes(charset));
		} catch (IOException e) {
			logger.error("Error replacing URL in file");
			e.printStackTrace();
		}
        
        // Pass the configuration to the application under test
        return ImmutableMap.of(
                "jwt-publickey-location", wiremockEndpoint + "/jwks.json"
        );
        
        
       

        

    }

    private static void generateMockFilesForAuth() {


    }


    // create a "fake" network using the same id as the one that will be used by Quarkus
    // using the network is the only way to make the withNetworkAliases work
    private Network getNetwork() {
        logger.info("devServicesContext.containerNetworkId() -> " + devServicesContext.containerNetworkId());
        return new Network() {
            @Override
            public String getId() {
                return devServicesContext.containerNetworkId().get();
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
