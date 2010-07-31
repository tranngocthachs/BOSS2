package uk.ac.warwick.dcs.boss.plugins.dbschema;
public class SQLColumnDefinition {
	public enum Datatype {
		BOOLEAN, INT, SMALLINT, DOUBLE, DATETIME, CHAR, VARCHAR, TEXT
	}
	
	private String columnName;
	private Datatype datatype;
	private int length = -1;
	private boolean notNull= false;
	private boolean unique = false;
	private boolean primaryKey = false;
	private boolean autoIncrement = false;

	
	private SQLColumnDefinition(String columnName, Datatype datatype,
			int length, boolean notNull, boolean unique, boolean primaryKey, boolean autoIncrement) {
		this.columnName = columnName;
		this.datatype = datatype;
		this.length = length;
		this.notNull = notNull;
		this.unique = unique;
		this.primaryKey = primaryKey;
		this.setAutoIncrement(autoIncrement);
	}

	public SQLColumnDefinition(String name, Datatype type) {
		this(name, type, -1, false, false, false, false);
	}
	
	public SQLColumnDefinition(String name, Datatype type, int length) {
		this(name, type, length, false, false, false, false);
	}
	
	public SQLColumnDefinition(String name, Datatype type, boolean notNull) {
		this(name, type, -1, notNull, false, false, false);
	}
	
	public SQLColumnDefinition(String name, Datatype type, int length, boolean notNull) {
		this(name, type, length, notNull, false, false, false);
	}
	
	public SQLColumnDefinition(String name, Datatype type, boolean primaryKey, boolean autoIncrement) {
		this(name, type, -1, true, false, primaryKey, autoIncrement);
	}
	/**
	 * @return the columnName
	 */
	public String getColumnName() {
		return columnName;
	}
	/**
	 * @param columnName the columnName to set
	 */
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	/**
	 * @return the datatype
	 */
	public Datatype getDatatype() {
		return datatype;
	}
	/**
	 * @param datatype the datatype to set
	 */
	public void setDatatype(Datatype datatype) {
		this.datatype = datatype;
	}
	/**
	 * @return the length
	 */
	public int getLength() {
		return length;
	}
	/**
	 * @param length the length to set
	 */
	public void setLength(int length) {
		this.length = length;
	}
	/**
	 * @return the notNull
	 */
	public boolean isNotNull() {
		return notNull;
	}
	/**
	 * @param notNull the notNull to set
	 */
	public void setNotNull(boolean notNull) {
		this.notNull = notNull;
	}
	/**
	 * @return the unique
	 */
	public boolean isUnique() {
		return unique;
	}
	/**
	 * @param unique the unique to set
	 */
	public void setUnique(boolean unique) {
		this.unique = unique;
	}
	/**
	 * @return the primaryKey
	 */
	public boolean isPrimaryKey() {
		return primaryKey;
	}
	/**
	 * @param primaryKey the primaryKey to set
	 */
	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}

	/**
	 * @param autoIncrement the autoIncrement to set
	 */
	public void setAutoIncrement(boolean autoIncrement) {
		this.autoIncrement = autoIncrement;
	}

	/**
	 * @return the autoIncrement
	 */
	public boolean isAutoIncrement() {
		return autoIncrement;
	}
	
	public String getColumnDefinitionString() {
		StringBuilder colDefBuilder = new StringBuilder();
		colDefBuilder.append(columnName);
		colDefBuilder.append(" ");
		colDefBuilder.append(datatype.toString());
		if (length != -1)
			colDefBuilder.append("(" + length + ")");
		colDefBuilder.append(" ");
		if (notNull)
			colDefBuilder.append("NOT NULL ");
		if (autoIncrement)
			colDefBuilder.append("AUTO_INCREMENT ");
		if (primaryKey)
			colDefBuilder.append("PRIMARY KEY ");
		else if (unique)
			colDefBuilder.append("UNIQUE KEY ");
		
		return colDefBuilder.toString().trim();
	}
}