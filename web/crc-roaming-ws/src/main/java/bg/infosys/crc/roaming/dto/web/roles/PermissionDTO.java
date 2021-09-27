package bg.infosys.crc.roaming.dto.web.roles;

import bg.infosys.crc.entities.web.security.Permission;

public class PermissionDTO {
	private Integer id;
	private String name;
	private String description;
	private Integer unavailableUnless;

	public PermissionDTO() {
	}

	public PermissionDTO(Permission p) {
		this.id = p.getId();
		this.name = p.getName();
		this.description = p.getDescription();
		if (p.getUnavailableUnless() != null) {
			this.unavailableUnless = p.getUnavailableUnless().getId();
		}
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

	public Integer getUnavailableUnless() {
		return unavailableUnless;
	}

	public void setUnavailableUnless(Integer unavailableUnless) {
		this.unavailableUnless = unavailableUnless;
	}

}
