package bg.infosys.crc.roaming.dto.web.reports;

import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonInclude;

import bg.infosys.crc.entities.pub.ReportedRoaming;
import bg.infosys.crc.roaming.dto.web.operators.ListOperatorDTO;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportedRoamingDTO {
	private Integer roamingId;
	private Integer userId;
	private String eventTs;
	private Double latitude;
	private Double longitude;
	private ListOperatorDTO operator;
	private RegionDTO region;
	private String os;

	public ReportedRoamingDTO(ReportedRoaming r) {
		this.roamingId = r.getId();
		this.userId = r.getUser().getId();
		this.latitude = r.getLatitude();
		this.longitude = r.getLongitude();
		this.eventTs = r.getEventTs() == null ? null : r.getEventTs().format(DateTimeFormatter.ISO_DATE_TIME);
		this.operator = r.getOperator() == null ? null : new ListOperatorDTO(r.getOperator());
		this.region = r.getBgRegion() == null ? null : new RegionDTO(r.getBgRegion());
		this.os = r.getOs() == null ? null : r.getOs().toString().toLowerCase();
	}

	public Integer getRoamingId() {
		return roamingId;
	}

	public Integer getUserId() {
		return userId;
	}

	public String getEventTs() {
		return eventTs;
	}

	public Double getLatitude() {
		return latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public ListOperatorDTO getOperator() {
		return operator;
	}

	public RegionDTO getRegion() {
		return region;
	}

	public String getOs() {
		return os;
	}

}
