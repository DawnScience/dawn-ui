package org.dawnsci.processing.ui.tool;

import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.january.dataset.IDataset;


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
