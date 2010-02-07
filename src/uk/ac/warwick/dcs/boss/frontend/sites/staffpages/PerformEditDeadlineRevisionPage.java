package uk.ac.warwick.dcs.boss.frontend.sites.staffpages;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

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
import uk.ac.warwick.dcs.boss.model.dao.IDeadlineRevisionDAO;
import uk.ac.warwick.dcs.boss.model.dao.IStaffInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.DeadlineRevision;

public class PerformEditDeadlineRevisionPage extends Page {

	public PerformEditDeadlineRevisionPage() throws PageLoadException {
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

		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		DeadlineRevision incoming = new DeadlineRevision();

		if (pageContext.getParameter("student") == null) {
			throw new ServletException("missing student parameter");
		}
		incoming.setPersonId(Long.valueOf(pageContext.getParameter("student")));

		if (pageContext.getParameter("assignment") == null) {
			throw new ServletException("missing assignment parameter");
		}
		incoming.setAssignmentId(Long.valueOf(pageContext.getParameter("assignment")));

		if (pageContext.getParameter("deadline") == null) {
			throw new ServletException("missing deadline parameter");
		}	
		try {
			incoming.setNewDeadline(simpleDateFormat.parse(pageContext.getParameter("deadline")));
		} catch (ParseException e) {
			throw new ServletException("date parse error", e);
		}

		if (pageContext.getParameter("comment") == null) {
			throw new ServletException("missing comment parameter");
		}
		incoming.setComment(pageContext.getParameter("comment"));
		
		if (pageContext.getParameter("deadlinerevision") == null) {
			throw new ServletException("missing deadlinerevision parameter");
		}
		incoming.setId(Long.valueOf(pageContext.getParameter("deadlinerevision")));
		
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
				
				IDeadlineRevisionDAO deadlineRevisionDao = f.getDeadlineRevisionDAOInstance();
				incoming.setId(deadlineRevisionDao.createPersistentCopy(incoming));
			} else {	
				IStaffInterfaceQueriesDAO staffInterfaceQueriesDao = f.getStaffInterfaceQueriesDAOInstance();
				IDeadlineRevisionDAO deadlineRevisionDao = f.getDeadlineRevisionDAOInstance();				
				IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
				Assignment assignment = assignmentDao.retrievePersistentEntity(incoming.getAssignmentId());
				
				if (!staffInterfaceQueriesDao.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), assignment.getModuleId())) {
					f.abortTransaction();
					throw new ServletException("permission denied");
				}
			
				if (pageContext.getParameter("delete") != null) {
					deadlineRevisionDao.deletePersistentEntity(incoming.getId());
				} else {
					deadlineRevisionDao.updatePersistentEntity(incoming);
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
		templateContext.put("nextPage", pageContext.getPageUrl(StaffPageFactory.SITE_NAME, StaffPageFactory.DEADLINE_REVISIONS_PAGE));
		templateContext.put("nextPageParamName", "assignment");
		templateContext.put("nextPageParamValue", incoming.getAssignmentId());
		pageContext.renderTemplate(template, templateContext);
	}
}
