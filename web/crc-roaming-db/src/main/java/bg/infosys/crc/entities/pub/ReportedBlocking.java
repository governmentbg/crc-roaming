package bg.infosys.crc.entities.pub;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import bg.infosys.crc.entities.mobile.security.MobileUser;

@Entity
@Table(schema = "public", name = "reported_blockings")
public class ReportedBlocking {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	public static final String _id = "id";

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private MobileUser user;
	public static final String _user = "user";

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "operator_id")
	private Operator operator;
	public static final String _operator = "operator";

	@Column(name = "event_ts")
	private LocalDateTime eventTs;
	public static final String _eventTs = "eventTs";

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public MobileUser getUser() {
		return user;
	}

	public void setUser(MobileUser user) {
		this.user = user;
	}

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	public LocalDateTime getEventTs() {
		return eventTs;
	}

	public void setEventTs(LocalDateTime eventTs) {
		this.eventTs = eventTs;
	}

}
