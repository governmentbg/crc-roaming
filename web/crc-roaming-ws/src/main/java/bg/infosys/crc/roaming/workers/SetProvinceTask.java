package bg.infosys.crc.roaming.workers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bg.infosys.common.geocoding.ReverseGeocoder;
import bg.infosys.common.geocoding.dto.GeocodeDTO;
import bg.infosys.common.geocoding.dto.GeocodingResponseDTO;
import bg.infosys.crc.entities.pub.ReportedRoaming;
import bg.infosys.crc.roaming.services.web.BackgroundService;

public class SetProvinceTask implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(SetProvinceTask.class);
	
	private BackgroundService service;
	private ReportedRoaming reportedRoaming;
	
	public SetProvinceTask(ReportedRoaming reportedRoaming, BackgroundService service) {
		this.reportedRoaming = reportedRoaming;
		this.service = service;
	}
	
	@Override
	public void run() {
		try { // Let's don't screw up the executor, okay?
			GeocodeDTO geocode = ReverseGeocoder.getGeocodeDTO(reportedRoaming.getLatitude(), reportedRoaming.getLongitude());
			GeocodingResponseDTO geocoding = geocode.getFeatures()[0].getProperties().getGeocoding();
			if ("България".equalsIgnoreCase(geocoding.getCountry())) {
				String province = geocoding.getAdmin().getLevel6();
				service.updateReportedRoamingRegion(reportedRoaming.getId(), province);				
			} else {
				service.deleteReportedRoaming(reportedRoaming.getId());
			}
		} catch (Exception e) {
			LOGGER.error("Error on set province task execution.", e);
		}
	}

}
