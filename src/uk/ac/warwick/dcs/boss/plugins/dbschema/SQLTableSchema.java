package uk.ac.warwick.dcs.boss.plugins.dbschema;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.warwick.dcs.boss.plugins.dbschema.SQLColumnDefinition.Datatype;

public class SQLTableSchema {
	private String tableName;
	private List<SQLColumnDefinition> columns;
	private Map<String, String> foreignKeys;

	public SQLTableSchema(String tableName) {
		this.tableName = tableName;
		columns = new LinkedList<SQLColumnDefinition>();
		foreignKeys = new HashMap<String, String>();
		// adding id column
		columns.add(new SQLColumnDefinition("id", Datatype.INT, true, true));
	}

	public void addIntColumn(String colName) {
		addIntColumn(colName, false, false);
	}

	public void addIntColumn(String colName, boolean notNull) {
		addIntColumn(colName, notNull, false);
	}

	public void addIntColumn(String colName, boolean notNull, boolean unique) {
		SQLColumnDefinition col = new SQLColumnDefinition(colName, Datatype.INT);
		col.setNotNull(notNull);
		col.setUnique(unique);
		columns.add(col);
	}

	public void addBooleanColumn(String colName) {
		addBooleanColumn(colName, false);
	}

	public void addBooleanColumn(String colName, boolean notNull) {
		columns.add(new SQLColumnDefinition(colName, Datatype.BOOLEAN, notNull));
	}

	public void addSmallIntColumn(String colName) {
		addSmallIntColumn(colName, false, false);
	}

	public void addSmallIntColumn(String colName, boolean notNull) {
		addSmallIntColumn(colName, notNull, false);
	}

	public void addSmallIntColumn(String colName, boolean notNull,
			boolean unique) {
		SQLColumnDefinition col = new SQLColumnDefinition(colName,
				Datatype.SMALLINT);
		col.setNotNull(notNull);
		col.setUnique(unique);
		columns.add(col);
	}

	public void addDateColumn(String colName) {
		addDateColumn(colName, false);
	}

	public void addDateColumn(String colName, boolean notNull) {
		columns.add(new SQLColumnDefinition(colName, Datatype.DATETIME, notNull));
	}

	public void addDoubleColumn(String colName) {
		addDoubleColumn(colName, false);
	}

	public void addDoubleColumn(String colName, boolean notNull) {
		columns.add(new SQLColumnDefinition(colName, Datatype.DOUBLE, notNull));
	}

	public void addCharColumn(String colName, int length) {
		addCharColumn(colName, length, false, false);
	}

	public void addCharColumn(String colName, int length, boolean notNull) {
		addCharColumn(colName, length, notNull, false);
	}

	public void addCharColumn(String colName, int length, boolean notNull,
			boolean unique) {
		columns.add(new SQLColumnDefinition(colName, Datatype.CHAR, length,
				notNull));
	}

	public void addVarCharColumn(String colName, int length) {
		addVarCharColumn(colName, length, false, false);
	}

	public void addVarCharColumn(String colName, int length, boolean notNull) {
		addVarCharColumn(colName, length, notNull, false);
	}

	public void addVarCharColumn(String colName, int length, boolean notNull,
			boolean unique) {
		columns.add(new SQLColumnDefinition(colName, Datatype.VARCHAR, length,
				notNull));
	}

	public void addTextColumn(String colName) {
		addTextColumn(colName, false);
	}

	public void addTextColumn(String colName, boolean notNull) {
		columns.add(new SQLColumnDefinition(colName, Datatype.TEXT, notNull));
	}

	public void setForeignKey(String colName, String referencedTable) {
		foreignKeys.put(colName, referencedTable);
	}

	public String getSQLCreateString() {
		StringBuilder createStrBld = new StringBuilder("CREATE TABLE "
				+ tableName + " ( ");
		for (SQLColumnDefinition col : columns) {
			createStrBld.append(col.getColumnDefinitionString());
			if (columns.indexOf(col) != columns.size() - 1) {
				createStrBld.append(", ");
			}
		}
		Iterator<Entry<String, String>> iter = foreignKeys.entrySet()
				.iterator();
		if (iter.hasNext())
			createStrBld.append(", ");
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			createStrBld.append("FOREIGN KEY (" + entry.getKey()
					+ ") REFERENCES " + entry.getValue()
					+ "(id) ON DELETE CASCADE");
			if (iter.hasNext())
				createStrBld.append(", ");
		}
		createStrBld.append(" ) ENGINE=InnoDB");
		return createStrBld.toString();
	}
	
	public static void main(String[] args) {
		SQLTableSchema tblSch = new SQLTableSchema("deadlinerevision");
		tblSch.addTextColumn("comment", true);
		tblSch.addDateColumn("deadline", true);
		tblSch.addIntColumn("person_id", true);
		tblSch.addIntColumn("assignment_id", true);
		tblSch.setForeignKey("person_id", "person");
		tblSch.setForeignKey("assignment_id", "assignment");
		System.out.println(tblSch.getSQLCreateString());
		
		SQLTableSchema testTbl = new SQLTableSchema("test");
		testTbl.addBooleanColumn("student_test", true);
		testTbl.addVarCharColumn("name", 64, true);
		testTbl.addVarCharColumn("classname", 128, true);
		testTbl.addVarCharColumn("executor_classname", 128, true);
		testTbl.addIntColumn("max_time", true);
		testTbl.addVarCharColumn("command", 255, true);
		testTbl.addIntColumn("assignment_id", true);
		testTbl.setForeignKey("assignment_id", "assignment");
		testTbl.addIntColumn("resource_id", true);
		testTbl.setForeignKey("resource_id", "resource");
		System.out.println(testTbl.getSQLCreateString());
		
	}
}