package uk.ac.warwick.dcs.boss.frontend.sites.adminpages;

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
import uk.ac.warwick.dcs.boss.model.dao.IPersonDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;

public class PeoplePage extends Page {
	
	public PeoplePage()
			throws PageLoadException {
		super("admin_people", AccessLevel.ADMIN);
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
		IPersonDAO.SortingType sortingType = null;
		if (pageContext.getParameter("sorting") != null) {
			if (pageContext.getParameter("sorting").equals("unique_identifier_asc")) {
				sortingType = IPersonDAO.SortingType.UNIQUE_IDENTIFIER_ASCENDING;
				templateContext.put("sorting", "unique_identifier_asc");
			} else if (pageContext.getParameter("sorting").equals("unique_identifier_desc")) {
				sortingType = IPersonDAO.SortingType.UNIQUE_IDENTIFIER_DESCENDING;
				templateContext.put("sorting", "unique_identifier_desc");
			} else if (pageContext.getParameter("sorting").equals("administrator_asc")) {
				sortingType = IPersonDAO.SortingType.ADMINISTRATOR_ASCENDING;
				templateContext.put("sorting", "administrator_asc");
			} else if (pageContext.getParameter("sorting").equals("administrator_desc")) {
				sortingType = IPersonDAO.SortingType.ADMINISTRATOR_DESCENDING;
				templateContext.put("sorting", "administrator_desc");
			} else {
				sortingType = IPersonDAO.SortingType.NONE;
				templateContext.put("sorting", "none");				
			}
		} else {
			sortingType = IPersonDAO.SortingType.NONE;
			templateContext.put("sorting", "none");
		}
		
		// Render page
		try {
			f.beginTransaction();
			
			IPersonDAO dao = f.getPersonDAOInstance();
			
			dao.setSortingType(sortingType);
			Collection<Person> result = dao.retrieveAllPersistentEntities();

			f.endTransaction();
		
			// TODO: localisation
			templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
			templateContext.put("people", result);

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
