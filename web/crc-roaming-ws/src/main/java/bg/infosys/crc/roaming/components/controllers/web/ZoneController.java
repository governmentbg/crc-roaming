package bg.infosys.crc.roaming.components.controllers.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bg.infosys.crc.roaming.dto.web.map.AddEditListZoneDTO;
import bg.infosys.crc.roaming.helpers.web.Permissions;
import bg.infosys.crc.roaming.services.mobile.ZoneService;

@RestController
@RequestMapping(value = "/api/web/zones")
public class ZoneController {
	@Autowired private ZoneService zoneService;

	@Secured(Permissions.EDIT_ZONES)
	@PostMapping(value = "/add")
	public Integer addZone(@RequestBody AddEditListZoneDTO geoZone) {
		return zoneService.save(geoZone);
	}
	
	@Secured(Permissions.EDIT_ZONES)
	@PostMapping("/edit/{id}")
	public void updateZone(@PathVariable Integer id, @RequestBody AddEditListZoneDTO geoZone) {
		zoneService.updateZone(id, geoZone);
	}
	
	@Secured(Permissions.EDIT_ZONES)
	@DeleteMapping("/delete/{id}")
	public void deleteZone(@PathVariable Integer id) {
		zoneService.deleteZone(id);
	}
}
