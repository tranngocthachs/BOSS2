package uk.ac.warwick.dcs.boss.model.dao.beans.queries;

import uk.ac.warwick.dcs.boss.model.dao.beans.DeadlineRevision;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;

/**
 * Specialised query result for the staff frontend.
 * 
 * Contains a deadline revision and the associated person.
 * @author davidbyard
 *
 */
public class StaffDeadlineRevisionsQueryResult {
	/**
	 * The person the deadline revision is for.
	 */
	private Person person;
	
	/**
	 * The deadline revision.
	 */
	private DeadlineRevision deadlineRevision;

	public void setPerson(Person person) {
		this.person = person;
	}

	public Person getPerson() {
		return person;
	}

	public void setDeadlineRevision(DeadlineRevision deadlineRevision) {
		this.deadlineRevision = deadlineRevision;
	}

	public DeadlineRevision getDeadlineRevision() {
		return deadlineRevision;
	}
}
