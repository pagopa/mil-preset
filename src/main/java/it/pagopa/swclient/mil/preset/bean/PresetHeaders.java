/**
 * 
 */
package it.pagopa.swclient.mil.preset.bean;

import it.pagopa.swclient.mil.ErrorCode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.HeaderParam;

public class PresetHeaders {
	/*
	 * Request ID
	 */
	@HeaderParam("RequestId")
	@NotNull(message = ErrorCode.REQUEST_ID_MUST_NOT_BE_NULL_MSG)
	@Pattern(regexp = "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$", message = ErrorCode.REQUEST_ID_MUST_MATCH_REGEXP_MSG)
	private String requestId;

	/*
	 * Version of the required API
	 */
	@HeaderParam("Version")
	@Size(max = 64, message = ErrorCode.VERSION_SIZE_MUST_BE_AT_MOST_MAX_MSG)
	@Pattern(regexp = "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$", message = ErrorCode.VERSION_MUST_MATCH_REGEXP_MSG)
	private String version;
	
	/**
	 * @return the requestId
	 */
	public String getRequestId() {
		return requestId;
	}

	/**
	 * @param requestId the requestId to set
	 */
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PresetHeaders [requestId=");
		builder.append(requestId);
		builder.append(", version=");
		builder.append(version);
		builder.append("]");
		return builder.toString();
	}

}
