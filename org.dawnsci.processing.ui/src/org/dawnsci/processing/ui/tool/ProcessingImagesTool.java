package org.dawnsci.processing.ui.tool;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;


public class ProcessingImagesTool extends AbstractProcessingTool {

	protected IDataset getData(){
		IImageTrace imageTrace = getImageTrace();
		if (imageTrace == null) return null;
		return getImageTrace().getData();
	}

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}
}
