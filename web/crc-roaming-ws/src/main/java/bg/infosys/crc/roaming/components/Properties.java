package bg.infosys.crc.roaming.components;

import bg.infosys.common.utils.PropertiesUtil;

public class Properties {
	public static String get(String key) {
		return APP_CONF.getProperty(key);
	}

	public static final java.util.Properties APP_CONF  = PropertiesUtil.loadProperties("application.properties");
	
	public static final String	API_URL					= Properties.get("api.url");
	public static final String	API_PORTAL				= API_URL + "/web/portal";
	
	public static final String 	PATH_CNF_DEL_USR		= "/o/cnfdlusr";
	public static final String	PATH_CNF_DEL_USR_FULL	= API_PORTAL + PATH_CNF_DEL_USR;
	
	public static final String	WEB_URL_EXPIRED_LINK	= Properties.get("webapp.urlMsg") + "linkExpired";
	public static final String 	WEB_URL_USR_DEL_OK		= Properties.get("webapp.urlMsg") + "userDelOk";
	public static final String	WEB_URL_PASS_CHANED		= Properties.get("webapp.urlMsg") + "passChangedOk";
	public static final String	WEB_URL_MAIL_CONFIRMED	= Properties.get("webapp.urlMsg") + "mailConfirmedOK";
	public static final String	WEB_URL_MAIL_ALRDY_CONF = Properties.get("webapp.urlMsg") + "mailConfirmedAlready";
	
	public static final int		SYS_USER_ID				= -1;
	public static final String	SYS_AUTO_NAME			= "System Automatic";
	public static final int		MAX_PAGE_SIZE			= 100;
	
	public static final String	CLAIM_USER_ID			= "uem";
	public static final String	CLAIM_USER_PASS			= "up";
	public static final int		MAIL_VALIDITY_MINS		= 120;
	
	public static final String	MOB_MAIL_CONF_LINK		= "/m-confirm";
	public static final String	MOB_MAIL_PASS_LINK		= "/m-npwd";
	
	public static final String	APPLE_KEYS_URL			= "https://appleid.apple.com/auth/keys";
	public static final String	IPINFO_TOKEN			= Properties.get("ipinfo.token");
}
