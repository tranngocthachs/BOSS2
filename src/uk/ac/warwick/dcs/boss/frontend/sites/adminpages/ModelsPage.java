package uk.ac.warwick.dcs.boss.frontend.sites.adminpages;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.openide.util.Lookup;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;
import uk.ac.warwick.dcs.boss.frontend.sites.AdminPageFactory;
import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.DAOFactory;
import uk.ac.warwick.dcs.boss.model.dao.IAdminInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.IAdminInterfaceQueriesDAO.AdminModelsQuerySortingType;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.AdminModelsQueryResult;
import uk.ac.warwick.dcs.boss.plugins.spi.extralinks.AdminPluginEntryLinkProvider;

public class ModelsPage extends Page {
	
	public ModelsPage()
			throws PageLoadException {
		super("admin_models", AccessLevel.ADMIN);
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
		AdminModelsQuerySortingType sortingType = null;
		if (pageContext.getParameter("sorting") != null) {
			if (pageContext.getParameter("sorting").equals("module_count_asc")) {
				sortingType = AdminModelsQuerySortingType.MODULE_COUNT_ASCENDING;
				templateContext.put("sorting", "module_count_asc");
			} else if (pageContext.getParameter("sorting").equals("module_count_desc")) {
				sortingType = AdminModelsQuerySortingType.MODULE_COUNT_DESCENDING;
				templateContext.put("sorting", "module_count_desc");
			} else {
				sortingType = AdminModelsQuerySortingType.NONE;
				templateContext.put("sorting", "none");
			}
		} else {
			sortingType = AdminModelsQuerySortingType.NONE;
			templateContext.put("sorting", "none");
		}
		
		// Render page
		try {
			f.beginTransaction();
			
			IAdminInterfaceQueriesDAO dao = f.getAdminInterfaceQueriesDAOInstance();
			
			Collection<AdminModelsQueryResult> result = dao.performAdminModelsQuery(sortingType);

			f.endTransaction();
		
			// TODO: localisation
			templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
			templateContext.put("models", result);
			
			// loading plugins' entry pages (if present)
			Collection<? extends AdminPluginEntryLinkProvider> extraLinkProviders = Lookup.getDefault().lookupAll(AdminPluginEntryLinkProvider.class);
			if (!extraLinkProviders.isEmpty()) {
				List<String> labels = new LinkedList<String>();
				List<String> links = new LinkedList<String>();
				for (AdminPluginEntryLinkProvider link : extraLinkProviders) {
					labels.add(link.getLinkLabel());
					links.add(pageContext.getPageUrl(AdminPageFactory.SITE_NAME, link.getEntryPageName()));
				}
				templateContext.put("extraLinks", links);
				templateContext.put("extraLabels", labels);
			}
			
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
