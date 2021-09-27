package bg.infosys.crc.roaming.services.web;

import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bg.infosys.common.db.helpers.ResultFilter;
import bg.infosys.crc.dao.mobile.security.MobileUserDAO;
import bg.infosys.crc.dao.pub.BgRegionDAO;
import bg.infosys.crc.dao.pub.ReportedBlockingDAO;
import bg.infosys.crc.dao.pub.ReportedRoamingDAO;
import bg.infosys.crc.entities.mobile.security.MobileUser;
import bg.infosys.crc.entities.pub.BgRegion;
import bg.infosys.crc.entities.pub.Country;
import bg.infosys.crc.entities.pub.Operator;
import bg.infosys.crc.entities.pub.ReportedRoaming;
import bg.infosys.crc.ndbo.TripleChart;
import bg.infosys.crc.roaming.dto.web.reports.RegionDTO;
import bg.infosys.crc.roaming.dto.web.reports.RegisteredUserRerportDTO;
import bg.infosys.crc.roaming.dto.web.reports.ReportLineChartDTO;
import bg.infosys.crc.roaming.dto.web.reports.ReportedBlockingDTO;
import bg.infosys.crc.roaming.dto.web.reports.ReportedRoamingDTO;
import bg.infosys.crc.roaming.dto.web.reports.TripleChartReportDTO;
import bg.infosys.crc.roaming.dto.web.reports.TripleChartReportDTO.BgRegionChartDTO;
import bg.infosys.crc.roaming.dto.web.reports.TripleChartReportDTO.CountryChartDTO;
import bg.infosys.crc.roaming.dto.web.reports.TripleChartReportDTO.OperatorChartDTO;
import bg.infosys.crc.roaming.helpers.web.PagingResultDTO;

@Service
public class ReportService {
	@Autowired private BgRegionDAO 			bgRegionDAO;
	@Autowired private MobileUserDAO		mobileUserDAO;
	@Autowired private ReportedRoamingDAO	reportedRoamingDAO;
	@Autowired private ReportedBlockingDAO	reportedBlockingDAO;
	
	public RegisteredUserRerportDTO getNumberOfRegisteredUsers(LocalDate from, LocalDate to) { 
		long countRegisteredUsers = mobileUserDAO.countRegisteredUsers(from, to);
		MobileUser mobileUser = mobileUserDAO.getLastCreatedUserByCreatedAt(from, to);
		LocalDateTime dateOfLastCreatedUser = mobileUser == null ? null : mobileUser.getCreatedAt();
		
		return new RegisteredUserRerportDTO(countRegisteredUsers, dateOfLastCreatedUser);
	}
	
	public PagingResultDTO<ReportedRoamingDTO> getAllReportedRoamings(Integer page, Integer pageSize, String sortBy, String sortDirection, LocalDate from, LocalDate to, Integer countryId, Integer operatorId, Integer regionId, boolean getAll) {
		List<ReportedRoamingDTO> reportedRoamings = 
		reportedRoamingDAO.findAllReportedRoamingsPaged(ResultFilter.firstResult(page, pageSize), pageSize, sortBy, sortDirection, from, to, countryId, operatorId, regionId, getAll)
						  .stream().map(r -> new ReportedRoamingDTO(r)).collect(Collectors.toList());
		return new PagingResultDTO<>(reportedRoamings, page, pageSize);
	}

	public long countAllReportedRoamings(LocalDate from, LocalDate to, Integer countryId, Integer operatorId, Integer regionId) {
		return reportedRoamingDAO.countReportedRoamings(from, to, countryId, operatorId, regionId);
	}

	public List<RegionDTO> getRegions() {
		return bgRegionDAO.findAllOrdered().stream().map(r -> new RegionDTO(r)).collect(Collectors.toList());
	}

	public List<ReportedRoaming> getAllMissingProvince() {
		return reportedRoamingDAO.findAllMissingProvince();
	}
	
	public List<ReportedRoamingDTO> getReportedRoamings(LocalDate from, LocalDate to, Integer countryId, Integer operatorId, Integer regionId) {
		return reportedRoamingDAO
			.findReportedRoamings(from, to, countryId, operatorId, regionId)
			.stream()
			.map(r -> new ReportedRoamingDTO(r))
			.collect(Collectors.toList());
	}

	@Transactional
	public TripleChartReportDTO getReportedRoamingsChart(LocalDate from, LocalDate to) {
		List<TripleChart> list = reportedRoamingDAO.getReportedRoamingGrouped(from, to);
		return createTripleChartReportDTO(list, true);
	}

	@Transactional
	public ReportLineChartDTO getReportedRoamingLineChartData(Integer fromMonth, Integer fromYear, Integer toMonth, Integer toYear, Integer countryId, Integer operatorId, Integer regionId) {
		List<Object[]> resultList = reportedRoamingDAO.countMonthlyGrouped(fromMonth, fromYear, toMonth, toYear, countryId, operatorId, regionId);
		return generateDTO(fromMonth, fromYear, toMonth, toYear, resultList);
	}

