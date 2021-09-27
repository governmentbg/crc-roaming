package bg.infosys.crc.roaming.workers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bg.infosys.common.mail.Email;
import bg.infosys.common.mail.Mailer;
import bg.infosys.crc.roaming.components.Properties;

public class SendMailTask implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(SendMailTask.class);
	private Email email;
	
	public SendMailTask(Email email) {
		this.email = email;
	}
	
	@Override
	public void run() {
		try { // Let's don't screw up the executor, okay?
			Mailer.sendMail(email, Properties.APP_CONF);
		} catch (Exception e) {
			LOGGER.error("Error on async e-mail sending", e);
		}
	}

}
