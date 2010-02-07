package uk.ac.warwick.dcs.boss.frontend.sites.studentpages;

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
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IStudentInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.StudentModulesQueryResult;

public class ModulesPage extends Page {
	
	public ModulesPage()
			throws PageLoadException {
		super("student_modules", AccessLevel.USER);
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
		
		// Ascertain sorting
		IStudentInterfaceQueriesDAO.StudentModulesQuerySortingType sortingType = null;
		if (pageContext.getParameter("sorting") != null) {
			if (pageContext.getParameter("sorting").equals("assignment_count_asc")) {
				sortingType = IStudentInterfaceQueriesDAO.StudentModulesQuerySortingType.ASSIGNMENT_COUNT_ASCENDING;
				templateContext.put("sorting", "assignment_count_asc");
			} else if (pageContext.getParameter("sorting").equals("assignment_count_desc")) {
				sortingType = IStudentInterfaceQueriesDAO.StudentModulesQuerySortingType.ASSIGNMENT_COUNT_DESCENDING;
				templateContext.put("sorting", "assignment_count_desc");
			} else if (pageContext.getParameter("sorting").equals("model_asc")) {
				sortingType = IStudentInterfaceQueriesDAO.StudentModulesQuerySortingType.MODEL_ID_ASCENDING;
				templateContext.put("sorting", "model_asc");
			} else if (pageContext.getParameter("sorting").equals("model_desc")) {
				sortingType = IStudentInterfaceQueriesDAO.StudentModulesQuerySortingType.MODEL_ID_DESCENDING;
				templateContext.put("sorting", "model_desc");
			} else if (pageContext.getParameter("sorting").equals("none")) {
				sortingType = IStudentInterfaceQueriesDAO.StudentModulesQuerySortingType.NONE;
				templateContext.put("sorting", "none");
			}  
		} else {
			sortingType = IStudentInterfaceQueriesDAO.StudentModulesQuerySortingType.NONE;
			templateContext.put("sorting", "none");
		}
		
		// Render page
		try {
			f.beginTransaction();
			
			IStudentInterfaceQueriesDAO dao = f.getStudentInterfaceQueriesDAOInstance();
			Collection<StudentModulesQueryResult> result = dao.performStudentModulesQuery(
					sortingType,
					pageContext.getSession().getPersonBinding().getId());

			f.endTransaction();
		
			// TODO: localisation
			templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
			templateContext.put("modules", result);

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
