package it.pagopa.swclient.mil.preset.dao;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import it.pagopa.swclient.mil.observability.TraceReactivePanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 *  MongoDB repository to access the preset subscribed terminals, reactive flavor
 */
@ApplicationScoped
@TraceReactivePanacheMongoRepository
public class SubscriberRepository implements ReactivePanacheMongoRepository<SubscriberEntity> {

}