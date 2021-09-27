package bg.infosys.crc.roaming.helpers.mobile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import bg.infosys.common.utils.GeoUtil;
import bg.infosys.common.ws.utils.HttpRequest;
import bg.infosys.crc.entities.pub.ReportedRoaming;
import bg.infosys.crc.roaming.components.Properties;
import bg.infosys.crc.roaming.dto.IPInfoResponseDTO;

public class Helper { private Helper() {}
	private static Logger LOGGER = LoggerFactory.getLogger(Helper.class);
	
	private static final String[] IP_HEADER_CANDIDATES = {
		"X-Real-IP",
		"X-Forwarded-For",
		"Proxy-Client-IP",
		"WL-Proxy-Client-IP",
		"HTTP_X_FORWARDED_FOR",
		"HTTP_X_FORWARDED",
		"HTTP_X_CLUSTER_CLIENT_IP",
		"HTTP_CLIENT_IP",
		"HTTP_FORWARDED_FOR",
		"HTTP_FORWARDED",
		"HTTP_VIA",
		"REMOTE_ADDR"
	};
	
	public static String getClientIP() {
		if (RequestContextHolder.getRequestAttributes() == null) {
			return "0.0.0.0";
		}

		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		for (String header : IP_HEADER_CANDIDATES) {
			String ipList = request.getHeader(header);
			if (ipList != null && ipList.length() != 0 && !"unknown".equalsIgnoreCase(ipList)) {
				String ip = ipList.split(",")[0];
				return ip;
			}
		}

		return request.getRemoteAddr();
	}
	
	public static IPInfoResponseDTO getIPData(String ip) {
		StringBuilder sb = new StringBuilder();
		sb.append("https://ipinfo.io/").append(ip);
		sb.append("/json").append("?token=").append(Properties.IPINFO_TOKEN);
		
		HttpRequest rq = new HttpRequest();
		for (int i = 1; i <= 3; i++) {
			try {
				return rq.doGet(sb.toString(), null, IPInfoResponseDTO.class).getBody();
			} catch (Exception e) {
				LOGGER.error("Try #" + i, e);
			}
		}
		
		return null;
	}
	
	public static String generatePassword(int length) {
		String letters = "abcdefghijklmnopqrstuvwxyz";
		String availableChars = letters + letters.toUpperCase() + "1234567890";
        
        Random random = new Random();        
        StringBuilder pwd = new StringBuilder();
        while (pwd.length() < length) { // length of the random string
            int index = (int) (random.nextFloat() * availableChars.length());
            pwd.append(availableChars.charAt(index));
        }
        
        return pwd.toString();
	}
	
//	public static String getPath(HttpServletRequest request) {
//		StringBuilder sb = new StringBuilder();
//		sb.append(request.getScheme()).append("://");
//		sb.append(request.getServerName()).append(":").append(request.getServerPort());
//		sb.append(request.getContextPath());
//		
//		return sb.toString();
//	}
	
	public static boolean okayToSave(ReportedRoaming rr, List<ReportedRoaming> checkList, int minDistance, int minTime) {
		if (checkList == null || checkList.size() == 0) {
			return true;
		}
		
		if (rr.getLatitude() == null || rr.getLatitude() == 0
		|| rr.getLongitude() == null || rr.getLongitude() == 0) {
			return false;
		}
		
		for (ReportedRoaming prev : checkList) {
			double distance = GeoUtil.distance(prev.getLatitude(), prev.getLongitude(), rr.getLatitude(), rr.getLongitude());
			if (!(distance >= minDistance || rr.getEventTs().isAfter(prev.getEventTs().plusMinutes(minTime)))) {
				return false;
			}
		}
		
		return true;
	}
	
	public static double getDistance(double fromLat, double fromLng, double toLat, double toLng) {
		double latDistance = Math.toRadians(fromLat - toLat);
	    double lonDistance = Math.toRadians(fromLng - toLng);
	    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
	            + Math.cos(Math.toRadians(fromLat)) * Math.cos(Math.toRadians(toLat))
	            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	    
	    // 6371 is the Earth's radius in km
	    return BigDecimal.valueOf(6371_000 * c).setScale(2, RoundingMode.HALF_UP).doubleValue();
	}
	
	public static Double correctCoordScale(Double val) {
		if (val == null) return null;
		
		BigDecimal bd = BigDecimal.valueOf(val);
		bd = bd.setScale(6, RoundingMode.HALF_UP);
		
		return bd.doubleValue();
	}
	
	public static void randomizeCoordinates(ReportedRoaming rr) {
		int min = 10;
		int max = 99;
		Random r = new Random();
		
		String lat = rr.getLatitude().toString();
		String lng = rr.getLongitude().toString();
		
		lat = lat.substring(0, lat.length() - 2);
		lng = lng.substring(0, lng.length() - 2);
		
		lat += Integer.toString(r.nextInt(max - min) + min);
		lng += Integer.toString(r.nextInt(max - min) + min);
		
		rr.setLatitude(Double.parseDouble(lat));
		rr.setLongitude(Double.parseDouble(lng));
	}
	
	public static LocalDateTime getTime(Long epochSeconds) {
		if (epochSeconds == null) {
			return LocalDateTime.now();
		}
		
		Long epochMilis = epochSeconds * 1000;
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilis), ZoneId.systemDefault());
	}
}
