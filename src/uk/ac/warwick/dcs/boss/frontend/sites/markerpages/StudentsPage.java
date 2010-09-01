package uk.ac.warwick.dcs.boss.frontend.sites.markerpages;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.openide.util.Lookup;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;
import uk.ac.warwick.dcs.boss.frontend.sites.MarkerPageFactory;
import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.DAOFactory;
import uk.ac.warwick.dcs.boss.model.dao.IAssignmentDAO;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IMarkerInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.MarkerStudentsQueryResult;
import uk.ac.warwick.dcs.boss.plugins.spi.extralinks.IMarkerAssignmentPluginEntryLink;

public class StudentsPage extends Page {
	
	public StudentsPage()
			throws PageLoadException {
		super("marker_students", AccessLevel.USER);
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
		
		// Obtain the assignment ID
		String assignmentString = pageContext.getParameter("assignment");
		if (assignmentString == null) {
			throw new ServletException("no assignment parameter given");
		}
		Long assignmentId = Long.valueOf(assignmentString);
	
		// Ascertain sorting
		IMarkerInterfaceQueriesDAO.StudentsToMarkSortingType sortingType = IMarkerInterfaceQueriesDAO.StudentsToMarkSortingType.NONE;
		if (pageContext.getParameter("sorting") != null) {
			if (pageContext.getParameter("sorting").equals("last_marked_asc")) {
				sortingType = IMarkerInterfaceQueriesDAO.StudentsToMarkSortingType.LAST_MARKED_ASCENDING;
				templateContext.put("sorting", "last_marked_asc");
			} else if (pageContext.getParameter("sorting").equals("last_marked_desc")) {
				sortingType = IMarkerInterfaceQueriesDAO.StudentsToMarkSortingType.LAST_MARKED_DESCENDING;
				templateContext.put("sorting", "last_marked_desc");
			} else if (pageContext.getParameter("sorting").equals("student_id_asc")) {
				sortingType = IMarkerInterfaceQueriesDAO.StudentsToMarkSortingType.STUDENT_ID_ASCENDING;
				templateContext.put("sorting", "student_id_asc");
			} else if (pageContext.getParameter("sorting").equals("student_id_desc")) {
				sortingType = IMarkerInterfaceQueriesDAO.StudentsToMarkSortingType.STUDENT_ID_DESCENDING;
				templateContext.put("sorting", "student_id_desc");
			} else if (pageContext.getParameter("sorting").equals("submission_count_asc")) {
				sortingType = IMarkerInterfaceQueriesDAO.StudentsToMarkSortingType.SUBMISSION_COUNT_ASCENDING;
				templateContext.put("sorting", "submission_count_asc");
			} else if (pageContext.getParameter("sorting").equals("submission_count_desc")) {
				sortingType = IMarkerInterfaceQueriesDAO.StudentsToMarkSortingType.SUBMISSION_COUNT_DESCENDING;
				templateContext.put("sorting", "submission_count_desc");
			}
		} else {
			sortingType = IMarkerInterfaceQueriesDAO.StudentsToMarkSortingType.NONE;
			templateContext.put("sorting", "none");
		}
		
		// Render page
		try {
			f.beginTransaction();
			
			IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
			IMarkerInterfaceQueriesDAO dao = f.getMarkerInterfaceQueriesDAOInstance();
			
			if (!dao.isMarkerAssignmentAccessAllowed(pageContext.getSession().getPersonBinding().getId(), assignmentId)) {
				throw new DAOException("permission denied");
			}
			
			Assignment assignment = assignmentDao.retrievePersistentEntity(assignmentId);
			
			Collection<MarkerStudentsQueryResult> result = null;
			result = dao.performStudentsToMarkQuery(
					sortingType,
					pageContext.getSession().getPersonBinding().getId(),
					assignmentId);
				
			f.endTransaction();
		
			// TODO: localisation
			templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
			templateContext.put("assignment", assignment);
			templateContext.put("students", result);

			// loading additional links for plugin entry on a marker assignment
			Collection<? extends IMarkerAssignmentPluginEntryLink> pluginEntryLinks = Lookup.getDefault().lookupAll(IMarkerAssignmentPluginEntryLink.class);
			if (!pluginEntryLinks.isEmpty()) {
				List<String> pluginLinks = new LinkedList<String>();
				List<String> pluginLinkAssParaStrs = new LinkedList<String>();
				List<String> pluginLinkLabels = new LinkedList<String>();
				for (IMarkerAssignmentPluginEntryLink pluginLink : pluginEntryLinks) {
					pluginLinks.add(pageContext.getPageUrl(MarkerPageFactory.SITE_NAME, pluginLink.getPageName()));
					pluginLinkAssParaStrs.add(pluginLink.getAssignmentParaName());
					pluginLinkLabels.add(pluginLink.getLinkLabel());
				}
				templateContext.put("pluginLinks", pluginLinks);
				templateContext.put("pluginLinkAssParaStrs", pluginLinkAssParaStrs);
				templateContext.put("pluginLinkLabels", pluginLinkLabels);
			}

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
