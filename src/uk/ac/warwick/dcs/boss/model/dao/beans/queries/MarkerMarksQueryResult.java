package uk.ac.warwick.dcs.boss.model.dao.beans.queries;

import uk.ac.warwick.dcs.boss.model.dao.beans.Mark;
import uk.ac.warwick.dcs.boss.model.dao.beans.MarkingCategory;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;

/**
 * Specialised query result for the marker interface.
 * 
 * Contains a mark, who marked it, what marking category, and whether it may be edited.
 * @author davidbyard
 *
 */
public class MarkerMarksQueryResult {

	/**
	 * The mark.
	 */
	private Mark mark;
	
	/**
	 * The person that made the mark.
	 */
	private Person marker;
	
	/**
	 * The category of the mark.
	 */
	private MarkingCategory markingCategory;
	
	/**
	 * Whether the mark is editable in context of the query.
	 */
	private boolean editable;
	
	public void setMark(Mark mark) {
		this.mark = mark;
	}

	public Mark getMark() {
		return mark;
	}

	public void setMarker(Person marker) {
		this.marker = marker;
	}

	public Person getMarker() {
		return marker;
	}

	public void setMarkingCategory(MarkingCategory markingCategory) {
		this.markingCategory = markingCategory;
	}

	public MarkingCategory getMarkingCategory() {
		return markingCategory;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public boolean isEditable() {
		return editable;
	}
	
}
