package uk.ac.warwick.dcs.boss.frontend.sites.studentpages;

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
import uk.ac.warwick.dcs.boss.frontend.sites.StudentPageFactory;
import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.DAOFactory;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IStudentInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.StudentSubmissionsQueryResult;
import uk.ac.warwick.dcs.boss.plugins.spi.extralinks.StudentSubmissionPluginEntryProvider;

public class AssignmentSubmissionsPage extends Page {

	public AssignmentSubmissionsPage() throws PageLoadException {
		super("student_assignment_submissions", AccessLevel.USER);
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
		String assignmentString = pageContext.getParameter("assignment");
		if (assignmentString == null) {
			throw new ServletException("No assignment parameter given");
		}
		Long assignmentId = Long
				.valueOf(pageContext.getParameter("assignment"));

		// Get the sorting
		IStudentInterfaceQueriesDAO.StudentSubmissionsQuerySortingType sortingType = null;
		if (pageContext.getParameter("sorting") != null) {
			if (pageContext.getParameter("sorting").equals("submission_time_asc")) {
				sortingType = IStudentInterfaceQueriesDAO.StudentSubmissionsQuerySortingType.SUBMISSION_TIME_ASCENDING;
				templateContext.put("sorting", "submission_time_asc");
			} else if (pageContext.getParameter("sorting").equals(
					"submission_time_desc")) {
				sortingType = IStudentInterfaceQueriesDAO.StudentSubmissionsQuerySortingType.SUBMISSION_TIME_DESCENDING;
				templateContext.put("sorting", "submission_time_desc");
			} else if (pageContext.getParameter("sorting").equals("active_asc")) {
				sortingType = IStudentInterfaceQueriesDAO.StudentSubmissionsQuerySortingType.ACTIVE_ASCENDING;
				templateContext.put("sorting", "active_asc");				
			} else if (pageContext.getParameter("sorting").equals("active_desc")) {
				sortingType = IStudentInterfaceQueriesDAO.StudentSubmissionsQuerySortingType.ACTIVE_DESCENDING;
				templateContext.put("sorting", "active_desc");				
			}

		} else {
			sortingType = IStudentInterfaceQueriesDAO.StudentSubmissionsQuerySortingType.SUBMISSION_TIME_DESCENDING;
			templateContext.put("sorting", "submission_time_desc");
		}

		
		// Render page
		try {
			f.beginTransaction();

			IStudentInterfaceQueriesDAO dao = f
					.getStudentInterfaceQueriesDAOInstance();
			Collection<StudentSubmissionsQueryResult> result = dao
					.performStudentSubmissionsQuery(
							sortingType,
							pageContext.getSession().getPersonBinding().getId(),
							assignmentId);

			f.endTransaction();

			// TODO: localisation
			templateContext.put("greet", pageContext.getSession()
					.getPersonBinding().getChosenName());
			if (!result.isEmpty()) {
				templateContext.put("assignment", result.iterator().next()
						.getAssignment());
			}
			templateContext.put("submissions", result);
			templateContext.put("assignmentId", assignmentId);
			
			// loading additional buttons for plugin actions on a student submission
			Collection<? extends StudentSubmissionPluginEntryProvider> pluginEntryLinks = Lookup.getDefault().lookupAll(StudentSubmissionPluginEntryProvider.class);
			if (!pluginEntryLinks.isEmpty()) {
				List<String> pluginLinks = new LinkedList<String>();
				List<String> pluginLinkSubmissionParaStrs = new LinkedList<String>();
				List<String> pluginLinkLabels = new LinkedList<String>();
				for (StudentSubmissionPluginEntryProvider pluginLink : pluginEntryLinks) {
					pluginLinks.add(pageContext.getPageUrl(StudentPageFactory.SITE_NAME, pluginLink.getEntryPageName()));
					pluginLinkSubmissionParaStrs.add(pluginLink.getSubmissionParaString());
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
