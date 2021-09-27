package bg.infosys.crc.entities.web.security;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.AssociationOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import bg.infosys.common.ws.security.annotations.TokenClaim;
import bg.infosys.common.ws.security.db.entities.AbstractUser;

@Entity
@Table(name = "users", schema = "web_security")
@AssociationOverride(name = "grantedAuthorities", joinTable =
	@JoinTable(name="users_authorities", schema="web_security",
		joinColumns = @JoinColumn(name = "user_id"),
		inverseJoinColumns = @JoinColumn(name = "authority_id")))
public class User extends AbstractUser<User, Authority> {
	private static final long serialVersionUID = 1L;
	
	@Column(name = "full_name")
	private String fullName;
	public static final String _fullName = "fullName";
	
	@Column(name = "created_at")
	private LocalDateTime createdAt;
	public static final String _createdAt = "createdAt";

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private User createdBy;
	public static final String _createdBy = "createdBy";

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	public static final String _updatedAt = "updatedAt";

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "updated_by")
	private User updatedBy;
	public static final String _updatedBy = "updatedBy";
	
	public String getFullName() {
		return fullName;
	}
	
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public User getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(User updatedBy) {
		this.updatedBy = updatedBy;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<SimpleGrantedAuthority> gaList = new ArrayList<>();
		Authority a = getGrantedAuthorities().get(0);
		for (Permission p : a.getPermissions()) {
			gaList.add(new SimpleGrantedAuthority(p.getName()));
		}
		
		return gaList;
	}
	
	@TokenClaim
	public Map<String, String> toTokenClaims() {
		Map<String, String> claims = new HashMap<>();
		claims.put("lau_fullName", getFullName());
		claims.put("lau_role", getGrantedAuthorities().get(0).getName());
		
		return claims;
	}

}