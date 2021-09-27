package bg.infosys.crc.entities.pub;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Entity
@Table(name = "translations", schema = "public")
public class Translation {

	@EmbeddedId
	private TranslationId translationId;
	public static final String _id = "translationId";

	@Column
	private String value;

	public TranslationId getTranslationId() {
		return translationId;
	}

	public void setTranslationId(TranslationId translationId) {
		this.translationId = translationId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Embeddable
	public static class TranslationId implements Serializable {
		private static final long serialVersionUID = 1L;

		@Enumerated(EnumType.STRING)
		private TranslationEnum key;
		public static final String _key = "key";

		@Enumerated(EnumType.ORDINAL)
		private LanguageEnum language;
		public static final String _language = "language";

		public TranslationId() {
		}

		public TranslationId(TranslationEnum key, LanguageEnum language) {
			this.key = key;
			this.language = language;
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

		@Override
		public int hashCode() {
			return Objects.hash(key, language);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof TranslationId))
				return false;
			TranslationId other = (TranslationId) obj;
			return key == other.key && language == other.language;
		}
	}

}
