package org.dawnsci.plotting.roi;

import uk.ac.diamond.scisoft.analysis.roi.IROI;

public interface IRegionTransformer {

	public IROI getROI()  throws Exception;
	
	public Object getValue(IROI value)  throws Exception;

	public String getRendererText();
}
