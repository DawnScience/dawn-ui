package org.dawnsci.dedi.configuration.calculations.results.models;

import javax.vecmath.Vector2d;

import org.dawnsci.dedi.configuration.BeamlineConfiguration;
import org.dawnsci.dedi.configuration.calculations.NumericRange;

/**
 * A model to store information about the visible, full, and user-requested q range for a particular {@link BeamlineConfiguration}.
 * (The min and max of the full range correspond to the q value that can be seen with the maximum allowed camera length and minimum allowed energy, 
 *  and with the minimum allowed camera length and maximum allowed energy, respectively).
 * 
 * As well as the q values themselves, the model also stores the positions of the end points of the visible and requested range on the detector.
 * (The positions of the end points of the full range are irrelevant, therefore not stored in the model).
 */
public interface IResultsModel extends IModel {
	
	public void setVisibleQRange(NumericRange range, Vector2d startPt, Vector2d endPt);
	
	public void setFullQRange(NumericRange range);
	
	public void setRequestedQRange(NumericRange range, Vector2d startPt, Vector2d endPt);
	
	public NumericRange getVisibleQRange();

	public NumericRange getFullQRange();

	public NumericRange getRequestedQRange();
	
	public Vector2d getVisibleRangeStartPoint();

	public Vector2d getVisibleRangeEndPoint();

	public Vector2d getRequestedRangeStartPoint();

	public Vector2d getRequestedRangeEndPoint();

	
	/**
	 * @return Whether the requested q range is within the visible q range.
	 */
	public boolean getIsSatisfied();

	
	/**
	 * @return Whether there is a visible q range.
	 */
	public boolean getHasSolution();
}
