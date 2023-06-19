package it.pagopa.swclient.mil.preset.it;

import java.time.Duration;
import java.util.Optional;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.startupcheck.IndefiniteWaitOneShotStartupCheckStrategy;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusIntegrationTest
@TestProfile(IntegrationTestProfile.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PostmanCollectionTestIT implements DevServicesContext.ContextAware {

    static final Logger logger = LoggerFactory.getLogger(PostmanCollectionTestIT.class);

    GenericContainer<?> newmanContainer;

    DevServicesContext devServicesContext;

    @Override
    public void setIntegrationTestContext(DevServicesContext devServicesContext) {
        this.devServicesContext = devServicesContext;
    }

    @BeforeAll
    void startNewmanContainer() {

        //newmanContainer = new GenericContainer<>(DockerImageName.parse("postman/newman:alpine"))
        newmanContainer = new GenericContainer<>(DockerImageName.parse("dannydainton/htmlextra"))
                //.withNetwork(getNetwork())
                //.withNetworkMode(devServicesContext.containerNetworkId().get())
                .waitingFor(Wait.forListeningPort())
                .withStartupCheckStrategy(new IndefiniteWaitOneShotStartupCheckStrategy());

        newmanContainer.withLogConsumer(new Slf4jLogConsumer(logger));

        String exposedPort = devServicesContext.devServicesProperties().get("test.wiremock.exposed-port");

        newmanContainer.setCommand(
                "run",
                "Preset.postman_collection.json",
                "--disable-unicode",
                "-k",
                "--environment", "Local_IT.postman_environment.json",
                "--env-var", "access_token_base_url=http://host.testcontainers.internal:" + exposedPort,
                "--reporters", "htmlextra",
                "--reporter-htmlextra-export", "reports/Preset_Service_Tests.html");

        newmanContainer.withFileSystemBind("./src/test/postman", "/etc/newman");

        // exposing port to newman container
        Config config = ConfigProvider.getConfig();
        Optional<Integer> testPort = config.getOptionalValue("quarkus.http.test-port", Integer.class);
        logger.info("quarkus.http.test-port -> {}", testPort);

        Testcontainers.exposeHostPorts(testPort.orElse(8081));
        Testcontainers.exposeHostPorts(Integer.parseInt(exposedPort));

    }

    @Test
    void testPostmanCollection() {

        try {
            newmanContainer.start();
        } catch (Exception e) {
            logger.error("Error while starting container", e);
        }

        logger.info("newmanContainer.getCurrentContainerInfo().getState() -> {}", newmanContainer.getCurrentContainerInfo().getState());
        Assertions.assertEquals(0, newmanContainer.getCurrentContainerInfo().getState().getExitCodeLong());
    }



    private Network getNetwork() {
        logger.info("devServicesContext.containerNetworkId() -> {}", devServicesContext.containerNetworkId());
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

}
