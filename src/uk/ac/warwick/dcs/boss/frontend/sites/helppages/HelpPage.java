package uk.ac.warwick.dcs.boss.frontend.sites.helppages;

import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.ServletException;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;

public class HelpPage extends Page {

	Template helpTemplate = null;
	
	public HelpPage(String pageName) throws PageLoadException {
		super("help", AccessLevel.NONE);
		try
		{
			helpTemplate = Velocity.getTemplate(pageName + ".help.vm.html");
		}
		catch (ResourceNotFoundException e)
		{
			throw new PageLoadException(404, "Help template resource not found", e);
		}
		catch (ParseErrorException e)
		{
			throw new PageLoadException(500, "Help template could not be parsed", e);
		}
		catch (MethodInvocationException e)
		{
			throw new PageLoadException(500, "Help template-induced exception caught", e);
		}
		catch (Exception e)
		{
			throw new PageLoadException(500, "Misc. help template exception", e);			
		}
	}

	public void handleGet(PageContext pageContext, Template template, VelocityContext templateContext) throws ServletException,
			IOException {
		StringWriter pw = new StringWriter();
		helpTemplate.merge(templateContext, pw);
		pw.close();

		templateContext.put("helpText", pw.toString());
		pageContext.renderTemplate(template, templateContext);
	}
	
	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		throw new ServletException("Not implemented yet");
	}
	

}
