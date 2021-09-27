package bg.infosys.crc.roaming.dto.web.roles;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import bg.infosys.crc.entities.web.security.Authority;
import bg.infosys.crc.entities.web.security.Permission;
import bg.infosys.crc.roaming.dto.IEntityDTO;

public class AddEditRoleDTO implements IEntityDTO<Authority> {
	private String name;
	private String description;
	private Boolean toSingleUser;
	private Boolean enabled;
	private List<Integer> permissionIds;

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
	
	public void setToSingleUser(Boolean toSingleUser) {
		this.toSingleUser = toSingleUser;
	}
	
	public Boolean getEnabled() {
		return enabled;
	}
	
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public List<Integer> getPermissionIds() {
		return permissionIds;
	}
	
	public void setPermissionIds(List<Integer> permissionIds) {
		this.permissionIds = permissionIds;
	}

	@Override
	public Authority toEntity() {
		Authority a = new Authority();
		return merge(a);
	}
	
	@Override
	public Authority merge(Authority a) {
		a.setName(name);
		a.setDescription(description);
		a.setToSingleUser(toSingleUser);
		a.setEnabled(enabled);
		a.setPermissions(permissionIds == null 
			? new ArrayList<>() 
			: permissionIds.stream().map(pId -> {
				Permission entity = new Permission();
				entity.setId(pId);
				return entity;
			}).collect(Collectors.toList()));
		
		return a;
	}

}
