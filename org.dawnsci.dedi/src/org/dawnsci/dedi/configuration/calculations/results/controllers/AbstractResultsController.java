package org.dawnsci.dedi.configuration.calculations.results.controllers;

import java.beans.PropertyChangeEvent;
import java.util.Observable;
import java.util.Observer;

import javax.measure.unit.Unit;
import javax.vecmath.Vector2d;

import org.dawnsci.dedi.configuration.BeamlineConfiguration;
import org.dawnsci.dedi.configuration.calculations.NumericRange;
import org.dawnsci.dedi.configuration.calculations.results.models.IResultsModel;
import org.dawnsci.dedi.configuration.calculations.results.models.ResultConstants;
import org.dawnsci.dedi.configuration.calculations.scattering.Q;
import org.dawnsci.dedi.configuration.calculations.scattering.ScatteringQuantity;
import org.jscience.physics.amount.Amount;


/**
 * An abstract controller for handling results of q-range calculations for a given {@link BeamlineConfiguration}
 * and the requested range entered by the users.
 *
 */
public abstract class AbstractResultsController extends AbstractController<IResultsModel> implements Observer {
	/**
	 * The beamline configuration for which the results should be calculated.
	 */
	protected BeamlineConfiguration configuration;
	
	/**
	 * PROPERTY constant used to notify listeners that the beamline configuration has changed.
	 */
	public static final String BEAMLINE_CONFIGURATION_PROPERTY = "BeamlineConfiguration";
	
	
	/**
	 * Constructs a new controller, with no models or views registered with it, initially.
	 * All interested views need to register themselves with the controller;
	 * and also need to register the models where they want the results to be stored.
	 * The controller will notify them whenever the beamline configuration changes,
	 * compute the new results and store them in the given models.
	 * 
	 * @param configuration - the beamline configuration for which the results will be calculated.
	 */
	public AbstractResultsController(BeamlineConfiguration configuration) {
		this.configuration = configuration;
		configuration.addObserver(this);
	}
	
	
	@Override
	public void update(Observable o, Object arg) {
		// Notify views that the BeamlineConfiguration changed.
		propertyChange(new PropertyChangeEvent(configuration, AbstractResultsController.BEAMLINE_CONFIGURATION_PROPERTY, null, configuration));
		
		// Compute the new results and store them in the models.
		computeQRanges();
	}
	
	
	/**
	 * Computes and sets the results.
	 */
	protected abstract void computeQRanges();
	
	
	
	/*
	 * Convenience setter methods that can be used by concrete controllers to update the data in the registered models.
	 */
	
	protected void setVisibleQRange(NumericRange range, Vector2d startPt, Vector2d endPt){
		for(IResultsModel model : registeredModels) model.setVisibleQRange(range, startPt, endPt);
	}
	
	
	protected void setFullQRange(NumericRange range){
		for(IResultsModel model : registeredModels) model.setFullQRange(range);
	}
	
	
	protected void setRequestedQRange(NumericRange range, Vector2d startPt, Vector2d endPt){
		for(IResultsModel model : registeredModels) model.setRequestedQRange(range, startPt, endPt);
	}

	
	
	/*
	 * Public methods that allow views to set the requested range entered by the user.
	 */
	
	public abstract void updateRequestedQRange(NumericRange requestedRange);
	
	
	public void updateRequestedRange(NumericRange requestedRange, ScatteringQuantity<?> quantity, Unit<?> unit) {
		updateRequestedQRange(convertRange(requestedRange, quantity, new Q(), unit, Q.BASE_UNIT));
	}
	
	
	
	/*
	 * Getter methods that allow views to access the data stored in the models.
	 * This allows views to be entirely independent of the underlying models, and interact only with the controller.
	 * The controller also provides convenient methods for converting the model data into different units/quantities that might 
	 * be required by the views, such as converting q values to other scattering quantities (see below).
	 */
	
	
	public NumericRange getVisibleQRange() {
		return (NumericRange) getModelProperty(ResultConstants.VISIBLE_Q_RANGE_PROPERTY);
	}
	
	
	public NumericRange getFullQRange() {
		return (NumericRange) getModelProperty(ResultConstants.FULL_Q_RANGE_PROPERTY);
	}
	

