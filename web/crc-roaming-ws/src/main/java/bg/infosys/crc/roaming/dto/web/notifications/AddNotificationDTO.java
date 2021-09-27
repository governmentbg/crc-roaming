package bg.infosys.crc.roaming.dto.web.notifications;

import bg.infosys.crc.entities.pub.Notification;
import bg.infosys.crc.roaming.dto.IEntityDTO;

public class AddNotificationDTO implements IEntityDTO<Notification> {
	private String subject;
	private String body;

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

	@Override
	public Notification toEntity() {
		Notification n = new Notification();
		return merge(n);
	}
	
	@Override
	public Notification merge(Notification n) {
		n.setBody(body);
		n.setSubject(subject);
		
		return n;
	}

}
