package bg.infosys.crc.entities.pub;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import bg.infosys.crc.entities.mobile.security.MobileUser;

@Entity
@Table(schema = "public", name = "reported_roamings")
public class ReportedRoaming {

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

	@Column(name = "latitude")
	private Double latitude;

	@Column(name = "longitude")
	private Double longitude;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bg_region_id")
	private BgRegion bgRegion;
	public static final String _bgRegion = "bgRegion";

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "source_operator_id")
	private SourceOperator sourceOperator;
	public static final String _sourceOperator = "sourceOperator";

	@Column(name = "real_detection")
	private Boolean realDetection;
	public static final String _realDetection = "realDetection";
	
	@Column(name = "hidden")
	private Boolean hidden;
	public static final String _hidden = "hidden";
	
	@Column(name = "os")
	@Enumerated(EnumType.ORDINAL)
	private OSEnum os;

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

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public BgRegion getBgRegion() {
		return bgRegion;
	}

	public void setBgRegion(BgRegion bgRegion) {
		this.bgRegion = bgRegion;
	}

	public SourceOperator getSourceOperator() {
		return sourceOperator;
	}

	public void setSourceOperator(SourceOperator sourceOperator) {
		this.sourceOperator = sourceOperator;
	}

	public Boolean getRealDetection() {
		return realDetection;
	}

	public void setRealDetection(Boolean realDetection) {
		this.realDetection = realDetection;
	}

	public Boolean getHidden() {
		return hidden;
	}

	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}
	
	public OSEnum getOs() {
		return os;
	}
	
	public void setOs(OSEnum os) {
		this.os = os;
	}

}
