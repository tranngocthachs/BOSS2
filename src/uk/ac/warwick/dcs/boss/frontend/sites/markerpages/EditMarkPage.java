package uk.ac.warwick.dcs.boss.frontend.sites.markerpages;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;

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
import uk.ac.warwick.dcs.boss.model.dao.IMarkDAO;
import uk.ac.warwick.dcs.boss.model.dao.IMarkerInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.IMarkingAssignmentDAO;
import uk.ac.warwick.dcs.boss.model.dao.IMarkingCategoryDAO;
import uk.ac.warwick.dcs.boss.model.dao.IPersonDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Mark;
import uk.ac.warwick.dcs.boss.model.dao.beans.MarkingAssignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.MarkingCategory;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;

public class EditMarkPage extends Page {

	public EditMarkPage() throws PageLoadException {
		super("marker_edit_mark", AccessLevel.USER);
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

		Mark original;
		MarkingAssignment originalParent;
		Assignment originalParentParent;
		Collection<MarkingCategory> markingCategories;
		Person student;
		
		try {
			if (pageContext.getParameter("create") != null) {
				// Get moduleId
				String markingAssignmentString = pageContext.getParameter("markingassignment");
				if (markingAssignmentString == null) {
					throw new ServletException("No markingassignment parameter given");
				}
				Long markingAssignmentId = Long
						.valueOf(pageContext.getParameter("markingassignment"));
				
				f.beginTransaction();	
				IMarkerInterfaceQueriesDAO markerInterfaceQueriesDao = f.getMarkerInterfaceQueriesDAOInstance();
				if (!markerInterfaceQueriesDao.isMarkerMarkingAssignmentAccessAllowed(pageContext.getSession().getPersonBinding().getId(), markingAssignmentId)) {
					f.abortTransaction();
					throw new ServletException("permission denied");
				}
				f.endTransaction();
				
				original = new Mark();
				original.setId(-1L);
				original.setMarkingAssignmentId(markingAssignmentId);
				original.setTimestamp(new Date());
				original.setValue(0);
				original.setComment("no comment");

				templateContext.put("create", true);
			} else {	
				// Get markId
				String markString = pageContext.getParameter("mark");
				if (markString == null) {
					throw new ServletException("No mark parameter given");
				}
				Long markId = Long
					.valueOf(pageContext.getParameter("mark"));
				
				
				f.beginTransaction();		
				IMarkerInterfaceQueriesDAO markerInterfaceQueriesDao = f.getMarkerInterfaceQueriesDAOInstance();
				IMarkDAO markDao = f.getMarkDAOInstance();
				original = markDao.retrievePersistentEntity(markId);
				if (!markerInterfaceQueriesDao.isMarkerMarkingAssignmentAccessAllowed(pageContext.getSession().getPersonBinding().getId(), original.getMarkingAssignmentId())) {
					f.abortTransaction();
					throw new ServletException("permission denied");
				}
				f.endTransaction();
				
				templateContext.put("create", false);
			}
			
			// Now get the marking assignment, assignment, student, and available markingcategories.
			f.beginTransaction();
			
			IMarkingAssignmentDAO markingAssignmentDao = f.getMarkingAssignmentDAOInstance();
			originalParent = markingAssignmentDao.retrievePersistentEntity(original.getMarkingAssignmentId());

			IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
			originalParentParent = assignmentDao.retrievePersistentEntity(originalParent.getAssignmentId());

			IMarkingCategoryDAO markingCategoryDao = f.getMarkingCategoryDAOInstance();			
			MarkingCategory example = new MarkingCategory();
			example.setAssignmentId(originalParent.getAssignmentId());
			markingCategories = markingCategoryDao.findPersistentEntitiesByExample(example);
			
			IPersonDAO personDao = f.getPersonDAOInstance();
			student = personDao.retrievePersistentEntity(originalParent.getStudentId());			
			
			f.endTransaction();
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao error", e);
		}
		
		// Got the original.
		templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
		templateContext.put("student", student);
		templateContext.put("assignment", originalParentParent);
		templateContext.put("markingAssignment", originalParent);
		templateContext.put("markingCategories", markingCategories);
		templateContext.put("mark", original);
		pageContext.renderTemplate(template, templateContext);
	}

	@Override
	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		throw new ServletException("unexpected POST");
	}

}
