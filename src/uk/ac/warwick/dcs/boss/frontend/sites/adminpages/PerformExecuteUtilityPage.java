package uk.ac.warwick.dcs.boss.frontend.sites.adminpages;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletException;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;
import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
import uk.ac.warwick.dcs.boss.model.utilities.AdminUtilityDescription;
import uk.ac.warwick.dcs.boss.model.utilities.AdminUtilityException;
import uk.ac.warwick.dcs.boss.model.utilities.AdminUtilityFactory;
import uk.ac.warwick.dcs.boss.model.utilities.AdminUtilityParameterDescription;
import uk.ac.warwick.dcs.boss.model.utilities.AdminUtilityResult;
import uk.ac.warwick.dcs.boss.model.utilities.IAdminUtility;
import uk.ac.warwick.dcs.boss.model.utilities.IAdminUtilityDirectory;

public class PerformExecuteUtilityPage extends Page {
	
	public PerformExecuteUtilityPage()
			throws PageLoadException {
		super("admin_utility_results", AccessLevel.ADMIN);
	}

	@Override
	protected void handleGet(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		throw new ServletException("Unexpected GET");
	}

	@Override
	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		IAdminUtilityDirectory f;
		try {
			AdminUtilityFactory df = (AdminUtilityFactory)FactoryRegistrar.getFactory(AdminUtilityFactory.class);
			f = df.getInstance();
		} catch (FactoryException e) {
			throw new ServletException("admin utility init error", e);
		} 
		
		if (pageContext.getParameter("utility") == null) {
			throw new ServletException("utility parameter was null");
		}
		String utilityClassName = pageContext.getParameter("utility").trim();
		

		Collection<AdminUtilityDescription> utilities = f.getAdminUtilityDescriptions();
		AdminUtilityDescription foundUtility = null;
		for (AdminUtilityDescription utility : utilities) {
			if (utility.getClassName().equals(utilityClassName)) {
				foundUtility = utility;
				break;
			}
		}
		
		if (foundUtility == null) {
			throw new ServletException("unknown utility class: " + utilityClassName);
		}
		
		
		// Obtained the utility, grab the parameters
		HashMap<String, String> parameters = new HashMap<String, String>();
		for (AdminUtilityParameterDescription parameter : foundUtility.getParameters()) {
			if (pageContext.getParameter(parameter.getName()) == null) {
				if (parameter.isOptional()) {
					parameters.put(parameter.getName(), null);
				} else {
					throw new ServletException("Missing parameter: " + parameter.getDescription());
				}
			} else {
				parameters.put(parameter.getName(), pageContext.getParameter(parameter.getName()));
			}
		}
		
		// Obtained the parameters, load the class.
		try {
			Class<? extends IAdminUtility> methodClass = Class.forName(foundUtility.getClassName()).asSubclass(IAdminUtility.class);
			IAdminUtility methodInstance = methodClass.newInstance();
			AdminUtilityResult result = methodInstance.execute(parameters);
			templateContext.put("result", result);
		} catch (ClassNotFoundException e) {
			throw new ServletException("Class not found", e);
		} catch (InstantiationException e) {
			throw new ServletException("Class not instantiated", e);
		} catch (IllegalAccessException e) {
			throw new ServletException("Class not accessible", e);
		} catch (AdminUtilityException e) {
			AdminUtilityResult badResult = new AdminUtilityResult();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			pw.flush();
			pw.close();
			badResult.setFinishTime(new Date());
			badResult.setComment("Unable to run utility");
			badResult.setSuccess(false);
			badResult.setOutput(sw.toString());
			templateContext.put("result", badResult);
		}
		
		templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());

		pageContext.renderTemplate(template, templateContext);
	}	
}
