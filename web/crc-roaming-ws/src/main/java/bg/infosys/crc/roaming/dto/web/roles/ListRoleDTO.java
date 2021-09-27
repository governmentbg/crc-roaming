package bg.infosys.crc.roaming.dto.web.roles;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;

import bg.infosys.crc.entities.web.security.Authority;
import bg.infosys.crc.roaming.dto.web.users.ListUserDTO;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ListRoleDTO {
	private Integer id;
	private String name;
	private String description;
	private Boolean toSingleUser;
	private Boolean enabled;
	private List<PermissionDTO> permissions;
	private ListUserDTO createdBy;
	private ListUserDTO updatedBy;
	private String createdAt;
	private String updatedAt;

	public ListRoleDTO(Authority a) {
		this.id = a.getId();
		this.name = a.getName();
		this.description = a.getDescription();
		this.toSingleUser = a.getToSingleUser();
		this.enabled = a.getEnabled();
		this.permissions = a.getPermissions().stream().map(p -> new PermissionDTO(p)).collect(Collectors.toList());
		this.createdBy = new ListUserDTO(a.getCreatedBy(), true);
		this.createdAt = a.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME);
		this.updatedBy = a.getUpdatedBy() == null ? null : new ListUserDTO(a.getUpdatedBy(), true);
		this.updatedAt = a.getUpdatedAt() == null ? null : a.getUpdatedAt().format(DateTimeFormatter.ISO_DATE_TIME);
		this.enabled = a.getEnabled();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public Boolean getToSingleUser() {
		return toSingleUser;
	}
	
	public void setToSingleUser(boolean toSingleUser) {
		this.toSingleUser = toSingleUser;
	}
	
	public Boolean getEnabled() {
		return enabled;
	}
	
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	
	public List<PermissionDTO> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<PermissionDTO> permissions) {
		this.permissions = permissions;
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
