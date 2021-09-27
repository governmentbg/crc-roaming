package bg.infosys.crc.roaming.dto.web;

import bg.infosys.crc.entities.pub.LanguageEnum;
import bg.infosys.crc.entities.pub.Translation;
import bg.infosys.crc.entities.pub.TranslationEnum;

public class TranslationDTO {
	private TranslationEnum key;
	private LanguageEnum language;
	private String value;
	
	public TranslationDTO() {
	}
	
	public TranslationDTO(Translation t) {
		this.key = t.getTranslationId().getKey();
		this.language = t.getTranslationId().getLanguage();
		this.value = t.getValue();
	}

	public TranslationEnum getKey() {
		return key;
	}

	public void setKey(TranslationEnum key) {
		this.key = key;
	}
	
	public LanguageEnum getLanguage() {
		return language;
	}
	
	public void setLanguage(LanguageEnum language) {
		this.language = language;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
