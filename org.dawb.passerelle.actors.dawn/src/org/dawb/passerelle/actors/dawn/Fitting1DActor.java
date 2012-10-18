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
import java.util.List;
import java.util.Map;

import org.dawb.passerelle.common.actors.AbstractDataMessageTransformer;
import org.dawb.passerelle.common.message.DataMessageComponent;
import org.dawb.passerelle.common.message.MessageUtils;

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Image;
import uk.ac.diamond.scisoft.analysis.fitting.functions.AFunction;

import com.isencia.passerelle.actor.ProcessingException;

public class Fitting1DActor extends AbstractDataMessageTransformer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 813882139346261410L;
	public StringParameter datasetName;
	public StringParameter functionName;
	public StringParameter xAxisName;

	public Fitting1DActor(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);

		datasetName = new StringParameter(this, "datasetName");
		registerConfigurableParameter(datasetName);
		functionName = new StringParameter(this, "functionName");
		registerConfigurableParameter(functionName);
		xAxisName = new StringParameter(this, "xAxisName");
		registerConfigurableParameter(xAxisName);


	}

	@Override
	protected DataMessageComponent getTransformedMessage(
			List<DataMessageComponent> cache) throws ProcessingException {
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
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// get the required datasets
		String dataset = datasetName.getExpression();
		String function = functionName.getExpression();
		String xAxis = xAxisName.getExpression();
		
		AbstractDataset dataDS = ((AbstractDataset)data.get(dataset)).clone();
		AFunction fitFunction = functions.get(function);
		AbstractDataset xAxisDS = ((AbstractDataset)data.get(xAxis)).clone();
		


		return result;
	}

	@Override
	protected String getOperationName() {
		return "Normalise by region";
	}

}