	public ReportLineChartDTO getReportComparisonChart(Integer forMonthFirstPeriod, Integer forYearFirstPeriod, Integer forMonthSecondPeriod, Integer forYearSecondPeriod, Integer countryId, Integer operatorId, Integer regionId) {
		ReportLineChartDTO resultDTO = new ReportLineChartDTO(2);
		
		if(forMonthFirstPeriod == 0) {
			// Comparison by two years
            LocalDate initialDateFirstPeriod = LocalDate.ofYearDay(forYearFirstPeriod, 1);
            LocalDate fromFirstPeriod = initialDateFirstPeriod.with(firstDayOfYear());
            LocalDate toFirstPeriod = initialDateFirstPeriod.with(lastDayOfYear());
            fillLabelsAndCounts(resultDTO, "First period", reportedRoamingDAO.getReportedRoamingComparison(fromFirstPeriod, toFirstPeriod, countryId, operatorId, regionId));
		} else {
			 // comparison by two years and months
            LocalDate initialDateFirstPeriod = LocalDate.of(forYearFirstPeriod, forMonthFirstPeriod, 1);
            LocalDate fromFirstPeriod = initialDateFirstPeriod.with(firstDayOfMonth());
            LocalDate toFirstPeriod = initialDateFirstPeriod.with(lastDayOfMonth());
            fillLabelsAndCounts(resultDTO, "First period", reportedRoamingDAO.getReportedRoamingComparison(fromFirstPeriod, toFirstPeriod, countryId, operatorId, regionId));
		}
		
		if(forMonthSecondPeriod == 0) {
			// Comparison by two years
			LocalDate initialDateSecondPeriod = LocalDate.ofYearDay(forYearSecondPeriod, 1);
            LocalDate fromSecondPeriod = initialDateSecondPeriod.with(firstDayOfYear());
            LocalDate toSecondPeriod = initialDateSecondPeriod.with(lastDayOfYear());
            fillLabelsAndCounts(resultDTO, "Second period", reportedRoamingDAO.getReportedRoamingComparison(fromSecondPeriod, toSecondPeriod, countryId, operatorId, regionId));
		} else {
			 // comparison by two years and months
			LocalDate initialDateSecondPeriod = LocalDate.of(forYearSecondPeriod, forMonthSecondPeriod, 1);
            LocalDate fromSecondPeriod = initialDateSecondPeriod.with(firstDayOfMonth());
            LocalDate toSecondPeriod = initialDateSecondPeriod.with(lastDayOfMonth());
            fillLabelsAndCounts(resultDTO, "Second period", reportedRoamingDAO.getReportedRoamingComparison(fromSecondPeriod, toSecondPeriod, countryId, operatorId, regionId));
		}
		
		return resultDTO;
	}
	
	private void fillLabelsAndCounts(ReportLineChartDTO resultDTO, String label, Long count) {
		resultDTO.getLabels().add(label);
		resultDTO.getCounts().add(count);
	}
	
	public PagingResultDTO<ReportedBlockingDTO> getAllReportedBlockings(Integer page, Integer pageSize, String sortBy, String sortDirection, LocalDate from, LocalDate to, Integer countryId, Integer operatorId, boolean getAll) {
		List<ReportedBlockingDTO> reportedBlockings = 
		reportedBlockingDAO.findAllReportedBlockingsPaged(ResultFilter.firstResult(page, pageSize), pageSize, sortBy, sortDirection, from, to, countryId, operatorId, getAll)
						  .stream().map(r -> new ReportedBlockingDTO(r)).collect(Collectors.toList());
		return new PagingResultDTO<>(reportedBlockings, page, pageSize);
	}
	
	public long countAllReportedBlockings(LocalDate from, LocalDate to, Integer countryId, Integer operatorId) {
		return reportedBlockingDAO.countReportedBlockings(from, to, countryId, operatorId);
	}

	public TripleChartReportDTO getReportedBlockingsDistributionChart(LocalDate from, LocalDate to) {
		List<TripleChart> list = reportedBlockingDAO.getReportedBlockingGrouped(from, to);
		return createTripleChartReportDTO(list, false);
	}
	
