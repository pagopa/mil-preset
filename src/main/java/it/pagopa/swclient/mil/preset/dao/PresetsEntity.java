package it.pagopa.swclient.mil.preset.dao;

import org.bson.codecs.pojo.annotations.BsonId;

import io.quarkus.mongodb.panache.common.MongoEntity;
import it.pagopa.swclient.mil.preset.bean.PaymentTransaction;

/**
 * Entity of the Preset service
 */
@MongoEntity(database = "mil", collection = "presets")
public class PresetsEntity {
	
	/*
	 * id set as presetId
	 */
	@BsonId
	private String id;
	
	/*
	 * Operation type
	 */
	public String operationType;
	
	/*
	 * Preset Id
	 */
	private String presetId;

	/*
	 * Tax code of the creditor company
	 */
	private String paTaxCode;
	
	/*
	 * Subscriber ID
	 */
	private String subscriberId;
	
	/*
	 * Creation timestamp
	 */
	private String creationTimestamp;
	
	/*
	 * Status
	 */
	private String status;
	
	/*
	 * Status timestamp
	 */
	private String statusTimestamp;
	
	/*
	 * Tax code of the creditor company
	 */
	private String noticeTaxCode;
	
	/*
	 * Notice number
	 */
	private String noticeNumber;

	/*
	 * Payment status
	 */
	private PaymentTransaction statusDetails;
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

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
}
