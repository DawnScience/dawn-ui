package org.dawb.workbench.ui.editors.plotting.swtxy;

import java.util.ArrayList;
import java.util.List;

import org.csstudio.swt.xygraph.figures.PlotArea;
import org.eclipse.draw2d.geometry.Rectangle;

public class RegionArea extends PlotArea {

	public RegionArea(XYRegionGraph xyGraph) {
		super(xyGraph);
	}
		
	final private List<RegionShape> regionList = new ArrayList<RegionShape>();

	@Override
	protected void layout() {
	    final Rectangle clientArea = getClientArea();
		for(RegionShape region : regionList){
			if(region != null && region.isVisible())
				region.setBounds(clientArea);			
		}		

		super.layout();
	}
	

	public void addRegion(final RegionShape region){
		regionList.add(region);
		region.setxyGraph(xyGraph);
		add(region);
		revalidate();
	}


	public boolean removeRegion(final RegionShape region){
	    final boolean result = regionList.remove(region);
		//if(!region.isFree()) region.getTrace().getDataProvider().removeDataProviderListener(region);
		if(result){
			remove(region);
			revalidate();
		}
		return result;
	}
	
	public List<RegionShape> getRegionList() {
		return regionList;
	}
}
