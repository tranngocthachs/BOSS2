package uk.ac.warwick.dcs.boss.model.dao.beans;

/**
 * The base for all stored beans.  Every bean has a unique identifier.
 * @author davidbyard
 *
 */
public class Entity {
	/**
	 * Unique identifier.
	 */
	private Long id;

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}
}
