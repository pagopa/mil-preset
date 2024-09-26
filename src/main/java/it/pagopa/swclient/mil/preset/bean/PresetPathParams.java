package it.pagopa.swclient.mil.preset.bean;

import it.pagopa.swclient.mil.preset.ErrorCode;
import jakarta.validation.constraints.NotNull;
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
	 * @param presetId the presetId to set
	 */
	public void setPresetId(String presetId) {
		this.presetId = presetId;
	}


	@Override
	public String toString() {
		return "PresetPathParam [presetId=" +
				presetId +
				"]";
	}
}
