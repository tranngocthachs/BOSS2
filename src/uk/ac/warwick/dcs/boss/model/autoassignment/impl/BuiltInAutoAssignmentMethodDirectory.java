package uk.ac.warwick.dcs.boss.model.autoassignment.impl;

import java.util.Collection;
import java.util.LinkedList;

import uk.ac.warwick.dcs.boss.model.autoassignment.AutoAssignmentMethodDescription;
import uk.ac.warwick.dcs.boss.model.autoassignment.IAutoAssignmentMethodDirectory;

public class BuiltInAutoAssignmentMethodDirectory implements
		IAutoAssignmentMethodDirectory {

	static LinkedList<AutoAssignmentMethodDescription> methods;
	static {
		methods = new LinkedList<AutoAssignmentMethodDescription>();
		
		AutoAssignmentMethodDescription oneMarkerDescription = new AutoAssignmentMethodDescription();
		oneMarkerDescription.setClassName(OneMarkerToAStudentMethod.class.getCanonicalName());
		oneMarkerDescription.setName("One marker to a student");
		oneMarkerDescription.setDescription("Randomly distributes markers among students such that each student has one marker.");
		methods.add(oneMarkerDescription);
		
		AutoAssignmentMethodDescription allMarkersDescription = new AutoAssignmentMethodDescription();
		allMarkersDescription.setClassName(AllMarkersToAStudentMethod.class.getCanonicalName());
		allMarkersDescription.setName("All markers to a student");
		allMarkersDescription.setDescription("Maps every marker to every student.  WARNING: can generate a LOT of mappings!");
		methods.add(allMarkersDescription);
	}
		
	public Collection<AutoAssignmentMethodDescription> getAutoAssignmentMethodDescriptions() {
		// TODO Auto-generated method stub
		return methods;
	}

}
