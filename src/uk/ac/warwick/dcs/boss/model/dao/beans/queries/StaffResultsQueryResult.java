package uk.ac.warwick.dcs.boss.model.dao.beans.queries;

import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;
import uk.ac.warwick.dcs.boss.model.dao.beans.Result;

/**
 * Specialised query result for the staff frontend.
 * 
 * Contains a marking assignment, the assignment it's for, the marker and the student.
 * @author davidbyard
 *
 */
public class StaffResultsQueryResult {
	/**
	 * The assignment the marking assignment is for.
	 */
	private Assignment assignment;
	
	/**
	 * The person doing the submitting.
	 */
	private Person student;
	
	/**
	 * The result.
	 */
	private Result result;

	public void setAssignment(Assignment assignment) {
		this.assignment = assignment;
	}

	public Assignment getAssignment() {
		return assignment;
	}

	public void setStudent(Person student) {
		this.student = student;
	}

	public Person getStudent() {
		return student;
	}

	public void setResult(Result result) {
		this.result = result;
	}

	public Result getResult() {
		return result;
	}


}
