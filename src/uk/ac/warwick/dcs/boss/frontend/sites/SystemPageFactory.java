package uk.ac.warwick.dcs.boss.frontend.sites;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageFactory;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;
import uk.ac.warwick.dcs.boss.frontend.sites.systempages.HomePage;
import uk.ac.warwick.dcs.boss.frontend.sites.systempages.LoggedInPage;
import uk.ac.warwick.dcs.boss.frontend.sites.systempages.LoginPage;
import uk.ac.warwick.dcs.boss.frontend.sites.systempages.LogoutPage;
import uk.ac.warwick.dcs.boss.frontend.sites.systempages.NewPasswordPage;
import uk.ac.warwick.dcs.boss.frontend.sites.systempages.NotAdminPage;
import uk.ac.warwick.dcs.boss.frontend.sites.systempages.NotFoundPage;
import uk.ac.warwick.dcs.boss.frontend.sites.systempages.PerformNewPasswordPage;
import uk.ac.warwick.dcs.boss.frontend.sites.systempages.RegisterPage;

public class SystemPageFactory extends PageFactory {

	public static String SITE_NAME = "system";
	
	public static String HOME_PAGE = "home";
	public static String NOT_FOUND_PAGE = "notfound";
	public static String LOGIN_PAGE = "login";
	public static String LOGOUT_PAGE = "logout";
	public static String LOGGED_IN_PAGE = "logged_in";
	public static String REGISTER_PAGE = "register";
	public static String NEW_PASSWORD_PAGE = "new_password";
	public static String PERFORM_NEW_PASSWORD_PAGE = "perform_new_password";
	public static String NOT_ADMIN_PAGE = "not_admin";
	
	@Override
	protected Page getPage(String pageName) throws PageLoadException {
		if (pageName.equals(HOME_PAGE)) {
			return new HomePage();
		} else if (pageName.equals(NOT_FOUND_PAGE)) {
			return new NotFoundPage();
		} else if (pageName.equals(LOGIN_PAGE)) {
			return new LoginPage();
		} else if (pageName.equals(LOGOUT_PAGE)) {
			return new LogoutPage();
		} else if (pageName.equals(LOGGED_IN_PAGE)) {
			return new LoggedInPage();
		} else if (pageName.equals(REGISTER_PAGE)) {
			return new RegisterPage();
		} else if (pageName.equals(NEW_PASSWORD_PAGE)) {
			return new NewPasswordPage();
		} else if (pageName.equals(PERFORM_NEW_PASSWORD_PAGE)) {
			return new PerformNewPasswordPage();
		} else if (pageName.equals(NOT_ADMIN_PAGE)) {
			return new NotAdminPage();
		} else {
			throw new PageLoadException(404, "Unknown page identifier");
		}
	}

}
