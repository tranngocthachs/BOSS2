package uk.ac.warwick.dcs.boss.frontend.sites.staffpages;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;

import javax.servlet.ServletException;

import org.apache.log4j.Level;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

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
import uk.ac.warwick.dcs.boss.model.dao.IStaffInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.IStaffInterfaceQueriesDAO.StaffResultsQuerySortingType;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.StaffResultsQueryResult;
import uk.ac.warwick.dcs.boss.model.mail.IMailSender;
import uk.ac.warwick.dcs.boss.model.mail.MailException;
import uk.ac.warwick.dcs.boss.model.mail.MailFactory;

public class PerformEmailResultsPage extends Page {
	Template emailTemplate = null;
	
	public PerformEmailResultsPage()
			throws PageLoadException {
		super("multi_edited", AccessLevel.USER);
		try
		{
			emailTemplate = Velocity.getTemplate("email_result.vm.txt");
		}
		catch (ResourceNotFoundException e)
		{
			throw new PageLoadException(500, "Email template resource not found", e);
		}
		catch (ParseErrorException e)
		{
			throw new PageLoadException(500, "Email template could not be parsed", e);
		}
		catch (MethodInvocationException e)
		{
			throw new PageLoadException(500, "Email template-induced exception caught", e);
		}
		catch (Exception e)
		{
			throw new PageLoadException(500, "Misc. email template exception", e);			
		}

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
		IMailSender mailSender;
		try {
			DAOFactory df = (DAOFactory)FactoryRegistrar.getFactory(DAOFactory.class);
			MailFactory mf = (MailFactory)FactoryRegistrar.getFactory(MailFactory.class);
			f = df.getInstance();
			mailSender = mf.getInstance();
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
			pageContext.performRedirect(pageContext.getPageUrl(StaffPageFactory.SITE_NAME, StaffPageFactory.EMAIL_RESULTS_PAGE) + "?assignment=" + assignmentId + "&missing=true");
		}
		
			
		// Render page
		try {
			f.beginTransaction();
			
			IStaffInterfaceQueriesDAO staffInterfaceQueriesDAO = f.getStaffInterfaceQueriesDAOInstance();
			IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
			Assignment assignment = assignmentDao.retrievePersistentEntity(assignmentId);
			
			if (!staffInterfaceQueriesDAO.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), assignment.getModuleId())) {
				f.abortTransaction();
				throw new ServletException("permission denied");
			}			


			Collection<StaffResultsQueryResult> results = staffInterfaceQueriesDAO.performStaffResultsQuery(StaffResultsQuerySortingType.STUDENT_ID_ASCENDING, assignmentId);

			f.endTransaction();
		
			// We have the results, now mail them.
			for (StaffResultsQueryResult r : results) {
				if (pageContext.getParameter("result" + r.getResult().getId()) != null) {
					VelocityContext emailContext = new VelocityContext();
					emailContext.put("result", r);
					
					// Email the student.
					StringWriter pw = new StringWriter();
					emailTemplate.merge(emailContext, pw);
					pw.close();
					
					try {
						mailSender.sendMail(
								r.getStudent().getEmailAddress(),
								"Result (" + r.getAssignment().getName() + ")",
								pw.toString());
						pageContext.log(Level.INFO, "result email queued to " + r.getStudent().getEmailAddress());						
					} catch (MailException e) {
						pageContext.log(Level.ERROR, "result email NOT queued to " + r.getStudent().getEmailAddress());
						pageContext.log(Level.ERROR, e);
					}
				}
			}
						
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
