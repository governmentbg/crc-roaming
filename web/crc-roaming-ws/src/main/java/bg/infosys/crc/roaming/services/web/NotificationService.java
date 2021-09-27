package bg.infosys.crc.roaming.services.web;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bg.infosys.common.db.helpers.ResultFilter;
import bg.infosys.common.exceptions.ResponseStatusException;
import bg.infosys.common.ws.security.SecuritySession;
import bg.infosys.crc.dao.pub.NotificationDAO;
import bg.infosys.crc.dao.web.security.UserDAO;
import bg.infosys.crc.entities.pub.Notification;
import bg.infosys.crc.roaming.components.Scheduler;
import bg.infosys.crc.roaming.dto.web.notifications.AddNotificationDTO;
import bg.infosys.crc.roaming.dto.web.notifications.ListNotificationDTO;
import bg.infosys.crc.roaming.helpers.web.PagingResultDTO;
import bg.infosys.crc.roaming.workers.SendMobileNotification;

@Service
public class NotificationService {
	@Autowired private BackgroundService	backgroundService;
	@Autowired private NotificationDAO		notificationDAO;
	@Autowired private UserDAO				userDAO;
	@Autowired private Scheduler			scheduler;
	
	public PagingResultDTO<ListNotificationDTO> getAllNotifications(Integer page, Integer pageSize) {
		List<ListNotificationDTO> notifications = notificationDAO.findAllPaged(ResultFilter.firstResult(page, pageSize), pageSize)
				.stream().map(n -> new ListNotificationDTO(n)).collect(Collectors.toList());
		return new PagingResultDTO<>(notifications, page, pageSize);
	}
	
	public long countNotifications() {
		return notificationDAO.count();
	}
	
	@Transactional
	public void send(Integer notificationId) {
		Notification n = notificationDAO.findById(notificationId);
		if (n == null) {
			throw new ResponseStatusException(HttpServletResponse.SC_GONE, "The notification does not exist", "notificationDoesNotExist");
		} else if (Boolean.TRUE.equals(n.getSent())) {
			throw new ResponseStatusException(HttpServletResponse.SC_CONFLICT, "Notification has been sent", "notificationHasBeenSent");
		}
		
		n.setSent(Boolean.TRUE);
		notificationDAO.save(n);
		
		scheduler.addTask(new SendMobileNotification(backgroundService, n));
	}
	
	@Transactional
	public void save(AddNotificationDTO notification) {
		Notification n = notification.toEntity();
		n.setCreatedAt(LocalDateTime.now());
		n.setCreatedBy(userDAO.findById(SecuritySession.getUserId()));
		n.setSent(Boolean.FALSE);
		notificationDAO.save(n);
	}
	
}
