package it.pagopa.swclient.mil.preset.bean;

import it.pagopa.swclient.mil.preset.ErrorCode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.PathParam;

public class UnsubscriberPathParam {

	/*
	 * Tax code of the creditor company
	 */
	@PathParam(value = "paTaxCode")
	@NotNull(message = "[" + ErrorCode.PATAX_CODE_MUST_NOT_BE_NULL + "] paTaxCode must not be null")
	@Pattern(regexp = "^[0-9]{11}$", message = "[" + ErrorCode.PATAX_CODE_MUST_MATCH_REGEXP + "] paTaxCode must match \"{regexp}\"")
	private String paTaxCode;

	/*
	 * ID assigned to subscribed terminal
	 */
	@PathParam(value = "subscriberId")
	@NotNull(message = "[" + ErrorCode.SUBSCRIBER_ID_MUST_NOT_BE_NULL + "] subscriberId must not be null")
	@Pattern(regexp = "^[0-9a-z]{6}$", message = "[" + ErrorCode.SUBSCRIBER_ID_MUST_MATCH_REGEXP + "] subscriberId must match \"{regexp}\"")
	private String subscriberId;
	
	/**
	 * @return the paTaxCode
	 */
	public String getPaTaxCode() {
		return paTaxCode;
	}

	/**
	 * @param paTaxCode the paTaxCode to set
	 */
	public void setPaTaxCode(String paTaxCode) {
		this.paTaxCode = paTaxCode;
	}

	/**
	 * @return the subscriberId
	 */
	public String getSubscriberId() {
		return subscriberId;
	}

	/**
	 * @param subscriberId the subscriberId to set
	 */
	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("UnsubscriberPathParam [paTaxCode=");
		builder.append(paTaxCode);
		builder.append(", subscriberId=");
		builder.append(subscriberId);
		builder.append("]");
		return builder.toString();
	}
}
