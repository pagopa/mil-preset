/**
 * 
 */
package it.pagopa.swclient.mil.preset.bean;

import it.pagopa.swclient.mil.preset.ErrorCode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class SubscribeRequest {

	/*
	 * Tax code of the creditor company
	 */
	@NotNull(message = "[" + ErrorCode.PA_TAX_CODE_MUST_NOT_BE_NULL + "] paTaxCode must not be null")
	@Pattern(regexp = "^[0-9]{11}$", message = "[" + ErrorCode.PA_TAX_CODE_MUST_MATCH_REGEXP + "] paTaxCode must match \"{regexp}\"")
	private String paTaxCode;
	
	/*
	 * Mnemonic terminal label
	 */
	@NotNull(message = "[" + ErrorCode.LABEL_MUST_NOT_BE_NULL + "] label must not be null")
	@Pattern(regexp = "^[\\u0001-\\uD7FF\\uE000-\\uFFFD\\u1000-\\u10FF]{1,256}$", message = "[" + ErrorCode.LABEL_MUST_MATCH_REGEXP + "] paTaxCode must match \"{regexp}\"")
	private String label;

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
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SubscriberRequest [paTaxCode=");
		builder.append(paTaxCode);
		builder.append(", label=");
		builder.append(label);
		builder.append("]");
		return builder.toString();
	}
	
}
