package bg.infosys.crc.entities.pub;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "settings", schema = "public")
public class Setting {

	@Id
	@Enumerated(EnumType.STRING)
	private SettingEnum key;
	public static final String _key = "key";
	
	@Column
	private String value;

	public SettingEnum getKey() {
		return key;
	}
	
	public void setKey(SettingEnum key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}


}
