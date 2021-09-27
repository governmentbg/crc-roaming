package bg.infosys.crc.roaming.dto.mobile;

import java.util.ArrayList;
import java.util.List;

import bg.infosys.crc.entities.pub.Country;

public class CountryDTO implements Comparable<CountryDTO> {
	private String mcc;
	private String dname;
	private String bgName;
	private String phoneCode;
	private Short isEU;
	private List<OperatorDTO> telcos;
	
	public CountryDTO(Country c, List<OperatorDTO> telcoms) {
		this.mcc	= c.getMcc().toString();
		this.dname	= c.getNameInt();
		this.bgName	= c.getNameBg();
		this.phoneCode	= c.getPhoneCode();
		this.isEU	= (short) (c.getEuMember() ? 1 : 0);
		this.telcos	= telcoms;
	}

	public String getMcc() {
		return mcc;
	}

	public String getDname() {
		return dname;
	}
	
	public String getBgName() {
		return bgName;
	}
	
	public String getPhoneCode() {
		return phoneCode;
	}

	public Short getIsEU() {
		return isEU;
	}

	public List<OperatorDTO> getTelcos() {
		return telcos;
	}

	public void addTelecom(OperatorDTO telecom) {
		if (this.telcos == null) {
			this.telcos = new ArrayList<>();
		}
		this.telcos.add(telecom);
	}
	
	@Override
	public int compareTo(CountryDTO o) {
		return this.dname.compareToIgnoreCase(o.dname);
	}

}
