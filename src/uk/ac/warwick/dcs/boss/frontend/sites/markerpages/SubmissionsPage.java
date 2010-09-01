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
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IMarkerInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.MarkerSubmissionsQueryResult;
import uk.ac.warwick.dcs.boss.plugins.spi.extralinks.IMarkerSubmissionPluginEntryLink;

public class SubmissionsPage extends Page {

	public SubmissionsPage() throws PageLoadException {
		super("marker_submissions", AccessLevel.USER);
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
		String markingAssignmentString = pageContext.getParameter("markingassignment");
		if (markingAssignmentString == null) {
			throw new ServletException("No markingassignment parameter given");
		}
		Long markingAssignmentId = Long
				.valueOf(pageContext.getParameter("markingassignment"));

		// Get the sorting
		IMarkerInterfaceQueriesDAO.MarkerSubmissionsQuerySortingType sortingType = null;
		if (pageContext.getParameter("sorting") != null) {
			if (pageContext.getParameter("sorting").equals("submission_time_asc")) {
				sortingType = IMarkerInterfaceQueriesDAO.MarkerSubmissionsQuerySortingType.SUBMISSION_TIME_ASCENDING;
				templateContext.put("sorting", "submission_time_asc");
			} else if (pageContext.getParameter("sorting").equals(
					"submission_time_desc")) {
				sortingType = IMarkerInterfaceQueriesDAO.MarkerSubmissionsQuerySortingType.SUBMISSION_TIME_DESCENDING;
				templateContext.put("sorting", "submission_time_desc");
			} else if (pageContext.getParameter("sorting").equals("active_asc")) {
				sortingType = IMarkerInterfaceQueriesDAO.MarkerSubmissionsQuerySortingType.ACTIVE_ASCENDING;
				templateContext.put("sorting", "active_asc");				
			} else if (pageContext.getParameter("sorting").equals("active_desc")) {
				sortingType = IMarkerInterfaceQueriesDAO.MarkerSubmissionsQuerySortingType.ACTIVE_DESCENDING;
				templateContext.put("sorting", "active_desc");				
			}

		} else {
			sortingType = IMarkerInterfaceQueriesDAO.MarkerSubmissionsQuerySortingType.ACTIVE_DESCENDING;
			templateContext.put("sorting", "active_desc");
		}

		
		// Render page
		try {
			f.beginTransaction();

			IMarkerInterfaceQueriesDAO markerInterfaceQueriesDao = f.getMarkerInterfaceQueriesDAOInstance();
			if (!markerInterfaceQueriesDao.isMarkerMarkingAssignmentAccessAllowed(pageContext.getSession().getPersonBinding().getId(),
					markingAssignmentId)) {
				f.abortTransaction();
				throw new ServletException("permission denied");
			}
			
			IMarkerInterfaceQueriesDAO dao = f
					.getMarkerInterfaceQueriesDAOInstance();
			Collection<MarkerSubmissionsQueryResult> result = dao
					.performSubmissionsQuery(
							sortingType,
							markingAssignmentId);

			f.endTransaction();

			// TODO: localisation
			templateContext.put("greet", pageContext.getSession()
					.getPersonBinding().getChosenName());
			if (!result.isEmpty()) {
				templateContext.put("assignment", result.iterator().next()
						.getAssignment());
			}
			templateContext.put("submissions", result);
			templateContext.put("markingAssignmentId", markingAssignmentId);
			
			// loading additional buttons for plugin actions on a student submission
			Collection<? extends IMarkerSubmissionPluginEntryLink> pluginEntryLinks = Lookup.getDefault().lookupAll(IMarkerSubmissionPluginEntryLink.class);
			if (!pluginEntryLinks.isEmpty()) {
				List<String> pluginLinks = new LinkedList<String>();
				List<String> pluginLinkSubmissionParaStrs = new LinkedList<String>();
				List<String> pluginLinkLabels = new LinkedList<String>();
				for (IMarkerSubmissionPluginEntryLink pluginLink : pluginEntryLinks) {
					pluginLinks.add(pageContext.getPageUrl(MarkerPageFactory.SITE_NAME, pluginLink.getPageName()));
					pluginLinkSubmissionParaStrs.add(pluginLink.getSubmissionParaName());
					pluginLinkLabels.add(pluginLink.getLinkLabel());
				}
				templateContext.put("pluginLinks", pluginLinks);
				templateContext.put("pluginLinkSubmissionParaStrs", pluginLinkSubmissionParaStrs);
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
