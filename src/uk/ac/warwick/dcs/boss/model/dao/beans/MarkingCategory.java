package uk.ac.warwick.dcs.boss.model.dao.beans;

/**
 * Marking categories are categories that marks may go under, providing basic waiting and details about
 * the mark scheme.
 * @author davidbyard
 *
 */
public class MarkingCategory extends Entity {
	/**
	 * The name of the marking category.
	 */
	private String name;
	
	/**
	 * The parent assignment.
	 */
	private Long assignmentId;
	
	/**
	 * The weighting.  If there are two categories for an assignment with weighting 3 and 7, then the one
	 * with 3 would be worth 30% and the one with 7 would be worth 70%.
	 */
	private Long weighting;
	
	/**
	 * The (theoretical, as of now) maximum mark for this marking category.
	 */
	private Long maximumMark;

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setAssignmentId(Long moduleId) {
		this.assignmentId = moduleId;
	}

	public Long getAssignmentId() {
		return assignmentId;
	}

	public void setWeighting(Long weighting) {
		this.weighting = weighting;
	}

	public Long getWeighting() {
		return weighting;
	}

	public void setMaximumMark(Long maximumMark) {
		this.maximumMark = maximumMark;
	}

	public Long getMaximumMark() {
		return maximumMark;
	}
}
