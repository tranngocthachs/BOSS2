package uk.ac.warwick.dcs.boss.frontend.sites.staffpages;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;
import uk.ac.warwick.dcs.boss.frontend.sites.StaffPageFactory;
import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.DAOFactory;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IModuleDAO;
import uk.ac.warwick.dcs.boss.model.dao.IStaffInterfaceQueriesDAO;

public class PerformRemoveStudentPage extends Page {

	public PerformRemoveStudentPage() throws PageLoadException {
		super("multi_edited", AccessLevel.USER);
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
		IDAOSession f;
		try {
			DAOFactory df = (DAOFactory)FactoryRegistrar.getFactory(DAOFactory.class);
			f = df.getInstance();
		} catch (FactoryException e) {
			throw new ServletException("dao init error", e);
		}
		
		// Get IDs
		String moduleIdString = pageContext.getParameter("module");
		if (moduleIdString == null) {
			throw new ServletException("No module parameter given");
		}
		Long moduleId = Long.valueOf(moduleIdString);

		String personIdString = pageContext.getParameter("student");
		if (personIdString == null) {
			throw new ServletException("No student parameter given");
		}
		Long personId = Long.valueOf(personIdString);

		// Perform remove
		try {
			f.beginTransaction();

			IStaffInterfaceQueriesDAO staffInterfaceQueriesDao = f.getStaffInterfaceQueriesDAOInstance();
			IModuleDAO moduleDao = f.getModuleDAOInstance();

			if (staffInterfaceQueriesDao.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), moduleId)) {
				moduleDao.removeStudentAssociation(moduleId, personId);
			} else {
				f.abortTransaction();
				throw new ServletException("permission denied");
			}

			f.endTransaction();			
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("DAO error", e);
		}
		
		// Show page.
		templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
		templateContext.put("nextPage", pageContext.getPageUrl(StaffPageFactory.SITE_NAME, StaffPageFactory.STUDENTS_PAGE));
		templateContext.put("nextPageParamName", "module");
		templateContext.put("nextPageParamValue", moduleId);
		templateContext.put("success", true);
		
		pageContext.renderTemplate(template, templateContext);
	}

}
