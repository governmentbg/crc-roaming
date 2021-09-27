package bg.infosys.crc.entities.mobile.security;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import bg.infosys.crc.entities.pub.OSEnum;

@Entity
@Table(schema = "mobile_security", name = "active_users")
public class ActiveUser {

	@Id
	@Column(name = "user_id")
	private Integer userId;
	public static final String _userId = "userId";

	@Column(name = "logged_at")
	private LocalDateTime loggedAt;

	@Column(name = "key")
	private String key;
	public static final String _key = "key";

	@Column(name = "device_id")
	private String deviceId;

	@Column(name = "device_os")
	@Enumerated(EnumType.ORDINAL)
	private OSEnum deviceOS;

	@Column(name = "omg_roaming")
	private Boolean omgRoaming;

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public LocalDateTime getLoggedAt() {
		return loggedAt;
	}

	public void setLoggedAt(LocalDateTime loggedAt) {
		this.loggedAt = loggedAt;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public OSEnum getDeviceOS() {
		return deviceOS;
	}

	public void setDeviceOS(OSEnum deviceOS) {
		this.deviceOS = deviceOS;
	}

	public Boolean getOmgRoaming() {
		return omgRoaming;
	}

	public void setOmgRoaming(Boolean omgRoaming) {
		this.omgRoaming = omgRoaming;
	}

}
