package bg.infosys.crc.roaming.components.controllers.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bg.infosys.common.ws.security.components.AuthService;
import bg.infosys.common.ws.security.dto.AuthRefreshTokenDTO;
import bg.infosys.common.ws.security.dto.AuthRequestDTO;
import bg.infosys.common.ws.security.dto.ChangePasswordDTO;
import bg.infosys.common.ws.security.dto.PasswordResetDTO;
import bg.infosys.crc.roaming.dto.web.users.EmailDTO;
import bg.infosys.crc.roaming.services.web.UserService;

@RestController
@RequestMapping(value = "/api/web/auth")
public class AuthController {
	@Autowired AuthService authService;
	@Autowired UserService userService;
	
	@PostMapping(value = "/o/login")
	public AuthRefreshTokenDTO login(@RequestBody AuthRequestDTO authRequest) {
		return authService.doLogin(authRequest);
	}
	
	@PostMapping(value = "/o/refresh")
	public AuthRefreshTokenDTO refresh(@RequestBody AuthRefreshTokenDTO refreshRequest) {
		return authService.doRefresh(refreshRequest);
	}
	
	@PostMapping(value = "/logout")
	public void logout() {
		authService.doLogout();
	}
	
	@PostMapping(value = "/change-password")
	public void changePassword(@RequestBody ChangePasswordDTO data) {
		authService.changePassword(data);
	}
	
	@PostMapping(value = "/o/request-password-reset")
	public void requestResetPassword(@RequestBody EmailDTO requestDTO) {
		userService.requestResetPassword(requestDTO);
	}
	
	@GetMapping(value = "/o/is-passwd-token-valid")
	public boolean isPasswdResetTokenValid(@RequestParam String token) {
		return authService.isPasswordResetTokenValid(token);
	}
	
	@PostMapping(value = "/o/reset-password")
	public void confirmResetPassword(@RequestBody PasswordResetDTO requestDTO) {
		authService.resetPassword(requestDTO);
	}

}
