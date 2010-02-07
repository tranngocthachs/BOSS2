package uk.ac.warwick.dcs.boss.frontend;

import java.util.HashMap;
import java.util.Map;

/**
 * A factory for holding pages that a user can visit.  A page is always under a particular site.
 * 
 * @author davidbyard
 *
 */
public abstract class PageFactory {

	static private Map<String, PageFactory> sitePages =
		new HashMap<String, PageFactory>(); 
	
	static public void registerFactory(String site, PageFactory f) {
		sitePages.put(site, f);
	}
	
	static public void clear() {
		sitePages.clear();
	}
	
	/**
	 * Get a page given a site and a page.
	 * @param site is the site name.
	 * @param pageName is the page name
	 * @return a valid Page object
	 * @throws PageLoadException if the page couldn't be loaded.
	 */
	static public Page getSitePage(String site, String pageName) 
		throws PageLoadException {
		if (!sitePages.containsKey(site)) {
			throw new PageLoadException(404, "Site not found.");
		}
		return sitePages.get(site).getPage(pageName);
	}
	
	/**
	 * Get a page for this instance.  The site has already been resolved by the static level function.
	 * @param pageName is the page to load
	 * @return a valid Page object
	 * @throws PageLoadException if the page couldn't be loaded.
	 */
	abstract protected Page getPage(String pageName)
		throws PageLoadException;
	
	
}
