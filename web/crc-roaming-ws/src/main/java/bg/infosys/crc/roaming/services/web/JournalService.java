package bg.infosys.crc.roaming.services.web;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import bg.infosys.common.db.helpers.ResultFilter;
import bg.infosys.crc.dao.web.JournalChangeDAO;
import bg.infosys.crc.entities.web.JournalChange;
import bg.infosys.crc.entities.web.JournalObjectTypeEnum;
import bg.infosys.crc.roaming.dto.web.journal.ListJournalDTO;
import bg.infosys.crc.roaming.helpers.web.PagingResultDTO;

@Service
public class JournalService {
	@Autowired private JournalChangeDAO journalChangeDAO;

	public PagingResultDTO<ListJournalDTO> getJournalRecords(Integer page, Integer pageSize, LocalDate from, LocalDate to, Integer userId, JournalObjectTypeEnum objType) {
		final ObjectMapper mapper = new ObjectMapper();
		List<ListJournalDTO> result = journalChangeDAO.find(ResultFilter.firstResult(page, pageSize), pageSize, from, to, userId, objType)
				.stream().map(jc -> new ListJournalDTO(jc, mapper))
				.collect(Collectors.toList());
		
		return new PagingResultDTO<>(result, page, pageSize);
	}
	
	public ListJournalDTO getRecord(Integer id) {
		JournalChange jc = journalChangeDAO.getById(id);
		final ObjectMapper mapper = new ObjectMapper();
		ListJournalDTO dto = new ListJournalDTO(jc, mapper);
		
		return dto;
	}

	public long countJournalRecords(LocalDate from, LocalDate to, Integer userId, JournalObjectTypeEnum objType) {
		return journalChangeDAO.count(from, to, userId, objType);
	}
}
