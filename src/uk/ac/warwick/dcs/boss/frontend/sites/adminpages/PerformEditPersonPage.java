package uk.ac.warwick.dcs.boss.frontend.sites.adminpages;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;
import uk.ac.warwick.dcs.boss.frontend.sites.AdminPageFactory;
import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.DAOFactory;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IPersonDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;

public class PerformEditPersonPage extends Page {

	public PerformEditPersonPage() throws PageLoadException {
		super("multi_edited", AccessLevel.ADMIN);
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
		
		// defs
		Person incoming = new Person();

		if (pageContext.getParameter("person") == null) {
			throw new ServletException("missing person parameter");
		}
		incoming.setId(Long.valueOf(pageContext.getParameter("person")));

		if (pageContext.getParameter("uniq") == null) {
			throw new ServletException("missing uniq parameter");
		}	
		incoming.setUniqueIdentifier(pageContext.getParameter("uniq"));

		if (pageContext.getParameter("name") == null) {
			throw new ServletException("missing name parameter");
		}
		incoming.setChosenName(pageContext.getParameter("name"));

		if (pageContext.getParameter("email") == null) {
			throw new ServletException("missing email parameter");
		}
		incoming.setEmailAddress(pageContext.getParameter("email"));

		if (pageContext.getParameter("admin") == null) {
			incoming.setAdministrator(false);
		} else {
			incoming.setAdministrator(true);
		}
				
		try {
			f.beginTransaction();

			if (pageContext.getParameter("create") != null) {	
				
				// create
				if (pageContext.getParameter("password") == null) {
					throw new ServletException("missing password parameter");
				}
				incoming.setPassword(Person.passwordHash(pageContext.getParameter("password")));

				IPersonDAO personDao = f.getPersonDAOInstance();
				incoming.setId(personDao.createPersistentCopy(incoming));
			} else {	
				if (pageContext.getParameter("delete") != null) {
					IPersonDAO personDao = f.getPersonDAOInstance();
					
					// Delete
					if (personDao.hasSubmissions(incoming.getId())) {
						throw new DAOException("Entity has children");
					}
					
					personDao.deletePersistentEntity(incoming.getId());
				} else {
					// update
					IPersonDAO personDao = f.getPersonDAOInstance();
					
					if (pageContext.getParameter("password") == null) {
						Person original = personDao.retrievePersistentEntity(incoming.getId());
						incoming.setPassword(original.getPassword());
					} else {
						incoming.setPassword(Person.passwordHash(pageContext.getParameter("password")));
					}
				
					personDao.updatePersistentEntity(incoming);
				}
			}

			// Done.
			f.endTransaction();
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao error", e);
		}

		// Got the original.
		templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
		templateContext.put("success", true);
		templateContext.put("nextPage", pageContext.getPageUrl(AdminPageFactory.SITE_NAME, AdminPageFactory.PEOPLE_PAGE));
		templateContext.put("nextPageParamName", "dummy");
		templateContext.put("nextPageParamValue", "nothing");
		pageContext.renderTemplate(template, templateContext);
	}
}
