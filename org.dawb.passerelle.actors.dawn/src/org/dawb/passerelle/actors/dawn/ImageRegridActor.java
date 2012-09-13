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
import org.dawb.passerelle.common.parameter.roi.ROIParameter;

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

import com.isencia.passerelle.actor.ProcessingException;

public class ImageRegridActor extends AbstractDataMessageTransformer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 813882139346261410L;
	public StringParameter datasetName;
	public StringParameter xAxisPositionsName;
	public StringParameter yAxisPositionsName;
	public StringParameter linearXAxisName;
	public StringParameter linearYAxisName;

	public ImageRegridActor(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);

		datasetName = new StringParameter(this, "datasetName");
		registerConfigurableParameter(datasetName);
		xAxisPositionsName = new StringParameter(this, "xAxisPositionsName");
		registerConfigurableParameter(xAxisPositionsName);
		yAxisPositionsName = new StringParameter(this, "yAxisPositionsName");
		registerConfigurableParameter(yAxisPositionsName);
		linearXAxisName = new StringParameter(this, "linearXAxisName");
		registerConfigurableParameter(linearXAxisName);
		linearYAxisName = new StringParameter(this, "linearYAxisName");
		registerConfigurableParameter(linearYAxisName);

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
		
		// remap the appropriate dataset
		String name = datasetName.getExpression();
		if (data.containsKey(name)) {
			
			//result.addList(ds.getName()+"_norm", ds.idivide(correction));
			//result.addList(ds.getName()+"_correction_map", correction);
		}
		
		// do the correction and put that into the pipeline., with a name that should be specified.
		return result;
	}

	@Override
	protected String getOperationName() {
		return "Normalise by region";
	}

}
