package uk.ac.warwick.dcs.boss.model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.IAdminInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Model;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.AdminModelsQueryResult;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.AdminModulesQueryResult;

public class MySQLAdminInterfaceQueriesDAO implements IAdminInterfaceQueriesDAO {
	
	private Connection connection;

	public MySQLAdminInterfaceQueriesDAO(Connection connection) throws DAOException {
		this.connection = connection;
	}

	public Collection<AdminModelsQueryResult> performAdminModelsQuery(
			AdminModelsQuerySortingType sortingType)
			throws DAOException {
		//  Begin the transaction
		String orderingString = null;
		switch (sortingType) {
		case NONE:
			orderingString = "";
			break;
		case MODULE_COUNT_ASCENDING:
			orderingString = "ORDER BY n_mod ASC";
			break;
		case MODULE_COUNT_DESCENDING:
			orderingString = "ORDER BY n_mod DESC";
			break;
		}
		
		try {
			// Construct the statement.
			String statementString = "SELECT model.*, COUNT(module.id) n_mod"
				+ " FROM model"
				+ " LEFT JOIN module ON module.model_id = model.id"
				+ " GROUP BY model.id"
				+ " " + orderingString;
			PreparedStatement statementObject = connection.prepareStatement(statementString);

			// Execute the statement.
			Logger.getLogger("mysql").log(Level.TRACE, "Executing SQL: " + statementObject.toString());
			ResultSet rs = statementObject.executeQuery();

			// Bundle the results into a thingy
			LinkedList<AdminModelsQueryResult> result = new LinkedList<AdminModelsQueryResult>();

			MySQLModelDAO modelDAO = new MySQLModelDAO(connection);
			
			while (rs.next()) {
				Model model = modelDAO.createInstanceFromDatabaseValues("model", rs);
				model.setId(rs.getLong("model.id"));
																			
				AdminModelsQueryResult n = new AdminModelsQueryResult();
				n.setModel(model);
				n.setModuleCount(rs.getLong("n_mod"));
				
				result.add(n);
			}

			rs.close();
			statementObject.close();
			
			// Done		
			return result;
		} catch (SQLException e) {
			throw new DAOException("SQL error", e);
		}
	}
	
	public Collection<AdminModulesQueryResult> performAdminModulesQuery(
			AdminModulesQuerySortingType sortingType, Long modelId)
			throws DAOException {
		//  Begin the transaction
		String orderingString = null;
		switch (sortingType) {
		case NONE:
			orderingString = "";
			break;
		case ASSIGNMENT_COUNT_ASCENDING:
			orderingString = "ORDER BY n_ass ASC";
			break;
		case ASSIGNMENT_COUNT_DESCENDING:
			orderingString = "ORDER BY n_ass DESC";
			break;
		case REGISTRATION_REQUIRED_ASC:
			orderingString = "ORDER BY module.registration_required ASC";
			break;
		case REGISTRATION_REQUIRED_DESC:
			orderingString = "ORDER BY module.registration_required DESC";
			break;
		case ADMINISTRATOR_COUNT_ASCENDING:
			orderingString = "ORDER BY n_adm ASC";
			break;
		case ADMINISTRATOR_COUNT_DESCENDING:
			orderingString = "ORDER BY n_adm DESC";
			break;
		case MODULE_ID_ASCENDING:
			orderingString = "ORDER BY module.uniq ASC";
			break;
		case MODULE_ID_DESCENDING:
			orderingString = "ORDER BY module.uniq DESC";
			break;
		default:
			throw new DAOException("unsupported sort method");
		}
		
		try {
			// Construct the statement.
			String statementString = "SELECT model.*, module.*, COUNT(DISTINCT assignment.id) n_ass, COUNT(DISTINCT module_administrators.administrator_id) n_adm"
				+ " FROM module"
				+ " INNER JOIN model ON model.id = module.model_id"
				+ " LEFT JOIN assignment ON assignment.module_id = module.id"
				+ " LEFT JOIN module_administrators ON module_administrators.module_id = module.id"
				+ " WHERE module.model_id = ?"
				+ " GROUP BY module.id"
				+ " " + orderingString;
			PreparedStatement statementObject = connection.prepareStatement(statementString);
			statementObject.setObject(1, modelId);

			// Execute the statement.
			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
			ResultSet rs = statementObject.executeQuery();

			// Bundle the results into a thingy
			LinkedList<AdminModulesQueryResult> result = new LinkedList<AdminModulesQueryResult>();

			MySQLModelDAO modelDAO = new MySQLModelDAO(connection);
			MySQLModuleDAO moduleDAO = new MySQLModuleDAO(connection);
			
			while (rs.next()) {
				Model model = modelDAO.createInstanceFromDatabaseValues("model", rs);
				model.setId(rs.getLong("model.id"));
				
				Module module = moduleDAO.createInstanceFromDatabaseValues("module", rs);
				module.setId(rs.getLong("module.id"));
															
				AdminModulesQueryResult n = new AdminModulesQueryResult();
				n.setModel(model);
				n.setAssignmentCount(rs.getLong("n_ass"));
				n.setAdministratorCount(rs.getLong("n_adm"));
				n.setModule(module);
				
				result.add(n);
			}

			rs.close();
			statementObject.close();
			
			// Done		
			return result;
		} catch (SQLException e) {
			throw new DAOException("SQL error", e);
		}
	}
	
}
