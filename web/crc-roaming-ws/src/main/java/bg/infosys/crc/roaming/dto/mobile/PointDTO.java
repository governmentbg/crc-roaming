package bg.infosys.crc.roaming.dto.mobile;

public class PointDTO {
	private Long ts;
	private String mcc;
	private String mnc;
	private Double[] va;
	private Double[] vb;


	public Double[] getVa() {
		return va;
	}

	public void setVa(Double[] va) {
		this.va = va;
	}

	public Double[] getVb() {
		return vb;
	}

	public void setVb(Double[] vb) {
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
