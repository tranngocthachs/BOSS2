package uk.ac.warwick.dcs.boss.frontend.sites.staffpages;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.log4j.Level;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;
import uk.ac.warwick.dcs.boss.frontend.sites.StaffPageFactory;
import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
import uk.ac.warwick.dcs.boss.model.autoassignment.AutoAssignmentException;
import uk.ac.warwick.dcs.boss.model.autoassignment.IAutoAssignmentMethod;
import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.DAOFactory;
import uk.ac.warwick.dcs.boss.model.dao.IAssignmentDAO;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IMarkingAssignmentDAO;
import uk.ac.warwick.dcs.boss.model.dao.IModuleDAO;
import uk.ac.warwick.dcs.boss.model.dao.IPersonDAO;
import uk.ac.warwick.dcs.boss.model.dao.IStaffInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.MarkingAssignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;

public class PerformAutoAssignMarkersPage extends Page {

	public PerformAutoAssignMarkersPage() throws PageLoadException {
		super("multi_edited", AccessLevel.USER);
	}

	@Override
	protected void handleGet(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {

		throw new ServletException("unexpected GET");
	}

	@Override
	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		IDAOSession f;
		try {
			DAOFactory df = (DAOFactory)FactoryRegistrar.getFactory(DAOFactory.class);
			f = df.getInstance();
		} catch (FactoryException e) {
			throw new ServletException("dao init error", e);
		}

		if (pageContext.getParameter("assignment") == null) {
			throw new ServletException("missing assignment parameter");
		}
		Long assignmentId = Long.valueOf(pageContext.getParameter("assignment"));

		if (pageContext.getParameter("method") == null) {
			throw new ServletException("missing name parameter");
		}
		String method = pageContext.getParameter("method");
			
		try {
			f.beginTransaction();

			IStaffInterfaceQueriesDAO staffInterfaceQueriesDao = f.getStaffInterfaceQueriesDAOInstance();
			IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
			Assignment assignment = assignmentDao.retrievePersistentEntity(assignmentId);
			
			if (!staffInterfaceQueriesDao.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), assignment.getModuleId())) {
				f.abortTransaction();
				throw new ServletException("permission denied");
			}

			// Get the method.
			Class<? extends IAutoAssignmentMethod> methodClass = Class.forName(method).asSubclass(IAutoAssignmentMethod.class);
			IAutoAssignmentMethod methodInstance = methodClass.newInstance();
			
			// Clear original marking assignments.
			IMarkingAssignmentDAO markingAssignmentDAO = f.getMarkingAssignmentDAOInstance();
			
			// Retrieve information required for auto-assignment.
			IModuleDAO moduleDao = f.getModuleDAOInstance();
			LinkedList<Person> students = new LinkedList<Person>();
			for (Person p : moduleDao.fetchStudents(IPersonDAO.SortingType.NONE, assignment.getModuleId())) {
				students.add(p);
			}
			
			LinkedList<Person> markers = new LinkedList<Person>();
			for (Person p : assignmentDao.fetchMarkers(IPersonDAO.SortingType.NONE, assignmentId)) {
				markers.add(p);
			}
			
			// Obtain mapping.
			Map<Person, Collection<Person>> mapping = methodInstance.createMarkerToStudentsMap(markers, students);
			pageContext.log(Level.DEBUG, mapping.entrySet().size() + " mappings");
			
			// Generate the marking assignments.
			for (Map.Entry<Person, Collection<Person>> entry : mapping.entrySet()) {
				pageContext.log(Level.DEBUG, entry.getKey().getUniqueIdentifier() + " has " + entry.getValue().size() + " students");
				Person marker = entry.getKey();
				Collection<Person> markerStudents = entry.getValue();
				for (Person student : markerStudents) {
					MarkingAssignment example = new MarkingAssignment();
					example.setAssignmentId(assignmentId);
					example.setStudentId(student.getId());
					example.setMarkerId(marker.getId());
					Collection<MarkingAssignment> originalAssignments = markingAssignmentDAO.findPersistentEntitiesByExample(example);
					if (originalAssignments.isEmpty()) {				
						MarkingAssignment markingAssignment = new MarkingAssignment();
						markingAssignment.setMarkerId(marker.getId());
						markingAssignment.setAssignmentId(assignmentId);
						markingAssignment.setModerator(false);
						markingAssignment.setStudentId(student.getId());
						markingAssignment.setBlind(false);
						markingAssignment.setId(markingAssignmentDAO.createPersistentCopy(markingAssignment));
					} else {
						pageContext.log(Level.DEBUG, "Skipping " + marker.getUniqueIdentifier() + " to " + student.getChosenName() + " mapping as it already exists");
					}
				}
			}
			
			// Done.
			f.endTransaction();
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao error", e);
		} catch (ClassCastException e) {
			f.abortTransaction();
			throw new ServletException("method instantiation error", e);
		} catch (ClassNotFoundException e) {
			f.abortTransaction();
			throw new ServletException("method instantiation error", e);			
		} catch (InstantiationException e) {
			f.abortTransaction();
			throw new ServletException("method instantiation error", e);
		} catch (IllegalAccessException e) {
			f.abortTransaction();
			throw new ServletException("method instantiation error", e);			
		} catch (AutoAssignmentException e) {
			f.abortTransaction();
			throw new ServletException("auto assignment error", e);
		}

		// Got the original.
		templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
		templateContext.put("success", true);
		
		templateContext.put("nextPage", pageContext.getPageUrl(StaffPageFactory.SITE_NAME, StaffPageFactory.MARKING_ASSIGNMENTS_PAGE));
		templateContext.put("nextPageParamName", "assignment");
		templateContext.put("nextPageParamValue", assignmentId);
		
			pageContext.renderTemplate(template, templateContext);
	}
}
