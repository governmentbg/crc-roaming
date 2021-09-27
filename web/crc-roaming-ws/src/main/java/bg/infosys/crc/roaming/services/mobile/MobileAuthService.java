package bg.infosys.crc.roaming.services.mobile;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;

import bg.infosys.common.mail.Email;
import bg.infosys.common.mail.MailException;
import bg.infosys.common.ws.security.components.AuthService;
import bg.infosys.common.ws.security.dto.FBAuthDTO;
import bg.infosys.common.ws.security.dto.FBDebugTokenDTO;
import bg.infosys.common.ws.security.utils.FacebookUtil;
import bg.infosys.common.ws.security.utils.GoogleUtil;
import bg.infosys.crc.dao.mobile.security.ActiveUserDAO;
import bg.infosys.crc.dao.mobile.security.MobileUserDAO;
import bg.infosys.crc.dao.pub.SettingDAO;
import bg.infosys.crc.dao.pub.TranslationDAO;
import bg.infosys.crc.entities.mobile.security.ActiveUser;
import bg.infosys.crc.entities.mobile.security.MobileUser;
import bg.infosys.crc.entities.pub.LanguageEnum;
import bg.infosys.crc.entities.pub.OSEnum;
import bg.infosys.crc.entities.pub.Setting;
import bg.infosys.crc.entities.pub.SettingEnum;
import bg.infosys.crc.entities.pub.Translation.TranslationId;
import bg.infosys.crc.entities.pub.TranslationEnum;
import bg.infosys.crc.roaming.components.Properties;
import bg.infosys.crc.roaming.components.Scheduler;
import bg.infosys.crc.roaming.dto.mobile.MobileRequestDTO;
import bg.infosys.crc.roaming.dto.mobile.MobileResponseDTO;
import bg.infosys.crc.roaming.dto.web.users.EmailDTO;
import bg.infosys.crc.roaming.helpers.mobile.Helper;
import bg.infosys.crc.roaming.helpers.mobile.MobileErrorCodeEnum;
import bg.infosys.crc.roaming.workers.SendMailTask;

@Service
public class MobileAuthService {
	private static final Algorithm MAIL_TOKEN_ALG = Algorithm.HMAC512(Properties.get("security.mailToken.signKey"));

	@Autowired private ActiveUserDAO	activeUserDAO;
	@Autowired private AuthService		authService;
	@Autowired private MobileUserDAO	mobileUserDAO;
	@Autowired private TranslationDAO	translationDAO;
	@Autowired private Scheduler		scheduler;
	@Autowired private SettingDAO		settingDAO;
	
	@Transactional
	public MobileResponseDTO registerUserByEmail(@RequestParam MobileRequestDTO params) throws MailException {
		MobileResponseDTO resp = registerUser(params.getEm(), params.getPw(), Boolean.TRUE);
		if (resp != null) {
			return resp;
		}
		
		Email email = new Email();
		email.setBody(generateMailBody(false, params.getEm(), true));
		email.setRecipients(params.getEm());
		email.setSubject(translationDAO.findById(new TranslationId(TranslationEnum.MOB_MAIL_CONFIRM_SUBJ, LanguageEnum.BG)).getValue());
		scheduler.addTask(new SendMailTask(email));
		
		return MobileResponseDTO.ok();
	}
	
	@Transactional
	public String confirmEmail(String token) {
		JWTVerifier verifier = JWT.require(MAIL_TOKEN_ALG).build();
		try {
			DecodedJWT decoded = verifier.verify(token);
			String userId = decoded.getClaim(Properties.CLAIM_USER_ID).asString();
			
			MobileUser user = mobileUserDAO.findByEmail(userId);
			if (Boolean.TRUE.equals(user.getConfirmed())) {
				return Properties.WEB_URL_MAIL_ALRDY_CONF;
			}
			user.setConfirmed(true);
			mobileUserDAO.update(user);
			
			return Properties.WEB_URL_MAIL_CONFIRMED;
		} catch (JWTVerificationException ve) {
			return Properties.WEB_URL_EXPIRED_LINK;
		}
	}
	
	@Transactional
	public String confirmPassChange(String token) {
		JWTVerifier verifier = JWT.require(MAIL_TOKEN_ALG).build();
		try {
			DecodedJWT decoded = verifier.verify(token);
			String userId = decoded.getClaim(Properties.CLAIM_USER_ID).asString();
			String encodedPass = decoded.getClaim(Properties.CLAIM_USER_PASS).asString();
			
			MobileUser user = mobileUserDAO.findByEmail(userId);
			user.setPassword(encodedPass);
			user.setConfirmed(Boolean.TRUE);
			mobileUserDAO.update(user);
			
			return Properties.WEB_URL_PASS_CHANED;
		} catch (JWTVerificationException ve) {
			return Properties.WEB_URL_EXPIRED_LINK;
		}
	}

