package bg.infosys.crc.roaming.services.web;

import java.time.LocalDateTime;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bg.infosys.crc.dao.pub.BgRegionDAO;
import bg.infosys.crc.dao.pub.NotificationDAO;
import bg.infosys.crc.dao.pub.ReportedRoamingDAO;
import bg.infosys.crc.entities.pub.BgRegion;
import bg.infosys.crc.entities.pub.Notification;
import bg.infosys.crc.entities.pub.ReportedRoaming;

@Service
public class BackgroundService {
	@Autowired private BgRegionDAO			bgProvinceDAO;
	@Autowired private NotificationDAO		notificationDAO;
	@Autowired private ReportedRoamingDAO	reportedRoamingDAO;

	@Transactional
	public void updateReportedRoamingRegion(Integer reportedRoamingId, String regionName) {
		BgRegion region = bgProvinceDAO.findByNominatimName(regionName);
		if (region != null) {
			ReportedRoaming rr = reportedRoamingDAO.findById(reportedRoamingId);
			rr.setBgRegion(region);
			reportedRoamingDAO.update(rr);
		}
	}
	
	@Transactional
	public void deleteReportedRoaming(Integer reportedRoamingId) {
		reportedRoamingDAO.deleteById(reportedRoamingId);
	}
	
	@Transactional
	public void updateSentAt(Integer notificationId) {
		Notification n = notificationDAO.findById(notificationId);
		n.setSentAt(LocalDateTime.now());
		notificationDAO.update(n);
	}

}
