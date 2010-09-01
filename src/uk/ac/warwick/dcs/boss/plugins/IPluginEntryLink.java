package uk.ac.warwick.dcs.boss.plugins;
/**
 * Plugin should not implement this. Instead, implement its subclasses.
 * @author tranngocthachs
 *
 */
public interface IPluginEntryLink {
	/**
	 * get the link label to be displayed in the core pages
	 * @return link label
	 */
	public String getLinkLabel();
	/**
	 * get the name of the destination page. URL will be constructed by .../&lt;site&gt;/&lt;pageName&gt;
	 * @return name of destination page
	 */
	public String getPageName();
}
