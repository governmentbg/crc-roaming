package bg.infosys.crc.roaming.dto.web.users;

import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonInclude;

import bg.infosys.common.db.dao.GenericDAOImpl;
import bg.infosys.crc.entities.web.security.User;
import bg.infosys.crc.roaming.dto.web.roles.ListRoleDTO;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ListUserDTO {
	private Integer id;
	private String fullName;
	private String email;
	private Boolean enabled;
	private ListRoleDTO role;
	private ListUserDTO createdBy;
	private ListUserDTO updatedBy;
	private String createdAt;
	private String updatedAt;

	public ListUserDTO(User u, boolean simple) {
		this.id = u.getId();
		this.fullName = u.getFullName();
		this.email = u.getUsername();
		
		if (!simple) {			
			this.createdBy = GenericDAOImpl.isLoadedAndNotNull(u.getCreatedBy()) ? new ListUserDTO(u.getCreatedBy(), true) : null;
			this.createdAt = u.getCreatedAt() != null ? u.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME) : null;
			this.updatedBy = GenericDAOImpl.isLoadedAndNotNull(u.getUpdatedBy()) ? new ListUserDTO(u.getUpdatedBy(), true) : null;
			this.updatedAt = u.getUpdatedAt() != null ? u.getUpdatedAt().format(DateTimeFormatter.ISO_DATE_TIME) : null;
			this.enabled = u.getEnabled();
			if (u.getAuthorities() != null && u.getAuthorities().size() > 0) {
				this.role = new ListRoleDTO(u.getGrantedAuthorities().get(0));
			}
		}
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Boolean getEnabled() {
		return enabled;
	}
	
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	
	public ListRoleDTO getRole() {
		return role;
	}
	
	public void setRole(ListRoleDTO role) {
		this.role = role;
	}

	public ListUserDTO getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(ListUserDTO createdBy) {
		this.createdBy = createdBy;
	}

	public ListUserDTO getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(ListUserDTO updatedBy) {
		this.updatedBy = updatedBy;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}

}
