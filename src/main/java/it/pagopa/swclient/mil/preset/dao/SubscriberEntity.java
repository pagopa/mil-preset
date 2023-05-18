package it.pagopa.swclient.mil.preset.dao;

import org.bson.codecs.pojo.annotations.BsonId;

import io.quarkus.mongodb.panache.common.MongoEntity;
import jakarta.validation.constraints.Pattern;

/**
 * Entity of the Preset service
 */
@MongoEntity(database = "mil", collection = "subscribers")
public class SubscriberEntity {
	
	/*
	 * id set as subscriber id
	 */
	@BsonId
	private String id;
	
	/*
	 * Acquirer ID assigned by PagoPA
	 */
	public String acquirerId;
	
	/*
	 * Channel originating the request
	 */
	private String channel;

	/*
	 * Merchant ID. Mandatory when Channel equals POS
	 */
	private String merchantId;
	
	/*
	 * ID of the terminal originating the transaction. It must be unique per acquirer and channel.
	 */
	private String terminalId;
	
	/*
	 * Tax code of the creditor company
	 */
	private String paTaxCode;
	
	/*
	 * Subscriber ID
	 */
	private String subscriberId;
	
	
	/*
	 * Mnemonic terminal label
	 */
	@Pattern(regexp = "^[\\u0001-\\uD7FF\\uE000-\\uFFFD\\u10000-\\u10FFFF]{1,256}$")
	private String label;
	
	/*
	 * Subscription timestamp
	 */
	private String subscriptionTimestamp;
	
	/*
	 * Last usage timestamp
	 */
	private String lastUsageTimestamp;

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
	 * @return the acquirerId
	 */
	public String getAcquirerId() {
		return acquirerId;
	}

	/**
	 * @param acquirerId the acquirerId to set
	 */
	public void setAcquirerId(String acquirerId) {
		this.acquirerId = acquirerId;
	}

	/**
	 * @return the channel
	 */
	public String getChannel() {
		return channel;
	}

	/**
	 * @param channel the channel to set
	 */
	public void setChannel(String channel) {
		this.channel = channel;
	}

	/**
	 * @return the merchantId
	 */
	public String getMerchantId() {
		return merchantId;
	}

	/**
	 * @param merchantId the merchantId to set
	 */
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	/**
	 * @return the terminalId
	 */
	public String getTerminalId() {
		return terminalId;
	}

	/**
	 * @param terminalId the terminalId to set
	 */
	public void setTerminalId(String terminalId) {
		this.terminalId = terminalId;
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

	/**
	 * @return the subscriptionTimestamp
	 */
	public String getSubscriptionTimestamp() {
		return subscriptionTimestamp;
	}

	/**
	 * @param subscriptionTimestamp the subscriptionTimestamp to set
	 */
	public void setSubscriptionTimestamp(String subscriptionTimestamp) {
		this.subscriptionTimestamp = subscriptionTimestamp;
	}

	/**
	 * @return the lastUsageTimestamp
	 */
	public String getLastUsageTimestamp() {
		return lastUsageTimestamp;
	}

	/**
	 * @param lastUsageTimestamp the lastUsageTimestamp to set
	 */
	public void setLastUsageTimestamp(String lastUsageTimestamp) {
		this.lastUsageTimestamp = lastUsageTimestamp;
	}
	
}
