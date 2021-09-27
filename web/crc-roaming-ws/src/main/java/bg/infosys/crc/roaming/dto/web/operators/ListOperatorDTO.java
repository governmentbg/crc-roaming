package bg.infosys.crc.roaming.dto.web.operators;

import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonInclude;

import bg.infosys.common.db.dao.GenericDAOImpl;
import bg.infosys.crc.entities.pub.Operator;
import bg.infosys.crc.roaming.dto.web.countries.ListCountryDTO;
import bg.infosys.crc.roaming.dto.web.users.ListUserDTO;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ListOperatorDTO {
	private Integer id;
	private ListCountryDTO country;
	private String name;
	private String mnc;
	private ListUserDTO createdBy;
	private ListUserDTO updatedBy;
	private String createdAt;
	private String updatedAt;

	public ListOperatorDTO(Operator t) {
		this.id = t.getId();
		this.country = GenericDAOImpl.isLoadedAndNotNull(t.getCountry()) ? new ListCountryDTO(t.getCountry(), true) : null;
		this.name = t.getName();
		this.mnc = t.getMnc();
		this.createdBy = GenericDAOImpl.isLoadedAndNotNull(t.getCreatedBy()) ? new ListUserDTO(t.getCreatedBy(), true) : null;
		this.createdAt = t.getCreatedAt() != null ? t.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME) : null;
		this.updatedBy = GenericDAOImpl.isLoadedAndNotNull(t.getUpdatedBy()) ? new ListUserDTO(t.getUpdatedBy(), true) : null;
		this.updatedAt = t.getUpdatedAt() != null ? t.getUpdatedAt().format(DateTimeFormatter.ISO_DATE_TIME) : null;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public ListCountryDTO getCountry() {
		return country;
	}

	public void setCountry(ListCountryDTO country) {
		this.country = country;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMnc() {
		return mnc;
	}

	public void setMnc(String mnc) {
		this.mnc = mnc;
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
