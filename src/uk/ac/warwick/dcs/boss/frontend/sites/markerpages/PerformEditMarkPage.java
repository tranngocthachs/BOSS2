package uk.ac.warwick.dcs.boss.frontend.sites.markerpages;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;
import uk.ac.warwick.dcs.boss.frontend.sites.MarkerPageFactory;
import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.DAOFactory;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IMarkDAO;
import uk.ac.warwick.dcs.boss.model.dao.IMarkerInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.IMarkingAssignmentDAO;
import uk.ac.warwick.dcs.boss.model.dao.IMarkingCategoryDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Mark;
import uk.ac.warwick.dcs.boss.model.dao.beans.MarkingAssignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.MarkingCategory;

public class PerformEditMarkPage extends Page {

	public PerformEditMarkPage() throws PageLoadException {
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

		Mark incoming = new Mark();

		if (pageContext.getParameter("mark") == null) {
			throw new ServletException("missing mark parameter");
		}
		incoming.setId(Long.valueOf(pageContext.getParameter("mark")));

		
		if (pageContext.getParameter("comment") == null) {
			throw new ServletException("missing comment parameter");
		}
		incoming.setComment(pageContext.getParameter("comment"));

		if (pageContext.getParameter("markingassignment") == null) {
			throw new ServletException("missing markingassignment parameter");
		}
		incoming.setMarkingAssignmentId(Long.valueOf(pageContext.getParameter("markingassignment")));

		if (pageContext.getParameter("markingcategory") == null) {
			throw new ServletException("missing markingcategory parameter");
		}	
		incoming.setMarkingCategoryId(Long.valueOf(pageContext.getParameter("markingcategory")));
		
		if (pageContext.getParameter("value") == null) {
			throw new ServletException("missing value parameter");
		}	
		incoming.setValue(Integer.valueOf(pageContext.getParameter("value")));		
		
		incoming.setTimestamp(new Date());
		
		try {
			f.beginTransaction();

			IMarkerInterfaceQueriesDAO markerInterfaceQueriesDao = f.getMarkerInterfaceQueriesDAOInstance();
			IMarkingAssignmentDAO markingAssignmentDao = f.getMarkingAssignmentDAOInstance();
			IMarkingCategoryDAO markingCategoryDao = f.getMarkingCategoryDAOInstance();

			if (!markerInterfaceQueriesDao.isMarkerMarkingAssignmentAccessAllowed(pageContext.getSession().getPersonBinding().getId(), incoming.getMarkingAssignmentId())) {
				f.abortTransaction();
				throw new ServletException("permission denied");
			}
			MarkingAssignment markingAssignment = markingAssignmentDao.retrievePersistentEntity(incoming.getMarkingAssignmentId());
			
			MarkingCategory markingCategory = markingCategoryDao.retrievePersistentEntity(incoming.getMarkingCategoryId());
			
			if (markingAssignment.getAssignmentId() != markingCategory.getAssignmentId()) {
				f.abortTransaction();
				throw new ServletException("permission denied");
			}
			
			// Check it's not a duplicate mark.
			IMarkDAO markDao = f.getMarkDAOInstance();
			
			if (pageContext.getParameter("create") != null) {			
				incoming.setId(markDao.createPersistentCopy(incoming));
			} else {	
				if(!markerInterfaceQueriesDao.isMarkerMarkingAssignmentAccessAllowed(pageContext.getSession().getPersonBinding().getId(), incoming.getMarkingAssignmentId())) {
					f.abortTransaction();
					throw new ServletException("permission denied");
				}
			
				if (pageContext.getParameter("delete") != null) {
					markDao.deletePersistentEntity(incoming.getId());
				} else {
					markDao.updatePersistentEntity(incoming);
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
		templateContext.put("nextPage", pageContext.getPageUrl(MarkerPageFactory.SITE_NAME, MarkerPageFactory.MARKS_PAGE));
		templateContext.put("nextPageParamName", "markingassignment");
		templateContext.put("nextPageParamValue", incoming.getMarkingAssignmentId());
		pageContext.renderTemplate(template, templateContext);
	}
}
