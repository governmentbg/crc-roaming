package bg.infosys.crc.entities.pub;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(schema = "public", name = "source_operators")
public class SourceOperator {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "name")
	private String name;
	public static final String _name = "name";

	@Column(name = "mnc")
	private Short mnc;
	public static final String _mnc = "mnc";

	@Column(name = "mcc")
	private Short mcc;
	public static final String _mcc = "mcc";

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

	public Short getMnc() {
		return mnc;
	}

	public void setMnc(Short mnc) {
		this.mnc = mnc;
	}

	public Short getMcc() {
		return mcc;
	}

	public void setMcc(Short mcc) {
		this.mcc = mcc;
	}

}
