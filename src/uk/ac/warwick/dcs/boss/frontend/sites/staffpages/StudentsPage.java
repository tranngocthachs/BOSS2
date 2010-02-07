package uk.ac.warwick.dcs.boss.frontend.sites.staffpages;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;
import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.DAOFactory;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IModuleDAO;
import uk.ac.warwick.dcs.boss.model.dao.IPersonDAO;
import uk.ac.warwick.dcs.boss.model.dao.IStaffInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;

public class StudentsPage extends Page {
	
	public StudentsPage()
			throws PageLoadException {
		super("staff_students", AccessLevel.USER);
	}

	@Override
	protected void handleGet(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		IDAOSession f;
		try {
			DAOFactory df = (DAOFactory)FactoryRegistrar.getFactory(DAOFactory.class);
			f = df.getInstance();
		} catch (FactoryException e) {
			throw new ServletException("dao init error", e);
		}
		
		// Get moduleId
		String moduleString = pageContext.getParameter("module");
		if (moduleString == null) {
			throw new ServletException("No module parameter given");
		}
		Long moduleId = Long
				.valueOf(pageContext.getParameter("module"));

		
		// Ascertain sorting
		IPersonDAO.SortingType sortingType = null;
		if (pageContext.getParameter("sorting") != null) {
			if (pageContext.getParameter("sorting").equals("unique_identifier_asc")) {
				sortingType = IPersonDAO.SortingType.UNIQUE_IDENTIFIER_ASCENDING;
				templateContext.put("sorting", "unique_identifier_asc");
			} else if (pageContext.getParameter("sorting").equals("unique_identifier_desc")) {
				sortingType = IPersonDAO.SortingType.UNIQUE_IDENTIFIER_DESCENDING;
				templateContext.put("sorting", "unique_identifier_desc");
			} else if (pageContext.getParameter("sorting").equals("chosen_name_asc")) {
				sortingType = IPersonDAO.SortingType.CHOSEN_NAME_ASCENDING;
				templateContext.put("sorting", "chosen_name_asc");
			} else if (pageContext.getParameter("sorting").equals("chosen_name_desc")) {
				sortingType = IPersonDAO.SortingType.CHOSEN_NAME_DESCENDING;
				templateContext.put("sorting", "chosen_name_desc");
			}
		} else {
			sortingType = IPersonDAO.SortingType.NONE;
			templateContext.put("sorting", "");
		}
		
		// Render page
		try {
			f.beginTransaction();
			
			IStaffInterfaceQueriesDAO staffInterfaceQueriesDao = f.getStaffInterfaceQueriesDAOInstance();
			IModuleDAO moduleDao = f.getModuleDAOInstance();
			if (!staffInterfaceQueriesDao.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), moduleId)) {
				f.abortTransaction();
				throw new ServletException("permission denied");
			}
			Module module = moduleDao.retrievePersistentEntity(moduleId);
			
			Collection<Person> result = moduleDao.fetchStudents(sortingType, moduleId);

			f.endTransaction();
		
			// TODO: localisation
			templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
			templateContext.put("module", module);
			templateContext.put("students", result);

			pageContext.renderTemplate(template, templateContext);
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao exception", e);
		}
	}

	@Override
	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		throw new ServletException("Unexpected POST");
	}

}
