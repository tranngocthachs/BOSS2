package uk.ac.warwick.dcs.boss.frontend.sites.staffpages;

import java.io.IOException;

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
import uk.ac.warwick.dcs.boss.model.dao.IMarkingCategoryDAO;
import uk.ac.warwick.dcs.boss.model.dao.IModuleDAO;
import uk.ac.warwick.dcs.boss.model.dao.IStaffInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.MarkingCategory;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;

public class EditMarkingCategoryPage extends Page {

	public EditMarkingCategoryPage() throws PageLoadException {
		super("staff_edit_markingcategory", AccessLevel.USER);
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

		MarkingCategory original;
		Assignment originalParent;
		Module originalParentParent;
		
		try {
			if (pageContext.getParameter("create") != null) {
				// Get assignmentId
				String assignmentString = pageContext.getParameter("assignment");
				if (assignmentString == null) {
					throw new ServletException("No assignment parameter given");
				}
				Long assignmentId = Long
						.valueOf(pageContext.getParameter("assignment"));
				
				f.beginTransaction();
				IStaffInterfaceQueriesDAO staffInterfaceQueriesDao = f.getStaffInterfaceQueriesDAOInstance();
				IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
				originalParent = assignmentDao.retrievePersistentEntity(assignmentId);
				if (!staffInterfaceQueriesDao.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), originalParent.getModuleId())) {
					f.abortTransaction();
					throw new ServletException("permission denied");
				}
				f.endTransaction();
				
				original = new MarkingCategory();
				original.setId(-1L);
				original.setMaximumMark(100L);
				original.setName("New Marking Category");
				original.setWeighting(1L);
				original.setAssignmentId(assignmentId);
				
				templateContext.put("create", true);
			} else {	
				// Get markingCategoryId
				String markingCategoryString = pageContext.getParameter("markingcategory");
				if (markingCategoryString == null) {
					throw new ServletException("No markingcategory parameter given");
				}
				Long markingCategoryId = Long
					.valueOf(pageContext.getParameter("markingcategory"));
				
				
				f.beginTransaction();
				IStaffInterfaceQueriesDAO staffInterfaceQueriesDao = f.getStaffInterfaceQueriesDAOInstance();
				IMarkingCategoryDAO markingCategoryDao = f.getMarkingCategoryDAOInstance();
				IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
				
				original = markingCategoryDao.retrievePersistentEntity(markingCategoryId);
				originalParent = assignmentDao.retrievePersistentEntity(original.getAssignmentId());
				
				if (!staffInterfaceQueriesDao.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), originalParent.getModuleId())) {
					f.abortTransaction();
					throw new ServletException("permission denied");
				}

				f.endTransaction();
				
				templateContext.put("create", false);
			}
			
			// Now get the assignment.
			f.beginTransaction();
			IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
			originalParent = assignmentDao.retrievePersistentEntity(original.getAssignmentId());
			IModuleDAO moduleDao = f.getModuleDAOInstance();
			originalParentParent = moduleDao.retrievePersistentEntity(originalParent.getModuleId());
			f.endTransaction();
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao error", e);
		}
		
		// Got the original.
		templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
		templateContext.put("module", originalParentParent);
		templateContext.put("assignment", originalParent);
		templateContext.put("markingCategory", original);
		pageContext.renderTemplate(template, templateContext);
	}

	@Override
	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		throw new ServletException("unexpected POST");
	}

}
