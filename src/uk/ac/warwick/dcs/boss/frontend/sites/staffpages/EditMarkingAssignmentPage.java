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
import uk.ac.warwick.dcs.boss.model.dao.IAssignmentDAO;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IMarkingAssignmentDAO;
import uk.ac.warwick.dcs.boss.model.dao.IModuleDAO;
import uk.ac.warwick.dcs.boss.model.dao.IPersonDAO;
import uk.ac.warwick.dcs.boss.model.dao.IStaffInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.MarkingAssignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;

public class EditMarkingAssignmentPage extends Page {

	public EditMarkingAssignmentPage() throws PageLoadException {
		super("staff_edit_markingassignment", AccessLevel.USER);
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

		MarkingAssignment original;
		Assignment originalParent;
		Module originalParentParent;
		Collection<Person> students;
		Collection<Person> markers;
		
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
				IStaffInterfaceQueriesDAO staffInterfaceQueriesDao = f.getStaffInterfaceQueriesDAOInstance();
				IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
				originalParent = assignmentDao.retrievePersistentEntity(assignemntId);
				if (!staffInterfaceQueriesDao.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), originalParent.getModuleId())) {
					f.abortTransaction();
					throw new ServletException("permission denied");
				}
				f.endTransaction();
				
				original = new MarkingAssignment();
				original.setId(-1L);
				original.setAssignmentId(assignemntId);
				original.setMarkerId(-1L);
				original.setStudentId(-1L);
				original.setBlind(false);
				original.setModerator(false);

				templateContext.put("create", true);
			} else {	
				// Get markingAssignmentId
				String markingAssignmentString = pageContext.getParameter("markingassignment");
				if (markingAssignmentString == null) {
					throw new ServletException("No markingassignment parameter given");
				}
				Long markingAssignmentId = Long
					.valueOf(pageContext.getParameter("markingassignment"));
				
				
				f.beginTransaction();		
				IStaffInterfaceQueriesDAO staffInterfaceQueriesDao = f.getStaffInterfaceQueriesDAOInstance();
				IMarkingAssignmentDAO markingAssignmentDao = f.getMarkingAssignmentDAOInstance();
				IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
				original = markingAssignmentDao.retrievePersistentEntity(markingAssignmentId);
				originalParent = assignmentDao.retrievePersistentEntity(original.getAssignmentId());
				
				if (!staffInterfaceQueriesDao.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), originalParent.getModuleId())) {
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
			markers = assignmentDao.fetchMarkers(IPersonDAO.SortingType.NONE, originalParent.getId());
			
			f.endTransaction();
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao error", e);
		}
		
		// Got the original.
		templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
		templateContext.put("module", originalParentParent);
		templateContext.put("assignment", originalParent);
		templateContext.put("markingAssignment", original);
		templateContext.put("markers", markers);
		templateContext.put("students", students);
		pageContext.renderTemplate(template, templateContext);
	}

	@Override
	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		throw new ServletException("unexpected POST");
	}

}
