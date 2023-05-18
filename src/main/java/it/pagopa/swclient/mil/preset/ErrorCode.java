/*
 * ErrorCode.java
 *
 * 12 dec 2022
 */

package it.pagopa.swclient.mil.preset;

/**
 * 
 * @author Antonio Tarricone
 */
public final class ErrorCode {
	public static final String MODULE_ID 								= "00A";
	
	public static final String PATAX_CODE_MUST_NOT_BE_NULL 				= MODULE_ID + "000001";
	public static final String PATAX_CODE_MUST_MATCH_REGEXP 			= MODULE_ID + "000002";
	
	public static final String ERROR_COMMUNICATION_MONGO_DB				= MODULE_ID + "000003";
	
	public static final String SUBSCRIBER_ID_MUST_NOT_BE_NULL 			= MODULE_ID + "000004";
	public static final String SUBSCRIBER_ID_MUST_MATCH_REGEXP 			= MODULE_ID + "000005";
	
	public static final String ERROR_SUBSCRIBER_NOT_FOUND				= MODULE_ID + "000006";
	
	public static final String ERROR_STORING_TERMINAL_IN_DB				= MODULE_ID + "000007";
	public static final String ERROR_CONFLICT_TERMINAL_IN_DB			= MODULE_ID + "000008";

	public static final String OPERATION_TYPE_MUST_NOT_BE_NULL 			= MODULE_ID + "000009";
	public static final String OPERATION_TYPE_MUST_MATCH_REGEXP 		= MODULE_ID + "00000A";
	
	public static final String NOTICE_TAX_CODE_MUST_NOT_BE_NULL 		= MODULE_ID + "00000B";
	public static final String NOTICE_TAX_CODE_MUST_MATCH_REGEXP 		= MODULE_ID + "00000C";
	
	public static final String NOTICE_NUMBER_MUST_NOT_BE_NULL 			= MODULE_ID + "00000D";
	public static final String NOTICE_NUMBER_MUST_MATCH_REGEXP 			= MODULE_ID + "00000E";
	
	public static final String ERROR_PRESET_OPERATION_NOT_FOUND			= MODULE_ID + "00000F";
	
	private ErrorCode() {
		
	}
}
