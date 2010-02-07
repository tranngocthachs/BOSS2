package uk.ac.warwick.dcs.boss.model.dao.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Vector;

import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.IResourceDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Resource;

public class MySQLResourceDAO extends MySQLEntityDAO<Resource> implements IResourceDAO {

	private String resourceDirectory = null;
	
	private HashSet<Long> modifiedResources = null;
	
	private String mySQLSortingString = "id DESC";


	public MySQLResourceDAO(Connection connection, String resourceDirectory, HashSet<Long> modifiedResources) throws DAOException {
		super(connection);
		this.resourceDirectory = resourceDirectory;
		this.modifiedResources = modifiedResources;
	}
	
	public String getTableName() {
		return "resource";
	}

	public void setSortingType(SortingType sortingType) throws DAOException {
		switch (sortingType) {
		case ID_ASC:
			this.mySQLSortingString = "id ASC";
			break;
		case NONE:
		case ID_DESC:
			this.mySQLSortingString = "id DESC";
			break;
		case FILENAME_ASCENDING:
			this.mySQLSortingString = "filename ASC";
			break;
		case FILENAME_DESCENDING:
			this.mySQLSortingString = "filename DESC";
			break;
		case TIMESTAMP_ASCENDING:
			this.mySQLSortingString = "timestamp ASC";
			break;			
		case TIMESTAMP_DESCENDING:
			this.mySQLSortingString = "timestamp DESC";
			break;
		default:
			throw new DAOException("Unsupported sorting type:" + sortingType);
		}
	}

	@Override
	public Resource createInstanceFromDatabaseValues(String tableName, ResultSet databaseValues)
	throws SQLException, DAOException {
		Resource resource = new Resource();
		resource.setFilename(databaseValues.getString(tableName + ".filename"));
		resource.setTimestamp(databaseValues.getTimestamp(tableName + ".timestamp"));
		resource.setMimeType(databaseValues.getString(tableName + ".mimetype"));

		return resource;
	}

	@Override
	public Collection<String> getDatabaseFieldNames() {
		Vector<String> fieldNames = new Vector<String>();
		fieldNames.add("filename");
		fieldNames.add("timestamp");
		fieldNames.add("mimetype");
		return fieldNames;
	}

	@Override
	public Collection<Object> getDatabaseValues(Resource entity) {
		Vector<Object> output = new Vector<Object>();
		output.add(entity.getFilename());
		output.add(entity.getTimestamp());
		output.add(entity.getMimeType());
		return output;
	}

	@Override
	public Long createPersistentCopy(Resource entity) throws DAOException {
		Long resourceId = super.createPersistentCopy(entity);
				
		File file = new File(resourceDirectory + File.separator + resourceId);
		try {
			if (!file.createNewFile()) {
				throw new IOException("createNewFile returned false for " + file);
			}
		} catch (IOException e) {
			try {
				super.deletePersistentEntity(resourceId);
			} catch (DAOException e2) {
				throw new DAOException("could not create resource file - additionally, could not delete redundant data object", e2);	
			}
			
			throw new DAOException("could not create resource file", e);
		}

		modifiedResources.add(resourceId);
		
		return resourceId;
	}
	
	@Override
	public void deletePersistentEntity(Long resourceId)
			throws DAOException {	
		if (modifiedResources.contains(resourceId)) {
			File newFile = new File(resourceDirectory + File.separator + resourceId);
			if (!newFile.delete()) {
				throw new DAOException("could not delete " + newFile);
			}
		} else {
			modifiedResources.add(resourceId);
			File newFile = new File(resourceDirectory + File.separator + resourceId);
			File oldFile = new File(resourceDirectory + File.separator + resourceId + ".old");
			if (!newFile.renameTo(oldFile)) {
				throw new DAOException("Could not rename " + newFile + " to " + oldFile);
			}
		}
		
		super.deletePersistentEntity(resourceId);
	}
	
	@Override
	public String getMySQLSortingString() {
		return mySQLSortingString;
	}
	
	public InputStream openInputStream(Long resourceId) throws DAOException {
		try {
			File file = new File(resourceDirectory + File.separator + resourceId);
			if (!file.exists()) {
				throw new DAOException("file not found: " + file);
			}
			return new FileInputStream(file);
		} catch (IOException e) {
			throw new DAOException("IO error", e);
		}
	}

	public OutputStream openOutputStream(Long resourceId) throws DAOException {
		if (modifiedResources.contains(resourceId)) {
			File oldFile = new File(resourceDirectory + File.separator + resourceId);
			if (!oldFile.delete()) {
				throw new DAOException("could not delete " + oldFile);
			}
		} else {
			modifiedResources.add(resourceId);
			File newFile = new File(resourceDirectory + File.separator + resourceId);
			File oldFile = new File(resourceDirectory + File.separator + resourceId + ".old");
			if (!newFile.renameTo(oldFile)) {
				throw new DAOException("Could not rename " + newFile + " to " + oldFile);
			}
		}

		try {
			File file = new File(resourceDirectory + File.separator + resourceId);			
			return new FileOutputStream(file);
		} catch (IOException e) {
			throw new DAOException("IO error", e);
		}
	}

	public static void abortTransaction(String resourceDirectory, HashSet<Long> modifiedResources) throws DAOException {
		for (Long modifiedResource : modifiedResources) {
			File oldFile = new File(resourceDirectory + File.separator + modifiedResource + ".old");
			File newFile = new File(resourceDirectory + File.separator + modifiedResource);
			if (newFile.exists()) {
				if (!newFile.delete()) {
					throw new DAOException("could not delete " + newFile);
				}
			}
			if (oldFile.exists()) {
				if (!oldFile.renameTo(newFile)) {
					throw new DAOException("Could not rename " + oldFile + " to " + newFile);
				}
			}

		}
		
		modifiedResources.clear();
	}
	
	public static void endTransaction(String resourceDirectory, HashSet<Long> modifiedResources) throws DAOException {
		for (Long modifiedResource : modifiedResources) {
			File oldFile = new File(resourceDirectory + File.separator + modifiedResource + ".old");
			if (oldFile.exists()) {
				if (!oldFile.delete()) {
					throw new DAOException("Could not delete " + oldFile);
				}
			}

		}
		
		modifiedResources.clear();
	}

}
