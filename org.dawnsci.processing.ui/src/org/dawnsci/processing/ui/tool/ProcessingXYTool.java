package org.dawnsci.processing.ui.tool;

import java.util.Iterator;

import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;

public class ProcessingXYTool extends AbstractProcessingTool {

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_1D;
	}
	
	@Override
	protected IDataset getData(){
		Iterator<ITrace> it = getPlottingSystem().getTraces(ILineTrace.class).iterator();
		if (!it.hasNext()) return null;
		ITrace next = it.next();
		IDataset d = next.getData();
		IDataset ax = ((ILineTrace)next).getXData();
		AxesMetadata am = null;
		try {
			am = MetadataFactory.createMetadata(AxesMetadata.class, 1);
			am.addAxis(0, ax);
		} catch (MetadataException e) {
			logger.error("Could not create axes metdata", e);
		}
		d.addMetadata(am);
		return d;
	}
}
