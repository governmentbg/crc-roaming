package bg.infosys.crc.roaming.dto.mobile;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import bg.infosys.crc.roaming.helpers.mobile.MobileErrorCodeEnum;

public class MobileResponseDTO {
	private Integer ec;
	private Integer id;
	private String key;
	@JsonProperty("telcos_vn")
	private Short telcosVn;
	private List<CountryDTO> countries;
	private List<ZoneDTO> zones;
	private List<ReportedZoneDTO> z;
	@JsonProperty("reported_roamings")
	private Set<ReportedRoamingDTO> reportedRoamings;
	@JsonProperty("roaming_radius")
	private Integer roamingRadius;
	private String result;

	public MobileResponseDTO() {
	}

	public MobileResponseDTO(MobileErrorCodeEnum error) {
		this.ec = error.getCode();
	}

	public static MobileResponseDTO ok() {
		return new MobileResponseDTO(MobileErrorCodeEnum.OK);
	}

	public int getEc() {
		return ec;
	}

	public void setError(MobileErrorCodeEnum error) {
		this.ec = error.getCode();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Short getTelcosVn() {
		return telcosVn;
	}

	public void setTelcosVn(Short telcosVn) {
		this.telcosVn = telcosVn;
	}

	public List<CountryDTO> getCountries() {
		return countries;
	}

	public void setCountries(List<CountryDTO> countries) {
		this.countries = countries;
	}

	public List<ZoneDTO> getZones() {
		return zones;
	}

	public void setZones(List<ZoneDTO> zones) {
		this.zones = zones;
	}

	public List<ReportedZoneDTO> getZ() {
		return z;
	}

	public void setZ(List<ReportedZoneDTO> z) {
		this.z = z;
	}

	public Set<ReportedRoamingDTO> getReportedRoamings() {
		return reportedRoamings;
	}

	public void setReportedRoamings(Set<ReportedRoamingDTO> reportedRoamings) {
		this.reportedRoamings = reportedRoamings;
	}
	
	public Integer getRoamingRadius() {
		return roamingRadius;
	}
	
	public void setRoamingRadius(Integer roamingRadius) {
		this.roamingRadius = roamingRadius;
	}
	
	public String getResult() {
		return result;
	}
	
	public void setResult(String result) {
		this.result = result;
	}

}
