package bg.infosys.crc.roaming.dto.web.map;

import bg.infosys.crc.entities.pub.GeometryTypeEnum;
import bg.infosys.crc.entities.pub.HighRiskZone;
import bg.infosys.crc.roaming.dto.IEntityDTO;

public class AddEditListZoneDTO implements IEntityDTO<HighRiskZone> {
	private Integer id;
	private String name;
	private String type;
	private Double[][] coordinates;
	
	public AddEditListZoneDTO() {
	}
	
	public AddEditListZoneDTO(HighRiskZone z) {
		this.id				= z.getId();
		this.name			= z.getName();
		this.type			= z.getType().toString();
		this.coordinates	= z.getCoordinates();
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
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public Double[][] getCoordinates() {
		return coordinates;
	}
	
	public void setCoordinates(Double[][] coordinates) {
		this.coordinates = coordinates;
	}
	
	@Override
	public HighRiskZone toEntity() {
		HighRiskZone z = new HighRiskZone();
		return merge(z);
	}
	
	@Override
	public HighRiskZone merge(HighRiskZone entity) {
		entity.setName(name);
		entity.setType(GeometryTypeEnum.valueOf(type.toUpperCase()));
		entity.setCoordinates(coordinates);
		
		return entity;
	}

}

