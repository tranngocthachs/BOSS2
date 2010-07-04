package uk.ac.warwick.dcs.boss.model.dao;

import uk.ac.warwick.dcs.boss.model.dao.beans.spi.PluginEntity;

public interface IDAOSession {

	// Transactions
	/**
	 * Begin a transaction.  If one is already occuring in this thread, abort the existing one.
	 * Should lock the thread if another thread has a running transaction until that thread clears
	 * its transaction.
	 */
	abstract public void beginTransaction() throws DAOException;
	
	/**
	 * Initialise the storage (for example, create tables).
	 * @param deleteExisting is a flag that, when set true, will cause all existing data to be deleted.
	 * @throws DAOException
	 */
	abstract public void initialiseStorage(boolean deleteExisting) throws DAOException;
	
	/**
	 * Abort a transaction.  Any modifications done with DAOs from this factory will be discarded.
	 * Clears the lock, so other threads may start a transaction.  This function should be safe
	 * to call at any moment.
	 */
	abstract public void abortTransaction();
	
	/**
	 * Ends a transaction.  Any modifications done with DAOs from this factory will be committed.
	 * Clears the lock, so other threads may start a transaction.  This function requires a transaction
	 * to be running.
	 * @throws DAOException
	 */
	abstract public void endTransaction() throws DAOException;
	
	// DAOs
	abstract public IResourceDAO getResourceDAOInstance() throws DAOException;
	abstract public ISubmissionDAO getSubmissionDAOInstance() throws DAOException;
	abstract public IDeadlineRevisionDAO getDeadlineRevisionDAOInstance() throws DAOException;
	abstract public IModelDAO getModelDAOInstance() throws DAOException;
	abstract public IModuleDAO getModuleDAOInstance() throws DAOException;
	abstract public IPersonDAO getPersonDAOInstance() throws DAOException;
	abstract public IAssignmentDAO getAssignmentDAOInstance() throws DAOException;
	
	abstract public IMarkingCategoryDAO getMarkingCategoryDAOInstance() throws DAOException;
	abstract public IMarkDAO getMarkDAOInstance() throws DAOException;
	abstract public IMarkingAssignmentDAO getMarkingAssignmentDAOInstance() throws DAOException;
	abstract public ITestDAO getTestDAOInstance() throws DAOException;
	abstract public IResultDAO getResultDAOInstance() throws DAOException;
	
	abstract public IStudentInterfaceQueriesDAO getStudentInterfaceQueriesDAOInstance() throws DAOException;
	abstract public IMarkerInterfaceQueriesDAO getMarkerInterfaceQueriesDAOInstance() throws DAOException;
	abstract public IStaffInterfaceQueriesDAO getStaffInterfaceQueriesDAOInstance() throws DAOException;
	abstract public IAdminInterfaceQueriesDAO getAdminInterfaceQueriesDAOInstance() throws DAOException;
	
	abstract public IEntityDAO<? extends PluginEntity> getAdditionalDAOInstance(Class<? extends PluginEntity> clazz) throws DAOException;
	
	// i18n
	/**
	 * TODO: Localisation is not done in the slightest as of yet.
	 */
	abstract public ILocalisationDAO getLocalisationDAO() throws DAOException;
	
}
