package uk.ac.warwick.dcs.boss.model.dao.beans;

import java.util.Date;

/**
 * A resource is a file stored by BOSS2.  It could be a library, course documentation, a submission...
 * 
 * The DAO handles the data streams.  This bean just stores metadata.  This is because it would be extremely
 * inefficient to store file contents as raw character arrays in most database systems.
 * @author davidbyard
 *
 */
public class Resource extends Entity {
	/**
	 * Mimetype of the resource.
	 */
	private String mimeType;
	
	/**
	 * Filename of the resource.
	 */
	private String filename;
	
	/**
	 * The time the resource was created.
	 */
	private Date timestamp;
	
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getMimeType() {
		return mimeType;
	}
}
