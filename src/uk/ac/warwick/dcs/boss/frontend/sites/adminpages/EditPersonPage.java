package uk.ac.warwick.dcs.boss.frontend.sites.adminpages;

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
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IPersonDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;

public class EditPersonPage extends Page {

	public EditPersonPage() throws PageLoadException {
		super("admin_edit_person", AccessLevel.ADMIN);
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
		
		// defs
		Person original;
				
		try {
			if (pageContext.getParameter("create") != null) {			
				original = new Person();
				original.setId(-1L);
				original.setUniqueIdentifier("person");
				original.setChosenName("a. n. other");
				original.setEmailAddress("a.n@other.example");
				original.setPassword("");
				
				templateContext.put("create", true);
			} else {	
				// Get personId
				String personString = pageContext.getParameter("person");
				if (personString == null) {
					throw new ServletException("No person parameter given");
				}
				Long personId = Long
					.valueOf(pageContext.getParameter("person"));
				
				f.beginTransaction();		
				IPersonDAO personDao = f.getPersonDAOInstance();
				original = personDao.retrievePersistentEntity(personId);
				templateContext.put("hasChildren", personDao.hasSubmissions(personId));
				f.endTransaction();
				
				templateContext.put("create", false);
			}			
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao error", e);
		}
				
		// Got the original.
		templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
		templateContext.put("person", original);
		pageContext.renderTemplate(template, templateContext);
	}

	@Override
	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		throw new ServletException("unexpected POST");
	}
	
}
