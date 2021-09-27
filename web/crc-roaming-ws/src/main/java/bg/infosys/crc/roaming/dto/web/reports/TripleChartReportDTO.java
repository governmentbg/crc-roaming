package bg.infosys.crc.roaming.dto.web.reports;

import java.util.ArrayList;
import java.util.List;

import bg.infosys.crc.entities.pub.BgRegion;
import bg.infosys.crc.entities.pub.Country;
import bg.infosys.crc.entities.pub.Operator;
import bg.infosys.crc.roaming.dto.web.countries.ListCountryDTO;
import bg.infosys.crc.roaming.dto.web.operators.ListOperatorDTO;

public class TripleChartReportDTO {
	private List<CountryChartDTO> countryChart;
	private List<OperatorChartDTO> operatorChart;
	private List<BgRegionChartDTO> regionChart;
	
	public TripleChartReportDTO() {
		this.countryChart = new ArrayList<>();
		this.operatorChart = new ArrayList<>();
		this.regionChart = new ArrayList<>();
	}
	
	public List<CountryChartDTO> getCountryChart() {
		return countryChart;
	}

	public void setCountryChart(List<CountryChartDTO> countryChart) {
		this.countryChart = countryChart;
	}

	public List<OperatorChartDTO> getOperatorChart() {
		return operatorChart;
	}

	public void setOperatorChart(List<OperatorChartDTO> operatorChart) {
		this.operatorChart = operatorChart;
	}

	public List<BgRegionChartDTO> getRegionChart() {
		return regionChart;
	}

	public void setRegionChart(List<BgRegionChartDTO> regionChart) {
		this.regionChart = regionChart;
	}

	public static class CountryChartDTO {
		private ListCountryDTO country;
		private Long count;
		
		public CountryChartDTO(Country country, Long count) {
			this.country = new ListCountryDTO(country, true);
			this.count = count;
		}

		public ListCountryDTO getCountry() {
			return country;
		}

		public void setCountry(ListCountryDTO country) {
			this.country = country;
		}

		public Long getCount() {
			return count;
		}

		public void setCount(Long count) {
			this.count = count;
		}
	}

	public static class OperatorChartDTO {
		private ListOperatorDTO operator;
		private Long count;
		
		public OperatorChartDTO(Operator operator, Long count) {
			this.operator = new ListOperatorDTO(operator);
			this.count = count;
		}

		public ListOperatorDTO getOperator() {
			return operator;
		}

		public void setOperator(ListOperatorDTO operator) {
			this.operator = operator;
		}

		public Long getCount() {
			return count;
		}

		public void setCount(Long count) {
			this.count = count;
		}
	}

	public static class BgRegionChartDTO {
		private RegionDTO region;
		private Long count;
		
		public BgRegionChartDTO(BgRegion region, Long count) {
			this.region = new RegionDTO(region);
			this.count = count;
		}

		public RegionDTO getRegion() {
			return region;
		}

		public void setRegion(RegionDTO region) {
			this.region = region;
		}

		public Long getCount() {
			return count;
		}

		public void setCount(Long count) {
			this.count = count;
		}
	}

}
