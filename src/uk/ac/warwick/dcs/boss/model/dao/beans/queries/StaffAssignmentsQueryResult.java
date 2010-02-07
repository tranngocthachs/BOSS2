package uk.ac.warwick.dcs.boss.model.dao.beans.queries;

import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;

/**
 * Specialised query result for the staff frontend.
 * 
 * Contains an assignment and counts of all its sub-entities.
 * @author davidbyard
 *
 */
public class StaffAssignmentsQueryResult {

	/**
	 * The assignment.
	 */
	private Assignment assignment;
	
	/**
	 * How many files must be submitted for the assignment.
	 */
	private Long filesCount;
	
	/**
	 * How many tests the assignment has.
	 */
	private Long testCount;
	
	/**
	 * How many marking categories the assignment has.
	 */
	private Long markingCategoryCount;
	
	/**
	 * How many markers the assignment has.
	 */
	private Long markersCount;
	
	/**
	 * How many marking assignments the assignment has.
	 */
	private Long markingAssignmentCount;
	
	/**
	 * How many submissions the assignment has.
	 */
	private Long submissionCount;
	
	/**
	 * How many deadline revisions the assignment has.
	 */
	private Long deadlineRevisionCount;
	
	/**
	 * How many published results there are.
	 */
	private Long resultCount;
	
	public void setAssignment(Assignment assignment) {
		this.assignment = assignment;
	}

	public Assignment getAssignment() {
		return assignment;
	}

	public void setFilesCount(Long filesCount) {
		this.filesCount = filesCount;
	}

	public Long getFilesCount() {
		return filesCount;
	}

	public void setTestCount(Long testCount) {
		this.testCount = testCount;
	}

	public Long getTestCount() {
		return testCount;
	}

	public void setMarkingCategoryCount(Long markingCategoryCount) {
		this.markingCategoryCount = markingCategoryCount;
	}

	public Long getMarkingCategoryCount() {
		return markingCategoryCount;
	}

	public void setMarkersCount(Long markersCount) {
		this.markersCount = markersCount;
	}

	public Long getMarkersCount() {
		return markersCount;
	}

	public void setMarkingAssignmentCount(Long markingAssignmentsCount) {
		this.markingAssignmentCount = markingAssignmentsCount;
	}

	public Long getMarkingAssignmentCount() {
		return markingAssignmentCount;
	}

	public void setSubmissionCount(Long submissionCount) {
		this.submissionCount = submissionCount;
	}

	public Long getSubmissionCount() {
		return submissionCount;
	}

	public void setDeadlineRevisionCount(Long deadlineRevisionCount) {
		this.deadlineRevisionCount = deadlineRevisionCount;
	}

	public Long getDeadlineRevisionCount() {
		return deadlineRevisionCount;
	}

	public void setResultCount(Long resultCount) {
		this.resultCount = resultCount;
	}

	public Long getResultCount() {
		return resultCount;
	}

}
