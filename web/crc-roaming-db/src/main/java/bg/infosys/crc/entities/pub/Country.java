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
@Table(schema = "public", name = "countries")
public class Country {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "name_int")
	private String nameInt;
	public static final String _nameInt = "nameInt";

	@Column(name = "name_bg")
	private String nameBg;
	public static final String _nameBg = "nameBg";

	@Column(name = "mcc")
	private Short mcc;
	public static final String _mcc = "mcc";

	@Column(name = "phone_code")
	private String phoneCode;
	public static final String _phoneCode = "phoneCode";

	@Column(name = "eu_member")
	private Boolean euMember;
	public static final String _euMember = "euMember";

	@Column(name = "created_at")
	private LocalDateTime createdAt;
	public static final String _createdAt = "createdAt";

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private User createdBy;
	public static final String _createdBy = "createdBy";

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	public static final String _updatedAt = "updatedAt";

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

	public String getNameInt() {
		return nameInt;
	}

	public void setNameInt(String nameInt) {
		this.nameInt = nameInt;
	}

	public String getNameBg() {
		return nameBg;
	}

	public void setNameBg(String nameBg) {
		this.nameBg = nameBg;
	}

	public Short getMcc() {
		return mcc;
	}

	public void setMcc(Short mcc) {
		this.mcc = mcc;
	}

	public String getPhoneCode() {
		return phoneCode;
	}

	public void setPhoneCode(String phoneCode) {
		this.phoneCode = phoneCode;
	}

	public Boolean getEuMember() {
		return euMember;
	}

	public void setEuMember(Boolean euMember) {
		this.euMember = euMember;
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

	@Override
	public int hashCode() {
		return Objects.hash(id, mcc, nameBg);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Country))
			return false;
		Country other = (Country) obj;
		return Objects.equals(id, other.id) && Objects.equals(mcc, other.mcc) && Objects.equals(nameBg, other.nameBg);
	}

}
