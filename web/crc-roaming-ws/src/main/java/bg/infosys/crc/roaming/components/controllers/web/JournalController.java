package bg.infosys.crc.roaming.components.controllers.web;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bg.infosys.crc.entities.web.JournalObjectTypeEnum;
import bg.infosys.crc.roaming.dto.web.journal.ListJournalDTO;
import bg.infosys.crc.roaming.helpers.web.PagingResultDTO;
import bg.infosys.crc.roaming.helpers.web.Permissions;
import bg.infosys.crc.roaming.services.web.JournalService;

@RestController
@RequestMapping(value = "/api/web/journal")
public class JournalController {
	@Autowired private JournalService journalService;

	@Secured(Permissions.VIEW_JOURNAL)
	@GetMapping(value = "/all-records")
	public PagingResultDTO<ListJournalDTO> getAllRecords(
			@RequestParam Integer page, 
			@RequestParam Integer size,
			@RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam("to")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(required = false) Integer userId,
			@RequestParam(required = false) JournalObjectTypeEnum objType) {
		return journalService.getJournalRecords(page, size, from, to, userId, objType);
	}
	
	@Secured(Permissions.VIEW_JOURNAL)
	@GetMapping(value = "/count-records")
	public long countAllReportedRoamings(
			@RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam("to")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(required = false) Integer userId,
			@RequestParam(required = false) JournalObjectTypeEnum objType) {

		return journalService.countJournalRecords(from, to, userId, objType);
	}
	
	@Secured(Permissions.VIEW_JOURNAL)
	@GetMapping(value = "/all-types")
	public JournalObjectTypeEnum[] getAllJournalTypes() {
		return JournalObjectTypeEnum.values();
	}
	
	@Secured(Permissions.VIEW_JOURNAL)
	@GetMapping(value = "/get/{id}")
	public ListJournalDTO getJournalRecord(@PathVariable Integer id) {
		return journalService.getRecord(id);
	}
}
