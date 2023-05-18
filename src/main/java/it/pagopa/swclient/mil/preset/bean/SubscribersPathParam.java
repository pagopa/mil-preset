package it.pagopa.swclient.mil.preset.bean;

import it.pagopa.swclient.mil.preset.ErrorCode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.PathParam;

public class SubscribersPathParam {

	/*
	 * Tax code of the creditor company
	 */
	@PathParam(value = "paTaxCode")
	@NotNull(message = "[" + ErrorCode.PATAX_CODE_MUST_NOT_BE_NULL + "] paTaxCode must not be null")
	@Pattern(regexp = "^[0-9]{11}$", message = "[" + ErrorCode.PATAX_CODE_MUST_MATCH_REGEXP + "] paTaxCode must match \"{regexp}\"")
	private String paTaxCode;

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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SubscribersPathParam [paTaxCode=");
		builder.append(paTaxCode);
		builder.append("]");
		return builder.toString();
	}

}
