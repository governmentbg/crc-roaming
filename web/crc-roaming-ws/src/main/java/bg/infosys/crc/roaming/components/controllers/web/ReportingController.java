package bg.infosys.crc.roaming.components.controllers.web;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bg.infosys.crc.roaming.dto.web.reports.RegisteredUserRerportDTO;
import bg.infosys.crc.roaming.dto.web.reports.ReportLineChartDTO;
import bg.infosys.crc.roaming.dto.web.reports.ReportedBlockingDTO;
import bg.infosys.crc.roaming.dto.web.reports.ReportedRoamingDTO;
import bg.infosys.crc.roaming.dto.web.reports.TripleChartReportDTO;
import bg.infosys.crc.roaming.helpers.web.PagingResultDTO;
import bg.infosys.crc.roaming.helpers.web.Permissions;
import bg.infosys.crc.roaming.services.web.ReportService;

@RestController
@RequestMapping(value = "/api/web/reports")
public class ReportingController {
	@Autowired private ReportService reportService;

	@Secured(Permissions.VIEW_REPORTS)
	@GetMapping(value = "/registered-users")
	public RegisteredUserRerportDTO getRegisteredUsers(
			@RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam("toDate")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		
		return reportService.getNumberOfRegisteredUsers(from, to);
	}
	
	@Secured(Permissions.VIEW_REPORTS)
	@GetMapping(value = "/roamings")
	public PagingResultDTO<ReportedRoamingDTO> getAllReportedRoamings(
			@RequestParam Integer page, 
			@RequestParam Integer size,
			@RequestParam(defaultValue = "id") String sortBy, 
			@RequestParam(defaultValue = "desc") String sortDirection,
			@RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam("toDate")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(required = false) Integer countryId,
			@RequestParam(required = false) Integer operatorId,
			@RequestParam(required = false) Integer regionId,
			@RequestParam(required = false) boolean getAll) {
		
		return reportService.getAllReportedRoamings(page, size, sortBy, sortDirection, from, to, countryId, operatorId, regionId, getAll);
	}
	
	@Secured(Permissions.VIEW_REPORTS)
	@GetMapping(value = "/roamings-count")
	public long countAllReportedRoamings(
			@RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam("toDate")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(required = false) Integer countryId,
			@RequestParam(required = false) Integer operatorId,
			@RequestParam(required = false) Integer regionId) {

		return reportService.countAllReportedRoamings(from, to, countryId, operatorId, regionId);
	}
	
	@Secured(Permissions.VIEW_REPORTS)
	@GetMapping(value = "/roamings-charts")
	public TripleChartReportDTO getReportedRoamingsChart(
			@RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam("toDate")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return reportService.getReportedRoamingsChart(from, to);
	}
	
	@Secured(Permissions.VIEW_REPORTS)
	@GetMapping(value = "/roamings-linear")
	public ReportLineChartDTO getReportLineChart(
			@RequestParam Integer fromMonth, @RequestParam Integer fromYear, @RequestParam Integer toMonth, @RequestParam Integer toYear,
			@RequestParam(required = false) Integer countryId,
			@RequestParam(required = false) Integer operatorId,
			@RequestParam(required = false) Integer regionId) {
		
		return reportService.getReportedRoamingLineChartData(fromMonth, fromYear, toMonth, toYear, countryId, operatorId, regionId);
	}
	
	
	@Secured(Permissions.VIEW_REPORTS)
	@GetMapping(value = "/roamings-comparison")
	public ReportLineChartDTO getReportComparisonChart(
			@RequestParam(required = false) Integer forMonthFirstPeriod, @RequestParam Integer forYearFirstPeriod, 
			@RequestParam(required = false) Integer forMonthSecondPeriod, @RequestParam Integer forYearSecondPeriod,
			@RequestParam(required = false) Integer countryId,
			@RequestParam(required = false) Integer operatorId,
			@RequestParam(required = false) Integer regionId) {
		
		return reportService.getReportComparisonChart(forMonthFirstPeriod, forYearFirstPeriod, forMonthSecondPeriod, forYearSecondPeriod, countryId, operatorId, regionId);
	}
	
	@Secured(Permissions.VIEW_REPORTS)
	@GetMapping(value = "/blockings")
	public PagingResultDTO<ReportedBlockingDTO> getAllReportedBlockings(
			@RequestParam Integer page, 
			@RequestParam Integer size,
			@RequestParam(defaultValue = "id") String sortBy, 
			@RequestParam(defaultValue = "desc") String sortDirection,
			@RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam("toDate")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(required = false) Integer countryId,
			@RequestParam(required = false) Integer operatorId,
			@RequestParam(required = false) boolean getAll) {
		
		return reportService.getAllReportedBlockings(page, size, sortBy, sortDirection, from, to, countryId, operatorId, getAll);
	}
	
	@Secured(Permissions.VIEW_REPORTS)
	@GetMapping(value = "/blockings-count")
	public long countAllReportedBlockings(
			@RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam("toDate")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(required = false) Integer countryId,
			@RequestParam(required = false) Integer operatorId) {

		return reportService.countAllReportedBlockings(from, to, countryId, operatorId);
	}
	
	@Secured(Permissions.VIEW_REPORTS)
	@GetMapping(value = "/blockings-charts")
	public TripleChartReportDTO getReportedBlockingsDistributionChart(
			@RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam("toDate")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return reportService.getReportedBlockingsDistributionChart(from, to);
	}
	
	@Secured(Permissions.VIEW_REPORTS)
	@GetMapping(value = "/blockings-linear")
	public ReportLineChartDTO getReportedBlockingEvolutionChart(
			@RequestParam Integer fromMonth, @RequestParam Integer fromYear, @RequestParam Integer toMonth, @RequestParam Integer toYear,
			@RequestParam(required = false) Integer countryId,
			@RequestParam(required = false) Integer operatorId) {
		
		return reportService.getReportedBlockingEvolutionChartData(fromMonth, fromYear, toMonth, toYear, countryId, operatorId);
	}
	
	@Secured(Permissions.VIEW_REPORTS)
	@GetMapping(value = "/blockings-comparison")
	public ReportLineChartDTO getReportedBlockingsComparisonChart(
			@RequestParam(required = false) Integer forMonthFirstPeriod, @RequestParam Integer forYearFirstPeriod, 
			@RequestParam(required = false) Integer forMonthSecondPeriod, @RequestParam Integer forYearSecondPeriod,
			@RequestParam(required = false) Integer countryId,
			@RequestParam(required = false) Integer operatorId) {
		
		return reportService.getReportedBlockingsComparisonChart(forMonthFirstPeriod, forYearFirstPeriod, forMonthSecondPeriod, forYearSecondPeriod, countryId, operatorId);
	}
}
