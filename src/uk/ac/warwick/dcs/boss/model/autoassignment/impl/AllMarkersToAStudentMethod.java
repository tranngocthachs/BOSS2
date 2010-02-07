package uk.ac.warwick.dcs.boss.model.autoassignment.impl;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import uk.ac.warwick.dcs.boss.model.autoassignment.AutoAssignmentException;
import uk.ac.warwick.dcs.boss.model.autoassignment.IAutoAssignmentMethod;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;


public class AllMarkersToAStudentMethod implements IAutoAssignmentMethod {

	public Map<Person, Collection<Person>> createMarkerToStudentsMap(
			LinkedList<Person> markers, LinkedList<Person> students)
			throws AutoAssignmentException {
		// Output
		Map<Person, Collection<Person>> result = new HashMap<Person, Collection<Person>>();
		
		// For each student...
		for (Person marker : markers) {
			Collection<Person> markerStudents = new LinkedList<Person>();
			for (Person student : students) {
				markerStudents.add(student);
			}
			
			result.put(marker, markerStudents);
		}
				
		// Done
		return result;
	}

	public String getDescription() {
		return "Map each student to one marker randomly as possible.";
	}

	public String getName() {
		return "One Marker Per Student";
	}

}
