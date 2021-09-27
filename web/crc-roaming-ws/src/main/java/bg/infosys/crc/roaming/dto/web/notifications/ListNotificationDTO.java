package bg.infosys.crc.roaming.dto.web.notifications;

import java.time.format.DateTimeFormatter;

import bg.infosys.crc.entities.pub.Notification;
import bg.infosys.crc.roaming.dto.web.users.ListUserDTO;

public class ListNotificationDTO {
	private Integer id;
	private String createdAt;
	private String sentAt;
	private Boolean sent;
	private String subject;
	private String body;
	private ListUserDTO createdBy;
	
	public ListNotificationDTO(Notification n) {
		this.id			= n.getId();
		this.createdAt	= n.getCreatedAt().format((DateTimeFormatter.ISO_DATE_TIME));
		this.sentAt		= n.getSentAt() == null ? null : n.getSentAt().format((DateTimeFormatter.ISO_DATE_TIME));
		this.sent		= n.getSent();
		this.subject	= n.getSubject();
		this.body		= n.getBody();
		this.createdBy	= new ListUserDTO(n.getCreatedBy(), true);
	}
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getSentAt() {
		return sentAt;
	}

	public void setSentAt(String sentAt) {
		this.sentAt = sentAt;
	}
	
	public Boolean getSent() {
		return sent;
	}
	
	public void setSent(Boolean sent) {
		this.sent = sent;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public ListUserDTO getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(ListUserDTO createdBy) {
		this.createdBy = createdBy;
	}
	
}
