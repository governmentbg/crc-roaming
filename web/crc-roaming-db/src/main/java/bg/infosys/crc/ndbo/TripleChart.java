package bg.infosys.crc.ndbo;

import bg.infosys.crc.entities.pub.BgRegion;
import bg.infosys.crc.entities.pub.Country;
import bg.infosys.crc.entities.pub.Operator;

public class TripleChart {
	private Operator operator;
	private Country country;
	private BgRegion region;
	private Long count;

	public TripleChart(
			Integer opId, String opName, String opMNC,	// Operator fields
			Integer cId, Short cMCC, String cNameBg,	// Country fields
			Integer rId, String rName,					// Region fields
			Long count) {
		
		this(opId, opName, opMNC, cId, cMCC, cNameBg, count);
		
		this.region = new BgRegion();
		this.region.setId(rId);
		this.region.setName(rName);
	}
	
	public TripleChart(
			Integer opId, String opName, String opMNC,	// Operator fields
			Integer cId, Short cMCC, String cNameBg,	// Country fields
			Long count) {
		
		this.operator = new Operator();
		this.operator.setId(opId);
		this.operator.setName(opName);
		this.operator.setMnc(opMNC);
		
		this.country = new Country();
		this.country.setId(cId);
		this.country.setMcc(cMCC);
		this.country.setNameBg(cNameBg);
		
		this.count = count;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	public BgRegion getRegion() {
		return region;
	}

	public void setRegion(BgRegion region) {
		this.region = region;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}
	
	

}
