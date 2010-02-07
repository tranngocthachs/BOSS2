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
import uk.ac.warwick.dcs.boss.model.dao.IMarkingCategoryDAO;
import uk.ac.warwick.dcs.boss.model.dao.IStaffInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.MarkingCategory;

public class PerformEditMarkingCategoryPage extends Page {

	public PerformEditMarkingCategoryPage() throws PageLoadException {
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

		MarkingCategory incoming = new MarkingCategory();

		if (pageContext.getParameter("assignment") == null) {
			throw new ServletException("missing assignment parameter");
		}
		incoming.setAssignmentId(Long.valueOf(pageContext.getParameter("assignment")));

		if (pageContext.getParameter("markingcategory") == null) {
			throw new ServletException("missing markingcategory parameter");
		}
		incoming.setId(Long.valueOf(pageContext.getParameter("markingcategory")));
		
		if (pageContext.getParameter("max_mark") == null) {
			throw new ServletException("missing max_mark parameter");
		}
		incoming.setMaximumMark(Long.valueOf(pageContext.getParameter("max_mark")));
		
		if (pageContext.getParameter("weighting") == null) {
			throw new ServletException("missing weighting parameter");
		}
		incoming.setWeighting(Long.valueOf(pageContext.getParameter("weighting")));
		
		if (pageContext.getParameter("name") == null) {
			throw new ServletException("missing name parameter");
		}
		incoming.setName(pageContext.getParameter("name"));

		
		
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
				
				IMarkingCategoryDAO markingCategoryDao = f.getMarkingCategoryDAOInstance();
				incoming.setId(markingCategoryDao.createPersistentCopy(incoming));
			} else {
				IStaffInterfaceQueriesDAO staffInterfaceQueriesDao = f.getStaffInterfaceQueriesDAOInstance();
				IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
				IMarkingCategoryDAO markingCategoryDao = f.getMarkingCategoryDAOInstance();
				Assignment assignment = assignmentDao.retrievePersistentEntity(incoming.getAssignmentId());
				
				if (!staffInterfaceQueriesDao.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), assignment.getModuleId())) {
					f.abortTransaction();
					throw new ServletException("permission denied");
				}
				
				if (pageContext.getParameter("delete") != null) {
					markingCategoryDao.deletePersistentEntity(incoming.getId());
				} else {
					markingCategoryDao.updatePersistentEntity(incoming);
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
		templateContext.put("nextPage", pageContext.getPageUrl(StaffPageFactory.SITE_NAME, StaffPageFactory.MARKING_CATEGORIES_PAGE));
		templateContext.put("nextPageParamName", "assignment");
		templateContext.put("nextPageParamValue", incoming.getAssignmentId());
		pageContext.renderTemplate(template, templateContext);
	}
}
