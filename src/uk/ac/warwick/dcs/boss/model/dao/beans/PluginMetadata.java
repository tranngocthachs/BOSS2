package uk.ac.warwick.dcs.boss.model.dao.beans;


public class PluginMetadata extends Entity {

	private String pluginId;
	private String name;
	private String author;
	private String email;
	private String version;
	private String description;
	private String[] libFilenames;
	private Boolean enable;
	private Boolean configurable;
	
	/**
	 * @return the pluginId
	 */
	public String getPluginId() {
		return pluginId;
	}
	/**
	 * @param pluginId the pluginId to set
	 */
	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}
	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}
	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}
	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the libFilenames
	 */
	public String[] getLibFilenames() {
		return libFilenames;
	}
	/**
	 * @param libFilenames the libFilenames to set
	 */
	public void setLibFilenames(String[] libFilenames) {
		this.libFilenames = libFilenames;
	}
	/**
	 * @return the enable
	 */
	public Boolean getEnable() {
		return enable;
	}
	/**
	 * @param enable the enable to set
	 */
	public void setEnable(Boolean enable) {
		this.enable = enable;
	}
	
	/**
	 * @return the configurable
	 */
	public Boolean getConfigurable() {
		return configurable;
	}
	
	/**
	 * @param configurable the configurable to set
	 */
	public void setConfigurable(Boolean configurable) {
		this.configurable = configurable;
	}
}
