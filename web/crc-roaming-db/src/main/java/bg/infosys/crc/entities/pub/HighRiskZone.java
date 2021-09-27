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

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import bg.infosys.common.db.entity.types.DoubleArrayType;
import bg.infosys.crc.entities.web.security.User;

@Entity
@Table(schema = "public", name = "high_risk_zones")
@TypeDefs({ @TypeDef(name = "double-array", typeClass = DoubleArrayType.class) })
public class HighRiskZone {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	public static final String _id = "id";

	@Column(name = "name")
	private String name;

	@Column(name = "shape_type")
	@Enumerated(EnumType.ORDINAL)
	private GeometryTypeEnum type;

	@Column(name = "coordinates", columnDefinition = "double precision[]")
	@Type(type = "double-array")
	private Double[][] coordinates;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private User createdBy;
	public static final String _createdBy = "createdBy";

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "updated_by")
	private User updatedBy;
	public static final String _updatedBy = "updatedBy";

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "deleted_by")
	private User deletedBy;
	public static final String _deletedBy = "deletedBy";

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public GeometryTypeEnum getType() {
		return type;
	}

	public void setType(GeometryTypeEnum type) {
		this.type = type;
	}

	public Double[][] getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(Double[][] coordinates) {
		this.coordinates = coordinates;
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

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public User getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(User updatedBy) {
		this.updatedBy = updatedBy;
	}

	public LocalDateTime getDeletedAt() {
		return deletedAt;
	}

	public void setDeletedAt(LocalDateTime deletedAt) {
		this.deletedAt = deletedAt;
	}

	public User getDeletedBy() {
		return deletedBy;
	}

	public void setDeletedBy(User deletedBy) {
		this.deletedBy = deletedBy;
	}

}
