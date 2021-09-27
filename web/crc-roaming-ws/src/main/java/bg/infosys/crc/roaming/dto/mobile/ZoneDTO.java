package bg.infosys.crc.roaming.dto.mobile;

public class ZoneDTO {
	private double[] lat;
	private double[] lng;
	private double centerLat;
	private double centerLng;
	private double radius;

	public double[] getLat() {
		return lat;
	}

	public void setLat(double[] lat) {
		this.lat = lat;
	}

	public double[] getLng() {
		return lng;
	}

	public void setLng(double[] lng) {
		this.lng = lng;
	}

	public double getCenterLat() {
		return centerLat;
	}

	public void setCenterLat(double centerLat) {
		this.centerLat = centerLat;
	}

	public double getCenterLng() {
		return centerLng;
	}

	public void setCenterLng(double centerLng) {
		this.centerLng = centerLng;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

}
