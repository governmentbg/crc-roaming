package bg.infosys.crc.roaming.services.web;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bg.infosys.common.exceptions.ResponseStatusException;
import bg.infosys.crc.dao.pub.ReportedRoamingDAO;
import bg.infosys.crc.dao.pub.TranslationDAO;
import bg.infosys.crc.entities.pub.LanguageEnum;
import bg.infosys.crc.entities.pub.ReportedRoaming;
import bg.infosys.crc.entities.pub.Translation;
import bg.infosys.crc.entities.pub.Translation.TranslationId;
import bg.infosys.crc.entities.pub.TranslationEnum;
import bg.infosys.crc.roaming.dto.web.TranslationDTO;

@Service
public class SystemService {
	@Autowired private TranslationDAO translationDAO;
	@Autowired private ReportedRoamingDAO reportedRoamingDAO;

	public List<TranslationDTO> getAllTexts() {
		return translationDAO.getAll().stream().map(t -> new TranslationDTO(t)).collect(Collectors.toList());
	}

	@Transactional
	public void updateText(TranslationEnum key, LanguageEnum lang, String value) {
		Translation t = translationDAO.findById(new TranslationId(key, lang));
		if (t == null) {
			throw new ResponseStatusException(HttpServletResponse.SC_GONE, "The key does not exist.");
		}
		
		t.setValue(value);
		translationDAO.update(t);	
	}
	
	public TranslationDTO getTranslation(TranslationEnum translationKey, LanguageEnum lang) {
		return new TranslationDTO(translationDAO.findById(new TranslationId(translationKey, lang)));
	}

	@Transactional
	public int setHiddenRoamings(LocalDate date) {
		return reportedRoamingDAO.setHidden(date);
	}
	
	public List<ReportedRoaming> getHidden() {
		return reportedRoamingDAO.findHidden();
	}

}
