package org.dawb.passerelle.actors.dawn;

import java.util.List;

import org.dawb.passerelle.common.actors.AbstractDataMessageTransformer;
import org.dawb.passerelle.common.message.DataMessageComponent;
import org.dawb.passerelle.common.message.MessageUtils;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

import com.isencia.passerelle.actor.ProcessingException;

public class PlotImageActor	extends AbstractDataMessageTransformer {

	public PlotImageActor(CompositeEntity container, String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);
	}
	
	@Override
	protected DataMessageComponent getTransformedMessage(
			List<DataMessageComponent> cache) throws ProcessingException {
		
		final List<IDataset>  data = MessageUtils.getDatasets(cache);


		try {		
			SDAPlotter.imagePlot("Plot 1", data.get(0));
		} catch (Exception e) {
		}
		
		
        try {
			final DataMessageComponent ret = new DataMessageComponent();
			return ret;
			
		} catch (Exception e) {
			throw createDataMessageException("Displaying data sets", e);
		}
	}

	@Override
	protected String getOperationName() {
		return "Simple Image Plot Actor";
	}

	
}