	@Transactional
	public MobileResponseDTO emailLogin(MobileRequestDTO params) {
		return login(params.getEm(), params.getPw(), params.getDevId(), params.getDeviceOS(), true);
	}

	@Transactional
	public MobileResponseDTO googleLogin(MobileRequestDTO params) {
		if (params.getT() == null || params.getDevId() == null) {
			return new MobileResponseDTO(MobileErrorCodeEnum.MISSING_REQUIRED_PARAM);
		}
		
		String appIds = Properties.get("security.google.appIds");
		Payload p = GoogleUtil.tokenWebVerify(params.getT());
		if (p == null || !p.getEmailVerified() || !appIds.contains(p.getAudience().toString())) {
			return new MobileResponseDTO(MobileErrorCodeEnum.UNAUTHORIZED);
		}
	
		return loginOrRegister(p.getEmail(), params.getDevId(), params.getDeviceOS());
	}
	
	@Transactional
	public MobileResponseDTO facebookLogin(MobileRequestDTO params) {
		if (params.getEm() == null || params.getFbUserId() == null || params.getT() == null || params.getDevId() == null) {
			return new MobileResponseDTO(MobileErrorCodeEnum.MISSING_REQUIRED_PARAM);
		}
		
		try {
			FBAuthDTO authDTO = FacebookUtil.getAccessToken(
					Properties.get("security.facebook.url"),
					Properties.get("security.facebook.appId"),
					Properties.get("security.facebook.appSecret"));
			if (authDTO == null || authDTO.getError() != null) {
				return new MobileResponseDTO(MobileErrorCodeEnum.UNAUTHORIZED);
			}
			
			FBDebugTokenDTO debugTokenDTO = FacebookUtil.getDebugData(Properties.get("security.facebook.debugUrl"),
					params.getT(), authDTO.getAccessToken());
			if (debugTokenDTO == null || debugTokenDTO.getError() != null || debugTokenDTO.getData() == null) {
				return new MobileResponseDTO(MobileErrorCodeEnum.UNAUTHORIZED);
			}
			
			return loginOrRegister(params.getEm(), params.getDevId(), params.getDeviceOS());
		} catch (Exception e) {
			return new MobileResponseDTO(MobileErrorCodeEnum.UNAUTHORIZED);
		}
	}
	
	@Transactional
	public MobileResponseDTO appleLogin(MobileRequestDTO params) {
		if (params.getT() == null || params.getDevId() == null) {
			return new MobileResponseDTO(MobileErrorCodeEnum.MISSING_REQUIRED_PARAM);
		}

		String email;
		try {
//			HttpRequest rq = new HttpRequest();
//			JWKS jwks = rq.doGet(Properties.APPLE_KEYS_URL, null, JWKS.class).getBody();
//			String kid = decoded.getKeyId(); // same as: getHeaderClaim("kid").asString();
//			JWKS.Key key = jwks.findByKid(kid);
			// FIXME: validate token
			
			DecodedJWT decoded = JWT.decode(params.getT());
			email = decoded.getClaim("email").asString();
		} catch (Exception e) {
			e.printStackTrace();
			return new MobileResponseDTO(MobileErrorCodeEnum.UNAUTHORIZED);
		}
		
		return loginOrRegister(email, params.getDevId(), "ios");
	}
	
	private MobileResponseDTO loginOrRegister(String email, String deviceId, String deviceOS) {
		MobileUser mu = mobileUserDAO.findByEmail(email);
		if (mu == null) {
//			FBUserDTO userDTO = FacebookUtil.getUser(
//					Properties.get("security.facebook.userUrl"),
//					debugTokenDTO.getData().getUserId(),
//					authDTO.getAccessToken());
//			
//			MobileResponseDTO resp = registerUser(userDTO.getName(), params.getEm(), null, null);
			MobileResponseDTO resp = registerUser(email, null, Boolean.FALSE);
			if (resp != null) {
				return resp;
			}
			mobileUserDAO.flushAndClear();
			mu = mobileUserDAO.findByEmail(email);
		}

		return login(mu, deviceId, deviceOS);
	}