	private TripleChartReportDTO createTripleChartReportDTO(List<TripleChart> list, boolean withRegions) {
		Map<Country, Long> countryMap  = new HashMap<>();
		Map<Operator, Long> operatorMap  = new HashMap<>();
		Map<BgRegion, Long> bgRegionMap  = new HashMap<>();
		
		for (TripleChart row : list) {
			countryMap.compute(row.getCountry(),   (key, value) -> value == null ? row.getCount() : value + row.getCount());
			operatorMap.compute(row.getOperator(), (key, value) -> value == null ? row.getCount() : value + row.getCount());
			if(withRegions) {				
				bgRegionMap.compute(row.getRegion(),   (key, value) -> value == null ? row.getCount() : value + row.getCount());
			}
		}
		
		TripleChartReportDTO chartDTO = new TripleChartReportDTO();
		
		countryMap.forEach((k, v) -> {
			 CountryChartDTO countryDTO = new CountryChartDTO(k, v);
             chartDTO.getCountryChart().add(countryDTO);
		});
		
		operatorMap.forEach((k, v) -> {
			 OperatorChartDTO operatorDTO = new OperatorChartDTO(k, v);
            chartDTO.getOperatorChart().add(operatorDTO);
		});
		if(withRegions) {
			bgRegionMap.forEach((k, v) -> {
				 BgRegionChartDTO regionDTO = new BgRegionChartDTO(k, v);
	            chartDTO.getRegionChart().add(regionDTO);
			});
		}
		
		return chartDTO;
	}
	
	@Transactional
	public ReportLineChartDTO getReportedBlockingEvolutionChartData(Integer fromMonth, Integer fromYear, Integer toMonth, Integer toYear, Integer countryId, Integer operatorId) {
		List<Object[]> resultList = reportedBlockingDAO.countMonthlyGrouped(fromMonth, fromYear, toMonth, toYear, countryId, operatorId);
		return generateDTO(fromMonth, fromYear, toMonth, toYear, resultList);
	}
	
	public ReportLineChartDTO getReportedBlockingsComparisonChart(Integer forMonthFirstPeriod, Integer forYearFirstPeriod, Integer forMonthSecondPeriod, Integer forYearSecondPeriod, Integer countryId, Integer operatorId) {
		ReportLineChartDTO resultDTO = new ReportLineChartDTO(2);
		
		if(forMonthFirstPeriod == 0) {
			// Comparison by two years
            LocalDate initialDateFirstPeriod = LocalDate.ofYearDay(forYearFirstPeriod, 1);
            LocalDate fromFirstPeriod = initialDateFirstPeriod.with(firstDayOfYear());
            LocalDate toFirstPeriod = initialDateFirstPeriod.with(lastDayOfYear());
            fillLabelsAndCounts(resultDTO, "First period", reportedBlockingDAO.getReportedBlockingComparison(fromFirstPeriod, toFirstPeriod, countryId, operatorId));
		} else {
			 // comparison by two years and months
            LocalDate initialDateFirstPeriod = LocalDate.of(forYearFirstPeriod, forMonthFirstPeriod, 1);
            LocalDate fromFirstPeriod = initialDateFirstPeriod.with(firstDayOfMonth());
            LocalDate toFirstPeriod = initialDateFirstPeriod.with(lastDayOfMonth());
            fillLabelsAndCounts(resultDTO, "First period", reportedBlockingDAO.getReportedBlockingComparison(fromFirstPeriod, toFirstPeriod, countryId, operatorId));
		}
		
		if(forMonthSecondPeriod == 0) {
			// Comparison by two years
			LocalDate initialDateSecondPeriod = LocalDate.ofYearDay(forYearSecondPeriod, 1);
            LocalDate fromSecondPeriod = initialDateSecondPeriod.with(firstDayOfYear());
            LocalDate toSecondPeriod = initialDateSecondPeriod.with(lastDayOfYear());
            fillLabelsAndCounts(resultDTO, "Second period", reportedBlockingDAO.getReportedBlockingComparison(fromSecondPeriod, toSecondPeriod, countryId, operatorId));
		} else {
			 // comparison by two years and months
			LocalDate initialDateSecondPeriod = LocalDate.of(forYearSecondPeriod, forMonthSecondPeriod, 1);
            LocalDate fromSecondPeriod = initialDateSecondPeriod.with(firstDayOfMonth());
            LocalDate toSecondPeriod = initialDateSecondPeriod.with(lastDayOfMonth());
            fillLabelsAndCounts(resultDTO, "Second period", reportedBlockingDAO.getReportedBlockingComparison(fromSecondPeriod, toSecondPeriod, countryId, operatorId));
		}
		
		return resultDTO;
	}
	
	private ReportLineChartDTO generateDTO(Integer fromMonth, Integer fromYear, Integer toMonth, Integer toYear, List<Object[]> resultList) {
		LocalDate from	= LocalDate.of(fromYear, fromMonth, 1);
		LocalDate to	= LocalDate.of(toYear, toMonth, 10);
		ReportLineChartDTO resultDTO = new ReportLineChartDTO((int) ChronoUnit.MONTHS.between(from, to) + 1);
		
		while (from.isBefore(to)) {
			String label = from.format(DateTimeFormatter.ofPattern("YYYY-MM"));
			long count = 0;
			for (Object[] row : resultList) {
				if (label.equals((String) row[0])) {
					count = ((BigInteger) row[1]).longValue();
					break;
				}
			}
			resultDTO.getLabels().add(label);
			resultDTO.getCounts().add(count);
			from = from.plus(1, ChronoUnit.MONTHS);
		}
		
		return resultDTO;
	}

}
