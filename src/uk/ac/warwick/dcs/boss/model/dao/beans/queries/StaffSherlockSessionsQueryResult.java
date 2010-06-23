package uk.ac.warwick.dcs.boss.model.dao.beans.queries;

import uk.ac.warwick.dcs.boss.model.dao.beans.SherlockSession;

public class StaffSherlockSessionsQueryResult {
	private SherlockSession sherlockSession;
	private String[] requiredFiles;
	/**
	 * @return the sherlockSession
	 */
	public SherlockSession getSherlockSession() {
		return sherlockSession;
	}
	/**
	 * @param sherlockSession the sherlockSession to set
	 */
	public void setSherlockSession(SherlockSession sherlockSession) {
		this.sherlockSession = sherlockSession;
	}
	/**
	 * @return the requiredFiles
	 */
	public String[] getRequiredFiles() {
		return requiredFiles;
	}
	/**
	 * @param requiredFiles the requiredFiles to set
	 */
	public void setRequiredFiles(String[] requiredFiles) {
		this.requiredFiles = requiredFiles;
	}
}
