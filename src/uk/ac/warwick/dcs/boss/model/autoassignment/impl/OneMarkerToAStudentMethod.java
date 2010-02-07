package uk.ac.warwick.dcs.boss.model.autoassignment.impl;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import uk.ac.warwick.dcs.boss.model.autoassignment.AutoAssignmentException;
import uk.ac.warwick.dcs.boss.model.autoassignment.IAutoAssignmentMethod;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;


public class OneMarkerToAStudentMethod implements IAutoAssignmentMethod {

	public Map<Person, Collection<Person>> createMarkerToStudentsMap(
			LinkedList<Person> markers, LinkedList<Person> students)
			throws AutoAssignmentException {
		// Shuffle
		Collections.shuffle(markers);
		Collections.shuffle(students);
		
		// Dispose list.
		LinkedList<Person> disposePile = new LinkedList<Person>();
		
		// Output
		Map<Person, Collection<Person>> result = new HashMap<Person, Collection<Person>>();
		
		// For each student...
		for (Person student : students) {
			Person marker = markers.remove();
			disposePile.add(marker);

			Collection<Person> markerStudents = null;
			if(result.containsKey(marker)) {
				markerStudents = result.get(marker);
			} else {
				markerStudents = new LinkedList<Person>();
				result.put(marker, markerStudents);
			}
			markerStudents.add(student);
			
			// Check for empty markers list.
			if (markers.isEmpty()) {
				LinkedList<Person> temp = disposePile;
				disposePile = markers;
				markers = temp;
				Collections.shuffle(markers);
			}
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
