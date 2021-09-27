package bg.infosys.crc.roaming.components.controllers.web;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bg.infosys.crc.entities.pub.LanguageEnum;
import bg.infosys.crc.entities.pub.TranslationEnum;
import bg.infosys.crc.roaming.dto.StringValueDTO;
import bg.infosys.crc.roaming.dto.mobile.ReportedRoamingDTO;
import bg.infosys.crc.roaming.dto.web.TranslationDTO;
import bg.infosys.crc.roaming.helpers.web.Permissions;
import bg.infosys.crc.roaming.services.web.OpenDataService;
import bg.infosys.crc.roaming.services.web.SystemService;

@RestController
@RequestMapping(value = "/api/web/system")
public class SystemController {
	@Autowired private SystemService systemService;
	@Autowired private OpenDataService openDataService;
	
	@Secured(Permissions.VIEW_TEXTS)
	@GetMapping(value = "/get-texts")
	public List<TranslationDTO> getAllTexts() {
		return systemService.getAllTexts();
	}
	
	@Secured(Permissions.EDIT_TEXTS)
	@PostMapping("/update-text/{key}/{lang}")
	public void updateText(@PathVariable TranslationEnum key, @PathVariable LanguageEnum lang, @RequestBody StringValueDTO data) {
		systemService.updateText(key, lang, data.getValue());
	}
	
	@Secured(Permissions.VIEW_JOURNAL)
	@PostMapping(value = "/execute-opendata-submit")
	public void executeOpendataSubmit() {
		openDataService.generateOpenData();
	}
	
	@Secured(Permissions.VIEW_JOURNAL)
	@PostMapping(value = "/set-hidden-roamings/{date}")
	public int setHiddenRoamings(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
		return systemService.setHiddenRoamings(date);
	}
	
	@Secured(Permissions.VIEW_JOURNAL)
	@GetMapping(value = "/get-hidden-roamings")
	public List<ReportedRoamingDTO> getHiddenRoamings() {
		return systemService.getHidden().stream().map(rr -> new ReportedRoamingDTO(rr)).collect(Collectors.toList());
	}
	
}
