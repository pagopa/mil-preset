package it.pagopa.swclient.mil.preset.dao;

import io.quarkus.mongodb.panache.common.MongoEntity;
import it.pagopa.swclient.mil.preset.bean.PresetOperation;
import org.bson.codecs.pojo.annotations.BsonId;

/**
 * Entity of the Preset service
 */
@MongoEntity(database = "mil", collection = "presets")
public class PresetEntity {
	
	/**
	 * The preset identifier
	 */
	@BsonId
	public String id;

	/**
	 * Bean containing the preset operation data
	 */
	public PresetOperation presetOperation;

}

