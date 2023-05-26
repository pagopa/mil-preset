package it.pagopa.swclient.mil.preset.bean;

/**
 * Possible status of a payment transaction
 */
public enum PaymentTransactionStatus {

	PRE_CLOSE,
	ERROR_ON_PAYMENT,
	PENDING,
	CLOSED,
	ERROR_ON_CLOSE,
	ERROR_ON_RESULT,
	ABORTED

}
