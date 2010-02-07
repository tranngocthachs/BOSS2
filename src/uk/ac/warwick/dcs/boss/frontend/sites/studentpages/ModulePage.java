package uk.ac.warwick.dcs.boss.frontend.sites.studentpages;

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
import uk.ac.warwick.dcs.boss.model.dao.IAssignmentDAO;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IModuleDAO;
import uk.ac.warwick.dcs.boss.model.dao.IPersonDAO;
import uk.ac.warwick.dcs.boss.model.dao.IStudentInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;

public class ModulePage extends Page {
	
	public ModulePage()
			throws PageLoadException {
		super("student_module", AccessLevel.USER);
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
		
		// Get the assignment
		String assignmentString = pageContext.getParameter("module");
		if (assignmentString == null) {
			throw new ServletException("No module parameter given");
		}
		Long moduleId = Long.valueOf(pageContext.getParameter("module"));
		
		// Render page
		try {
			f.beginTransaction();
			
			IStudentInterfaceQueriesDAO studentInterfaceQueriesDao = f.getStudentInterfaceQueriesDAOInstance();
			IModuleDAO moduleDao = f.getModuleDAOInstance();
			
			if (!studentInterfaceQueriesDao.isStudentModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), moduleId)) {
				f.abortTransaction();
				throw new DAOException("permission denied");
			} else {
				Module result = moduleDao.retrievePersistentEntity(moduleId);
				Collection<Person> markers = moduleDao.fetchAdministrators(IPersonDAO.SortingType.CHOSEN_NAME_ASCENDING, moduleId);
				
				IAssignmentDAO adao = f.getAssignmentDAOInstance();
				Assignment example = new Assignment();
				example.setModuleId(moduleId);
				Collection<Assignment> assignments = adao.findPersistentEntitiesByExample(example);
				
				templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
				templateContext.put("module", result);
				templateContext.put("administrators", markers);
				templateContext.put("assignments", assignments);
			}
			
			f.endTransaction();
			
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
