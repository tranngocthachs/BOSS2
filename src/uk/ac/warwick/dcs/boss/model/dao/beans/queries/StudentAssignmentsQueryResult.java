package uk.ac.warwick.dcs.boss.model.dao.beans.queries;

import java.util.Date;

import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.DeadlineRevision;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;

/**
 * Specialised query result for the student frontend.
 * 
 * Contains an assignment, the parent module, the latest deadline revision, and the last submission time.
 * @author davidbyard
 *
 */
public class StudentAssignmentsQueryResult {

		/**
		 * The assignment.
		 */
		private Assignment assignment;
		
		/**
		 * The parent module.
		 */
		private Module parentModule;
		
		/**
		 * The latest deadline revision.
		 */
		private DeadlineRevision deadlineRevision;
		
		/**
		 * The last submission time.
		 */
		private Date lastSubmissionTime;
		
		public void setAssignment(Assignment assignment) {
			this.assignment = assignment;
		}
		
		public Assignment getAssignment() {
			return assignment;
		}
		
		public void setParentModule(Module module) {
			this.parentModule = module;
		}
		
		public Module getParentModule() {
			return parentModule;
		}
		
		public void setDeadlineRevision(DeadlineRevision deadlineRevision) {
			this.deadlineRevision = deadlineRevision;
		}
		
		public DeadlineRevision getDeadlineRevision() {
			return deadlineRevision;
		}

		public void setLastSubmissionTime(Date lastSubmissionTime) {
			this.lastSubmissionTime = lastSubmissionTime;
		}

		public Date getLastSubmissionTime() {
			return lastSubmissionTime;
		}
	
		public boolean hasSubmitted() {
			return lastSubmissionTime != null;
		}
		
		public boolean hasDeadlineRevision() {
			return deadlineRevision != null;
		}
		
		public boolean isOverdue() {
			if (hasSubmitted()) {
				return false;
			}
			
			return new Date().after(assignment.getDeadline());
		}
		
		public boolean canSubmit() {
			if (hasDeadlineRevision()) {
				return new Date().before(getDeadlineRevision().getNewDeadline());
			} else {
				return new Date().before(assignment.getClosingTime());
			}
		}
}
