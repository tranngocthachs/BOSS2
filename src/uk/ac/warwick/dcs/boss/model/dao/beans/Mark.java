package uk.ac.warwick.dcs.boss.model.dao.beans;

import java.util.Date;

/**
 * A mark provided for a particular marking assignment and marking category.
 * @author davidbyard
 *
 */
public class Mark extends Entity {
	/**
	 * Comment associated with the mark.
	 */
	private String comment;
	
	/**
	 * Time the mark was created.
	 */
	private Date timestamp;
	
	/**
	 * The value of the mark.
	 */
	private Integer value;
	
	/**
	 * The marking category this mark is under.
	 */
	private Long markingCategoryId;
	
	/**
	 * The marking assignment this mark is a mark for.
	 */
	private Long markingAssignmentId;

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}

	public void setTimestamp(Date datetime) {
		this.timestamp = datetime;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setValue(Integer value) {
		this.value = value;
	}

	public Integer getValue() {
		return value;
	}

	public void setMarkingCategoryId(Long markingCategoryId) {
		this.markingCategoryId = markingCategoryId;
	}

	public Long getMarkingCategoryId() {
		return markingCategoryId;
	}

	public void setMarkingAssignmentId(Long markerId) {
		this.markingAssignmentId = markerId;
	}

	public Long getMarkingAssignmentId() {
		return markingAssignmentId;
	}

}
