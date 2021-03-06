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
import uk.ac.warwick.dcs.boss.model.dao.IAssignmentDAO;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IMarkingAssignmentDAO;
import uk.ac.warwick.dcs.boss.model.dao.IStaffInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.MarkingAssignment;

public class PerformEditMarkingAssignmentPage extends Page {

	public PerformEditMarkingAssignmentPage() throws PageLoadException {
		super("multi_edited", AccessLevel.USER);
	}

	@Override
	protected void handleGet(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {

		throw new ServletException("unexpected GET");
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

		MarkingAssignment incoming = new MarkingAssignment();

		if (pageContext.getParameter("student") == null) {
			throw new ServletException("missing student parameter");
		}
		incoming.setStudentId(Long.valueOf(pageContext.getParameter("student")));

		if (pageContext.getParameter("assignment") == null) {
			throw new ServletException("missing assignment parameter");
		}
		incoming.setAssignmentId(Long.valueOf(pageContext.getParameter("assignment")));

		if (pageContext.getParameter("marker") == null) {
			throw new ServletException("missing marker parameter");
		}
		incoming.setMarkerId(Long.valueOf(pageContext.getParameter("marker")));
		
		if (pageContext.getParameter("markingassignment") == null) {
			throw new ServletException("missing markingassignment parameter");
		}
		incoming.setId(Long.valueOf(pageContext.getParameter("markingassignment")));
		
		if (pageContext.getParameter("blind") == null) {
			incoming.setBlind(false);
		} else {
			incoming.setBlind(true);
		}

		if (pageContext.getParameter("moderator") == null) {
			incoming.setModerator(false);
		} else {
			incoming.setModerator(true);
		}

		
		try {
			f.beginTransaction();

			if (pageContext.getParameter("create") != null) {
				IStaffInterfaceQueriesDAO staffInterfaceQueriesDao = f.getStaffInterfaceQueriesDAOInstance();
				IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
				Assignment assignment = assignmentDao.retrievePersistentEntity(incoming.getAssignmentId());
				
				if (!staffInterfaceQueriesDao.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), assignment.getModuleId())) {
					f.abortTransaction();
					throw new ServletException("permission denied");
				}
				
				IMarkingAssignmentDAO markingAssignmentDAO = f.getMarkingAssignmentDAOInstance();
				incoming.setId(markingAssignmentDAO.createPersistentCopy(incoming));
			} else {	
				IStaffInterfaceQueriesDAO staffInterfaceQueriesDao = f.getStaffInterfaceQueriesDAOInstance();
				IMarkingAssignmentDAO markingAssignmentDAO = f.getMarkingAssignmentDAOInstance();
				IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
				Assignment assignment = assignmentDao.retrievePersistentEntity(incoming.getAssignmentId());
				
				if (!staffInterfaceQueriesDao.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), assignment.getModuleId())) {
					f.abortTransaction();
					throw new ServletException("permission denied");
				}
			
				if (pageContext.getParameter("delete") != null) {
					markingAssignmentDAO.deletePersistentEntity(incoming.getId());
				} else {
					markingAssignmentDAO.updatePersistentEntity(incoming);
				}
			}

			// Done.
			f.endTransaction();
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao error", e);
		}

		// Got the original.
		templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
		templateContext.put("success", true);
		templateContext.put("nextPage", pageContext.getPageUrl(StaffPageFactory.SITE_NAME, StaffPageFactory.MARKING_ASSIGNMENTS_PAGE));
		templateContext.put("nextPageParamName", "assignment");
		templateContext.put("nextPageParamValue", incoming.getAssignmentId());
		pageContext.renderTemplate(template, templateContext);
	}
}
