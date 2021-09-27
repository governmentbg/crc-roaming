package bg.infosys.crc.entities.pub;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(schema = "public", name = "ip_data")
public class IpData {

	@Id
	private String ip;

	@Column(name = "country")
	private String country;

	@Column(name = "lookup_response")
	private String lookupResponse;

	@Column(name = "lookup_ts")
	private LocalDateTime lookupTs;

	@Column(name = "user_id")
	private Integer userId;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getLookupResponse() {
		return lookupResponse;
	}

	public void setLookupResponse(String lookupResponse) {
		this.lookupResponse = lookupResponse;
	}

	public LocalDateTime getLookupTs() {
		return lookupTs;
	}

	public void setLookupTs(LocalDateTime lookupTs) {
		this.lookupTs = lookupTs;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

}
