package bg.infosys.crc.entities.web.security;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import bg.infosys.common.ws.security.db.entities.AbstractAuthority;

@Entity
@Table(name = "authorities", schema = "web_security")
public class Authority extends AbstractAuthority {
	private static final long serialVersionUID = -7345022558378790390L;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "authorities_permissions", schema = "web_security",
		joinColumns = @JoinColumn(name = "authority_id"),
		inverseJoinColumns = @JoinColumn(name = "permission_id"))
	private List<Permission> permissions;
	public static final String _permissions = "permissions";
	
	@Column(name = "to_single_user")
	private Boolean toSingleUser;
	
	@Column(name = "enabled")
	private Boolean enabled;
	public static final String _enabled = "enabled";

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private User createdBy;
	public static final String _createdBy = "createdBy";

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "updated_by")
	private User updatedBy;
	public static final String _updatedBy = "updatedBy";

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "deleted_by")
	private User deletedBy;
	public static final String _deletedBy = "deletedBy";

	public List<Permission> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<Permission> permissions) {
		this.permissions = permissions;
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

	public LocalDateTime getDeletedAt() {
		return deletedAt;
	}

	public void setDeletedAt(LocalDateTime deletedAt) {
		this.deletedAt = deletedAt;
	}

	public User getDeletedBy() {
		return deletedBy;
	}

	public void setDeletedBy(User deletedBy) {
		this.deletedBy = deletedBy;
	}

}