	@Transactional
	public MobileResponseDTO logout(MobileRequestDTO params) {
		int rows = activeUserDAO.delete(params.getId(), params.getKey());
		if (rows == 1) {
			return MobileResponseDTO.ok();			
		}
		return new MobileResponseDTO(MobileErrorCodeEnum.FAILED_ACTION);
	}
	
	public MobileResponseDTO generateUserPassword(MobileRequestDTO params) throws MailException {
		MobileUser user = mobileUserDAO.findByEmail(params.getEm());
		if (user == null) {
			return MobileResponseDTO.ok();
		}
		
		
		Email email = new Email();
		email.setRecipients(user.getEmail());
		email.setSubject(translationDAO.findById(new TranslationId(TranslationEnum.MOB_MAIL_RESET_PASS_SUBJ, LanguageEnum.BG)).getValue());
		email.setBody(generateMailBody(true, user.getEmail(), Boolean.TRUE.equals(user.getPassLogin())));
		scheduler.addTask(new SendMailTask(email));
		
		return MobileResponseDTO.ok();
	}
	
	// Whiz Security Token :)
	public MobileResponseDTO validateLoggedUser(MobileRequestDTO params) {
		if (params.getId() == null || params.getKey() == null || "".equals(params.getKey())) {
			return new MobileResponseDTO(MobileErrorCodeEnum.MISSING_REQUIRED_PARAM);
		}
		
		ActiveUser example = new ActiveUser();
		example.setUserId(params.getId());
		example.setKey(params.getKey());
		long count = activeUserDAO.countByExample(example, false);
		
		if (count == 0) {
			return new MobileResponseDTO(MobileErrorCodeEnum.UNAUTHORIZED);
		}
		
		return null;
	}
	
	public void deleteMobileUser(EmailDTO dto) {
		MobileUser u = mobileUserDAO.findByEmail(dto.getEmail());
		if (u != null) {
			String token = JWT.create()
				.withExpiresAt(new Date(System.currentTimeMillis() + Properties.MAIL_VALIDITY_MINS * 60_000))
				.withClaim(Properties.CLAIM_USER_ID, dto.getEmail()).sign(MAIL_TOKEN_ALG);

			StringBuilder sb = new StringBuilder(Properties.PATH_CNF_DEL_USR_FULL)
				.append("?key=").append(token);
			
			String subj = translationDAO.findById(new TranslationId(TranslationEnum.WEB_MAIL_REQUEST_USER_DEL_SUBJ, LanguageEnum.BG)).getValue();
			String body = translationDAO.findById(new TranslationId(TranslationEnum.WEB_MAIL_REQUEST_USER_DEL_BODY, LanguageEnum.BG)).getValue();
			
			body = body.replace("{link}", sb.toString());
			
			Email email = new Email();
			email.setRecipients(dto.getEmail());
			email.setSubject(subj);
			email.setBody(body);
			scheduler.addTask(new SendMailTask(email));
		}
	}
	
	@Transactional
	public String confirmProfileDelete(String token) {
		JWTVerifier verifier = JWT.require(MAIL_TOKEN_ALG).build();
		try {
			DecodedJWT decoded = verifier.verify(token);
			String userId = decoded.getClaim(Properties.CLAIM_USER_ID).asString();
			
			MobileUser user = mobileUserDAO.findByEmail(userId);
			if (user != null) {
				String deletedText = "deleted-" + user.getId();
				user.setEmail(deletedText);
				user.setPassword(deletedText);
				user.setConfirmed(null);
				user.setPassLogin(null);
				user.setDeletedAt(LocalDateTime.now());
				mobileUserDAO.update(user);
				
				return Properties.WEB_URL_USR_DEL_OK;
			}
		} catch (JWTVerificationException ve) {
		}
		
		return Properties.WEB_URL_EXPIRED_LINK;
	}
	
	private MobileResponseDTO login(String email, String password, String deviceId, String deviceOS, boolean checkPasswd) {
		MobileUser user = mobileUserDAO.findByEmail(email);
		if (user == null) {
			return new MobileResponseDTO(MobileErrorCodeEnum.UNAUTHORIZED);
		}
		
		if (checkPasswd && !authService.passwordMatches(password, user.getPassword())) {
			return new MobileResponseDTO(MobileErrorCodeEnum.UNAUTHORIZED);
		}
		
		if (user == null || !Boolean.TRUE.equals(user.getConfirmed())) {
			return new MobileResponseDTO(MobileErrorCodeEnum.UNAUTHORIZED);
		}
		
		return login(user, deviceId, deviceOS);
	}
	
