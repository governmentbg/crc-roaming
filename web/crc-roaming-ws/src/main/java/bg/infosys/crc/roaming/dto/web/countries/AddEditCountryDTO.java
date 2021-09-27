package bg.infosys.crc.roaming.dto.web.countries;

import bg.infosys.crc.entities.pub.Country;
import bg.infosys.crc.roaming.dto.IEntityDTO;

public class AddEditCountryDTO implements IEntityDTO<Country> {
	private String nameBg;
	private String nameInt;
	private Short mcc;
	private String phoneCode;
	private Boolean euMember;

	public String getNameBg() {
		return nameBg;
	}

	public void setNameBg(String nameBg) {
		this.nameBg = nameBg;
	}

	public String getNameInt() {
		return nameInt;
	}

	public void setNameInt(String nameInt) {
		this.nameInt = nameInt;
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

	@Override
	public Country toEntity() {
		Country c = new Country();
		return merge(c);
	}

	@Override
	public Country merge(Country c) {
		c.setNameBg(nameBg);
		c.setNameInt(nameInt);
		c.setMcc(mcc);
		c.setPhoneCode(phoneCode);
		c.setEuMember(euMember);

		return c;
	}
}
