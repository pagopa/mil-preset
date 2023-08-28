package it.pagopa.swclient.mil.preset.bean;

import it.pagopa.swclient.mil.preset.ErrorCode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.PathParam;

public class PresetPathParams {


	/*
	 * ID assigned to subscribed terminal
	 */
	@PathParam(value = "presetId")
	@NotNull(message = "[" + ErrorCode.PRESET_ID_MUST_NOT_BE_NULL + "] presetId must not be null")
	private String presetId;
	
	/**
	 * @return the presetId
	 */
	public String getPresetId() {
		return presetId;
	}

	/**
	 * @param subscriberId the subscriberId to set
	 */
	public void setPresetId(String subscriberId) {
		this.presetId = presetId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PresetPathParam [presetId=");
		builder.append(presetId);
		builder.append("]");
		return builder.toString();
	}
}
