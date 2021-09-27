package bg.infosys.crc.roaming.helpers.mobile;

public enum MobileErrorCodeEnum {

	OK							(0),
    UNKNOWN_ACTION				(1),
    FAILED_ACTION				(3),
//    SQL_ERROR					(5),
    MISSING_REQUIRED_PARAM		(18),  
    EMAIL_SENDING_FAILED		(29),
//    INVALID_SQUARE			(30),
//    USER_LOGGED_FROM_DEVICE	(31),
    ALREADY_REGISTERED			(32),
    EMAIL_UNKNOWN				(33),
    WRONG_PASSWORD				(34),
    UNAUTHORIZED				(401),
    FORBIDDEN					(403);
    
	private int code;
	
	private MobileErrorCodeEnum(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}

}
