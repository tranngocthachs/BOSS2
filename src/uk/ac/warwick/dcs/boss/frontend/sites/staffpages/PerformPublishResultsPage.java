package uk.ac.warwick.dcs.boss.frontend.sites.staffpages;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

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
import uk.ac.warwick.dcs.boss.model.dao.IMarkingCategoryDAO;
import uk.ac.warwick.dcs.boss.model.dao.IPersonDAO;
import uk.ac.warwick.dcs.boss.model.dao.IResultDAO;
import uk.ac.warwick.dcs.boss.model.dao.IStaffInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Mark;
import uk.ac.warwick.dcs.boss.model.dao.beans.MarkingCategory;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;
import uk.ac.warwick.dcs.boss.model.dao.beans.Result;

public class PerformPublishResultsPage extends Page {
	
	public PerformPublishResultsPage()
			throws PageLoadException {
		super("multi_edited", AccessLevel.USER);
	}

	@Override
	protected void handleGet(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		throw new ServletException("Unexpected GET");
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
		
		// Get assignmentId
		String assignmentString = pageContext.getParameter("assignment");
		if (assignmentString == null) {
			throw new ServletException("No assignment parameter given");
		}
		Long assignmentId = Long
				.valueOf(pageContext.getParameter("assignment"));
		
		// Redirect if not confirmed
		if (pageContext.getParameter("confirm") == null) {
			pageContext.performRedirect(pageContext.getPageUrl(StaffPageFactory.SITE_NAME, StaffPageFactory.PUBLISH_RESULTS_PAGE) + "?assignment=" + assignmentId + "&missing=true");
		}
		
	
		try {
			f.beginTransaction();
			
			IStaffInterfaceQueriesDAO staffInterfaceQueriesDao = f.getStaffInterfaceQueriesDAOInstance();
			IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
			Assignment assignment = assignmentDao.retrievePersistentEntity(assignmentId);
			
			if (!staffInterfaceQueriesDao.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), assignment.getModuleId())) {
				f.abortTransaction();
				throw new ServletException("permission denied");
			}

			// Obtain marking categories
			IMarkingCategoryDAO markingCategoryDAO = f.getMarkingCategoryDAOInstance();
			MarkingCategory exampleMarkingCategory = new MarkingCategory();
			exampleMarkingCategory.setAssignmentId(assignmentId);
			Collection<MarkingCategory> markingCategories = markingCategoryDAO.findPersistentEntitiesByExample(exampleMarkingCategory);			
			double totalWeighting = 0.0;
			for (MarkingCategory mc : markingCategories) {
				totalWeighting += (double)mc.getWeighting();
			}
			
			// Firstly, fetch people...
			Collection<Person> targets = assignmentDao.fetchSubmittersAndStudents(IPersonDAO.SortingType.UNIQUE_IDENTIFIER_ASCENDING, assignmentId);
			
			// Main results loop.
			IStaffInterfaceQueriesDAO staffInterfaceQueriesDAO = f.getStaffInterfaceQueriesDAOInstance();
			IResultDAO resultDAO = f.getResultDAOInstance();
			HashMap<MarkingCategory, Mark> marksMap = new HashMap<MarkingCategory, Mark>();

			for (Person p : targets) {			
				// Clear marks.
				marksMap.clear();
				
				// Obtain previous results, if they exist.
				Result exampleResult = new Result();
				exampleResult.setStudentId(p.getId());
				exampleResult.setAssignmentId(assignmentId);
				Collection<Result> originalResults = resultDAO.findPersistentEntitiesByExample(exampleResult);
				
				Result result;
				if (originalResults.size() == 0) {
					result = new Result();
				} else if (originalResults.size() == 1 ){
					result = originalResults.iterator().next();
				} else {
					throw new DAOException("database integrity error: more than one result for student " + p.getId() + " under assignment " + assignmentId);
				}
				
				// Obtain marks
				for (MarkingCategory mc : markingCategories) {
					marksMap.put(mc, staffInterfaceQueriesDAO.fetchLatestMarkForStudent(mc.getId(), p.getId()));
				}
				
				// Average results
				double resultValue = 0.0;
				boolean incompleteMarks = false;
				for (Map.Entry<MarkingCategory, Mark> e : marksMap.entrySet()) {
					if (e.getValue() != null) {
						double partialResult = (((double)e.getValue().getValue()) / ((double)e.getKey().getMaximumMark()));
						partialResult *= (((double)e.getKey().getWeighting()) / totalWeighting); 
						resultValue += partialResult;
					} else {
						incompleteMarks = true;
					}
				}
				
				// Percentage
				resultValue *= 100;
				
				// Round to 2 dp
				resultValue = (double)((int)(100 * resultValue)) / 100.0;
				
				// Construct result.
				result.setHadIncompleteMarking(incompleteMarks);				
				result.setResult(resultValue);
				result.setTimestamp(new Date());

				if (result.getId() == null) {
					result.setAssignmentId(assignmentId);
					result.setStudentId(p.getId());
					
					result.setId(resultDAO.createPersistentCopy(result));
				} else {
					resultDAO.updatePersistentEntity(result);
				}
			
			}

			f.endTransaction();
		
			// TODO: localisation
			templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
			templateContext.put("success", true);
			templateContext.put("nextPage", pageContext.getPageUrl(StaffPageFactory.SITE_NAME, StaffPageFactory.RESULTS_PAGE));
			templateContext.put("nextPageParamName", "assignment");
			templateContext.put("nextPageParamValue", assignmentId);

			
			pageContext.renderTemplate(template, templateContext);
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao exception", e);
		}
	}

}
