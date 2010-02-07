package uk.ac.warwick.dcs.boss.frontend.sites.staffpages;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
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
import uk.ac.warwick.dcs.boss.model.dao.IDeadlineRevisionDAO;
import uk.ac.warwick.dcs.boss.model.dao.IModuleDAO;
import uk.ac.warwick.dcs.boss.model.dao.IPersonDAO;
import uk.ac.warwick.dcs.boss.model.dao.IStaffInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.DeadlineRevision;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;

public class EditDeadlineRevisionPage extends Page {

	public EditDeadlineRevisionPage() throws PageLoadException {
		super("staff_edit_deadlinerevision", AccessLevel.USER);
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
		
		DeadlineRevision original;
		Assignment originalParent;
		Module originalParentParent;
		Collection<Person> students;
	
		try {
			if (pageContext.getParameter("create") != null) {
				// Get moduleId
				String assignmentString = pageContext.getParameter("assignment");
				if (assignmentString == null) {
					throw new ServletException("No assignment parameter given");
				}
				Long assignemntId = Long
						.valueOf(pageContext.getParameter("assignment"));
				
				f.beginTransaction();		
				
				IStaffInterfaceQueriesDAO staffInterfaceQueriesDAO = f.getStaffInterfaceQueriesDAOInstance();
				IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
				originalParent = assignmentDao.retrievePersistentEntity(assignemntId);
				
				if (!staffInterfaceQueriesDAO.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), originalParent.getModuleId())) {
					f.abortTransaction();
					throw new ServletException("permission denied");
				}
				f.endTransaction();
				
				original = new DeadlineRevision();
				original.setId(-1L);
				original.setAssignmentId(assignemntId);
				original.setComment("<None>");
				original.setNewDeadline(new Date());
				original.setPersonId(-1L);

				templateContext.put("create", true);
			} else {	
				// Get deadlineRevisionId
				String deadlineRevisionString = pageContext.getParameter("deadlinerevision");
				if (deadlineRevisionString == null) {
					throw new ServletException("No deadlinerevision parameter given");
				}
				Long deadlineRevisionId = Long
					.valueOf(pageContext.getParameter("deadlinerevision"));
				
				
				f.beginTransaction();		
				IStaffInterfaceQueriesDAO staffInterfaceQueriesDAO = f.getStaffInterfaceQueriesDAOInstance();
				IDeadlineRevisionDAO deadlineRevisionDao = f.getDeadlineRevisionDAOInstance();
				IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
				
				original = deadlineRevisionDao.retrievePersistentEntity(deadlineRevisionId);
				originalParent = assignmentDao.retrievePersistentEntity(original.getAssignmentId());
				if (!staffInterfaceQueriesDAO.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), originalParent.getModuleId())) {
					f.abortTransaction();
					throw new ServletException("permission denied");
				}
				
				f.endTransaction();
				
				templateContext.put("create", false);
			}
						
			// Now get the assignment, module and students.
			f.beginTransaction();
			
			IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
			originalParent = assignmentDao.retrievePersistentEntity(original.getAssignmentId());
			
			IModuleDAO moduleDao = f.getModuleDAOInstance();
			originalParentParent = moduleDao.retrievePersistentEntity(originalParent.getModuleId());
			
			students = moduleDao.fetchStudents(IPersonDAO.SortingType.NONE, originalParentParent.getId());
			
			f.endTransaction();
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao error", e);
		}
		
		// Got the original.
		templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
		templateContext.put("module", originalParentParent);
		templateContext.put("assignment", originalParent);
		templateContext.put("deadlineRevision", original);
		templateContext.put("students", students);
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
