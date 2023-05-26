package it.pagopa.swclient.mil.preset.dao;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 *  MongoDB repository to access the preset information, reactive flavor
 */
@ApplicationScoped
public class PresetRepository implements ReactivePanacheMongoRepository<PresetEntity> {

}