package bg.infosys.crc.roaming.dto.web.reports;

import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonInclude;

import bg.infosys.crc.entities.pub.ReportedBlocking;
import bg.infosys.crc.roaming.dto.web.operators.ListOperatorDTO;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportedBlockingDTO {
	private Integer blockingId;
	private Integer userId;
	private String eventTs;
	private ListOperatorDTO operator;

	public ReportedBlockingDTO(ReportedBlocking r) {
		this.blockingId = r.getId();
		this.userId = r.getUser().getId();
		this.eventTs = r.getEventTs() == null ? null : r.getEventTs().format(DateTimeFormatter.ISO_DATE_TIME);
		this.operator = r.getOperator() == null ? null : new ListOperatorDTO(r.getOperator());
	}

	public Integer getBlockingId() {
		return blockingId;
	}

	public void setBlockingId(Integer blockingId) {
		this.blockingId = blockingId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getEventTs() {
		return eventTs;
	}

	public void setEventTs(String eventTs) {
		this.eventTs = eventTs;
	}

	public ListOperatorDTO getOperator() {
		return operator;
	}

	public void setOperator(ListOperatorDTO operator) {
		this.operator = operator;
	}

}
