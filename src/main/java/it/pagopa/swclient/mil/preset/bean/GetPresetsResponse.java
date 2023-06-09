/**
 * 
 */
package it.pagopa.swclient.mil.preset.bean;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@RegisterForReflection
public class GetPresetsResponse {

	/*
	 * List of preset operations
	 */
	@NotNull
	private List<PresetOperation> presets;

	/**
	 * @return the presets
	 */
	public List<PresetOperation> getPresets() {
		return presets;
	}

	/**
	 * @param presets the presets to set
	 */
	public void setPresets(List<PresetOperation> presets) {
		this.presets = presets;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GetPresetsResponse [presets=");
		builder.append(presets);
		builder.append("]");
		return builder.toString();
	}
}
