package uk.ac.warwick.dcs.boss.frontend.sites.staffpages;

import java.io.IOException;
import java.util.Collection;

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
import uk.ac.warwick.dcs.boss.model.dao.IAssignmentDAO;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IModuleDAO;
import uk.ac.warwick.dcs.boss.model.dao.IPersonDAO;
import uk.ac.warwick.dcs.boss.model.dao.IStaffInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;

public class PerformFindMarkerPage extends Page {

	public PerformFindMarkerPage() throws PageLoadException {
		super("multi_found_person", AccessLevel.USER);
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

		// Get id
		String assignmentIdString = pageContext.getParameter("assignment");
		if (assignmentIdString == null) {
			throw new ServletException("No assignment parameter given");
		}
		Long assignmentId = Long.valueOf(assignmentIdString);
		
		String nameCriterion = pageContext.getParameter("name");
		String uniqCriterion = pageContext.getParameter("uniq");
		if (nameCriterion == null && uniqCriterion == null) {
			pageContext.performRedirect(pageContext.getPageUrl(StaffPageFactory.SITE_NAME, StaffPageFactory.FIND_MARKER_PAGE) + "?assignment=" + assignmentId);
			return;
		}
		
		// Perform search
		try {
			f.beginTransaction();
			
			IStaffInterfaceQueriesDAO staffInterfaceQueriesDao = f.getStaffInterfaceQueriesDAOInstance();
			IModuleDAO moduleDao = f.getModuleDAOInstance();
			IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
			
			Assignment assignment = assignmentDao.retrievePersistentEntity(assignmentId);
			Module module = moduleDao.retrievePersistentEntity(assignment.getModuleId());

			
			if (staffInterfaceQueriesDao.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), assignment.getModuleId())) {			
				templateContext.put("module", module);
				templateContext.put("assignment", assignment);
			} else {
				f.abortTransaction();
				throw new ServletException("permission denied");
			}
			
			IPersonDAO personDao = f.getPersonDAOInstance();
			Person example = new Person();
			if (nameCriterion != null) {
				example.setChosenName("%" + nameCriterion + "%");
			}
			if (uniqCriterion != null) {
				example.setUniqueIdentifier("%" + uniqCriterion + "%");
			}
			Collection<Person> results = personDao.findPersistentEntitiesByWildcards(example);
			
			f.endTransaction();
			
			templateContext.put("results", results);
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("DAO error", e);
		}
		
		// Show page.
		templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
		templateContext.put("personType", "marker");
		pageContext.renderTemplate(template, templateContext);
	}

}
