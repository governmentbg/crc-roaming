package bg.infosys.crc.roaming.services.web;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bg.infosys.common.ws.utils.HttpRequest;
import bg.infosys.crc.dao.pub.ReportedRoamingDAO;
import bg.infosys.crc.entities.pub.ReportedRoaming;
import bg.infosys.crc.roaming.components.Properties;
import bg.infosys.crc.roaming.dto.opendata.AddDataDTO;
import bg.infosys.crc.roaming.dto.opendata.AddResourceDTO;
import bg.infosys.crc.roaming.dto.opendata.AddResourceResponseDTO;
import bg.infosys.crc.roaming.dto.opendata.BasicResponseDTO;

@Service
public class OpenDataService {
	private static final Logger LOGGER = LoggerFactory.getLogger("cronLogger");
	@Autowired private ReportedRoamingDAO reportedRoamingDAO;
	
	public void generateOpenData() {
		LocalDateTime from = LocalDate.now().minusDays(7).atStartOfDay();
		LocalDateTime to = LocalDate.now().minusDays(1).atTime(LocalTime.MAX);
		
		String resourceUri = null;
		final int possibleTries = 5;
		int triesLeft = possibleTries;
		while (true) {
			resourceUri = createResource(from, to);
			if (resourceUri != null || --triesLeft == 0) break;
			
			LOGGER.error("Coundn't create resource. Tries left: {}", triesLeft);
			try {
				Thread.sleep(5 * 60 * 1000);
			} catch (InterruptedException e) {
				throw new RuntimeException("Interrupted", e);
			}
		}
		
		if (resourceUri == null) {
			LOGGER.error("Resource wasn't created and operation cannot continue.");
			return;
		}
		
		triesLeft = possibleTries;
		while (true) {
			boolean result = submitData(resourceUri, createHeader(), generateRows(from, to));
			if (result || --triesLeft == 0) break;
			
			LOGGER.error("Couldn't submit data. Tries left: {}", triesLeft);
			try {
				Thread.sleep(5 * 60 * 1000);
			} catch (InterruptedException e) {
				throw new RuntimeException("Interrupted", e);
			}
		}
	}
	
	private String createResource(LocalDateTime from, LocalDateTime to) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(Properties.get("opendata.dateTimeFormat"));
		String apiKey = Properties.get("opendata.apiKey");
		String datasetUri = Properties.get("opendata.datasetUri");
		String webAppUrl = Properties.get("webapp.url");
		String title = "Събития в периода: {1} - {2}"
				.replace("{1}", from.format(dtf))
				.replace("{2}", to.format(dtf));
		
		AddResourceDTO addResourceDto = new AddResourceDTO(apiKey, datasetUri, title, webAppUrl);
		
		try {
			LOGGER.info("Going to create resource: {}", title);
			HttpRequest rq = new HttpRequest();
			AddResourceResponseDTO resp = rq
				.doPost(addResourceDto, Properties.get("opendata.createResourceUrl"), null, AddResourceResponseDTO.class, true)
				.getBody();
			if (resp.getSuccess()) {
				LOGGER.info("Resource created: {}", resp.getData().getUri());
				return resp.getData().getUri();
			} else {
				LOGGER.error("Resource cannot be created. Error: {} | Detailed: {}", resp.getError(), resp.getErrorsAsString());
			}
		} catch (URISyntaxException | IOException e) {
			LOGGER.error("Error on open data resource creation.", e);
		}
		
		return null;
	}
	
	private String[] createHeader() {
		String[] header = {
			"Час и дата",
			"Държава",
			"MCC код",
			"Оператор",
			"MNC код",
			"Координати",
			"Район"
		};
		
		return header;
	}
	
	private List<String[]> generateRows(LocalDateTime from, LocalDateTime to) {
		List<ReportedRoaming> rrList = reportedRoamingDAO.findAll(from, to);
		List<String[]> rows = new ArrayList<>(rrList.size());
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(Properties.get("opendata.dateTimeFormat"));
		for (ReportedRoaming rr : rrList) {
			String[] row = {
				rr.getEventTs().format(dtf),
				rr.getOperator().getCountry().getNameBg(),
				rr.getOperator().getCountry().getMcc().toString(),
				rr.getOperator().getName(),
				rr.getOperator().getMnc().toString(),
				rr.getLatitude() + ", " + rr.getLongitude(),
				rr.getBgRegion().getName()
			};
			rows.add(row);
		}
		
		return rows;
	}
	
	private boolean submitData(String resourceUri, String[] header, List<String[]> rows) {
		String apiKey = Properties.get("opendata.apiKey");
		AddDataDTO addDto = new AddDataDTO(apiKey, resourceUri, header, rows);
		try {
			LOGGER.info("Going to submit resource data with {} rows", rows.size());
			HttpRequest rq = new HttpRequest();
			BasicResponseDTO resp = rq
				.doPost(addDto, Properties.get("opendata.addResourceDataUrl"), null, BasicResponseDTO.class, true)
				.getBody();
			if (resp.getSuccess()) {
				LOGGER.info("Resource data submitted successfully.");
				return true;
			} else {
				LOGGER.error("Resource data cannot be submitted: Error: {}", resp.getError());
			}
		} catch (URISyntaxException | IOException e) {
			LOGGER.error("Error on resource data submission.", e);
		}
		
		return false;
	}

}
