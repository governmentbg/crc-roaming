package bg.infosys.crc.roaming.services.mobile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import bg.infosys.crc.dao.mobile.security.ActiveUserDAO;
import bg.infosys.crc.dao.mobile.security.MobileUserDAO;
import bg.infosys.crc.dao.pub.CountryDAO;
import bg.infosys.crc.dao.pub.HighRiskZoneDAO;
import bg.infosys.crc.dao.pub.IpDataDAO;
import bg.infosys.crc.dao.pub.OperatorDAO;
import bg.infosys.crc.dao.pub.ReportedBlockingDAO;
import bg.infosys.crc.dao.pub.ReportedRoamingDAO;
import bg.infosys.crc.dao.pub.SettingDAO;
import bg.infosys.crc.dao.pub.SourceOperatorDAO;
import bg.infosys.crc.dao.web.security.UserDAO;
import bg.infosys.crc.entities.mobile.security.ActiveUser;
import bg.infosys.crc.entities.mobile.security.MobileUser;
import bg.infosys.crc.entities.pub.Country;
import bg.infosys.crc.entities.pub.HighRiskZone;
import bg.infosys.crc.entities.pub.IpData;
import bg.infosys.crc.entities.pub.OSEnum;
import bg.infosys.crc.entities.pub.Operator;
import bg.infosys.crc.entities.pub.ReportedBlocking;
import bg.infosys.crc.entities.pub.ReportedRoaming;
import bg.infosys.crc.entities.pub.Setting;
import bg.infosys.crc.entities.pub.SettingEnum;
import bg.infosys.crc.entities.pub.SourceOperator;
import bg.infosys.crc.entities.web.security.User;
import bg.infosys.crc.roaming.components.Properties;
import bg.infosys.crc.roaming.components.Scheduler;
import bg.infosys.crc.roaming.dto.IPInfoResponseDTO;
import bg.infosys.crc.roaming.dto.mobile.CountryDTO;
import bg.infosys.crc.roaming.dto.mobile.MobileRequestDTO;
import bg.infosys.crc.roaming.dto.mobile.MobileResponseDTO;
import bg.infosys.crc.roaming.dto.mobile.OperatorDTO;
import bg.infosys.crc.roaming.dto.mobile.PointDTO;
import bg.infosys.crc.roaming.dto.mobile.ReportedRoamingDTO;
import bg.infosys.crc.roaming.dto.mobile.ReportedZoneDTO;
import bg.infosys.crc.roaming.dto.mobile.ZoneDTO;
import bg.infosys.crc.roaming.helpers.mobile.Helper;
import bg.infosys.crc.roaming.helpers.mobile.MobileErrorCodeEnum;
import bg.infosys.crc.roaming.services.web.BackgroundService;
import bg.infosys.crc.roaming.workers.SetProvinceTask;

@Service
public class MobileOperationsService {
	private static final Logger LOGGER = LoggerFactory.getLogger(MobileOperationsService.class);
	
	@Autowired private BackgroundService	backgroundService;
	@Autowired private Scheduler			scheduler;
	
	@Autowired private ActiveUserDAO		activeUserDAO;
	@Autowired private CountryDAO			countryDAO;
	@Autowired private HighRiskZoneDAO		highRiskZoneDAO;
	@Autowired private IpDataDAO			ipDataDAO;
	@Autowired private MobileUserDAO		mobileUserDAO;
	@Autowired private OperatorDAO			operatorDAO;
	@Autowired private ReportedBlockingDAO	reportedBlockingDAO;
	@Autowired private ReportedRoamingDAO	reportedRoamingDAO;
	@Autowired private SettingDAO			settingDAO;
	@Autowired private SourceOperatorDAO	sourceOperatorDAO;
	@Autowired private UserDAO				userDAO;
	
