package uk.ac.warwick.dcs.boss.frontend.sites.staffpages;

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
import uk.ac.warwick.dcs.boss.frontend.sites.StaffPageFactory;
import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.DAOFactory;
import uk.ac.warwick.dcs.boss.model.dao.IAssignmentDAO;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IModuleDAO;
import uk.ac.warwick.dcs.boss.model.dao.IStaffInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.StaffSubmissionsQueryResult;
import uk.ac.warwick.dcs.boss.plugins.spi.extralinks.IStaffAssignmentPluginEntryLink;

public class SubmissionsPage extends Page {
	
	public SubmissionsPage()
			throws PageLoadException {
		super("staff_submissions", AccessLevel.USER);
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
		
		// Get assignmentId
		String assignmentString = pageContext.getParameter("assignment");
		if (assignmentString == null) {
			throw new ServletException("No assignment parameter given");
		}
		Long assignmentId = Long
				.valueOf(pageContext.getParameter("assignment"));

		
		// Ascertain sorting
		IStaffInterfaceQueriesDAO.StaffSubmissionsQuerySortingType sortingType = null;
		if (pageContext.getParameter("sorting") != null) {
			if (pageContext.getParameter("sorting").equals("uniq_asc")) {
				sortingType = IStaffInterfaceQueriesDAO.StaffSubmissionsQuerySortingType.STUDENT_UNIQUE_IDENTIFIER_ASCENDING;
				templateContext.put("sorting", "uniq_asc");
			} else if (pageContext.getParameter("sorting").equals("uniq_desc")) {
				sortingType = IStaffInterfaceQueriesDAO.StaffSubmissionsQuerySortingType.STUDENT_UNIQUE_IDENTIFIER_DESCENDING;
				templateContext.put("sorting", "uniq_desc");
			} else if (pageContext.getParameter("sorting").equals("submission_time_asc")) {
				sortingType = IStaffInterfaceQueriesDAO.StaffSubmissionsQuerySortingType.SUBMISSION_TIME_ASCENDING;
				templateContext.put("sorting", "submission_time_asc");
			} else if (pageContext.getParameter("sorting").equals("submission_time_desc")) {
				sortingType = IStaffInterfaceQueriesDAO.StaffSubmissionsQuerySortingType.SUBMISSION_TIME_DESCENDING;
				templateContext.put("sorting", "submission_time_desc");
			}
		} else {
			sortingType = IStaffInterfaceQueriesDAO.StaffSubmissionsQuerySortingType.NONE;
			templateContext.put("sorting", "");
		}
		
		// Render page
		try {
			f.beginTransaction();
			
			IStaffInterfaceQueriesDAO staffInterfaceQueriesDao = f.getStaffInterfaceQueriesDAOInstance();
			IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
			Assignment assignment = assignmentDao.retrievePersistentEntity(assignmentId);
			
			if (!staffInterfaceQueriesDao.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), assignment.getModuleId())) {
				f.abortTransaction();
				throw new ServletException("permission denied");
			}

			
			IModuleDAO moduleDao = f.getModuleDAOInstance();
			Module module = moduleDao.retrievePersistentEntity(assignment.getModuleId());

			IStaffInterfaceQueriesDAO staffInterfaceQueriesDAO = f.getStaffInterfaceQueriesDAOInstance();
			Collection<StaffSubmissionsQueryResult> result = staffInterfaceQueriesDAO.performStaffSubmissionsQuery(sortingType, assignmentId);

			f.endTransaction();
		
			// TODO: localisation
			templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
			templateContext.put("module", module);
			templateContext.put("assignment", assignment);
			templateContext.put("submissions", result);

			// loading additional links for plugin entry on a student assignment
			Collection<? extends IStaffAssignmentPluginEntryLink> pluginEntryLinks = Lookup.getDefault().lookupAll(IStaffAssignmentPluginEntryLink.class);
			if (!pluginEntryLinks.isEmpty()) {
				List<String> pluginLinks = new LinkedList<String>();
				List<String> pluginLinkAssParaStrs = new LinkedList<String>();
				List<String> pluginLinkLabels = new LinkedList<String>();
				for (IStaffAssignmentPluginEntryLink pluginLink : pluginEntryLinks) {
					pluginLinks.add(pageContext.getPageUrl(StaffPageFactory.SITE_NAME, pluginLink.getPageName()));
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
