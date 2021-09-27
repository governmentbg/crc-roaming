package bg.infosys.crc.roaming.dto.mobile;

import bg.infosys.crc.entities.pub.Operator;

public class OperatorDTO implements Comparable<OperatorDTO> {
	private String n;
	private String c;

	public OperatorDTO(Operator operator) {
		this.n = operator.getName();
		this.c = operator.getMnc().toString();
	}

	public String getN() {
		return n;
	}

	public void setN(String n) {
		this.n = n;
	}

	public String getC() {
		return c;
	}

	public void setC(String c) {
		this.c = c;
	}
	
	@Override
	public int compareTo(OperatorDTO o) {
		return this.n.compareToIgnoreCase(o.n);
	}

}
