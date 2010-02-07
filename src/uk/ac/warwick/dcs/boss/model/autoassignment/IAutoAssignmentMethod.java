package uk.ac.warwick.dcs.boss.model.autoassignment;

import java.util.LinkedList;
import java.util.Map;
import java.util.Collection;

import uk.ac.warwick.dcs.boss.model.dao.beans.Person;

/**
 * The IAsutoAssignmentMethod is used for auto-assigning students to markers.
 * @author davidbyard
 *
 */
public interface IAutoAssignmentMethod {

	/**
	 * Map markers to students.
	 * @param markers is a list of markers.
	 * @param students is a list of students.
	 * @return is a map of markers to the students they will mark.
	 * @throws AutoAssignmentException
	 */
	public Map<Person, Collection<Person>> createMarkerToStudentsMap(LinkedList<Person> markers, LinkedList<Person> students) throws AutoAssignmentException;
	
}
