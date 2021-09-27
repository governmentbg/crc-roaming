package bg.infosys.crc.roaming.dto.mobile;

import java.util.Objects;

import bg.infosys.crc.entities.pub.ReportedRoaming;

public class ReportedRoamingDTO {
	private int id;
	private String mnc;
	private short mcc;
	private double latitude;
	private double longitude;
	private short srcMnc;
	private short srcMcc;
	private String srcName;

	public ReportedRoamingDTO(ReportedRoaming rr) {
		this.id			= rr.getId();
		this.mnc		= rr.getOperator().getMnc();
		this.mcc		= rr.getOperator().getCountry().getMcc();
		this.latitude	= rr.getLatitude();
		this.longitude	= rr.getLongitude();
		this.srcMcc		= rr.getSourceOperator().getMcc();
		this.srcMnc		= rr.getSourceOperator().getMnc();
		this.srcName	= rr.getSourceOperator().getName();
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public String getMnc() {
		return mnc;
	}

	public void setMnc(String mnc) {
		this.mnc = mnc;
	}

	public short getMcc() {
		return mcc;
	}

	public void setMcc(short mcc) {
		this.mcc = mcc;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public short getSrcMnc() {
		return srcMnc;
	}

	public void setSrcMnc(short srcMnc) {
		this.srcMnc = srcMnc;
	}

	public short getSrcMcc() {
		return srcMcc;
	}

	public void setSrcMcc(short srcMcc) {
		this.srcMcc = srcMcc;
	}

	public String getSrcName() {
		return srcName;
	}

	public void setSrcName(String srcName) {
		this.srcName = srcName;
	}

	@Override
	public int hashCode() {
		return Objects.hash(latitude, longitude, mcc, mnc);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof ReportedRoamingDTO))
			return false;

		ReportedRoamingDTO other = (ReportedRoamingDTO) obj;
		return Double.doubleToLongBits(latitude) == Double.doubleToLongBits(other.latitude)
				&& Double.doubleToLongBits(longitude) == Double.doubleToLongBits(other.longitude)
				&& mcc == other.mcc
				&& mnc == other.mnc
				&& srcMcc == other.srcMcc
				&& srcMnc == other.srcMnc
				&& Objects.equals(srcName, other.srcName);
	}

}
