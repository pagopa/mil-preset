/**
 * 
 */
package it.pagopa.swclient.mil.preset.bean;

import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotNull;

@RegisterForReflection
public class PresetsResponse {
	/*
	 * List of preset operations
	 */
	@NotNull
	private List<PresetResponse> presets;

	/**
	 * @return the presets
	 */
	public List<PresetResponse> getPresets() {
		return presets;
	}

	/**
	 * @param presets the presets to set
	 */
	public void setPresets(List<PresetResponse> presets) {
		this.presets = presets;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PresetsResponse [presets=");
		builder.append(presets);
		builder.append("]");
		return builder.toString();
	}
}
