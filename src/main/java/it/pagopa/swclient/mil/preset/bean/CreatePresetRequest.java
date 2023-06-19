/**
 * 
 */
package it.pagopa.swclient.mil.preset.bean;

import it.pagopa.swclient.mil.preset.ErrorCode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class CreatePresetRequest {
	
	/*
	 * 	Type of preset operation
	 */
	@NotNull(message = "[" + ErrorCode.OPERATION_TYPE_MUST_NOT_BE_NULL + "] operationType must not be null")
	@Pattern(regexp = "PAYMENT_NOTICE", message = "[" + ErrorCode.OPERATION_TYPE_MUST_MATCH_REGEXP + "] operationType must match \"{regexp}\"")
	private String operationType;

	
	/*
	 * Tax code of the creditor company
	 */
	@NotNull(message = "[" + ErrorCode.PA_TAX_CODE_MUST_NOT_BE_NULL + "] paTaxCode must not be null")
	@Pattern(regexp = "^[0-9]{11}$", message = "[" + ErrorCode.PA_TAX_CODE_MUST_MATCH_REGEXP + "] paTaxCode must match \"{regexp}\"")
	private String paTaxCode;
	
	
	/*
	 * Subscriber ID
	 */
	@NotNull(message = "[" + ErrorCode.SUBSCRIBER_ID_MUST_NOT_BE_NULL + "] subscriberId must not be null")
	@Pattern(regexp = "^[0-9a-z]{6}$", message = "[" + ErrorCode.SUBSCRIBER_ID_MUST_MATCH_REGEXP + "] subscriberId must match \"{regexp}\"")
	private String subscriberId;
	
	
	/*
	 * Tax code of the creditor company
	 */
	@NotNull(message = "[" + ErrorCode.NOTICE_TAX_CODE_MUST_NOT_BE_NULL + "] noticeTaxCode must not be null")
	@Pattern(regexp = "^[0-9]{11}$", message = "[" + ErrorCode.NOTICE_TAX_CODE_MUST_MATCH_REGEXP + "] noticeTaxCode must match \"{regexp}\"")
	private String noticeTaxCode;
	
	/*
	 * Notice number
	 */
	@NotNull(message = "[" + ErrorCode.NOTICE_NUMBER_MUST_NOT_BE_NULL + "] noticeNumber must not be null")
	@Pattern(regexp = "^[0-9]{18}$", message = "[" + ErrorCode.NOTICE_NUMBER_MUST_MATCH_REGEXP + "] noticeNumber must match \"{regexp}\"")
	private String noticeNumber;

	/**
	 * @return the operationType
	 */
	public String getOperationType() {
		return operationType;
	}

	/**
	 * @param operationType the operationType to set
	 */
	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}

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

	/**
	 * @return the noticeTaxCode
	 */
	public String getNoticeTaxCode() {
		return noticeTaxCode;
	}

	/**
	 * @param noticeTaxCode the noticeTaxCode to set
	 */
	public void setNoticeTaxCode(String noticeTaxCode) {
		this.noticeTaxCode = noticeTaxCode;
	}

	/**
	 * @return the noticeNumber
	 */
	public String getNoticeNumber() {
		return noticeNumber;
	}

	/**
	 * @param noticeNumber the noticeNumber to set
	 */
	public void setNoticeNumber(String noticeNumber) {
		this.noticeNumber = noticeNumber;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PresetRequest [operationType=");
		builder.append(operationType);
		builder.append(", paTaxCode=");
		builder.append(paTaxCode);
		builder.append(", subscriberId=");
		builder.append(subscriberId);
		builder.append(", noticeTaxCode=");
		builder.append(noticeTaxCode);
		builder.append(", noticeNumber=");
		builder.append(noticeNumber);
		builder.append("]");
		return builder.toString();
	} 
}
