package uk.ac.warwick.dcs.boss.model.mail.impl;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Worker implements Runnable {	

	private final String mailProtocol;
	private final String mailHost;
	private final String mailUsername;
	private final String mailPassword;
	private final String mailFromAddress;
	
	private final String recipient;
	private final String subject;
	private final String message;
	

	public Worker(String mailProtocol, String mailHost, String mailUsername, String mailPassword, String mailFromAddress,
			String recipient, String subject, String message) {
		super();
		
		this.mailHost = mailHost;
		this.mailPassword = mailPassword;
		this.mailProtocol = mailProtocol;
		this.mailUsername = mailUsername;
		this.mailFromAddress = mailFromAddress;
		
		this.message = message;
		this.recipient = recipient;
		this.subject = subject;
	}

	public void run() {
	      Properties props = new Properties();
	      props.setProperty("mail.transport.protocol", mailProtocol);
	      props.setProperty("mail.host", mailHost);
	      props.setProperty("mail.user", mailUsername);
	      props.setProperty("mail.password", mailPassword);

	      Logger logger = Logger.getLogger("mail");
	      
	      try {    	  
	    	  logger.log(Level.INFO, "Sending mail " + subject + " to " + recipient);
		      Session mailSession = Session.getDefaultInstance(props, null);
		      Transport transport = mailSession.getTransport();

		      MimeMessage mimeMessage = new MimeMessage(mailSession);
		      mimeMessage.setSubject(subject);
		      mimeMessage.setContent(message, "text/plain");
		      mimeMessage.setFrom(new InternetAddress(mailFromAddress));
		      mimeMessage.addRecipient(Message.RecipientType.TO,
		           new InternetAddress(recipient));

	    	  logger.log(Level.INFO, mimeMessage);

	    	  transport.connect();
	    	  transport.sendMessage(mimeMessage,
	    			  mimeMessage.getRecipients(Message.RecipientType.TO));
	    	  transport.close();
	    	  
	    	  logger.log(Level.INFO, "Sent mail " + subject + " to " + recipient);
	      } catch (MessagingException e) {
	    	  Logger.getLogger("mail").log(Level.ERROR, "couldn't send mail");
	    	  Logger.getLogger("mail").log(Level.ERROR, e);
	    	  return;
	      }
	     
	}
}