package uk.ac.warwick.dcs.boss.model.dao.beans;

import java.util.jar.JarFile;

public class PluginMetadata extends Entity {

	private String pluginId;
	private String author;
	private String email;
	private int version;
	private String description;
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
	public int getVersion() {
		return version;
	}
	/**
	 * @param version the version to set
	 */
	public void setVersion(int version) {
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
}
