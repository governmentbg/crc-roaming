package bg.infosys.crc.entities.pub;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import bg.infosys.crc.entities.web.security.User;

@Entity
@Table(schema = "public", name = "notifications")
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "subject")
	private String subject;

	@Column(name = "body")
	private String body;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private User createdBy;
	public static final String _createdBy = "createdBy";

	@Column(name = "sent_at")
	private LocalDateTime sentAt;
	
	@Column(name = "sent")
	private Boolean sent;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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

	public LocalDateTime getSentAt() {
		return sentAt;
	}

	public void setSentAt(LocalDateTime sentAt) {
		this.sentAt = sentAt;
	}
	
	public Boolean getSent() {
		return sent;
	}
	
	public void setSent(Boolean sent) {
		this.sent = sent;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, subject, body, createdAt, sent);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Notification))
			return false;
		Notification other = (Notification) obj;
		return Objects.equals(id, other.id)
				&& Objects.equals(subject, other.subject)
				&& Objects.equals(body, other.body)
				&& Objects.equals(createdAt, other.createdAt)
				&& Objects.equals(sent, other.sent);
	}

}
