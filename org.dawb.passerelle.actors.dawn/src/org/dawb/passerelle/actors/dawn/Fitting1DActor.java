/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawb.passerelle.actors.dawn;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dawb.passerelle.common.actors.AbstractDataMessageTransformer;
import org.dawb.passerelle.common.message.DataMessageComponent;
import org.dawb.passerelle.common.message.DataMessageException;
import org.dawb.passerelle.common.message.MessageUtils;

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Slice;
import uk.ac.diamond.scisoft.analysis.fitting.Fitter;
import uk.ac.diamond.scisoft.analysis.fitting.functions.AFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction;
import uk.ac.diamond.scisoft.analysis.optimize.ApacheNelderMead;
import uk.ac.diamond.scisoft.analysis.optimize.GeneticAlg;

public class Fitting1DActor extends AbstractDataMessageTransformer {

	private static final long serialVersionUID = 813882139346261410L;
	public StringParameter datasetName;
	public StringParameter functionName;
	public StringParameter xAxisName;
	public StringParameter fitDirection;

	public Fitting1DActor(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);

		datasetName = new StringParameter(this, "datasetName");
		registerConfigurableParameter(datasetName);
		functionName = new StringParameter(this, "functionName");
		registerConfigurableParameter(functionName);
		xAxisName = new StringParameter(this, "xAxisName");
		registerConfigurableParameter(xAxisName);
		fitDirection = new StringParameter(this, "fitDirection");
		registerConfigurableParameter(fitDirection);
	}

	@Override
	protected DataMessageComponent getTransformedMessage(
			List<DataMessageComponent> cache) throws DataMessageException {
		
		// get the data out of the message, name of the item should be specified
		final Map<String, Serializable>  data = MessageUtils.getList(cache);
		
		// prepare the output message
		DataMessageComponent result = new DataMessageComponent();
		
		// put all the datasets in for reprocessing
		for (String key : data.keySet()) {
			result.addList(key, (AbstractDataset) data.get(key));
		}
		
		Map<String, AFunction> functions = null;
		try {
			functions = MessageUtils.getFunctions(cache);
		} catch (Exception e1) {
			throw createDataMessageException("Failed to get the list of functions from the incomming message", e1);
		}
		
		// get the required datasets
		String dataset = datasetName.getExpression();
		String function = functionName.getExpression();
		String xAxis = xAxisName.getExpression();
		Integer fitDim = Integer.parseInt(fitDirection.getExpression());
		
		AbstractDataset dataDS = ((AbstractDataset)data.get(dataset)).clone();
		AFunction fitFunction = functions.get(function);
		AbstractDataset xAxisDS = null;
		if (data.containsKey(xAxis)) {
			xAxisDS = ((AbstractDataset)data.get(xAxis)).clone();
		} else {
			xAxisDS = DoubleDataset.arange(dataDS.getShape()[fitDim],0,-1);
		}
		
		// get the general parameters from the first fit.
		ArrayList<Slice> slices = new ArrayList<Slice>();
		for (int i = 0; i < dataDS.getShape().length; i++) {
			if (i == fitDim) {
				slices.add(new Slice(0,dataDS.getShape()[i], 1));
			} else {
				slices.add(new Slice(0,1,1));
			}
		}
		AbstractDataset slice = dataDS.getSlice(slices.toArray(new Slice[0]));
		slice.squeeze();
		try {
			Fitter.fit(xAxisDS, slice, new GeneticAlg(0.0001), fitFunction);
		} catch (Exception e1) {
			throw createDataMessageException("Failed to fit the data profile using the Genetic Algorithm", e1);
		}
			
		ArrayList<AbstractDataset> parametersDS = new ArrayList<AbstractDataset>(fitFunction.getNoOfParameters()); 
		for(int i = 0; i < fitFunction.getNoOfParameters(); i++) {
			int[] shape = dataDS.getShape();
			shape[fitDim] = 1;
			DoubleDataset parameterDS = new DoubleDataset(shape);
			parameterDS.squeeze();
			parametersDS.add(parameterDS);
		}
		
		AbstractDataset functionsDS = new DoubleDataset(shape);
		
		for(int i = 0; i < dataDS.getShape()[0]; i++) {
			slice = dataDS.getSlice(new int[] {i,0}, new int[] {i+1,dataDS.getShape()[1]}, new int[] {1,1});
			slice.squeeze();
			try {
				CompositeFunction fitResult = Fitter.fit(xAxisDS, slice, new ApacheNelderMead(), fitFunction);
				for(int p = 0; p < fitResult.getNoOfParameters(); p++) {
					parametersDS.get(p).set(fitResult.getParameter(p).getValue(), i);
				}
				
				DoubleDataset resultFunctionDS = fitResult.makeDataset(xAxisDS);
				functionsDS.setSlice(resultFunctionDS, new int[] {i,0}, new int[] {i+1,dataDS.getShape()[1]}, new int[] {1,1});
				
			} catch (Exception e) {
				throw createDataMessageException("Failed to fit row "+i+" of the data", e);
			}
			
		}
		
		result.addList("fit_image", functionsDS);
		for(int i = 0; i < fitFunction.getNoOfParameters(); i++) {
			result.addList("fit_parameter_"+i, parametersDS.get(i));
		}

		return result;
	}

	@Override
	protected String getOperationName() {
		return "Fit 1D data in 2D image";
	}

}
