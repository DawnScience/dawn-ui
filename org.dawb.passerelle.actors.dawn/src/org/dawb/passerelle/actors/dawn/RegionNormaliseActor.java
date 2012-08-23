package org.dawb.passerelle.actors.dawn;

import java.util.List;

import org.dawb.passerelle.common.actors.AbstractDataMessageTransformer;
import org.dawb.passerelle.common.actors.ActorUtils;
import org.dawb.passerelle.common.message.DataMessageComponent;
import org.dawb.passerelle.common.message.MessageUtils;
import org.dawb.passerelle.common.parameter.roi.ROIParameter;

import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

import com.isencia.passerelle.actor.ProcessingException;

public class RegionNormaliseActor extends AbstractDataMessageTransformer {

	public ROIParameter NormalisationROI;

	public RegionNormaliseActor(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);

		NormalisationROI = new ROIParameter(this, "NormalisationROI");
		registerConfigurableParameter(NormalisationROI);
	}

	@Override
	protected DataMessageComponent getTransformedMessage(
			List<DataMessageComponent> cache) throws ProcessingException {
		// get the data out of the message, name of the item should be specified
		List<IDataset> datasets = MessageUtils.getDatasets(cache);
		
		// get the roi out of the message, name of the roi should be specified
		RectangularROI roi = (RectangularROI) NormalisationROI.getRoi();
		
		// prepare the output message
		DataMessageComponent result = new DataMessageComponent();
		
		// for each dataset process the output
		for (IDataset dataset : datasets) {
			AbstractDataset ds = DatasetUtils.convertToAbstractDataset(dataset.clone());
			//ds = ds.cast(AbstractDataset.ARRAYFLOAT32);
			AbstractDataset[] profiles = ROIProfile.box(ds, roi);
			AbstractDataset tile = profiles[1].reshape(profiles[1].getShape()[0],1);
			double width = roi.getLengths()[0];
			tile.idivide(width);
			AbstractDataset correction = DatasetUtils.tile(tile, ds.getShape()[1]);
			
			
			result.addList(dataset.getName(), ds.idivide(correction));
			result.addList("Correction", correction);
			
			
		}
		
		// do the correction and put that into the pipeline., with a name that should be specified.
		return result;
	}

	@Override
	protected String getOperationName() {
		return "Normalise by region";
	}

}
