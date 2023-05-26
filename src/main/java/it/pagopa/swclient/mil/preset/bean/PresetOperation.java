/**
 * 
 */
package it.pagopa.swclient.mil.preset.bean;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@RegisterForReflection
public class PresetOperation {
	/*
	 * Operation type
	 */
	@NotNull
	@Pattern(regexp = "PAYMENT_NOTICE")
	public String operationType;
	
	/*
	 * Preset Id
	 */
	@NotNull
	@Pattern(regexp = "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$")
	private String presetId;

	/*
	 * Tax code of the creditor company
	 */
	@NotNull
	@Pattern(regexp = "^[0-9]{11}$")
	private String paTaxCode;
	
	/*
	 * Subscriber ID
	 */
	@NotNull
	@Pattern(regexp = "^[0-9a-z]{6}$")
	private String subscriberId;
	
	/*
	 * Creation timestamp
	 */
	@NotNull
	@Max(value = 19)
	private String creationTimestamp;
	
	/*
	 * Status
	 */
	@Pattern(regexp = "TO_EXECUTE|EXECUTED")
	private String status;
	
	/*
	 * Status timestamp
	 */
	@NotNull
	@Max(value = 19)
	private String statusTimestamp;
	
	/*
	 * Tax code of the creditor company
	 */
	@NotNull
	@Pattern(regexp = "^[0-9]{11}$")
	private String noticeTaxCode;
	
	/*
	 * Notice number
	 */
	@NotNull
	@Pattern(regexp = "^[0-9]{18}$")
	private String noticeNumber;
	
	/*
	 * Payment status
	 */
	@NotNull
	private PaymentTransaction statusDetails;

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
	 * @return the creationTimestamp
	 */
	public String getCreationTimestamp() {
		return creationTimestamp;
	}

	/**
	 * @param creationTimestamp the creationTimestamp to set
	 */
	public void setCreationTimestamp(String creationTimestamp) {
		this.creationTimestamp = creationTimestamp;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the statusTimestamp
	 */
	public String getStatusTimestamp() {
		return statusTimestamp;
	}

	/**
	 * @param statusTimestamp the statusTimestamp to set
	 */
	public void setStatusTimestamp(String statusTimestamp) {
		this.statusTimestamp = statusTimestamp;
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

	/**
	 * @return the statusDetails
	 */
	public PaymentTransaction getStatusDetails() {
		return statusDetails;
	}

	/**
	 * @param statusDetails the statusDetails to set
	 */
	public void setStatusDetails(PaymentTransaction statusDetails) {
		this.statusDetails = statusDetails;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PresetOperation [operationType=");
		builder.append(operationType);
		builder.append(", presetId=");
		builder.append(presetId);
		builder.append(", paTaxCode=");
		builder.append(paTaxCode);
		builder.append(", subscriberId=");
		builder.append(subscriberId);
		builder.append(", creationTimestamp=");
		builder.append(creationTimestamp);
		builder.append(", status=");
		builder.append(status);
		builder.append(", statusTimestamp=");
		builder.append(statusTimestamp);
		builder.append(", noticeTaxCode=");
		builder.append(noticeTaxCode);
		builder.append(", noticeNumber=");
		builder.append(noticeNumber);
		builder.append(", statusDetails=");
		builder.append(statusDetails);
		builder.append("]");
		return builder.toString();
	}
}
