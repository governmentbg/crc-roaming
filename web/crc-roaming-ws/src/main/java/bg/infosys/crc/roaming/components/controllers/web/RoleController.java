package bg.infosys.crc.roaming.components.controllers.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bg.infosys.crc.roaming.dto.web.roles.AddEditRoleDTO;
import bg.infosys.crc.roaming.dto.web.roles.ListRoleDTO;
import bg.infosys.crc.roaming.dto.web.roles.PermissionDTO;
import bg.infosys.crc.roaming.helpers.web.Permissions;
import bg.infosys.crc.roaming.services.web.RoleService;

@RestController
@RequestMapping(value = "/api/web/roles")
public class RoleController {
	@Autowired private RoleService roleService;

	@Secured(Permissions.VIEW_ROLES)
	@GetMapping(value = "/permissions/get-all")
	public List<PermissionDTO> getAllPermissions() {
		return roleService.getAllPermissions();
	}

	@Secured({Permissions.VIEW_ROLES, Permissions.EDIT_USERS})
	@GetMapping(value = "/all")
	public List<ListRoleDTO> getAllRoles(@RequestParam(required = false) boolean onlyValid) {
		return roleService.getAllRoles(onlyValid);
	}
	
	@Secured(Permissions.EDIT_ROLES)
	@GetMapping(value = "/get/{id}")
	public ListRoleDTO getRole(@PathVariable Integer id) {
		return roleService.getRole(id);
	}
	
	@Secured(Permissions.EDIT_ROLES)
	@PostMapping(value = "/add")
	public void addRole(@RequestBody AddEditRoleDTO role) {
		roleService.add(role);
	}
	
	@Secured(Permissions.EDIT_ROLES)
	@PostMapping(value = "/edit/{id}")
	public void editRole(@PathVariable Integer id, @RequestBody AddEditRoleDTO role) {
		roleService.update(id, role);
	}
	
	@Secured(Permissions.EDIT_ROLES)
	@DeleteMapping(value = "/delete/{id}")
	public void deleteRole(@PathVariable Integer id) {
		roleService.deleteRole(id);
	}
	
}
