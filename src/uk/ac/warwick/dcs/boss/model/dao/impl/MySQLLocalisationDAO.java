package uk.ac.warwick.dcs.boss.model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.ILocalisationDAO;

public class MySQLLocalisationDAO implements ILocalisationDAO {
	
	private Connection connection;
		
	public MySQLLocalisationDAO(Connection connection) throws DAOException {
		this.connection = connection;
	}

	public void addLocalisation(String locale, String original,
			String translation) throws DAOException {
		removeLocalisation(locale, original);
		
		// Begin the transaction
		try {
			// Construct the statement.
			String statementString = "INSERT INTO i18n (original, locale, translation)" 
				+ " VALUES (?, ?, ?)";
			PreparedStatement statementObject = connection.prepareStatement(statementString);
			statementObject.setString(1, original);
			statementObject.setString(2, locale);
			statementObject.setString(3, translation);
			
			// Execute the statement.
			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
			statementObject.executeUpdate();
			
			statementObject.close();
		} catch (SQLException e) {
			throw new DAOException("SQL error", e);
		}		
	}

	public String getLocalisation(String locale, String original)
			throws DAOException {
		// Begin the transaction
		try {
			// Construct the statement.
			String statementString = "SELECT translation FROM i18n (original, locale, translation)" 
				+ " WHERE original=? AND locale=?";
			PreparedStatement statementObject = connection.prepareStatement(statementString);
			statementObject.setString(1, original);
			statementObject.setString(2, locale);
			
			// Execute the statement.
			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
			ResultSet rs = statementObject.executeQuery();
			if (!rs.first()) {
				rs.close();
				statementObject.close();
				return original;
			} else {
				String result = rs.getString("translation");
				rs.close();
				statementObject.close();
				return result;
			}
		} catch (SQLException e) {
			throw new DAOException("SQL error", e);
		}				

	}

	public void removeLocalisation(String locale, String original) throws DAOException {
		// Begin the transaction
		try {
			// Construct the statement.
			String statementString = "DELETE FROM i18n (original, locale, translation)" 
				+ " WHERE original=? AND locale=?";
			PreparedStatement statementObject = connection.prepareStatement(statementString);
			statementObject.setString(1, original);
			statementObject.setString(2, locale);
			
			// Execute the statement.
			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
			statementObject.executeUpdate();
		} catch (SQLException e) {
			throw new DAOException("SQL error", e);
		}				
	}
}
