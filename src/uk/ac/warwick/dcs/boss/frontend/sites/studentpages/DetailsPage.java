package uk.ac.warwick.dcs.boss.frontend.sites.studentpages;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;

public class DetailsPage extends Page {
	
	public DetailsPage()
			throws PageLoadException {
		super("student_details", AccessLevel.USER);
	}

	@Override
	protected void handleGet(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		templateContext.put("missing", pageContext.getParameter("missing") != null);
		templateContext.put("mismatch", pageContext.getParameter("mismatch") != null);
		templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
		templateContext.put("student", pageContext.getSession().getPersonBinding());
			
		pageContext.renderTemplate(template, templateContext);
	}

	@Override
	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		throw new ServletException("Unexpected POST");
	}

}
