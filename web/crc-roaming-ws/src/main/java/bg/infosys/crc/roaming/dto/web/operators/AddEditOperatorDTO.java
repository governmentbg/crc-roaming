package bg.infosys.crc.roaming.dto.web.operators;

import bg.infosys.crc.entities.pub.Country;
import bg.infosys.crc.entities.pub.Operator;
import bg.infosys.crc.roaming.dto.IEntityDTO;

public class AddEditOperatorDTO implements IEntityDTO<Operator> {
	private Integer countryId;
	private String name;
	private String mnc;

	public Integer getCountryId() {
		return countryId;
	}
	
	public void setCountryId(Integer countryId) {
		this.countryId = countryId;
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

	@Override
	public Operator toEntity() {
		Operator t = new Operator();
		return merge(t);
	}

	@Override
	public Operator merge(Operator o) {
		Country c = new Country();
		c.setId(countryId);
		
		o.setCountry(c);
		o.setName(name);
		o.setMnc(mnc);
		
		return o;
	}
}
