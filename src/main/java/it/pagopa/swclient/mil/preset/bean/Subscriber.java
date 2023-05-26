package it.pagopa.swclient.mil.preset.bean;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@RegisterForReflection
public class Subscriber {
	
	/*
	 * Acquirer ID assigned by PagoPA
	 */
	@NotNull
	@Pattern(regexp = "^\\d{1,11}$")
	private String acquirerId;
	
	/*
	 * Channel originating the request
	 */
	@NotNull
	@Pattern(regexp = "[ATM|POS|TOTEM|CASH_REGISTER|CSA]")
	private String channel;

	/*
	 * Merchant ID. Mandatory when Channel equals POS
	 */
	@NotNull
	@Pattern(regexp = "^[0-9a-zA-Z]")
	private String merchantId;
	
	/*
	 * ID of the terminal originating the transaction. It must be unique per acquirer and channel.
	 */
	@NotNull
	@Pattern(regexp = "^[0-9a-zA-Z]{1,8}$")
	private String terminalId;
	
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
	 * Mnemonic terminal label
	 */
	@NotNull
	@Pattern(regexp = "^[\\u0001-\\uD7FF\\uE000-\\uFFFD\\u10000-\\u10FFFF]{1,256}$")
	private String label;
	
	/*
	 * Subscription timestamp
	 */
	@NotNull
	@Max(value = 19)
	private String subscriptionTimestamp;
	
	/*
	 * Last usage timestamp
	 */
	@NotNull
	@Max(value = 19)
	private String lastUsageTimestamp;

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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SubscribersResponse [acquirerId=");
		builder.append(acquirerId);
		builder.append(", channel=");
		builder.append(channel);
		builder.append(", merchantId=");
		builder.append(merchantId);
		builder.append(", terminalId=");
		builder.append(terminalId);
		builder.append(", paTaxCode=");
		builder.append(paTaxCode);
		builder.append(", subscriberId=");
		builder.append(subscriberId);
		builder.append(", label=");
		builder.append(label);
		builder.append(", subscriptionTimestamp=");
		builder.append(subscriptionTimestamp);
		builder.append(", lastUsageTimestamp=");
		builder.append(lastUsageTimestamp);
		builder.append("]");
		return builder.toString();
	}
}
