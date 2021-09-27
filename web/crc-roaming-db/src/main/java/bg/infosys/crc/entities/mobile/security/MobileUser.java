package bg.infosys.crc.entities.mobile.security;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(schema = "mobile_security", name = "users")
public class MobileUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "created_at")
	private LocalDateTime createdAt;
	public static String _createdAt = "createdAt";

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Column(name = "email")
	private String email;
	public static String _email = "email";

	@Column(name = "password")
	private String password;

	@Column(name = "confirmed")
	private Boolean confirmed;

	@Column(name = "pass_login")
	private Boolean passLogin;
	public static String _passLogin = "passLogin";

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getDeletedAt() {
		return deletedAt;
	}

	public void setDeletedAt(LocalDateTime deletedAt) {
		this.deletedAt = deletedAt;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Boolean getConfirmed() {
		return confirmed;
	}

	public void setConfirmed(Boolean confirmed) {
		this.confirmed = confirmed;
	}

	public Boolean getPassLogin() {
		return passLogin;
	}

	public void setPassLogin(Boolean passLogin) {
		this.passLogin = passLogin;
	}

}
