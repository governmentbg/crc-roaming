package bg.infosys.crc.roaming.dto.opendata;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AddDataDTO {
	@JsonProperty("api_key")
	private String apiKey;
	@JsonProperty("resource_uri")
	private String resourceUri;
	@JsonProperty("extension_format")
	private String extFormat;
	private Map<String, String[]> data;

	public AddDataDTO(String apiKey, String resourceUri, String[] headers, List<String[]> rows) {
		this.apiKey = apiKey;
		this.resourceUri = resourceUri;
		this.extFormat = "CSV";
		data = new LinkedHashMap<>();
		data.put("headers", headers);

		int rowId = 0;
		for (String[] row : rows) {
			rowId++;
			data.put("row" + rowId, row);
		}
	}

	public String getApiKey() {
		return apiKey;
	}

	public String getResourceUri() {
		return resourceUri;
	}

	public String getExtFormat() {
		return extFormat;
	}

	public Map<String, String[]> getData() {
		return data;
	}

}
