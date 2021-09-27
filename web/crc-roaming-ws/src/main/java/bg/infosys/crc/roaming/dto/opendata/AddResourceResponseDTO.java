package bg.infosys.crc.roaming.dto.opendata;

import java.util.Arrays;
import java.util.Map;

public class AddResourceResponseDTO extends BasicResponseDTO {
	private Map<String, String[]> errors;

	public Map<String, String[]> getErrors() {
		return errors;
	}

	public void setErrors(Map<String, String[]> errors) {
		this.errors = errors;
	}

	public String getErrorsAsString() {
		StringBuilder sb = new StringBuilder();
		if (errors != null) {
			sb.append("{");
			for (String key : errors.keySet()) {
				sb.append(key);
				sb.append("=");
				String[] errs = errors.get(key);
				if (errs.length == 1) {
					sb.append(errs[0]);
				} else {
					sb.append(Arrays.toString(errors.get(key)));
				}
				sb.append(", ");
			}
			sb.delete(sb.length() - 2, sb.length());
			sb.append("}");
		}
		
		return sb.toString();
	}

	public static class DataDTO {
		private String uri;

		public String getUri() {
			return uri;
		}

		public void setUri(String uri) {
			this.uri = uri;
		}
	}

}
