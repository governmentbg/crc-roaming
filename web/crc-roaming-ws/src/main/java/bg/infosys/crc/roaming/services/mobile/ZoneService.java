package bg.infosys.crc.roaming.services.mobile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bg.infosys.common.exceptions.ResponseStatusException;
import bg.infosys.common.ws.security.SecuritySession;
import bg.infosys.crc.dao.pub.HighRiskZoneDAO;
import bg.infosys.crc.dao.web.security.UserDAO;
import bg.infosys.crc.entities.pub.HighRiskZone;
import bg.infosys.crc.roaming.dto.web.map.AddEditListZoneDTO;

@Service
public class ZoneService {
	@Autowired private HighRiskZoneDAO highRiskZoneDAO;
	@Autowired private UserDAO userDAO;

	@Transactional
	public Integer save(AddEditListZoneDTO geoJSON) {
		HighRiskZone z = geoJSON.toEntity();
		
		z.setCreatedAt(LocalDateTime.now());
		z.setCreatedBy(userDAO.findById(SecuritySession.getUserId()));
		
		return highRiskZoneDAO.save(z);
	}
	
	public List<AddEditListZoneDTO> getAllZones() {
		List<HighRiskZone> zones = highRiskZoneDAO.findAllOrdered("id", false);
		List<AddEditListZoneDTO> dtos = new ArrayList<>(zones.size());
		for (HighRiskZone z : zones) {
			dtos.add(new AddEditListZoneDTO(z));
		}
		return dtos;
	}

	@Transactional
	public void updateZone(Integer id, AddEditListZoneDTO geoZone) {
		HighRiskZone z = getAndCheckIfZoneExists(id);
		
		z.setUpdatedAt(LocalDateTime.now());
		z.setUpdatedBy(userDAO.findById(SecuritySession.getUserId()));
		
		highRiskZoneDAO.update(geoZone.merge(z));
	}
	
	@Transactional
	public void deleteZone(Integer id) {
		HighRiskZone z = getAndCheckIfZoneExists(id);
		
		z.setDeletedAt(LocalDateTime.now());
		z.setDeletedBy(userDAO.findById(SecuritySession.getUserId()));
		
		highRiskZoneDAO.delete(z);
	}
	
	private HighRiskZone getAndCheckIfZoneExists(Integer id) {
		HighRiskZone z = highRiskZoneDAO.findById(id);
		if (z == null) {
			throw new ResponseStatusException(HttpServletResponse.SC_GONE, "The user does not exist", "userDoesNotExist");
		}
		
		return z;
	}
	
	
	
}
