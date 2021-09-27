package bg.infosys.crc.roaming.dto.web.reports;

import bg.infosys.crc.entities.pub.BgRegion;

public class RegionDTO {
	private Integer id;
	private String name;
	private String intName;
	
	public RegionDTO(BgRegion r) {
		this.id = r.getId();
		this.name = r.getName();
		this.intName = r.getIntName();
	}

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
	
	public String getIntName() {
		return intName;
	}
	
	public void setIntName(String intName) {
		this.intName = intName;
	}

}