	@Transactional
	public synchronized MobileResponseDTO reportRoaming(MobileRequestDTO params) {
		PointDTO[] roamingPoints = new Gson().fromJson(params.getZ(), PointDTO[].class);
		if (roamingPoints == null || roamingPoints.length == 0) {
			return new MobileResponseDTO(MobileErrorCodeEnum.MISSING_REQUIRED_PARAM);
		}
		
		String srcOpName = params.getSrcTelecomName();
		String srcMnc	 = params.getSrcTelecomMnc();
		String srcMcc	 = params.getSrcTelecomMcc();
		SourceOperator src = null;
		if (srcOpName != null && srcMnc != null && srcMcc != null) {
			src = getOrCreateSrcOperator(params.getSrcTelecomName(), Short.parseShort(srcMnc), Short.parseShort(srcMcc));
		}
		
		List<ReportedRoaming> reportedRoamings = new ArrayList<>();
		MobileUser mobUser = mobileUserDAO.findById(params.getId());
		
		int minDistance = Integer.parseInt(Properties.get("app.roamingReportMinDistance"));
		int minTime		= Integer.parseInt(Properties.get("app.roamingReportMinTime"));
		List<ReportedRoaming> checkList = reportedRoamingDAO.getPrev(params.getId());
		OSEnum deviceOS = activeUserDAO.findById(params.getId()).getDeviceOS();
		Setting emails = settingDAO.findById(SettingEnum.HIDE_ROAMINGS_EMAILS);
		boolean hide = emails != null && emails.getValue() != null
			&& emails.getValue().toLowerCase().contains(mobUser.getEmail().toLowerCase());
		
		Operator operator = null;
		for (PointDTO rp : roamingPoints) {
			String mnc = rp.getMnc();
			if (mnc.length() == 1) {
				mnc = "0" + mnc;
			}
			
			Short mcc = Short.parseShort(rp.getMcc());
			if (operator == null || !operator.getMnc().equals(mnc)) {
				operator = getOrCreateOperator(mnc, mcc);
			}
			
			LocalDateTime eventTs = Helper.getTime(rp.getTs());
			ReportedRoaming rr = new ReportedRoaming();
			rr.setLatitude(Helper.correctCoordScale(rp.getVa()[0]));
			rr.setLongitude(Helper.correctCoordScale(rp.getVa()[1]));
			rr.setEventTs(eventTs);
			
			if (Helper.okayToSave(rr, checkList, minDistance, minTime)) {
				rr.setOperator(operator);
				rr.setUser(mobUser);
				rr.setSourceOperator(src);
				rr.setRealDetection(true);
				rr.setHidden(hide);
				rr.setOs(deviceOS);
				reportedRoamings.add(rr);
				checkList.add(0, rr);
			}
			
			ReportedRoaming rr2 = new ReportedRoaming();
			rr2.setEventTs(eventTs);
			rr2.setLatitude(Helper.correctCoordScale(rp.getVb()[0]));
			rr2.setLongitude(Helper.correctCoordScale(rp.getVb()[1]));
			
			if (Helper.okayToSave(rr2, checkList, minDistance, minTime)) {
				rr2.setOperator(operator);
				rr2.setUser(mobUser);
				rr2.setSourceOperator(src);
				rr2.setRealDetection(true);
				rr2.setHidden(hide);
				rr.setOs(deviceOS);
				reportedRoamings.add(rr2);
				checkList.add(0, rr2);
			}
		}
		
		if (deviceOS == null || deviceOS == OSEnum.IOS) { // Randomize coordinates
			for (ReportedRoaming rr : reportedRoamings) {
				Helper.randomizeCoordinates(rr);
				rr.setRealDetection(false);
			}			
		}
		
		reportedRoamingDAO.save(reportedRoamings);
		reportedRoamingDAO.flushAndClear();
		
		for (ReportedRoaming rr : reportedRoamings) {
			scheduler.addTask(new SetProvinceTask(rr, backgroundService));
		}
	
		return MobileResponseDTO.ok();
	}
	
	@Transactional
	public MobileResponseDTO reportBlocking(MobileRequestDTO params) {
		String mnc;
		short mcc;
		try {
			mnc = params.getMnc();
			if (mnc.length() == 1) {
				mnc = "0" + mnc;
			}
			mcc = Short.parseShort(params.getMcc());
		} catch (Exception e) {
			return new MobileResponseDTO(MobileErrorCodeEnum.MISSING_REQUIRED_PARAM);
		}
		
		Operator operator = getOrCreateOperator(mnc, mcc);
		MobileUser mobUser = mobileUserDAO.findById(params.getId());
		
		ReportedBlocking rb = new ReportedBlocking();
		rb.setUser(mobUser);
		rb.setEventTs(Helper.getTime(params.getTs()));
		rb.setOperator(operator);
		reportedBlockingDAO.save(rb);
		
		return MobileResponseDTO.ok();
	}
	
	private Operator getOrCreateOperator(String mnc, Short mcc) {
		Operator operator = operatorDAO.findByMNCandMCC(mnc, mcc);
		if (operator == null) {
			User user = userDAO.findById(Properties.SYS_USER_ID);
			Country country = countryDAO.findByMCC(mcc);
			if (country == null) {
				country = new Country();
				country.setCreatedAt(LocalDateTime.now());
				country.setCreatedBy(user);
				country.setMcc(mcc);
				country.setNameInt(Properties.SYS_AUTO_NAME);
				country.setNameBg(Properties.SYS_AUTO_NAME);
				country.setEuMember(false);
				country.setPhoneCode("000");
				countryDAO.save(country);
			}
			
			operator = new Operator();
			operator.setCountry(country);
			operator.setMnc(mnc);
			operator.setCreatedAt(LocalDateTime.now());
			operator.setCreatedBy(user);
			operator.setName(Properties.SYS_AUTO_NAME);
			operatorDAO.save(operator);
		}
		
		return operator;
	}
	
