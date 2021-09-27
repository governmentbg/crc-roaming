package bg.infosys.crc.roaming.components.controllers.mobile;

import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bg.infosys.common.mail.MailException;
import bg.infosys.crc.roaming.components.Properties;
import bg.infosys.crc.roaming.dto.mobile.MobileRequestDTO;
import bg.infosys.crc.roaming.dto.mobile.MobileResponseDTO;
import bg.infosys.crc.roaming.helpers.mobile.MobileErrorCodeEnum;
import bg.infosys.crc.roaming.services.mobile.MobileAuthService;
import bg.infosys.crc.roaming.services.mobile.MobileOperationsService;

@RestController
@RequestMapping(value = "/api")
public class MobileController {
	private static final Logger LOGGER = LoggerFactory.getLogger(MobileController.class);
	public static final Short TELECOM_VERSION = 1;
	
	@Autowired private MobileOperationsService	mobileOperationsService;
	@Autowired private MobileAuthService		mobileAuthService;
	
	private static final String USER_REGISTER				= "register";
	private static final String USER_LOGIN					= "login";
	private static final String USER_LOGIN_GOOGLE			= "login_google";
	private static final String USER_LOGIN_FACEBOOK 		= "login_facebook";
	private static final String USER_LOGIN_APPLE	 		= "login_apple";
	private static final String USER_LOGOUT					= "logout";
	private static final String USER_GENERATE_PASSWORD		= "generate_password";
	private static final String GET_TELECOMS				= "get_telecoms";
	private static final String GET_ROAMINGS				= "get_roamings";
	private static final String REPORT_ROAMING				= "add_roaming";
	private static final String REPORT_BLOCKING				= "add_blocking";
	private static final String GET_ZONES					= "get_zones_ex";
	private static final String GET_HISTORY					= "get_history";
	private static final String CHANGE_PASSWD				= "change_passwd";
	private static final String OMG_ROAMING					= "omg_roaming";
	
	@GetMapping(value = "/mobile", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<MobileResponseDTO> handleActionRequest(MobileRequestDTO params) {
		MobileResponseDTO resp = null;

		String action = params.getA();
		if (action == null) {
			resp = new MobileResponseDTO(MobileErrorCodeEnum.UNKNOWN_ACTION);
		} else if (!Stream.of(USER_REGISTER, USER_LOGIN, USER_LOGIN_GOOGLE, USER_LOGIN_FACEBOOK, USER_LOGIN_APPLE, USER_GENERATE_PASSWORD)
				.anyMatch(action::equalsIgnoreCase)) {
			resp = mobileAuthService.validateLoggedUser(params);
		}
		
		if (resp == null) {		
			switch (params.getA() == null ? "" : params.getA()) {
			case USER_REGISTER:
				try {
					resp = mobileAuthService.registerUserByEmail(params);
				} catch (MailException e) {
					LOGGER.error("Error in e-mail sending", e);
					resp = new MobileResponseDTO(MobileErrorCodeEnum.EMAIL_SENDING_FAILED);
				}
				break;
				
			case USER_LOGIN:
				resp = mobileAuthService.emailLogin(params);
				break;
				
			case USER_LOGIN_GOOGLE:
				resp = mobileAuthService.googleLogin(params);
				break;
				
			case USER_LOGIN_FACEBOOK:
				resp = mobileAuthService.facebookLogin(params);
				break;
				
			case USER_LOGIN_APPLE:
				resp = mobileAuthService.appleLogin(params);
				break;
				
			case USER_LOGOUT:
				resp = mobileAuthService.logout(params);
				break;
				
			case USER_GENERATE_PASSWORD:
				try {
					resp = mobileAuthService.generateUserPassword(params);
				} catch (MailException e) {
					LOGGER.error("Error in e-mail sending", e);
					resp = new MobileResponseDTO(MobileErrorCodeEnum.EMAIL_SENDING_FAILED);
				}
				break;
				
			case GET_TELECOMS:
				resp = mobileOperationsService.getOperators();
				break;
				
			case REPORT_ROAMING:
				resp = mobileOperationsService.reportRoaming(params);
				break;
				
			case REPORT_BLOCKING:
				resp = mobileOperationsService.reportBlocking(params);
				break;
				
			case GET_ZONES:
				resp = mobileOperationsService.getRoamingZones();
				break;
				
			case GET_HISTORY:
				resp = mobileOperationsService.getReportedHistory(params);
				break;
				
			case GET_ROAMINGS:
				resp = mobileOperationsService.getReportedRoamings();
				break;
				
			case CHANGE_PASSWD:
				resp = mobileAuthService.changePasswd(params);
				break;
				
			case OMG_ROAMING:
				resp = mobileOperationsService.omgRoaming(params);
				break;
				
			default:
				resp = new MobileResponseDTO(MobileErrorCodeEnum.UNKNOWN_ACTION);
			}
		}
		
		return ResponseEntity.ok(resp);
	}

	@GetMapping(value = Properties.MOB_MAIL_CONF_LINK, produces = "text/html; charset=UTF-8")
	public ResponseEntity<?> confirmEmail(@RequestParam String key) {
		String location = mobileAuthService.confirmEmail(key);
		return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, location).build();
	}

	@GetMapping(value = Properties.MOB_MAIL_PASS_LINK, produces = "text/html; charset=UTF-8")
	public ResponseEntity<?> changePassword(@RequestParam String key) {
		String location = mobileAuthService.confirmPassChange(key);
		return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, location).build();
	}
	
}
