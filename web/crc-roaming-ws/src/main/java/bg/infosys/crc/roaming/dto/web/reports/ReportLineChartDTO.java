package bg.infosys.crc.roaming.dto.web.reports;

import java.util.ArrayList;
import java.util.List;

public class ReportLineChartDTO {

	private List<Long> counts;
	private List<String> labels;

	public ReportLineChartDTO(int size) {
		this.counts = new ArrayList<>(size);
		this.labels = new ArrayList<>(size);
	}

	public List<Long> getCounts() {
		return counts;
	}

	public void setCounts(List<Long> counts) {
		this.counts = counts;
	}

	public List<String> getLabels() {
		return labels;
	}

	public void setLabels(List<String> labels) {
		this.labels = labels;
	}

}