	private SourceOperator getOrCreateSrcOperator(String name, Short mnc, Short mcc) {
		SourceOperator src = sourceOperatorDAO.find(name, mnc, mcc);
		if (src == null) {
			src = new SourceOperator();
			src.setMcc(mcc);
			src.setMnc(mnc);
			src.setName(name);
			sourceOperatorDAO.save(src);
		}
		
		return src;
	}
	
	public MobileResponseDTO getOperators() {
		MobileResponseDTO resp = new MobileResponseDTO();
		resp.setError(MobileErrorCodeEnum.OK);
		
		List<Operator> operators = operatorDAO.findAll("id", false);
		if (operators.size() == 0) {
			resp.setCountries(new ArrayList<CountryDTO>());
			return resp;
		}
		
		Map<Country, List<OperatorDTO>> ctMap = new HashMap<>();
		for (Operator operator : operators) {
			Country c = operator.getCountry();
			if (!ctMap.containsKey(c)) {
				ctMap.put(c, new ArrayList<>());
			}
			
			ctMap.get(c).add(new OperatorDTO(operator));
		}
		
		List<CountryDTO> countryDtoList = new ArrayList<>(10);
		ctMap.forEach((k, v) -> {
			Collections.sort(v);
			countryDtoList.add(new CountryDTO(k, v));
		});

		Collections.sort(countryDtoList);
		resp.setCountries(countryDtoList);
		return resp;
	}
		
	public MobileResponseDTO getReportedHistory(MobileRequestDTO params) {
		if (params.getId() == null) {
			return new MobileResponseDTO(MobileErrorCodeEnum.MISSING_REQUIRED_PARAM);
		}
		
		List<ReportedRoaming> reports = reportedRoamingDAO.getReportedRoamingsByUserId(params.getId());
		List<ReportedZoneDTO> result = new ArrayList<>();
		for (ReportedRoaming rr : reports) {
			List<Double> vertex = new ArrayList<>(2);
			vertex.add(rr.getLatitude());
			vertex.add(rr.getLongitude());
			
			ReportedZoneDTO dto = new ReportedZoneDTO();
			dto.setVa(vertex);
			dto.setVb(vertex);
			dto.setMnc(rr.getOperator().getMnc().toString());
			dto.setMcc(rr.getOperator().getCountry().getMcc().toString());
			dto.setTs(rr.getEventTs().toEpochSecond(ZoneOffset.UTC));
			
			result.add(dto);
		}
		
		MobileResponseDTO resp = new MobileResponseDTO();
		resp.setZ(result);
		resp.setError(MobileErrorCodeEnum.OK);
		return resp;
	}
	
	public MobileResponseDTO getRoamingZones() {
		MobileResponseDTO resp = new MobileResponseDTO();
		resp.setError(MobileErrorCodeEnum.OK);

		List<HighRiskZone> zones = highRiskZoneDAO.findAll();
		if (zones.size() > 0) {
			List<ZoneDTO> zoneDtoList = new ArrayList<>(zones.size());
			for (HighRiskZone dbZone : zones) {
				Double[][] coordinates = dbZone.getCoordinates();
				double[] lat = new double[coordinates.length];
				double[] lng = new double[coordinates.length];
				int iMaxLat = 0, iMinLat = 0, iMaxLng = 0, iMinLng = 0;
				for (int i = 0; i < dbZone.getCoordinates().length; i++) {
					Double[] lngLat = coordinates[i];
					lat[i] = lngLat[1];
					lng[i] = lngLat[0];
					
					if (lat[iMaxLat] < lat[i]) iMaxLat = i;
					if (lat[iMinLat] > lat[i]) iMinLat = i;
					if (lng[iMaxLng] < lng[i]) iMaxLng = i;
					if (lng[iMinLng] > lng[i]) iMinLng = i;
				}
				
				double centerLat = BigDecimal.valueOf(lat[iMaxLat]).add(BigDecimal.valueOf(lat[iMinLat]))
						.divide(BigDecimal.valueOf(2), 6, RoundingMode.HALF_UP).doubleValue();
				double centerLng = BigDecimal.valueOf(lng[iMaxLng]).add(BigDecimal.valueOf(lng[iMinLng]))
						.divide(BigDecimal.valueOf(2), 6, RoundingMode.HALF_UP).doubleValue();
				
				double radius1 = Helper.getDistance(centerLat, centerLng, lat[iMaxLat], lng[iMaxLat]);
				double radius2 = Helper.getDistance(centerLat, centerLng, lat[iMaxLng], lng[iMaxLng]);
				
				ZoneDTO zoneDto = new ZoneDTO();
				zoneDto.setLat(lat);
				zoneDto.setLng(lng);
				zoneDto.setCenterLat(centerLat);
				zoneDto.setCenterLng(centerLng);
				zoneDto.setRadius(radius1 > radius2 ? radius1 : radius2);
				
				zoneDtoList.add(zoneDto);
			}
			resp.setZones(zoneDtoList);
		}
		
		return resp;
	}
	
	
	public Double convertToDouble(String value, Double defaultValue) {
		return Double.parseDouble(value.replace(",", "."));
	}
	
