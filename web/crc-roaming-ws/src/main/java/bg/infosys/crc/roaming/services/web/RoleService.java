package bg.infosys.crc.roaming.services.web;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bg.infosys.common.exceptions.ResponseStatusException;
import bg.infosys.common.ws.security.SecuritySession;
import bg.infosys.crc.dao.web.security.AuthorityDAO;
import bg.infosys.crc.dao.web.security.PermissionDAO;
import bg.infosys.crc.dao.web.security.UserDAO;
import bg.infosys.crc.entities.web.security.Authority;
import bg.infosys.crc.roaming.dto.web.roles.AddEditRoleDTO;
import bg.infosys.crc.roaming.dto.web.roles.ListRoleDTO;
import bg.infosys.crc.roaming.dto.web.roles.PermissionDTO;

@Service
public class RoleService {
	@Autowired private AuthorityDAO		authorityDAO;
	@Autowired private PermissionDAO	permissionDAO;
	@Autowired private UserDAO			userDAO;

	public List<PermissionDTO> getAllPermissions() {
		return permissionDAO.findAllOrdered("id", true)
			.stream().map(p -> new PermissionDTO(p))
			.collect(Collectors.toList());
	}

	public List<ListRoleDTO> getAllRoles(boolean onlyValid) {
		return authorityDAO.findAllOrdered(onlyValid)
			.stream().map(a -> new ListRoleDTO(a))
			.collect(Collectors.toList());
	}
	
	public ListRoleDTO getRole(Integer id) {
		return new ListRoleDTO(authorityDAO.findById(id));
	}

	@Transactional
	public void add(AddEditRoleDTO role) {
		checkIfRoleNameExists(role.getName(), null);
		
		Authority a = role.toEntity();
		a.setCreatedBy(userDAO.findById(SecuritySession.getUserId()));
		a.setCreatedAt(LocalDateTime.now());
		
		authorityDAO.save(a);
	}

	@Transactional
	public void update(Integer id, AddEditRoleDTO role) {
		Authority a = getAndCheckIfRoleIdExists(id);
		checkIfRoleNameExists(role.getName(), id);
		if (Boolean.TRUE.equals(role.getToSingleUser()) && userDAO.countUsingAuhtority(id) > 1) {
			throw new ResponseStatusException(HttpServletResponse.SC_CONFLICT,
				"The role is assigned to more than one user", "roleAssignedToManyUsers");
		}
		
		if (Boolean.FALSE.equals(role.getEnabled()) && userDAO.countUsingAuhtority(id) > 0) {
			throw new ResponseStatusException(HttpServletResponse.SC_CONFLICT,
				"The role has already been assigned to the user", "roleHasAlreadyAssigned");
		}
		
		a.setUpdatedBy(userDAO.findById(SecuritySession.getUserId()));
		a.setUpdatedAt(LocalDateTime.now());
		
		authorityDAO.update(role.merge(a));
	}

	@Transactional
	public void deleteRole(Integer id) {
		Authority a = getAndCheckIfRoleIdExists(id);
		
		if (userDAO.countUsingAuhtority(id) > 0) {
			throw new ResponseStatusException(HttpServletResponse.SC_CONFLICT,
				"The role has already been assigned to the user", "roleHasAlreadyAssigned");
		}
		
		a.setDeletedBy(userDAO.findById(SecuritySession.getUserId()));
		a.setDeletedAt(LocalDateTime.now());
		
		authorityDAO.update(a);
	}
	
	private Authority getAndCheckIfRoleIdExists(Integer id) {
		Authority a = authorityDAO.findById(id);
		if (a == null) {
			throw new ResponseStatusException(HttpServletResponse.SC_GONE,
				"The role does not exist", "roleDoesNotExist");
		}
		
		return a;
	}
	
	private void checkIfRoleNameExists(String name, Integer id) {
		if (authorityDAO.findByName(name, id) != null) {
			throw new ResponseStatusException(HttpServletResponse.SC_CONFLICT,
				"Role with such name already exists", "roleNameExists");
		}
	}

}
