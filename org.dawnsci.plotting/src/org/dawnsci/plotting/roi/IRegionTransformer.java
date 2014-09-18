package org.dawnsci.plotting.roi;

import org.eclipse.dawnsci.analysis.api.roi.IROI;

public interface IRegionTransformer {

	public IROI getROI()  throws Exception;
	
	public Object getValue(IROI value)  throws Exception;

	public String getRendererText();
}
