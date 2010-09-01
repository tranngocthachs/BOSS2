package uk.ac.warwick.dcs.boss.frontend.sites;

import java.util.Iterator;

import org.openide.util.Lookup;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageFactory;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;
import uk.ac.warwick.dcs.boss.frontend.sites.adminpages.EditModelPage;
import uk.ac.warwick.dcs.boss.frontend.sites.adminpages.EditModulePage;
import uk.ac.warwick.dcs.boss.frontend.sites.adminpages.EditPersonPage;
import uk.ac.warwick.dcs.boss.frontend.sites.adminpages.ExecuteUtilityPage;
import uk.ac.warwick.dcs.boss.frontend.sites.adminpages.FindModuleAdministratorPage;
import uk.ac.warwick.dcs.boss.frontend.sites.adminpages.ModelsPage;
import uk.ac.warwick.dcs.boss.frontend.sites.adminpages.ModuleAdministratorsPage;
import uk.ac.warwick.dcs.boss.frontend.sites.adminpages.ModulesPage;
import uk.ac.warwick.dcs.boss.frontend.sites.adminpages.PeoplePage;
import uk.ac.warwick.dcs.boss.frontend.sites.adminpages.PerformAddModuleAdministratorPage;
import uk.ac.warwick.dcs.boss.frontend.sites.adminpages.PerformEditModelPage;
import uk.ac.warwick.dcs.boss.frontend.sites.adminpages.PerformEditModulePage;
import uk.ac.warwick.dcs.boss.frontend.sites.adminpages.PerformEditPersonPage;
import uk.ac.warwick.dcs.boss.frontend.sites.adminpages.PerformExecuteUtilityPage;
import uk.ac.warwick.dcs.boss.frontend.sites.adminpages.PerformFindModuleAdministratorPage;
import uk.ac.warwick.dcs.boss.frontend.sites.adminpages.PerformRemoveModuleAdministratorPage;
import uk.ac.warwick.dcs.boss.frontend.sites.adminpages.UtilitiesPage;
import uk.ac.warwick.dcs.boss.plugins.ConfigPluginPage;
import uk.ac.warwick.dcs.boss.plugins.PerformConfigPluginPage;
import uk.ac.warwick.dcs.boss.plugins.PerformEditPluginPage;
import uk.ac.warwick.dcs.boss.plugins.PluginPage;
import uk.ac.warwick.dcs.boss.plugins.PluginsPage;
import uk.ac.warwick.dcs.boss.plugins.spi.pages.IAdminPluginPage;

public class AdminPageFactory extends PageFactory {

	public static String SITE_NAME = "admin";

	public static String MODELS_PAGE = "models";
	public static String EDIT_MODEL_PAGE = "edit_model";
	public static String PERFORM_EDIT_MODEL_PAGE = "perform_edit_model";
	public static String MODULES_PAGE = "modules";
	public static String EDIT_MODULE_PAGE = "edit_module";
	public static String PERFORM_EDIT_MODULE_PAGE = "perform_edit_module";
	public static String PEOPLE_PAGE = "people";
	public static String EDIT_PERSON_PAGE = "edit_person";
	public static String PERFORM_EDIT_PERSON_PAGE = "perform_edit_person";
	public static String MODULE_ADMINISTRATORS_PAGE = "module_administrators";
	public static String FIND_MODULE_ADMINISTRATOR_PAGE = "find_module_administrator";
	public static String PERFORM_FIND_MODULE_ADMINISTRATOR_PAGE = "perform_find_module_administrator";
	public static String PERFORM_ADD_MODULE_ADMINISTRATOR_PAGE = "perform_add_module_administrator";
	public static String PERFORM_REMOVE_MODULE_ADMINISTRATOR_PAGE = "perform_remove_module_administrator";
	
	public static String UTILITIES_PAGE = "utilities";
	public static String EXECUTE_UTILITY_PAGE = "execute_utility";
	public static String PERFORM_EXECUTE_UTILITY_PAGE = "perform_execute_utility";
	
	public static String PLUGINS_PAGE = "plugins";
	public static String PERFORM_EDIT_PLUGIN_PAGE = "perform_edit_plugin";
	public static String CONFIG_PLUGIN_PAGE = "config_plugin";
	public static String PERFORM_CONFIG_PLUGIN_PAGE = "perform_config_plugin";
	
	@Override
	protected Page getPage(String pageName) throws PageLoadException {
		if (pageName.equals(MODELS_PAGE)) {
			return new ModelsPage();
		} else if (pageName.equals(EDIT_MODEL_PAGE)) {
			return new EditModelPage();
		} else if (pageName.equals(PERFORM_EDIT_MODEL_PAGE)) {
			return new PerformEditModelPage();
		} else if (pageName.equals(MODULES_PAGE)) {
			return new ModulesPage();
		} else if (pageName.equals(EDIT_MODULE_PAGE)) {
			return new EditModulePage();
		} else if (pageName.equals(PERFORM_EDIT_MODULE_PAGE)) {
			return new PerformEditModulePage();
		} else if (pageName.equals(MODULE_ADMINISTRATORS_PAGE)) {
			return new ModuleAdministratorsPage();
		} else if (pageName.equals(FIND_MODULE_ADMINISTRATOR_PAGE)) {
			return new FindModuleAdministratorPage();
		} else if (pageName.equals(PERFORM_FIND_MODULE_ADMINISTRATOR_PAGE)) {
			return new PerformFindModuleAdministratorPage();
		} else if (pageName.equals(PERFORM_ADD_MODULE_ADMINISTRATOR_PAGE)) {
			return new PerformAddModuleAdministratorPage();
		} else if (pageName.equals(PERFORM_REMOVE_MODULE_ADMINISTRATOR_PAGE)) {
			return new PerformRemoveModuleAdministratorPage();
		} else if (pageName.equals(PEOPLE_PAGE)) {
			return new PeoplePage();
		} else if (pageName.equals(EDIT_PERSON_PAGE)) {
			return new EditPersonPage();
		} else if (pageName.equals(PERFORM_EDIT_PERSON_PAGE)) {
			return new PerformEditPersonPage();
		} else if (pageName.equals(UTILITIES_PAGE)) {
			return new UtilitiesPage();
		} else if (pageName.equals(EXECUTE_UTILITY_PAGE)) {
			return new ExecuteUtilityPage();
		} else if (pageName.equals(PERFORM_EXECUTE_UTILITY_PAGE)) {
			return new PerformExecuteUtilityPage();
		} else if (pageName.equals(PLUGINS_PAGE)) {
			return new PluginsPage();
		} else if (pageName.equals(PERFORM_EDIT_PLUGIN_PAGE)) {
			return new PerformEditPluginPage();
		} else if (pageName.equals(CONFIG_PLUGIN_PAGE)) {
			return new ConfigPluginPage();
		} else if (pageName.equals(PERFORM_CONFIG_PLUGIN_PAGE)) {
			return new PerformConfigPluginPage();
		} else {
			Iterator<? extends IAdminPluginPage> adminPluginPagesIter = Lookup.getDefault().lookupAll(IAdminPluginPage.class).iterator();
			while (adminPluginPagesIter.hasNext()) {
				IAdminPluginPage provider = adminPluginPagesIter.next();
				if (provider.getPageName().equals(pageName))
					return new PluginPage(provider);
			}
			
			throw new PageLoadException(404, "page not found");
		}
	}

}
