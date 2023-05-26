package it.pagopa.swclient.mil.preset.resource;

import com.google.common.collect.ImmutableMap;
import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.util.Iterator;
import java.util.Map;

public class MongoTestResource implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {
    
	private static final Logger logger = LoggerFactory.getLogger(MongoTestResource.class);
    private static final String MONGO_NETWORK_ALIAS = "mongo-it";

    private GenericContainer<?> mongoContainer;

    private DevServicesContext devServicesContext;
    
	@Override
	public void setIntegrationTestContext(DevServicesContext devServicesContext){
		  this.devServicesContext = devServicesContext;
	}
	
	
	@Override
	public Map<String, String> start() {
		
		 // create a "fake" network using the same id as the one that will be used by Quarkus
        // using the network is the only way to make the withNetworkAliases work
        logger.info("devServicesContext.containerNetworkId() -> " + devServicesContext.containerNetworkId());
        Network testNetwork = new Network() {
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

        mongoContainer = new GenericContainer<>(DockerImageName.parse("mongo:latest"))
                .withNetwork(testNetwork)
                .withNetworkAliases(MONGO_NETWORK_ALIAS)
                //.withNetworkMode(devServicesContext.containerNetworkId().get())
                .waitingFor(Wait.forListeningPort());

        mongoContainer.withLogConsumer(new Slf4jLogConsumer(logger));

        mongoContainer.withFileSystemBind("./src/test/resources", "/home/mongo");
//        mongoContainer.setCommand("--verbose");
        mongoContainer.start();
        
        try {
        	logger.info("----------------------execInContainer START ----------------------");
			

			ExecResult result = mongoContainer.execInContainer("mongosh", "<", "/home/mongo/mongoInit.js");
			
			logger.info("----------------------script executed {} ----------------------",result);
			


			logger.info("----------------------execInContainer END ----------------------");
		} catch (Exception e) {
			logger.error("ERROR ", e);
			e.printStackTrace();
		}

        // Pass the configuration to the application under test
		
		Map<String, String> map = ImmutableMap.of(
					"quarkus.log.level", "DEBUG",
					 "quarkus.log.category.\"it.pagopa.swclient.mil.preset\".level", "DEBUG",
	                "quarkus.mongodb.connection-string","mongodb://" + MONGO_NETWORK_ALIAS + ":" + 27017
	        );
		
		Iterator<Map.Entry<String, String>> itr = map.entrySet().iterator();
		 
		while (itr.hasNext()) {
		    System.out.println(itr.next());
		}
		return map;
	}

	@Override
	public void stop() {
		if (null != mongoContainer) {
			mongoContainer.stop();
		}
		
	}
	
}
