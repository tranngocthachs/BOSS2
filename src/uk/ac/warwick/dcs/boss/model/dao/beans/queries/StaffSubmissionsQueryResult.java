package uk.ac.warwick.dcs.boss.model.dao.beans.queries;

import uk.ac.warwick.dcs.boss.model.dao.beans.Person;
import uk.ac.warwick.dcs.boss.model.dao.beans.Submission;

/**
 * Specialised query result for the staff frontend.
 * 
 * Contains a submission and who submitted it.
 * @author davidbyard
 *
 */
public class StaffSubmissionsQueryResult {

	/**
	 * Who submitted the submission.
	 */
	private Person person;
	
	/**
	 * The submission.
	 */
	private Submission submission;
	
	public void setPerson(Person person) {
		this.person = person;
	}
	
	public Person getPerson() {
		return person;
	}
	
	public void setSubmission(Submission submission) {
		this.submission = submission;
	}
	
	public Submission getSubmission() {
		return submission;
	}
		
}
