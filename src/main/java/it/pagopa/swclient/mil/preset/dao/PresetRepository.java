package it.pagopa.swclient.mil.preset.dao;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import it.pagopa.swclient.mil.observability.TraceReactivePanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 *  MongoDB repository to access the preset information, reactive flavor
 */
@ApplicationScoped
@TraceReactivePanacheMongoRepository
public class PresetRepository implements ReactivePanacheMongoRepository<PresetEntity> {

}