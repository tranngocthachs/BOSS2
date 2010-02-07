package uk.ac.warwick.dcs.boss.model.dao.beans.queries;

import java.util.Date;

import uk.ac.warwick.dcs.boss.model.dao.beans.MarkingAssignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;

/**
 * Specialised query result for the marking interface.
 * 
 * Returns a student, the marking assignment associated with them, when they were last marked, and how many submissions they have.
 * @author davidbyard
 *
 */
public class MarkerStudentsQueryResult {

	/**
	 * Marking assignment associated with the student.
	 */
	private MarkingAssignment markingAssignment;
	
	/**
	 * The student.
	 */
	private Person student;
	
	/**
	 * When the student was last marked.
	 */
	private Date lastMarked;
	
	/**
	 * How many submissions the student has.
	 */
	private long submissionCount;
	
	public void setMarkingAssignment(MarkingAssignment markingAssignment) {
		this.markingAssignment = markingAssignment;
	}

	public MarkingAssignment getMarkingAssignment() {
		return markingAssignment;
	}

	public void setStudent(Person student) {
		this.student = student;
	}
	
	public Person getStudent() {
		return student;
	}

	public void setLastMarked(Date deadline) {
		this.lastMarked = deadline;
	}

	public Date getLastMarked() {
		return lastMarked;
	}

	public void setSubmissionCount(long submissionCount) {
		this.submissionCount = submissionCount;
	}

	public long getSubmissionCount() {
		return submissionCount;
	}
	
	public boolean hasBeenMarked() {
		return this.lastMarked != null;
	}
	
}
