/**
 * 
 */
package it.pagopa.swclient.mil.preset.bean;

import it.pagopa.swclient.mil.preset.ErrorCode;
import jakarta.validation.constraints.Pattern;

public class SubscriberRequest {
	/*
	 * Tax code of the creditor company
	 */
	@Pattern(regexp = "^[0-9]{11}$", message = "[" + ErrorCode.PATAX_CODE_MUST_MATCH_REGEXP + "] paTaxCode must match \"{regexp}\"")
	private String paTaxCode;
	
	/*
	 * Mnemonic terminal label
	 */
	@Pattern(regexp = "^[\\u0001-\\uD7FF\\uE000-\\uFFFD\\u10000-\\u10FFFF]{1,256}$")
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
