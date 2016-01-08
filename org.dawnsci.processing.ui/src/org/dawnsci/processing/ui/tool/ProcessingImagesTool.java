package org.dawnsci.processing.ui.tool;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;


public class ProcessingImagesTool extends AbstractProcessingTool {

	protected IDataset getData(){
		return getImageTrace().getData();
	}

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}
}
