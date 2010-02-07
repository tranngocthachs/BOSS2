package uk.ac.warwick.dcs.boss.model.mail.impl;

import java.util.Collection;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.Executors;

import uk.ac.warwick.dcs.boss.model.ConfigurationOption;
import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.mail.IMailSender;
import uk.ac.warwick.dcs.boss.model.mail.MailException;
import uk.ac.warwick.dcs.boss.model.mail.MailFactory;

public class ThreadedMailSenderFactory extends MailFactory {

	ThreadedMailSender mailSender = null;
	
	@Override
	public Collection<ConfigurationOption> getConfigurationOptions() {
		Vector<ConfigurationOption> options = new Vector<ConfigurationOption>();
		
		options.add(new ConfigurationOption("email.protocol", "Change the following to set the protocol that BOSS will use for sending mail.  (e.g. smtp)", "smtp"));
		options.add(new ConfigurationOption("email.host", "Change the following to set the host that BOSS will use for sending mail.", "smtp.example.invalid"));
		options.add(new ConfigurationOption("email.username", "Change the following to set the username that BOSS will use for sending mail.", "boss2"));
		options.add(new ConfigurationOption("email.password", "Change the following to set the password that BOSS will use for sending mail.", "floozyflarf"));
		options.add(new ConfigurationOption("email.from", "Change the following to set the address that BOSS will say email was sent from.", "boss@exampleinvalid"));			

		return options;
	}

	
	@Override
	public void init(Properties configuration) throws FactoryException {
		try {
			this.mailSender = new ThreadedMailSender(Executors.newCachedThreadPool(),
				configuration.getProperty("email.protocol"),
				configuration.getProperty("email.host"),
				configuration.getProperty("email.username"),
				configuration.getProperty("email.password"),
				configuration.getProperty("email.from"));
		} catch (MailException e) {
			throw new FactoryException("init error", e);
		}
	}
	
	@Override
	public IMailSender getInstance() throws FactoryException {
		if (mailSender != null) {
			return mailSender;
		} else {
			throw new FactoryException("Factory not initialised");
		}
	}
	
	@Override
	public void cleanUp() {
		if (this.mailSender != null) {
			this.mailSender.cleanUp();
		}
		this.mailSender = null;
		super.cleanUp();
	}

}
