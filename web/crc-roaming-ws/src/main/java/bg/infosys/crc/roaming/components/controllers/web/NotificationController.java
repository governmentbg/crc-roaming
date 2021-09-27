package bg.infosys.crc.roaming.components.controllers.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import bg.infosys.crc.roaming.dto.web.notifications.AddNotificationDTO;
import bg.infosys.crc.roaming.dto.web.notifications.ListNotificationDTO;
import bg.infosys.crc.roaming.helpers.web.PagingResultDTO;
import bg.infosys.crc.roaming.helpers.web.Permissions;
import bg.infosys.crc.roaming.services.web.NotificationService;

@RestController
@RequestMapping(value = "/api/web/notifications")
public class NotificationController {
	@Autowired private NotificationService notificationService;

	@Secured(Permissions.SEND_NOTIFICATIONS)
	@GetMapping(value = "/all")
	public PagingResultDTO<ListNotificationDTO> getAllNotifications(@RequestParam Integer page, @RequestParam Integer size) {
		return notificationService.getAllNotifications(page, size);
	}
	
	@Secured(Permissions.SEND_NOTIFICATIONS)
	@GetMapping(value = "/count-all")
	public long countNotifications() {
		return notificationService.countNotifications();
	}
	
	@Secured(Permissions.SEND_NOTIFICATIONS)
	@PostMapping(value = "/add")
	public void addNotification(@RequestBody AddNotificationDTO notification) {
		notificationService.save(notification);
	}
	
	@Secured(Permissions.SEND_NOTIFICATIONS)
	@PostMapping(value = "/send/{id}")
	public void sendNotification(@PathVariable Integer id) {
		notificationService.send(id);
	}

}
