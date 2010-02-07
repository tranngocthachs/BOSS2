package uk.ac.warwick.dcs.boss.model.junit;

import java.util.Properties;

import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
import uk.ac.warwick.dcs.boss.model.dao.DAOFactory;
import uk.ac.warwick.dcs.boss.model.dao.impl.MySQLDAOFactory;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() throws Exception {
		Properties loggingConfiguration = new Properties();
		loggingConfiguration.put("log4j.rootLogger", "TRACE, A1");
		loggingConfiguration.put("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
		loggingConfiguration.put("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
		loggingConfiguration.put("log4j.appender.A1.layout.ConversionPattern", "%-4r [%t] %-5p %c %x - %m%n");
		
		Properties boss2Configuration = new Properties();
		boss2Configuration.put("db.host", "localhost");
		boss2Configuration.put("db.db", "boss2test");
		boss2Configuration.put("db.username", "boss2test");
		boss2Configuration.put("db.password", "boss2test");
		boss2Configuration.put("db.resource_dir", "/tmp/boss2test/resource");
		boss2Configuration.put("submission.secret_salt", "resource");
		
		
		DAOFactory f = new MySQLDAOFactory();
		f.init(boss2Configuration);
		FactoryRegistrar.registerFactory(DAOFactory.class, f);

		TestSuite suite = new TestSuite(
				"Test for uk.ac.warwick.dcs.boss.model.junit");
		//$JUnit-BEGIN$
		suite.addTestSuite(DAOBasicsTestCase.class);
		suite.addTestSuite(DAOPermissionsTestCase.class);
		suite.addTestSuite(DAOStorageTestCase.class);
		//$JUnit-END$
		return suite;
	}

}
