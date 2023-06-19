package it.pagopa.swclient.mil.preset.bean;

import it.pagopa.swclient.mil.ErrorCode;
import it.pagopa.swclient.mil.bean.Channel;
import it.pagopa.swclient.mil.preset.validation.constraints.AcquirerIdNotNullForRole;
import it.pagopa.swclient.mil.preset.validation.constraints.ChannelNotNullForRole;
import it.pagopa.swclient.mil.preset.validation.constraints.MerchantIdNotNullForRole;
import it.pagopa.swclient.mil.preset.validation.constraints.TerminalIdNotNullForRole;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.HeaderParam;

@MerchantIdNotNullForRole(roles = {Role.SLAVE_POS}, message = ErrorCode.MERCHANT_ID_MUST_NOT_BE_NULL_FOR_POS_MSG)
@AcquirerIdNotNullForRole(roles = {Role.SLAVE_POS}, message = ErrorCode.ACQUIRER_ID_MUST_NOT_BE_NULL_MSG)
@ChannelNotNullForRole(roles = {Role.SLAVE_POS}, message = ErrorCode.CHANNEL_MUST_NOT_BE_NULL_MSG)
@TerminalIdNotNullForRole(roles = {Role.SLAVE_POS}, message = ErrorCode.TERMINAL_ID_MUST_NOT_BE_NULL_MSG)
public class UnsubscribeHeaders {
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

	/*
	 * Acquirer ID assigned by PagoPA
	 */
	@HeaderParam("AcquirerId")
	@Pattern(regexp = "^\\d{1,11}$", message = ErrorCode.ACQUIRER_ID_MUST_MATCH_REGEXP_MSG)
	private String acquirerId;

	/*
	 * Channel originating the request
	 */
	@HeaderParam("Channel")
	@Pattern(regexp = "^(" + Channel.ATM + "|" + Channel.POS + "|" + Channel.TOTEM + "|" + Channel.CASH_REGISTER + "|" + Channel.CSA + ")$", message = ErrorCode.CHANNEL_MUST_MATCH_REGEXP_MSG)
	private String channel;

	/*
	 * Merchant ID originating the transaction. If Channel equals to POS, MerchantId must not be null.
	 */
	@HeaderParam("MerchantId")
	@Pattern(regexp = "^[0-9a-zA-Z]{1,15}$", message = ErrorCode.MERCHANT_ID_MUST_MATCH_REGEXP_MSG)
	private String merchantId;

	/*
	 * ID of the terminal originating the transaction. It must be unique per acquirer, channel and
	 * merchant if present.
	 */
	@HeaderParam("TerminalId")
	@Pattern(regexp = "^[0-9a-zA-Z]{1,8}$", message = ErrorCode.TERMINAL_ID_MUST_MATCH_REGEXP_MSG)
	private String terminalId;

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

	@Override
	public String toString() {
		return new StringBuilder("CommonHeader [requestId=").append(requestId)
			.append(", version=").append(version)
			.append(", acquirerId=").append(acquirerId)
			.append(", channel=").append(channel)
			.append(", merchantId=").append(merchantId)
			.append(", terminalId=").append(terminalId)
			.append("]")
			.toString();
	}
}
