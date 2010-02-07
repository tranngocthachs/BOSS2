package uk.ac.warwick.dcs.boss.model.mail;

/**
 * Interface for sending emails.
 * @author davidbyard
 */
public interface IMailSender {

	/**
	 * Send an email.  May be asynchronous.
	 * 
	 * No attachment support for now.
	 * @param recipient is the recipient email address
	 * @param subject is the subject of the email
	 * @param message is the contents of the email
	 * @throws MailException
	 */
	public abstract void sendMail(String recipient, String subject, String message) throws MailException;

}
