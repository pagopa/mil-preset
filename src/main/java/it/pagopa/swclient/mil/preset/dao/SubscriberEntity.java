package it.pagopa.swclient.mil.preset.dao;

import io.quarkus.mongodb.panache.common.MongoEntity;
import it.pagopa.swclient.mil.preset.bean.Subscriber;
import org.bson.codecs.pojo.annotations.BsonId;

/**
 * Entity mapping a terminal subscribed for executing preset operations
 */
@MongoEntity(database = "mil", collection = "subscribers")
public class SubscriberEntity {
	
	/**
	 * The id of the subscribed terminal
	 */
	@BsonId
	public String id;

	/**
	 * Bean containing the terminal data
	 */
	public Subscriber subscriber;
	
}
