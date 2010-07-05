package uk.ac.warwick.dcs.boss.frontend;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.velocity.app.Velocity;

import uk.ac.warwick.dcs.boss.frontend.sites.AdminPageFactory;
import uk.ac.warwick.dcs.boss.frontend.sites.ConfigPageFactory;
import uk.ac.warwick.dcs.boss.frontend.sites.HelpPageFactory;
import uk.ac.warwick.dcs.boss.frontend.sites.MarkerPageFactory;
import uk.ac.warwick.dcs.boss.frontend.sites.StaffPageFactory;
import uk.ac.warwick.dcs.boss.frontend.sites.StudentPageFactory;
import uk.ac.warwick.dcs.boss.frontend.sites.SystemPageFactory;
import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
import uk.ac.warwick.dcs.boss.model.autoassignment.AutoAssignmentMethodDirectoryFactory;
import uk.ac.warwick.dcs.boss.model.dao.DAOFactory;
import uk.ac.warwick.dcs.boss.model.mail.MailFactory;
import uk.ac.warwick.dcs.boss.model.session.SessionAutenticatorFactory;
import uk.ac.warwick.dcs.boss.model.testing.TestRunnerFactory;
import uk.ac.warwick.dcs.boss.model.testing.executors.TestExecutorFactory;
import uk.ac.warwick.dcs.boss.model.testing.tests.TestMethodFactory;
import uk.ac.warwick.dcs.boss.model.utilities.AdminUtilityFactory;
/**
 * The BOSS2 web app!
 * @author davidbyard
 *
 */
