package bg.infosys.crc.roaming.dto.web.users;

import java.util.ArrayList;

import bg.infosys.crc.entities.web.security.User;
import bg.infosys.crc.roaming.dto.IEntityDTO;

public class AddEditUserDTO implements IEntityDTO<User> {
	private String fullName;
	private String email;
	private Boolean enabled;
	private String password;
	private Integer roleId;

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
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public Integer getRoleId() {
		return roleId;
	}
	
	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}
	
	@Override
	public User toEntity() {
		User u = new User();
		return merge(u);
	}
	
	@Override
	public User merge(User u) {
		u.setFullName(fullName);
		u.setUsername(email);
		u.setEnabled(enabled);
		u.setGrantedAuthorities(new ArrayList<>());
		
		return u;
	}

}
