package org.dawnsci.plotting.histogram.ui;

import org.eclipse.dawnsci.plotting.api.tool.IToolPage.ToolPageRole;

public class HistogramToolPageColor extends HistogramToolPage2 {

	public static final String ID = "org.dawnsci.plotting.histogram.ui.HistogramToolPageColor";
	
	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_JUST_COLOUR;
	}
	
}
