package uk.ac.warwick.dcs.boss.model.dao.beans.queries;

import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.MarkingAssignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;

/**
 * Specialised query result for the staff frontend.
 * 
 * Contains a marking assignment, the assignment it's for, the marker and the student.
 * @author davidbyard
 *
 */
public class StaffMarkingAssignmentQueryResult {
	/**
	 * The marking assignment
	 */
	private MarkingAssignment markingAssignment;
	
	/**
	 * The assignment the marking assignment is for.
	 */
	private Assignment assignment;
	
	/**
	 * The person doing the marking.
	 */
	private Person marker;
	
	/**
	 * The person doing the submitting.
	 */
	private Person student;
	
	public void setMarkingAssignment(MarkingAssignment markingAssignment) {
		this.markingAssignment = markingAssignment;
	}
	
	public MarkingAssignment getMarkingAssignment() {
		return markingAssignment;
	}

	public void setAssignment(Assignment assignment) {
		this.assignment = assignment;
	}

	public Assignment getAssignment() {
		return assignment;
	}

	public void setMarker(Person marker) {
		this.marker = marker;
	}

	public Person getMarker() {
		return marker;
	}

	public void setStudent(Person student) {
		this.student = student;
	}

	public Person getStudent() {
		return student;
	}
}
