package uk.ac.warwick.dcs.boss.model.mail.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import uk.ac.warwick.dcs.boss.model.mail.IMailSender;
import uk.ac.warwick.dcs.boss.model.mail.MailException;

public class ThreadedMailSender implements IMailSender {

	private ExecutorService executor;
	private final String mailProtocol;
	private final String mailHost;
	private final String mailUsername;
	private final String mailPassword;
	private final String mailFromAddress;
	
	public ThreadedMailSender(ExecutorService executor,
			String mailProtocol, String mailHost, String mailUsername, String mailPassword, String mailFromAddress) throws MailException {
		this.executor = executor;
		this.mailProtocol = mailProtocol;
		this.mailHost = mailHost;
		this.mailUsername = mailUsername;
		this.mailPassword = mailPassword;
		this.mailFromAddress = mailFromAddress;
	}
	
	public void sendMail(String recipient, String subject, String message)
			throws MailException {
		Worker worker = new Worker(
				mailProtocol, mailHost, mailUsername, mailPassword, mailFromAddress,
				recipient, subject, message
		);
		this.executor.execute(worker);		
	}
	
	public void cleanUp() {
		Logger logger = Logger.getLogger("mail");
		try {
			logger.log(Level.WARN, "Waiting 5 seconds for mails to be sent.");
			if (!this.executor.awaitTermination(5, TimeUnit.SECONDS)) {
				logger.log(Level.WARN, "Wait interrupted, killing anyway.");
			}
		} catch (InterruptedException e) {
			logger.log(Level.WARN, "Wait interrupted, killing anyway.");
		}
		
		this.executor.shutdownNow();
	}
}
