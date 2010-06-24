package uk.ac.warwick.dcs.boss.model.dao;

import java.util.Collection;

import uk.ac.warwick.dcs.boss.model.dao.beans.SherlockSession;

public interface ISherlockSessionDAO extends IEntityDAO<SherlockSession> {
	public abstract void addRequiredFilenames(Long sherlockSessionId, Collection<String> fileNames) throws DAOException;
	public abstract Collection<String> fetchRequiredFilenames(Long sherlockSessionId) throws DAOException;
}
