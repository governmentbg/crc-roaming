package bg.infosys.crc.roaming.dto.mobile;

import java.util.List;

public class ReportedZoneDTO {
	private List<Double> va;
	private List<Double> vb;
	private String mcc;
	private String mnc;
	private Long ts;

	public List<Double> getVa() {
		return va;
	}

	public void setVa(List<Double> va) {
		this.va = va;
	}

	public List<Double> getVb() {
		return vb;
	}

	public void setVb(List<Double> vb) {
		this.vb = vb;
	}

	public String getMcc() {
		return mcc;
	}

	public void setMcc(String mcc) {
		this.mcc = mcc;
	}

	public String getMnc() {
		return mnc;
	}

	public void setMnc(String mnc) {
		this.mnc = mnc;
	}

	public Long getTs() {
		return ts;
	}

	public void setTs(Long ts) {
		this.ts = ts;
	}

}
