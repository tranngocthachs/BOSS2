package uk.ac.warwick.dcs.boss.model.dao.beans.queries;

import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;

/**
 * Specialised query result for the student frontend.
 * 
 * Contains an assignment, the parent module, and the number of previous submissions for that assignment.
 * @author davidbyard
 *
 */
public class StudentSubmissionCountsQueryResult {
		/**
		 * The parent module.
		 */
		private Module parentModule;
		
		/**
		 * The assignment.
		 */
		private Assignment assignment;
		
		/**
		 * Number of previous submissions for the assignment.
		 */
		private Long numberOfSubmissions;

		public void setParentModule(Module parentModule) {
			this.parentModule = parentModule;
		}
		
		public Module getParentModule() {
			return parentModule;
		}
		
		public void setAssignment(Assignment assignment) {
			this.assignment = assignment;
		}
		
		public Assignment getAssignment() {
			return assignment;
		}

		public void setNumberOfSubmissions(Long numberOfSubmissions) {
			this.numberOfSubmissions = numberOfSubmissions;
		}
		
		public Long getNumberOfSubmissions() {
			return numberOfSubmissions;
		}
}