	private MobileResponseDTO login(MobileUser user, String deviceId, String deviceOS) {
		ActiveUser au = new ActiveUser();
		au.setUserId(user.getId());
		au.setLoggedAt(LocalDateTime.now());
		au.setKey(UUID.randomUUID().toString().replace("-", ""));
		au.setDeviceId(deviceId);
		au.setOmgRoaming(false);
		
		try {
			// FIXME: one day all devices should pass this parameter
			au.setDeviceOS(deviceOS == null || deviceOS.equals("") ? OSEnum.IOS : OSEnum.valueOf(deviceOS.toUpperCase()));
		} catch (Exception e) {
			au.setDeviceOS(null);
		}
		
		activeUserDAO.deleteById(user.getId());
		activeUserDAO.save(au);
		Setting telcosVn = settingDAO.findById(SettingEnum.TELCOS_VN);
		
		MobileResponseDTO resp = new MobileResponseDTO(MobileErrorCodeEnum.OK);
		resp.setId(user.getId());
		resp.setKey(au.getKey());
		resp.setTelcosVn(Short.parseShort(telcosVn.getValue()));
		
		return resp;
	}
	
	private String generateMailBody(boolean changePassword, String email, boolean passLogin) {
		TranslationEnum bodyKey = null;
		if (!changePassword) {
			bodyKey = TranslationEnum.MOB_MAIL_CONFIRM_BODY;
		} else if (passLogin) {
			bodyKey = TranslationEnum.MOB_MAIL_RESET_PASS_BODY;
		} else {
			bodyKey = TranslationEnum.MOB_MAIL_RESET_NOPASS_BODY;
		}
		
		String body = translationDAO.findById(new TranslationId(bodyKey, LanguageEnum.BG)).getValue();
		if (bodyKey.equals(TranslationEnum.MOB_MAIL_RESET_NOPASS_BODY)) {
			return body;
		} else {
			String link;
			if (changePassword) {
				String pass = Helper.generatePassword(10);
				link = generateMailLink(changePassword, email, pass);
				body = body.replace("{pass}", pass);
			} else {
				link = generateMailLink(changePassword, email, null);
			}
			return body.replace("{link}", link);
		}
	}
	
	private String generateMailLink(boolean newPass, String email, String pass) {
		Builder tokenBuilder = JWT.create()
			.withExpiresAt(new Date(System.currentTimeMillis() + Properties.MAIL_VALIDITY_MINS * 60_000))
			.withClaim(Properties.CLAIM_USER_ID, email);
		
		StringBuilder sb = new StringBuilder(Properties.API_URL);
		if (newPass) {
			sb.append(Properties.MOB_MAIL_PASS_LINK);
			tokenBuilder.withClaim(Properties.CLAIM_USER_PASS, authService.encodePassword(pass));
		} else {			
			sb.append(Properties.MOB_MAIL_CONF_LINK);
		}
		sb.append("?key=").append(tokenBuilder.sign(MAIL_TOKEN_ALG));
		
		return sb.toString();
	}
	
	private MobileResponseDTO registerUser(String email, String password, Boolean passLogin) {
		MobileUser user = new MobileUser();
		user.setEmail(email);

		boolean exists = mobileUserDAO.countByExample(user, false) > 0;
		if (exists) {
			return new MobileResponseDTO(MobileErrorCodeEnum.ALREADY_REGISTERED);
		}
		
		user.setCreatedAt(LocalDateTime.now());
		user.setPassLogin(passLogin);
		
		if (Boolean.TRUE.equals(passLogin)) {
			user.setPassword(authService.encodePassword(password));
		} else {
			user.setConfirmed(Boolean.TRUE);
		}
		
		mobileUserDAO.save(user);
		return null;
	}

	@Transactional
	public MobileResponseDTO changePasswd(MobileRequestDTO params) {
		MobileUser u = mobileUserDAO.findById(params.getId());
		if (u == null || Boolean.FALSE.equals(u.getPassLogin())) {
			return new MobileResponseDTO(MobileErrorCodeEnum.FORBIDDEN);
		}
		
		if (!authService.passwordMatches(params.getPw(), u.getPassword())) {
			return new MobileResponseDTO(MobileErrorCodeEnum.WRONG_PASSWORD);
		}
		
		u.setPassword(authService.encodePassword(params.getNewPw()));
		mobileUserDAO.update(u);
		
		return new MobileResponseDTO(MobileErrorCodeEnum.OK);
	}

}
