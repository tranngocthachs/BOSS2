package uk.ac.warwick.dcs.boss.frontend.sites.staffpages;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
import uk.ac.warwick.dcs.boss.model.dao.IAssignmentDAO;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IModuleDAO;
import uk.ac.warwick.dcs.boss.model.dao.IStaffInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;

public class EditAssignmentPage extends Page {

	public EditAssignmentPage() throws PageLoadException {
		super("staff_edit_assignment", AccessLevel.USER);
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

		Assignment original;
		Module originalParent;

		if (pageContext.getParameter("requires_more_confirmation") != null) {
			templateContext.put("requiresMoreConfirmation", true);
		}
		
		try {
			if (pageContext.getParameter("create") != null) {
				// Get moduleId
				String moduleString = pageContext.getParameter("module");
				if (moduleString == null) {
					throw new ServletException("No module parameter given");
				}
				Long moduleId = Long
						.valueOf(pageContext.getParameter("module"));
				
				f.beginTransaction();		
				
				IStaffInterfaceQueriesDAO staffInterfaceQueriesDao = f.getStaffInterfaceQueriesDAOInstance();
				if (!staffInterfaceQueriesDao.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), moduleId)) {
					f.abortTransaction();
					throw new ServletException("permission denied");
				}
				f.endTransaction();
				
				original = new Assignment();
				original.setId(-1L);
				original.setClosingTime(new Date());
				original.setDeadline(new Date());
				original.setModuleId(moduleId);
				original.setOpeningTime(new Date());
				original.setResourceId(null);
				original.setName("New Assignment");
				
				templateContext.put("create", true);
			} else {	
				// Get assignmentId
				String assignmentString = pageContext.getParameter("assignment");
				if (assignmentString == null) {
					throw new ServletException("No assignment parameter given");
				}
				Long assignmentId = Long
					.valueOf(pageContext.getParameter("assignment"));
				
				
				f.beginTransaction();		
				IStaffInterfaceQueriesDAO staffInterfaceQueriesDAO = f.getStaffInterfaceQueriesDAOInstance();
				IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
				original = assignmentDao.retrievePersistentEntity(assignmentId);
				if (!staffInterfaceQueriesDAO.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), original.getModuleId())) {
					f.abortTransaction();
					throw new ServletException("permission denied");
				}
				
				templateContext.put("hasChildren", assignmentDao.hasDeadlineRevisions(assignmentId)
						|| assignmentDao.hasMarkingCategories(assignmentId)
						|| assignmentDao.hasSubmissions(assignmentId)
						|| assignmentDao.hasTests(assignmentId));
				f.endTransaction();
				
				templateContext.put("create", false);
			}
			
			// Now get the module.
			f.beginTransaction();
			IModuleDAO moduleDao = f.getModuleDAOInstance();
			originalParent = moduleDao.retrievePersistentEntity(original.getModuleId());
			f.endTransaction();
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao error", e);
		}
		
		// Got the original.
		templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
		templateContext.put("module", originalParent);
		templateContext.put("assignment", original);
		templateContext.put("dateFormat", new SimpleDateFormat("yyyy-MM-dd HH:mm"));
		pageContext.renderTemplate(template, templateContext);
	}

	@Override
	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		throw new ServletException("unexpected POST");
	}

}
