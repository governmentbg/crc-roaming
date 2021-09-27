package bg.infosys.crc.roaming.components.controllers.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bg.infosys.crc.roaming.dto.web.users.AddEditUserDTO;
import bg.infosys.crc.roaming.dto.web.users.ListUserDTO;
import bg.infosys.crc.roaming.helpers.web.PagingResultDTO;
import bg.infosys.crc.roaming.helpers.web.Permissions;
import bg.infosys.crc.roaming.services.web.UserService;

@RestController
@RequestMapping(value = "/api/web/users")
public class UserController {
	@Autowired private UserService userService;
	
	@Secured(Permissions.VIEW_USERS)
	@GetMapping(value = "/get-all")
	public PagingResultDTO<ListUserDTO> getAllUsers(@RequestParam Integer page, @RequestParam Integer size) {
		return userService.getAllUsers(page, size);
	}
	
	@Secured(Permissions.VIEW_USERS)
	@GetMapping(value = "/count-all")
	public long counUsers() {
		return userService.countUsers();
	}
	
	@Secured(Permissions.EDIT_USERS)
	@GetMapping(value = "/get/{id}")
	public ListUserDTO getUser(@PathVariable Integer id) {
		return userService.getUser(id);
	}
	
	@Secured(Permissions.EDIT_USERS)
	@PostMapping(value = "/add")
	public void addUser(@RequestBody AddEditUserDTO user) {
		userService.saveUser(user);
	}
	
	@Secured(Permissions.EDIT_USERS)
	@PostMapping("/edit/{id}")
	public void updateUser(@PathVariable Integer id, @RequestBody AddEditUserDTO user) {
		userService.updateUser(id, user);
	}
	
//	@Secured(Permissions.EDIT_USERS)
//	@PostMapping("/delete/{id}")
//	public void deleteUser(@PathVariable Integer id) {
//		userService.deleteUser(id);
//	}
	
}
