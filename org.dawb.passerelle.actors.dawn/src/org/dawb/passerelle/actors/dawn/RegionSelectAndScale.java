package org.dawb.passerelle.actors.dawn;

import java.util.List;

import org.dawb.passerelle.common.actors.AbstractDataMessageTransformer;
import org.dawb.passerelle.common.message.DataMessageComponent;
import org.dawb.passerelle.common.parameter.roi.ROIParameter;

import com.isencia.passerelle.actor.ProcessingException;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.Initializable;
import ptolemy.actor.Manager;
import ptolemy.actor.Receiver;
import ptolemy.actor.util.FunctionDependency;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

public class RegionSelectAndScale extends AbstractDataMessageTransformer {

	public ROIParameter selectionROI;
	public StringParameter roiName;
	public StringParameter datasetName;
	public StringParameter xAxisAdjustName;
	public StringParameter yAxisAdjustName;
	
	public RegionSelectAndScale(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected DataMessageComponent getTransformedMessage(
			List<DataMessageComponent> cache) throws ProcessingException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getOperationName() {
		// TODO Auto-generated method stub
		return null;
	}

	

}
