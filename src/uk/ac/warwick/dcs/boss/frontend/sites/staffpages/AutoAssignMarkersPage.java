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
import uk.ac.warwick.dcs.boss.model.autoassignment.AutoAssignmentMethodDescription;
import uk.ac.warwick.dcs.boss.model.autoassignment.AutoAssignmentMethodDirectoryFactory;
import uk.ac.warwick.dcs.boss.model.autoassignment.IAutoAssignmentMethodDirectory;
import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.DAOFactory;
import uk.ac.warwick.dcs.boss.model.dao.IAssignmentDAO;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IModuleDAO;
import uk.ac.warwick.dcs.boss.model.dao.IStaffInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;

public class AutoAssignMarkersPage extends Page {

	public AutoAssignMarkersPage() throws PageLoadException {
		super("staff_auto_assign_markers", AccessLevel.USER);
	}
	
	@Override
	protected void handleGet(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		IDAOSession f;
		IAutoAssignmentMethodDirectory directory;
		try {
			DAOFactory df = (DAOFactory)FactoryRegistrar.getFactory(DAOFactory.class);
			AutoAssignmentMethodDirectoryFactory aamdf = (AutoAssignmentMethodDirectoryFactory)FactoryRegistrar.getFactory(AutoAssignmentMethodDirectoryFactory.class);
			f = df.getInstance();
			directory = aamdf.getInstance();
		} catch (FactoryException e) {
			throw new ServletException("dao init error", e);
		}

		Assignment assignment;
		Module module;

		// Get assignmentId
		String assignmentString = pageContext.getParameter("assignment");
		if (assignmentString == null) {
			throw new ServletException("No assignment parameter given");
		}
		Long assignmentId = Long
				.valueOf(pageContext.getParameter("assignment"));
		
		try {
			f.beginTransaction();
			
			IStaffInterfaceQueriesDAO staffInterfaceQueriesDao = f.getStaffInterfaceQueriesDAOInstance();
			IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
			assignment = assignmentDao.retrievePersistentEntity(assignmentId);
			
			if (!staffInterfaceQueriesDao.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), assignment.getModuleId())) {
				throw new DAOException("permission denied");
			}
			
			IModuleDAO moduleDao = f.getModuleDAOInstance();
			module = moduleDao.retrievePersistentEntity(assignment.getModuleId());
						
			f.endTransaction();
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao error", e);
		}

		// Get the types...
		Collection<AutoAssignmentMethodDescription> methods = directory.getAutoAssignmentMethodDescriptions();
		
		// Got them.
		templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
		templateContext.put("module", module);
		templateContext.put("assignment", assignment);
		templateContext.put("methods", methods);
		pageContext.renderTemplate(template, templateContext);
	}

	@Override
	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		throw new ServletException("unexpected POST");
	}

}
