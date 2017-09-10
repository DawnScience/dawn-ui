package org.dawnsci.dedi.configuration.calculations.results.models;

import javax.vecmath.Vector2d;

import org.dawnsci.dedi.configuration.calculations.NumericRange;

/**
 * The default implementation of {@link IResultsModel}.
 *
 */
public class Results extends AbstractModel implements IResultsModel {
	private NumericRange visibleQRange;
	private NumericRange fullQRange;
	private NumericRange requestedQRange;
	
	private Vector2d visibleRangeStartPoint;
	private Vector2d visibleRangeEndPoint;
	private Vector2d requestedRangeStartPoint;
	private Vector2d requestedRangeEndPoint;
	
	
	
	// Getters
	@Override
	public NumericRange getVisibleQRange() {
		return visibleQRange;
	}


	@Override
	public NumericRange getFullQRange() {
		return fullQRange;
	}


	@Override
	public NumericRange getRequestedQRange() {
		return requestedQRange;
	}
	
	
	@Override
	public Vector2d getVisibleRangeStartPoint() {
		return visibleRangeStartPoint;
	}


	@Override
	public Vector2d getVisibleRangeEndPoint() {
		return visibleRangeEndPoint;
	}


	@Override
	public Vector2d getRequestedRangeStartPoint() {
		return requestedRangeStartPoint;
	}


	@Override
	public Vector2d getRequestedRangeEndPoint() {
		return requestedRangeEndPoint;
	}


	@Override
	public boolean getIsSatisfied() {
		return  visibleQRange != null && requestedQRange != null &&
				visibleQRange.contains(requestedQRange);
	}


	@Override
	public boolean getHasSolution(){
		return (visibleQRange != null);
	}


	// Setters
	@Override
	public void setVisibleQRange(NumericRange range, Vector2d startPt, Vector2d endPt){
		NumericRange oldRange = visibleQRange;
		visibleQRange = range;
		visibleRangeStartPoint = startPt;
		visibleRangeEndPoint = endPt;
		firePropertyChange(ResultConstants.VISIBLE_Q_RANGE_PROPERTY, oldRange, visibleQRange);
	}
	
	
	@Override
	public void setFullQRange(NumericRange range){
		firePropertyChange(ResultConstants.FULL_Q_RANGE_PROPERTY, fullQRange, fullQRange = range);
	}
	
	
	@Override
	public void setRequestedQRange(NumericRange range, Vector2d startPt, Vector2d endPt){
		NumericRange oldRange = requestedQRange;
		requestedQRange = range;
		requestedRangeStartPoint = startPt;
		requestedRangeEndPoint = endPt;
		firePropertyChange(ResultConstants.REQUESTED_Q_RANGE_PROPERTY, oldRange, requestedQRange);
	}
}
