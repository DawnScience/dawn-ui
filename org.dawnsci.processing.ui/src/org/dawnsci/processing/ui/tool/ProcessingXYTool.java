package org.dawnsci.processing.ui.tool;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.metadata.AxesMetadata;
import org.eclipse.dawnsci.analysis.dataset.metadata.AxesMetadataImpl;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage.ToolPageRole;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;

public class ProcessingXYTool extends ProcessingImagesTool {

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_1D;
	}
	
	
	protected IDataset getData(){
		ITrace next = getPlottingSystem().getTraces(ILineTrace.class).iterator().next();
		IDataset d = next.getData();
		IDataset ax = ((ILineTrace)next).getXData();
		AxesMetadataImpl am = new AxesMetadataImpl(1);
		am.addAxis(0, ax);
		d.addMetadata(am);
		return d;
	}
}