	public NumericRange getRequestedQRange() {
		return  (NumericRange) getModelProperty(ResultConstants.REQUESTED_Q_RANGE_PROPERTY);
	}
	
	
	public Vector2d getVisibleRangeStartPoint() {
		return (Vector2d) getModelProperty(ResultConstants.VISIBLE_RANGE_START_POINT_PROPERTY);
	}
	

	public Vector2d getVisibleRangeEndPoint() {
		return (Vector2d) getModelProperty(ResultConstants.VISIBLE_RANGE_END_POINT_PROPERTY);
	}


	public Vector2d getRequestedRangeStartPoint() {
		return (Vector2d) getModelProperty(ResultConstants.REQUESTED_RANGE_START_POINT_PROPERTY);
	}


	public Vector2d getRequestedRangeEndPoint() {
		return (Vector2d) getModelProperty(ResultConstants.REQUESTED_RANGE_END_POINT_PROPERTY);
	}

	
	public abstract Double getQResolution(double qValue);
	
	
	/**
	 * @return Whether the requested q range lies within the visible q range.
	 */
	public boolean getIsSatisfied() {
		return (boolean) getModelProperty(ResultConstants.IS_SATISFIED_PROPERTY);
	}


	/**
	 * @return Whether there is a visible q range.
	 */
	public boolean getHasSolution() {
		return (boolean) getModelProperty(ResultConstants.HAS_SOLUTION_PROPERTY);
	}
	
	
	/**
	 * @return The {@link BeamlineConfiguration} instance that this controller uses to compute the results.
	 */
	public BeamlineConfiguration getBeamlineConfiguration(){
		return configuration;
	}
	
	
	
	/*
	 * Methods that allow views to obtain the results in the required quantities and units.
	 */
	
	
	public NumericRange getVisibleRange(ScatteringQuantity<?> quantity, Unit<?> unit) {
		return convertRange(getVisibleQRange(), new Q(), quantity, Q.BASE_UNIT, unit);
	}
	
	
	public NumericRange getFullRange(ScatteringQuantity<?> quantity, Unit<?> unit) {
		return convertRange(getFullQRange(), new Q(), quantity, Q.BASE_UNIT, unit);
	}
	
	
	public NumericRange getRequestedRange(ScatteringQuantity<?> quantity, Unit<?> unit) {
		return convertRange(getRequestedQRange(), new Q(), quantity, Q.BASE_UNIT, unit);
	}
	
	
	/**
	 * 
	 * @return - given range converted from the old quantity to the new quantity in the given units.
	 *           Returns null if the given range was null.
	 * 
	 * @throws NullPointerException if any of the given scattering quantities are null.
	 */
	public NumericRange convertRange(NumericRange range, ScatteringQuantity<?> oldQuantity, ScatteringQuantity<?> newQuantity, 
            Unit<?> oldUnit, Unit<?> newUnit){
		
		if(range == null) return null;
		
		Double min = convertValue(range.getMin(), oldQuantity, newQuantity, oldUnit, newUnit);
		Double max = convertValue(range.getMax(), oldQuantity, newQuantity, oldUnit, newUnit);
		
		return (min == null || max == null) ? null : new NumericRange(min, max);
	}
	
	
	/**
	 * 
	 * @return - given value converted from the old quantity to the new quantity in the given units.
	 *           Returns null if the given value was null.
	 * 
	 * @throws NullPointerException if any of the given scattering quantities are null.
	 */
	public Double convertValue(Double value, ScatteringQuantity<?> oldQuantity, ScatteringQuantity<?> newQuantity, 
                           Unit<?> oldUnit, Unit<?> newUnit){
		if(value == null) return null;
		oldQuantity.setValue(Amount.valueOf(value, oldUnit));
		ScatteringQuantity<?> newSQ = oldQuantity.to(newQuantity);
		
		return (newSQ == null) ? null : newSQ.getValue().to(newUnit).getEstimatedValue();
	}
}
