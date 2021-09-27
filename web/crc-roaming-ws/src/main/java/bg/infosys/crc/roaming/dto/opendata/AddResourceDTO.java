package bg.infosys.crc.roaming.dto.opendata;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AddResourceDTO {
	@JsonProperty("api_key")
	private String apiKey;
	@JsonProperty("dataset_uri")
	private String datasetUri;
	private DataDTO data;

	public AddResourceDTO(String apiKey, String datasetUri, String name, String resourceUrl) {
		this.apiKey = apiKey;
		this.datasetUri = datasetUri;
		this.data = new DataDTO(name, resourceUrl);
	}

	public String getApiKey() {
		return apiKey;
	}

	public String getDatasetUri() {
		return datasetUri;
	}

	public DataDTO getData() {
		return data;
	}

	public static class DataDTO {
		private Integer type;
		@JsonProperty("http_rq_type")
		private String rqType;
		private String locale;
		@JsonProperty("file_format")
		private String fileFormat;
		private String name;
		@JsonProperty("resource_url")
		private String resourceUrl;
		private String description;

		public DataDTO(String name, String resourceUrl) {
			this.type = 3;
			this.rqType = "POST";
			this.locale = "bg";
			this.fileFormat = "CSV";
			this.name = name;
			this.resourceUrl = resourceUrl;
		}

		public Integer getType() {
			return type;
		}

		public String getRqType() {
			return rqType;
		}

		public String getLocale() {
			return locale;
		}

		public String getFileFormat() {
			return fileFormat;
		}

		public String getName() {
			return name;
		}

		public String getResourceUrl() {
			return resourceUrl;
		}

		public String getDescription() {
			return description;
		}
	}

}
