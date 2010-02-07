package uk.ac.warwick.dcs.boss.frontend.sites.markerpages;

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
import uk.ac.warwick.dcs.boss.model.dao.IMarkerInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.IMarkingAssignmentDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.MarkingAssignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.MarkerMarksQueryResult;

public class MarksPage extends Page {
	
	public MarksPage()
			throws PageLoadException {
		super("marker_marks", AccessLevel.USER);
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
		
		// Obtain the markingassignment ID
		String markingAssignmentString = pageContext.getParameter("markingassignment");
		if (markingAssignmentString == null) {
			throw new ServletException("no markingassignment parameter given");
		}
		Long markingAssignmentId = Long.valueOf(markingAssignmentString);
	
		// Ascertain sorting
		IMarkerInterfaceQueriesDAO.MarksSortingType sortingType = IMarkerInterfaceQueriesDAO.MarksSortingType.NONE;
		if (pageContext.getParameter("sorting") != null) {
			if (pageContext.getParameter("sorting").equals("mark_asc")) {
				sortingType = IMarkerInterfaceQueriesDAO.MarksSortingType.MARK_ASCENDING;
				templateContext.put("sorting", "mark_asc");
			} else if (pageContext.getParameter("sorting").equals("mark_desc")) {
				sortingType = IMarkerInterfaceQueriesDAO.MarksSortingType.MARK_DESCENDING;
				templateContext.put("sorting", "mark_desc");				
			} else if (pageContext.getParameter("sorting").equals("marker_id_asc")) {
				sortingType = IMarkerInterfaceQueriesDAO.MarksSortingType.MARKER_ID_ASCENDING;
				templateContext.put("sorting", "marker_id_asc");				
			} else if (pageContext.getParameter("sorting").equals("marker_id_desc")) {
				sortingType = IMarkerInterfaceQueriesDAO.MarksSortingType.MARKER_ID_DESCENDING;
				templateContext.put("sorting", "marker_id_desc");				
			} else if (pageContext.getParameter("sorting").equals("marking_category_id_asc")) {
				sortingType = IMarkerInterfaceQueriesDAO.MarksSortingType.MARKING_CATEGORY_ID_ASCENDING;
				templateContext.put("sorting", "marking_category_id_asc");				
			} else if (pageContext.getParameter("sorting").equals("marking_category_id_desc")) {
				sortingType = IMarkerInterfaceQueriesDAO.MarksSortingType.MARKING_CATEGORY_ID_DESCENDING;
				templateContext.put("sorting", "marking_category_id_desc");				
			} else if (pageContext.getParameter("sorting").equals("timestamp_asc")) {
				sortingType = IMarkerInterfaceQueriesDAO.MarksSortingType.TIMESTAMP_ASCENDING;
				templateContext.put("sorting", "timestamp_asc");				
			} else if (pageContext.getParameter("sorting").equals("timestamp_desc")) {
				sortingType = IMarkerInterfaceQueriesDAO.MarksSortingType.TIMESTAMP_DESCENDING;
				templateContext.put("sorting", "timestamp_desc");				
			}
		} else {
			sortingType = IMarkerInterfaceQueriesDAO.MarksSortingType.NONE;
			templateContext.put("sorting", "none");
		}
		
		// Render page
		try {
			f.beginTransaction();
			
			IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
			IMarkingAssignmentDAO markingAssignmentDao = f.getMarkingAssignmentDAOInstance();
			IMarkerInterfaceQueriesDAO dao = f.getMarkerInterfaceQueriesDAOInstance();
			
			if (!dao.isMarkerMarkingAssignmentAccessAllowed(pageContext.getSession().getPersonBinding().getId(), markingAssignmentId)) {
				f.abortTransaction();
				throw new ServletException("permission denied");
			}
			
			MarkingAssignment markingAssignment = markingAssignmentDao.retrievePersistentEntity(markingAssignmentId);
			Assignment assignment = assignmentDao.retrievePersistentEntity(markingAssignment.getAssignmentId());
			
			Collection<MarkerMarksQueryResult> result = null;
			
			if (markingAssignment.getModerator()) {
				result = dao.performModeratorMarksQuery(sortingType,
						pageContext.getSession().getPersonBinding().getId(),
						markingAssignment.getStudentId(),
						markingAssignment.getAssignmentId());
			} else {
				result = dao.performMarkerMarksQuery(sortingType,
					markingAssignmentId);
			}
				
			f.endTransaction();
		
			// TODO: localisation
			templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
			templateContext.put("assignment", assignment);
			templateContext.put("markingAssignment", markingAssignment);
			templateContext.put("marks", result);

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
