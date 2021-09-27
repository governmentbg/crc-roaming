package bg.infosys.crc.roaming.services.web;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bg.infosys.common.db.helpers.ResultFilter;
import bg.infosys.common.exceptions.ResponseStatusException;
import bg.infosys.common.mail.Email;
import bg.infosys.common.ws.security.SecuritySession;
import bg.infosys.common.ws.security.components.AuthService;
import bg.infosys.crc.dao.pub.TranslationDAO;
import bg.infosys.crc.dao.web.security.AuthorityDAO;
import bg.infosys.crc.dao.web.security.UserDAO;
import bg.infosys.crc.entities.pub.LanguageEnum;
import bg.infosys.crc.entities.pub.Translation.TranslationId;
import bg.infosys.crc.entities.pub.TranslationEnum;
import bg.infosys.crc.entities.web.security.Authority;
import bg.infosys.crc.entities.web.security.User;
import bg.infosys.crc.roaming.components.Properties;
import bg.infosys.crc.roaming.components.Scheduler;
import bg.infosys.crc.roaming.dto.web.users.AddEditUserDTO;
import bg.infosys.crc.roaming.dto.web.users.EmailDTO;
import bg.infosys.crc.roaming.dto.web.users.ListUserDTO;
import bg.infosys.crc.roaming.helpers.mobile.Helper;
import bg.infosys.crc.roaming.helpers.web.PagingResultDTO;
import bg.infosys.crc.roaming.workers.SendMailTask;

@Service
public class UserService {
	@Autowired private Scheduler		scheduler;
	@Autowired private AuthService		authService;
	@Autowired private TranslationDAO	translationDAO;
	@Autowired private UserDAO			userDAO;
	@Autowired private AuthorityDAO		authorityDAO;
	
	public void requestResetPassword(EmailDTO requestDTO) {
		User u = userDAO.findByUsername(requestDTO.getEmail());
		if (u == null) return;
		
		String token	= authService.generatePasswordResetToken(u, Properties.MAIL_VALIDITY_MINS);
		String link		= Properties.get("webapp.urlResetPass") + token;
		String subject	= translationDAO.findById(new TranslationId(TranslationEnum.WEB_RESET_PASS_SUBJ, LanguageEnum.BG)).getValue();
		String bodyRaw	= translationDAO.findById(new TranslationId(TranslationEnum.WEB_RESET_PASS_BODY, LanguageEnum.BG)).getValue();
		String body  	= bodyRaw.replace("{name}", u.getFullName()).replace("{link}", link);
		
		Email email = new Email(u.getUsername(), subject, body);
		scheduler.addTask(new SendMailTask(email));
	}
	
	public PagingResultDTO<ListUserDTO> getAllUsers(Integer page, Integer pageSize) {
		List<ListUserDTO> users = userDAO.findAllPaged(ResultFilter.firstResult(page, pageSize), pageSize)
				.stream().map(u -> new ListUserDTO(u, false)).collect(Collectors.toList());
		return new PagingResultDTO<>(users, page, pageSize);
	}
	
	public long countUsers() {
		return userDAO.count() - 1; // -- subtract the default system user
	}

	public ListUserDTO getUser(Integer userId) {
		return new ListUserDTO(userDAO.findUserById(userId), false);
	}

	@Transactional
	public void saveUser(AddEditUserDTO user) {
		checkIfUsernameExists(user.getEmail(), null);
		
		User u = user.toEntity();
		String rawPass = Helper.generatePassword(10);
		String encodedPass = authService.encodePassword(rawPass);
		u.setPassword(encodedPass);
		
		String subj = translationDAO.findById(new TranslationId(TranslationEnum.WEB_MAIL_NEW_USER_SUBJ, LanguageEnum.BG)).getValue();
		String body = translationDAO.findById(new TranslationId(TranslationEnum.WEB_MAIL_NEW_USER_BODY, LanguageEnum.BG)).getValue();
		body = body
			.replace("{name}", u.getFullName())
			.replace("{link}", Properties.get("webapp.url"))
			.replace("{pass}", rawPass);
		
		if (user.getRoleId() != null) {
			Authority a = authorityDAO.findById(user.getRoleId());
			checkAuthorityForSingleUser(a, null);
			u.getGrantedAuthorities().add(a);
		}
		u.setCreatedBy(userDAO.findById(SecuritySession.getUserId()));
		u.setCreatedAt(LocalDateTime.now());
		userDAO.save(u);
		userDAO.flush();
		
		Email email = new Email(u.getUsername(), subj, body);
		scheduler.addTask(new SendMailTask(email));
	}

	@Transactional
	public void updateUser(Integer userId, AddEditUserDTO user) {
		User u = getAndCheckIfUserExists(userId);
		checkIfUsernameExists(user.getEmail(), userId);
		
		user.merge(u);
		Authority a = authorityDAO.findById(user.getRoleId());
		checkAuthorityForSingleUser(a, userId);
		
		u.setUpdatedBy(userDAO.findById(SecuritySession.getUserId()));
		u.setUpdatedAt(LocalDateTime.now());
		u.getGrantedAuthorities().add(a);
		
		userDAO.update(u);
	}
	
	@Transactional
	public void deleteUser(Integer userId) {
		User u = getAndCheckIfUserExists(userId);
		
		userDAO.delete(u);
	}
	
	private User getAndCheckIfUserExists(Integer id) {
		User u = userDAO.findUserById(id);
		if (u == null) {
			throw new ResponseStatusException(HttpServletResponse.SC_GONE,
				"The user does not exist", "userDoesNotExist");
		}
		
		return u;
	}
	
	private void checkIfUsernameExists(String username, Integer userId) {
		if (userDAO.findByUsername(username, userId) != null) {
			throw new ResponseStatusException(HttpServletResponse.SC_CONFLICT,
				"Email already exists", "userEmailAlreadyExists");
		}
	}
	
	private void checkAuthorityForSingleUser(Authority a, Integer userId) {
		if (Boolean.TRUE.equals(a.getToSingleUser()) && userDAO.countUsingAuhtority(a.getId()) > 0) {
			throw new ResponseStatusException(HttpServletResponse.SC_CONFLICT,
				"The role can be assigned to a single user and it's already assigned.",
				"singleUserRoleAlreadyAssigned");
		}
	}

}
