package bg.infosys.crc.roaming.dto.web.countries;

import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonInclude;

import bg.infosys.crc.entities.pub.Country;
import bg.infosys.crc.roaming.dto.web.users.ListUserDTO;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ListCountryDTO {
	private Integer id;
	private String nameInt;
	private String nameBg;
	private Short mcc;
	private String phoneCode;
	private Boolean euMember;
	private ListUserDTO createdBy;
	private ListUserDTO updatedBy;
	private String createdAt;
	private String updatedAt;

	public ListCountryDTO(Country c, boolean simple) {
		this.id = c.getId();
		this.nameBg = c.getNameBg();
		this.nameInt = c.getNameInt();
		this.phoneCode = c.getPhoneCode();
		this.mcc = c.getMcc();
		this.euMember = c.getEuMember();
		
		if (!simple) {
			this.createdBy = new ListUserDTO(c.getCreatedBy(), true);
			this.createdAt = c.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME);
			this.updatedBy = c.getUpdatedBy() == null ? null : new ListUserDTO(c.getUpdatedBy(), true);
			this.updatedAt = c.getUpdatedAt() == null ? null : c.getUpdatedAt().format(DateTimeFormatter.ISO_DATE_TIME);
		}
	}

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

	public ListUserDTO getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(ListUserDTO createdBy) {
		this.createdBy = createdBy;
	}

	public ListUserDTO getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(ListUserDTO updatedBy) {
		this.updatedBy = updatedBy;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}

}