public class PageDispatcherServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Logger logger = null;
	public static String realPath = null; 

	/**
	 * Load configuration.
	 */
	public static Properties loadConfiguration(String path) {
		try {
			Properties configuration = new Properties();
			File propertiesFile = new File(path);
			FileInputStream reader = new FileInputStream(propertiesFile);
			configuration = new Properties();
			configuration.load(reader);
			reader.close();
			return configuration;
		} catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * Load factories from configuration
	 */
	public static void loadFactories(Properties factoryBindings) throws Exception {
		// Get names
		String utilitiesClassName = factoryBindings.getProperty("utilities");
		String autoAssignmentClassName = factoryBindings.getProperty("autoassignment");
		String daoClassName = factoryBindings.getProperty("dao");
		String mailClassName = factoryBindings.getProperty("mail");
		String sessionClassName = factoryBindings.getProperty("session");

		String testExecutorsClassName = factoryBindings.getProperty("testexecutors");
		String testRunnersClassName = factoryBindings.getProperty("testrunners");
		String testMethodsClassName = factoryBindings.getProperty("testmethods");

		// Load classes
		Class<? extends AdminUtilityFactory> utilitiesClass = Class.forName(utilitiesClassName).asSubclass(AdminUtilityFactory.class);
		Class<? extends AutoAssignmentMethodDirectoryFactory> autoAssignmentClass = Class.forName(autoAssignmentClassName).asSubclass(AutoAssignmentMethodDirectoryFactory.class);
		Class<? extends DAOFactory> daoClass = Class.forName(daoClassName).asSubclass(DAOFactory.class);
		Class<? extends MailFactory> mailClass = Class.forName(mailClassName).asSubclass(MailFactory.class);
		Class<? extends SessionAutenticatorFactory> sessionClass = Class.forName(sessionClassName).asSubclass(SessionAutenticatorFactory.class);
		
		Class<? extends TestExecutorFactory> testExecutorsClass = Class.forName(testExecutorsClassName).asSubclass(TestExecutorFactory.class);
		Class<? extends TestRunnerFactory> testRunnersClass = Class.forName(testRunnersClassName).asSubclass(TestRunnerFactory.class);
		Class<? extends TestMethodFactory> testMethodsClass = Class.forName(testMethodsClassName).asSubclass(TestMethodFactory.class);
		
		// Register
		FactoryRegistrar.registerFactory(AdminUtilityFactory.class, utilitiesClass.newInstance());
		FactoryRegistrar.registerFactory(AutoAssignmentMethodDirectoryFactory.class, autoAssignmentClass.newInstance());
		FactoryRegistrar.registerFactory(DAOFactory.class, daoClass.newInstance());
		FactoryRegistrar.registerFactory(MailFactory.class, mailClass.newInstance());
		FactoryRegistrar.registerFactory(SessionAutenticatorFactory.class, sessionClass.newInstance());
		
		FactoryRegistrar.registerFactory(TestExecutorFactory.class, testExecutorsClass.newInstance());
		FactoryRegistrar.registerFactory(TestRunnerFactory.class, testRunnersClass.newInstance());
		FactoryRegistrar.registerFactory(TestMethodFactory.class, testMethodsClass.newInstance());
	}
		
	/**
	 * Load page factories (normal operation)
	 */
	public static void loadPageFactories() {
		PageFactory.clear();
		
		logger.log(Level.INFO, "Registering page factories for normal operation...");
		
		logger.log(Level.INFO, "  system");
		PageFactory.registerFactory(SystemPageFactory.SITE_NAME, new SystemPageFactory());

		logger.log(Level.INFO, "  staff");
		PageFactory.registerFactory(StaffPageFactory.SITE_NAME, new StaffPageFactory());
		
		logger.log(Level.INFO, "  student");
		PageFactory.registerFactory(StudentPageFactory.SITE_NAME, new StudentPageFactory());
		
		logger.log(Level.INFO, "  admin");
		PageFactory.registerFactory(AdminPageFactory.SITE_NAME, new AdminPageFactory());
		
		logger.log(Level.INFO, "  marker");
		PageFactory.registerFactory(MarkerPageFactory.SITE_NAME, new MarkerPageFactory());

		logger.log(Level.INFO, "  help");
		PageFactory.registerFactory(HelpPageFactory.SITE_NAME, new HelpPageFactory());
	}
	
	/**
	 * Load page factories (configuration mode)
	 */
	public static void loadConfigurationPageFactories() {
		PageFactory.clear();
		
		logger.log(Level.INFO, "Registering page factories for configuration...");
		
		logger.log(Level.INFO, "  config");
		PageFactory.registerFactory(ConfigPageFactory.SITE_NAME, new ConfigPageFactory());
		
		logger.log(Level.INFO, "  help");
		PageFactory.registerFactory(HelpPageFactory.SITE_NAME, new HelpPageFactory());
	}
	
	/**
	 * Initialise logging.
	 */
	public static void initialiseLogging(Properties logProperties) {
		PropertyConfigurator.configure(logProperties);
	}
	
	/**
	 * Main servlet entry point.
	 */
	public void init() throws ServletException {
		super.init();
		
		// Initialise logging.
		Properties logProperties = loadConfiguration(this.getServletContext().getRealPath("WEB-INF/logging.properties"));
		if (logProperties == null) {
			throw new ServletException("Missing: " + this.getServletContext().getRealPath("WEB-INF/logging.properties"));
		}
		initialiseLogging(logProperties);
		logger = Logger.getLogger(PageDispatcherServlet.class);
		
		// Initialise Velocity
		try {
			logger.log(Level.INFO, "Loading velocity...");
			Velocity.setProperty("resource.loader", "class");
			Velocity.setProperty("class.resource.loader.description", "Velocity Classpath Resource Loader");
			Velocity.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
			Velocity.init();
		} catch (Exception e) {
			throw new ServletException("Velocity init failed", e);
		}
		
		// Load factories
		Properties factoryBindings = loadConfiguration(this.getServletContext().getRealPath("WEB-INF/factories.properties"));
		if (factoryBindings == null) {
			throw new ServletException("Missing: " + this.getServletContext().getRealPath("WEB-INF/factories.properties"));
		}
		try {
			loadFactories(factoryBindings);
		} catch (Exception e) {
			throw new ServletException("Factory load error: ", e);
		}
		
		// Load configuration.
		Properties configuration = loadConfiguration(this.getServletContext().getRealPath("WEB-INF/config.properties"));
		
		if (configuration == null) {
			loadConfigurationPageFactories();
		} else {
			try {
				FactoryRegistrar.initialiseFactories(configuration);
			} catch (FactoryException e) {
				throw new ServletException("Testing initialisation failed", e);
			}
			
			loadPageFactories();
		}
		realPath = this.getServletContext().getRealPath(".");
	}	
	
	/**
	 * Handle a get request by farming out the right Page using the factories.
	 * 
	 * Handles 404 and 500s.
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {		
		// Display the page.
		PageContext context = new PageContext(this.getServletContext(), req, resp);	

		try {
			Page page = PageFactory.getSitePage(context.getSiteName(), context.getPageName());
			page.internalHandleGet(context);
		} catch (PageLoadException e) {
			if (e.getHttpErrorEquivalent() == 404) {
				try {
					Page notFoundPage = PageFactory.getSitePage(SystemPageFactory.SITE_NAME, SystemPageFactory.NOT_FOUND_PAGE);
					notFoundPage.internalHandleGet(context);
					resp.setStatus(404);
				} catch (PageLoadException ignore) {
					throw new ServletException("404 detected (and 404 page could not be loaded)", e);
				}
			} else {
				throw new ServletException("500 detected", e);
			}
		}
	}

	/**
	 * Handle a post request by farming out the right Page using the factories.
	 * 
	 * Handles 404 and 500s.
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {		
		// Display the page.
		PageContext context = new PageContext(this.getServletContext(), req, resp);	

		try {
			Page page = PageFactory.getSitePage(context.getSiteName(), context.getPageName());
			page.internalHandlePost(context);
		} catch (PageLoadException e) {
			if (e.getHttpErrorEquivalent() == 404) {
				try {
					Page notFoundPage = PageFactory.getSitePage(SystemPageFactory.SITE_NAME, SystemPageFactory.NOT_FOUND_PAGE);
					notFoundPage.internalHandleGet(context);
					resp.setStatus(404);
				} catch (PageLoadException ignore) {
					throw new ServletException("404 detected (and 404 page could not be loaded)", e);
				}
			} else {
				throw new ServletException("500 detected", e);
			}
		}
	}

}
