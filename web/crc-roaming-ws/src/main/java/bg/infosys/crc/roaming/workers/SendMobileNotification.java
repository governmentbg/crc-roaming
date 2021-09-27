package bg.infosys.crc.roaming.workers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import bg.infosys.crc.roaming.services.web.BackgroundService;

public class SendMobileNotification implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(SendMobileNotification.class);
	private static final String FIREBASE_APP_NAME = "roaming-app";
	private static final String[] TOPICS = { "android", "ios" };
	
	private BackgroundService service;
	private bg.infosys.crc.entities.pub.Notification notification;
	
	static {
		try (InputStream is = SendMobileNotification.class.getClassLoader().getResourceAsStream("firebase-service-account.json")) {
			FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(GoogleCredentials.fromStream(is))
					.build();
			FirebaseApp.initializeApp(options, FIREBASE_APP_NAME);
		} catch (Exception e) {
			LOGGER.error("Error on firebase application initialization", e);
		}
	}
	
	public SendMobileNotification(BackgroundService service, bg.infosys.crc.entities.pub.Notification notification) {
		this.service = service;
		this.notification = notification;
	}
	
	@Override
	public void run() {
		try { // Let's don't screw up the executor, okay?
			List<Message> messages = new ArrayList<>(TOPICS.length);
			for (String topic : TOPICS) {
				messages.add(Message.builder()
					.setNotification(Notification.builder()
						.setTitle(notification.getSubject())
						.setBody(notification.getBody()).build())
					.setTopic(topic)
					.build());
			}
			
			FirebaseMessaging.getInstance(FirebaseApp.getInstance(FIREBASE_APP_NAME)).sendAll(messages);
			LOGGER.info("Notification to topics {} was sent. Subject: {} | Body: {}",
					TOPICS, notification.getSubject(), notification.getBody());
			
			service.updateSentAt(notification.getId());
		} catch (Exception e) {
			LOGGER.error("Error on set province task execution.", e);
		}
	}

}