	public MobileResponseDTO getReportedRoamings() {
		Setting roamingAge = settingDAO.findById(SettingEnum.ROAMGINS_AGE);
		LocalDate since = LocalDate.now().minus(Integer.parseInt(roamingAge.getValue()), ChronoUnit.MONTHS);
		Integer roamingRadius = Integer.parseInt(settingDAO.findById(SettingEnum.ROAMING_RADIUS).getValue());
		List<ReportedRoaming> rrList = reportedRoamingDAO.findAllRealSince(since);
		
		Set<ReportedRoamingDTO> rrDtoList = new HashSet<>(rrList.size());
		rrList.forEach(rr -> {
			rrDtoList.add(new ReportedRoamingDTO(rr));
		});
		
		MobileResponseDTO resp = new MobileResponseDTO();
		resp.setError(MobileErrorCodeEnum.OK);
		resp.setRoamingRadius(roamingRadius);
		resp.setReportedRoamings(rrDtoList);
		
		return resp;
	}
	
	

	@Transactional
	public synchronized MobileResponseDTO omgRoaming(MobileRequestDTO params) {
		final String YES = "yes", NO = "no", BG = "bg";
		if (params.getPointId() == null) {
			return new MobileResponseDTO(MobileErrorCodeEnum.MISSING_REQUIRED_PARAM);
		}
		
		ReportedRoaming rr = reportedRoamingDAO.findById(params.getPointId());
		if (rr == null) {
			return new MobileResponseDTO(MobileErrorCodeEnum.MISSING_REQUIRED_PARAM);
		}
		
		String result = NO;
		String ip = Helper.getClientIP();
		if (ip != null) {
			LOGGER.info("OMG Roaming: IP found: {}; User ID: {}; Point ID: {}", ip, params.getId(), params.getPointId());
			IpData ipData = ipDataDAO.findById(ip);
			if (ipData == null) {
				IPInfoResponseDTO ipInfo = Helper.getIPData(ip);
				if (ipInfo != null) {
					LOGGER.info("OMG Roaming: IP info found: {}", ipInfo.toString());
					ipData = new IpData();
					ipData.setIp(ip);
					ipData.setCountry(ipInfo.getCountry());
					ipData.setLookupTs(LocalDateTime.now());
					ipData.setLookupResponse(ipInfo.toString());
					ipData.setUserId(params.getId());
					ipDataDAO.save(ipData);
				}
			}
			
			if (!BG.equalsIgnoreCase(ipData.getCountry())) {
				result = YES;
			}
		}
		
		result = YES;
		
		ActiveUser au = activeUserDAO.findById(params.getId());
		if (YES.equals(result)) {
			reportedRoamingDAO.detach(rr);
			rr.setId(null);
			rr.setEventTs(LocalDateTime.now());
			int minDistance = Integer.parseInt(Properties.get("app.roamingReportMinDistance"));
			int minTime		= Integer.parseInt(Properties.get("app.roamingReportMinTime"));
			List<ReportedRoaming> checkList = reportedRoamingDAO.getPrev(params.getId());
			if (Boolean.FALSE.equals(au.getOmgRoaming()) || Helper.okayToSave(rr, checkList, minDistance, minTime)) {
				au.setOmgRoaming(true);
				activeUserDAO.update(au);
				
				MobileUser mobUser = mobileUserDAO.findById(params.getId());
				Setting emails = settingDAO.findById(SettingEnum.HIDE_ROAMINGS_EMAILS);
				boolean hide = emails != null && emails.getValue() != null
					&& emails.getValue().toLowerCase().contains(mobUser.getEmail().toLowerCase());
				
				reportedRoamingDAO.detach(rr);
				rr.setId(null);
				rr.setUser(mobUser);
				rr.setRealDetection(true);
				rr.setHidden(hide);
				rr.setOs(OSEnum.IOS);
				Helper.randomizeCoordinates(rr);
				reportedRoamingDAO.save(rr);
			}
		} else if (Boolean.TRUE.equals(au.getOmgRoaming())) {
			au.setOmgRoaming(false);
			activeUserDAO.update(au);
		}
		
		MobileResponseDTO resp = new MobileResponseDTO();
		resp.setError(MobileErrorCodeEnum.OK);
		resp.setResult(result);
		
		return resp;
	}

}