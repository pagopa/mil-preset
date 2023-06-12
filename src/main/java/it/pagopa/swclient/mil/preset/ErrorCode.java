package it.pagopa.swclient.mil.preset;

public final class ErrorCode {
	public static final String MODULE_ID 								= "00A";


	// validation errors
	public static final String SUBSCRIBE_REQUEST_MUST_NOT_BE_EMPTY 		= MODULE_ID + "000001";

	public static final String PA_TAX_CODE_MUST_NOT_BE_NULL 			= MODULE_ID + "000002";
	public static final String PA_TAX_CODE_MUST_MATCH_REGEXP 			= MODULE_ID + "000003";

	public static final String LABEL_MUST_NOT_BE_NULL 					= MODULE_ID + "000004";
	public static final String LABEL_MUST_MATCH_REGEXP 					= MODULE_ID + "000005";

	public static final String CREATE_PRESET_REQUEST_MUST_NOT_BE_EMPTY 	= MODULE_ID + "000006";

	public static final String SUBSCRIBER_ID_MUST_NOT_BE_NULL 			= MODULE_ID + "000007";
	public static final String SUBSCRIBER_ID_MUST_MATCH_REGEXP 			= MODULE_ID + "000008";

	public static final String OPERATION_TYPE_MUST_NOT_BE_NULL 			= MODULE_ID + "000009";
	public static final String OPERATION_TYPE_MUST_MATCH_REGEXP 		= MODULE_ID + "00000A";
	
	public static final String NOTICE_TAX_CODE_MUST_NOT_BE_NULL 		= MODULE_ID + "00000B";
	public static final String NOTICE_TAX_CODE_MUST_MATCH_REGEXP 		= MODULE_ID + "00000C";
	
	public static final String NOTICE_NUMBER_MUST_NOT_BE_NULL 			= MODULE_ID + "00000D";
	public static final String NOTICE_NUMBER_MUST_MATCH_REGEXP 			= MODULE_ID + "00000E";

	// business logic errors
	public static final String SUBSCRIBER_NOT_FOUND						= MODULE_ID + "00000F";
	public static final String SUBSCRIBER_ALREADY_EXISTS				= MODULE_ID + "00000F";
	public static final String PRESET_OPERATION_NOT_FOUND				= MODULE_ID + "000010";


	// integration errors
	public static final String ERROR_WRITING_DATA_IN_DB					= MODULE_ID + "000011";
	public static final String ERROR_READING_DATA_FROM_DB				= MODULE_ID + "000012";
	
	public static final String ERROR_UNAUTHORIZED						= MODULE_ID + "000013";
	public static final String ERROR_FORBIDDEN							= MODULE_ID + "000014";


	private ErrorCode() {
		
	}
}
