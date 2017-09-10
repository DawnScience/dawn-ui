package org.dawnsci.dedi.configuration.calculations.results.models;

public class ResultConstants {
	
	private ResultConstants() {
		throw new IllegalStateException("This class is not meant to be instantiated.");
	}

	// PROPERTY constants used to indicate which of the properties has changed. 
	public static final String FULL_Q_RANGE_PROPERTY = "FullQRange";
	public static final String VISIBLE_Q_RANGE_PROPERTY = "VisibleQRange";
	public static final String REQUESTED_Q_RANGE_PROPERTY = "RequestedQRange";
	public static final String VISIBLE_RANGE_START_POINT_PROPERTY = "VisibleRangeStartPoint";
	public static final String VISIBLE_RANGE_END_POINT_PROPERTY = "VisibleRangeEndPoint";
	public static final String REQUESTED_RANGE_START_POINT_PROPERTY = "RequestedRangeStartPoint";
	public static final String REQUESTED_RANGE_END_POINT_PROPERTY = "RequestedRangeEndPoint";
	public static final String HAS_SOLUTION_PROPERTY = "HasSolution";
	public static final String IS_SATISFIED_PROPERTY = "IsSatisfied";
}
