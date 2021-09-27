package bg.infosys.crc.entities.pub;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(schema = "public", name = "bg_regions")
public class BgRegion {

	@Id
	private Integer id;

	@Column(name = "name")
	private String name;
	public static final String _name = "name";

	@Column(name = "nominatim_name")
	private String nominatimName;
	public static final String _nominatimName = "nominatimName";
	
	@Column(name = "int_name")
	private String intName;

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

	public String getNominatimName() {
		return nominatimName;
	}

	public void setNominatimName(String nominatimName) {
		this.nominatimName = nominatimName;
	}
	
	public String getIntName() {
		return intName;
	}
	
	public void setIntName(String intName) {
		this.intName = intName;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof BgRegion))
			return false;
		BgRegion other = (BgRegion) obj;
		return Objects.equals(id, other.id) && Objects.equals(name, other.name);
	}

}
