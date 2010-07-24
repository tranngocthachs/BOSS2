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
import uk.ac.warwick.dcs.boss.model.dao.IModuleDAO;
import uk.ac.warwick.dcs.boss.model.dao.IStudentInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.StudentAssignmentsQueryResult;
import uk.ac.warwick.dcs.boss.plugins.spi.extralinks.StudentPluginEntryLinkProvider;

public class AssignmentsPage extends Page {
	
	public AssignmentsPage()
			throws PageLoadException {
		super("student_assignments", AccessLevel.USER);
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
		
		IStudentInterfaceQueriesDAO.AssignmentStatus status = IStudentInterfaceQueriesDAO.AssignmentStatus.OPEN;
		Long moduleId = null;
		
		// Check if given a module
		if (pageContext.getParameter("module") != null) {
			moduleId = Long.valueOf(pageContext.getParameter("module"));
		}
		// Otherwise ascertain status filter
		else {
			if (pageContext.getParameter("show") != null) {
				if (pageContext.getParameter("show").equals("both")) {
					status = IStudentInterfaceQueriesDAO.AssignmentStatus.BOTH;
					templateContext.put("showing", "both");
				} else if (pageContext.getParameter("show").equals("closed")) {
					status = IStudentInterfaceQueriesDAO.AssignmentStatus.CLOSED;
					templateContext.put("showing", "closed");
				} else {
					templateContext.put("showing", "open");
				}
			} else {
				templateContext.put("showing", "open");
			}
		}
		
		// Ascertain sorting
		IStudentInterfaceQueriesDAO.StudentAssignmentsQuerySortingType sortingType = IStudentInterfaceQueriesDAO.StudentAssignmentsQuerySortingType.DEADLINE_DESCENDING;
		if (pageContext.getParameter("sorting") != null) {
			if (pageContext.getParameter("sorting").equals("deadline_asc")) {
				sortingType = IStudentInterfaceQueriesDAO.StudentAssignmentsQuerySortingType.DEADLINE_ASCENDING;
				templateContext.put("sorting", "deadline_asc");
			} else if (pageContext.getParameter("sorting").equals("deadline_desc")) {
				sortingType = IStudentInterfaceQueriesDAO.StudentAssignmentsQuerySortingType.DEADLINE_DESCENDING;
				templateContext.put("sorting", "deadline_desc");
			} else if (pageContext.getParameter("sorting").equals("last_submitted_asc")) {
				sortingType = IStudentInterfaceQueriesDAO.StudentAssignmentsQuerySortingType.LAST_SUBMITTED_ASCENDING;
				templateContext.put("sorting", "last_submitted_asc");
			} else if (pageContext.getParameter("sorting").equals("last_submitted_desc")) {
				sortingType = IStudentInterfaceQueriesDAO.StudentAssignmentsQuerySortingType.LAST_SUBMITTED_DESCENDING;
				templateContext.put("sorting", "last_submitted_desc");
			} else if (pageContext.getParameter("sorting").equals("closing_asc")) {
				sortingType = IStudentInterfaceQueriesDAO.StudentAssignmentsQuerySortingType.CLOSING_TIME_ASCENDING;
				templateContext.put("sorting", "closing_asc");
			} else if (pageContext.getParameter("sorting").equals("closing_desc")) {
				sortingType = IStudentInterfaceQueriesDAO.StudentAssignmentsQuerySortingType.CLOSING_TIME_DESCENDING;
				templateContext.put("sorting", "closing_desc");
			}
		} else {
			sortingType = IStudentInterfaceQueriesDAO.StudentAssignmentsQuerySortingType.DEADLINE_ASCENDING;
			templateContext.put("sorting", "deadline_asc");
		}
		
		// Render page
		try {
			f.beginTransaction();
			
			IStudentInterfaceQueriesDAO dao = f.getStudentInterfaceQueriesDAOInstance();
			
			Collection<StudentAssignmentsQueryResult> result = null;
			
			if (moduleId == null) {
				result = dao.performStudentAssignmentsQuery(
					status,
					sortingType,
					pageContext.getSession().getPersonBinding().getId());
				
				templateContext.put("moduleSpecified", false);
			} else {
				IModuleDAO moduleDao = f.getModuleDAOInstance();
				
				if (!dao.isStudentModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), moduleId)) {
					f.abortTransaction();
					throw new ServletException("permission denied");
				}
				
				Module module = moduleDao.retrievePersistentEntity(moduleId);
				result = dao.performStudentAssignmentsQuery(
						sortingType,
						pageContext.getSession().getPersonBinding().getId(),
						moduleId);
				templateContext.put("module", module);
				templateContext.put("moduleSpecified", true);
			}

			f.endTransaction();
		
			// TODO: localisation
			templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
			templateContext.put("assignments", result);
			
			// loading plugins' entry pages (if present)
			Collection<? extends StudentPluginEntryLinkProvider> extraLinkProviders = Lookup.getDefault().lookupAll(StudentPluginEntryLinkProvider.class);
			if (!extraLinkProviders.isEmpty()) {
				List<String> labels = new LinkedList<String>();
				List<String> links = new LinkedList<String>();
				for (StudentPluginEntryLinkProvider link : extraLinkProviders) {
					labels.add(link.getLinkLabel());
					links.add(pageContext.getPageUrl(StudentPageFactory.SITE_NAME, link.getEntryPageName()));
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
