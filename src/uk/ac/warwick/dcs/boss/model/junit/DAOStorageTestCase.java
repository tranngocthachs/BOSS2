package uk.ac.warwick.dcs.boss.model.junit;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;


import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.DAOFactory;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IResourceDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Resource;
import junit.framework.TestCase;

public class DAOStorageTestCase extends TestCase {

	public final int testRuns = 10;
	public final int fileCount = 1000;
	Random random;
	IDAOSession f;

	protected void setUp() throws Exception {
		super.setUp();
		DAOFactory df = (DAOFactory)FactoryRegistrar.getFactory(DAOFactory.class);
		f = df.getInstance();
		f.beginTransaction();
		f.initialiseStorage(true);
		f.endTransaction();
		random = new Random(new Date().getTime());
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		f.abortTransaction();
	}

	public void testStorage() throws DAOException, IOException {
		IResourceDAO dao;
		LinkedList<Long> transactionIds = new LinkedList<Long>();
		HashSet<Long> createdIds = new HashSet<Long>();

		// Creating
		for (int i = 0; i < testRuns; i++) {
			transactionIds.clear();
			f.beginTransaction();
			dao = f.getResourceDAOInstance();
	
			for (int j = 0; j < fileCount; j++) {
				transactionIds.add(createResource(dao));
			}
	
			if (random.nextInt(2) == 0) {
				f.abortTransaction();
			} else {
				f.endTransaction();
				for (Long id : transactionIds) {
					if (createdIds.contains(id)) {
						fail("Duplicate id");
					} else {
						createdIds.add(id);
					}
	
				}
			}
		}
				
		// Reading
		f.beginTransaction();
		dao = f.getResourceDAOInstance();
		for (Long id : createdIds) {
			verifyResource(dao, id, 1);
		}
		f.abortTransaction();
		
		// Editing/deleting
		HashSet<Long> editedIds = new HashSet<Long>();
		HashSet<Long> deletedIds = new HashSet<Long>();
		HashSet<Long> commitedEditedIds = new HashSet<Long>();
		HashSet<Long> commitedOriginalIds = new HashSet<Long>();
		f.beginTransaction();
		dao = f.getResourceDAOInstance();
		transactionIds.clear();
		int counter = 0;
		for (Long id : createdIds) {
			// Edit resource.
			editResource(dao, id, 3);
			editedIds.add(id);
			
			// Delete about half
			if (random.nextInt(2) == 0) {
				dao.deletePersistentEntity(id);
				deletedIds.add(id);
			}
			
			// Randomly abort transaction now and then
			if (++counter == 10) {
				counter = 0;
				if (random.nextInt(2) == 0) {
					f.abortTransaction();
					for (Long editedId : editedIds) {
						commitedOriginalIds.add(editedId);
					}
					editedIds.clear();
					deletedIds.clear();
					f.beginTransaction();
					dao = f.getResourceDAOInstance();
				} else {
					f.endTransaction();
					for (Long editedId : editedIds) {
						commitedEditedIds.add(editedId);
					}
					for (Long deletedId : deletedIds) {
						commitedEditedIds.remove(deletedId);
					}
					editedIds.clear();
					deletedIds.clear();
					f.beginTransaction();
					dao = f.getResourceDAOInstance();
				}
			}
		}
		f.endTransaction();
		for (Long editedId : editedIds) {
			commitedEditedIds.add(editedId);
		}
		for (Long deletedId : deletedIds) {
			commitedEditedIds.remove(deletedId);
		}
		editedIds.clear();
		deletedIds.clear();

		// Final check
		f.beginTransaction();
		dao = f.getResourceDAOInstance();
		for (Long id : commitedEditedIds) {
			verifyResource(dao, id, 3);
		}
		for (Long id : commitedOriginalIds) {
			verifyResource(dao, id, 1);
		}
		f.abortTransaction();

	}

	private void editResource(IResourceDAO dao, Long id, int multiply) throws DAOException, IOException {
		InputStream is = dao.openInputStream(id);
		ObjectInputStream ois = new ObjectInputStream(is);
		Long writtenId = ois.readLong();
		ois.close();
		is.close();
		((FileInputStream)is).close();
		
		OutputStream os = dao.openOutputStream(id);
		ObjectOutputStream oos = new ObjectOutputStream(os);
		oos.writeLong(writtenId * multiply);
		oos.flush();
		oos.close();
		os.flush();
		os.close();
		((FileOutputStream)os).close();
	}
	
	private void verifyResource(IResourceDAO dao, Long id, int multiply) throws DAOException, IOException {
		InputStream is = dao.openInputStream(id);
		ObjectInputStream ois = new ObjectInputStream(is);
		Long writtenId = ois.readLong();
		ois.close();
		is.close();
		((FileInputStream)is).close();
		
		assertEquals((Long)(id * multiply), (Long)writtenId);
	}
	
	private Long createResource(IResourceDAO dao) throws DAOException,
			IOException {
		Resource newResource = new Resource();
		newResource.setFilename(String.valueOf(random.nextInt()) + ".txt");
		newResource.setTimestamp(new Date());
		newResource.setMimeType("application/binary");
		Long id = dao.createPersistentCopy(newResource);

		OutputStream os = dao.openOutputStream(id);
		ObjectOutputStream oos = new ObjectOutputStream(os);
		try {
			oos.writeLong(id);
		} catch (UnsupportedEncodingException e) {
			throw new Error(e);
		}
		oos.flush();
		oos.close();
		os.flush();
		os.close();
		
		return id;
	}
}
