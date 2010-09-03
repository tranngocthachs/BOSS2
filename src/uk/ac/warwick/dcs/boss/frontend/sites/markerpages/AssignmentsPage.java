package uk.ac.warwick.dcs.boss.frontend.sites.markerpages;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.openide.util.Lookup;

import boss.plugins.spi.extralinks.IMarkerPluginEntryLink;

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
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.MarkerAssignmentsQueryResult;

public class AssignmentsPage extends Page {
	
	public AssignmentsPage()
			throws PageLoadException {
		super("marker_assignments", AccessLevel.USER);
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
		
		IMarkerInterfaceQueriesDAO.AssignmentStatus status = IMarkerInterfaceQueriesDAO.AssignmentStatus.OPEN;
		
		// Otherwise ascertain status filter
		if (pageContext.getParameter("show") != null) {
			if (pageContext.getParameter("show").equals("all")) {
				status = IMarkerInterfaceQueriesDAO.AssignmentStatus.ALL;
				templateContext.put("showing", "both");
			} else if (pageContext.getParameter("show").equals("closed")) {
				status = IMarkerInterfaceQueriesDAO.AssignmentStatus.CLOSED;
				templateContext.put("showing", "closed");
			} else if (pageContext.getParameter("show").equals("published")) {
				status = IMarkerInterfaceQueriesDAO.AssignmentStatus.PUBLISHED;
				templateContext.put("showing", "published");
			} else {
				templateContext.put("showing", "open");
			}
		} else {
			templateContext.put("showing", "open");
		}
	
		// Ascertain sorting
		// TODO: More sorting types (see also marker_assignments.vm.html template)
		IMarkerInterfaceQueriesDAO.AssignmentsToMarkSortingType sortingType = IMarkerInterfaceQueriesDAO.AssignmentsToMarkSortingType.NONE;
		if (pageContext.getParameter("sorting") != null) {
			if (pageContext.getParameter("sorting").equals("student_count_asc")) {
				sortingType = IMarkerInterfaceQueriesDAO.AssignmentsToMarkSortingType.STUDENT_COUNT_ASCENDING;
				templateContext.put("sorting", "student_count_asc");
			} else if (pageContext.getParameter("sorting").equals("student_count_desc")) {
				sortingType = IMarkerInterfaceQueriesDAO.AssignmentsToMarkSortingType.STUDENT_COUNT_DESCENDING;
				templateContext.put("sorting", "student_count_desc");				
			}
		} else {
			sortingType = IMarkerInterfaceQueriesDAO.AssignmentsToMarkSortingType.NONE;
			templateContext.put("sorting", "none");
		}
		
		// Render page
		try {
			f.beginTransaction();
			
			IMarkerInterfaceQueriesDAO dao = f.getMarkerInterfaceQueriesDAOInstance();
			
			Collection<MarkerAssignmentsQueryResult> result = null;
			
			result = dao.performAssignmentsToMarkQuery(
					status,
					sortingType,
					pageContext.getSession().getPersonBinding().getId());
				
			f.endTransaction();
		
			// TODO: localisation
			templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
			templateContext.put("assignments", result);

			// loading plugins' entry pages (if present)
			Collection<? extends IMarkerPluginEntryLink> extraLinkProviders = Lookup.getDefault().lookupAll(IMarkerPluginEntryLink.class);
			if (!extraLinkProviders.isEmpty()) {
				List<String> labels = new LinkedList<String>();
				List<String> links = new LinkedList<String>();
				for (IMarkerPluginEntryLink link : extraLinkProviders) {
					labels.add(link.getLinkLabel());
					links.add(pageContext.getPageUrl(MarkerPageFactory.SITE_NAME, link.getPageName()));
				}
				templateContext.put("extraLinks", links);
				templateContext.put("extraLabels", labels);
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
