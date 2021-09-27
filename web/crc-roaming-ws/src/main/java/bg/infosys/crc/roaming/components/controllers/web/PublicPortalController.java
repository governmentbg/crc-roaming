package bg.infosys.crc.roaming.components.controllers.web;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bg.infosys.crc.entities.pub.LanguageEnum;
import bg.infosys.crc.entities.pub.TranslationEnum;
import bg.infosys.crc.roaming.components.Properties;
import bg.infosys.crc.roaming.dto.web.TranslationDTO;
import bg.infosys.crc.roaming.dto.web.countries.ListCountryDTO;
import bg.infosys.crc.roaming.dto.web.map.AddEditListZoneDTO;
import bg.infosys.crc.roaming.dto.web.operators.ListOperatorDTO;
import bg.infosys.crc.roaming.dto.web.reports.RegionDTO;
import bg.infosys.crc.roaming.dto.web.reports.ReportedRoamingDTO;
import bg.infosys.crc.roaming.dto.web.users.EmailDTO;
import bg.infosys.crc.roaming.services.mobile.MobileAuthService;
import bg.infosys.crc.roaming.services.mobile.ZoneService;
import bg.infosys.crc.roaming.services.web.CountryService;
import bg.infosys.crc.roaming.services.web.OperatorService;
import bg.infosys.crc.roaming.services.web.ReportService;
import bg.infosys.crc.roaming.services.web.SystemService;

@RestController
@RequestMapping(value = "/api/web/portal")
public class PublicPortalController {
	@Autowired private MobileAuthService	mobileAuthService;
	@Autowired private CountryService		countryService;
	@Autowired private OperatorService		operatorService;
	@Autowired private ReportService		reportService;
	@Autowired private ZoneService			zoneService;
	@Autowired private SystemService		systemService;

	@PostMapping(value = "/request-delete")
	public void requestProfileDelete(@RequestBody EmailDTO dto) {
		mobileAuthService.deleteMobileUser(dto);
	}
	
	@GetMapping(value = Properties.PATH_CNF_DEL_USR)
	public ResponseEntity<?> confirmProfileDelete(@RequestParam String key) throws IOException {
		String location = mobileAuthService.confirmProfileDelete(key);
		return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, location).build();
	}
	
	@GetMapping(value = "/reported-roamings")
	public List<ReportedRoamingDTO> getAllReportedRoamings(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(required = false) Integer countryId,
			@RequestParam(required = false) Integer operatorId,
			@RequestParam(required = false) Integer regionId) {
		
		return reportService.getReportedRoamings(from, to, countryId, operatorId, regionId);
	}
	
	@GetMapping(value = "/regions")
	public List<RegionDTO> getRegions() {
		return reportService.getRegions();
	}
	
	@GetMapping(value = "/countries")
	public List<ListCountryDTO> getCountries(
			@RequestParam(defaultValue = "id") String orderBy, 
			@RequestParam(defaultValue = "false") Boolean asc) {
		return countryService.getCountries(orderBy, asc);
	}
	
	@GetMapping(value = "/operators")
	public List<ListOperatorDTO> getOperators(
			@RequestParam(defaultValue = "id") String orderBy, 
			@RequestParam(defaultValue = "false") Boolean asc) {
		return operatorService.getOperators(orderBy, asc);
	}
	
	@GetMapping(value = "/roaming-zones")
	public List<AddEditListZoneDTO> getRoamingZones() {
		return zoneService.getAllZones();
	}
	
	@GetMapping(value = "/about/{lang}")
	public TranslationDTO getAbout(@PathVariable LanguageEnum lang) {
		return systemService.getTranslation(TranslationEnum.WEB_ABOUT_PAGE, lang);
	}
	
	@GetMapping(value = "/privacy-policy/{lang}")
	public TranslationDTO getPrivacyPolicy(@PathVariable LanguageEnum lang) {
		return systemService.getTranslation(TranslationEnum.WEB_PRIVACY_POLICY, lang);
	}
}
