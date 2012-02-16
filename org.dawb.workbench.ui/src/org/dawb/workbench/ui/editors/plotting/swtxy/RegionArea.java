package org.dawb.workbench.ui.editors.plotting.swtxy;

import java.util.ArrayList;
import java.util.List;

import org.csstudio.swt.xygraph.figures.PlotArea;
import org.eclipse.draw2d.Graphics;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Image;

public class RegionArea extends PlotArea {

	protected ISelectionProvider selectionProvider;
	
	public RegionArea(XYRegionGraph xyGraph) {
		super(xyGraph);
	}
		
	final private List<Region> regionList = new ArrayList<Region>();	

	public void addRegion(final Region region){
		regionList.add(region);
		region.setXyGraph(xyGraph);
		region.createContents(this);
		region.setSelectionProvider(selectionProvider);
		revalidate();
	}


	public boolean removeRegion(final Region region){
	    final boolean result = regionList.remove(region);
		//if(!region.isFree()) region.getTrace().getDataProvider().removeDataProviderListener(region);
		if (result){
			region.remove();
			revalidate();
		}
		return result;
	}
	
	public List<Region> getRegionList() {
		return regionList;
	}
	
	private Image rawImage;
	
	@Override
	protected void paintClientArea(final Graphics graphics) {
	
// TODO
//		if (rawImage==null) {
//			rawImage = new Image(Display.getCurrent(), "C:/tmp/ESRF_Pilatus_Data.png");
//		}
//		
//		final Rectangle bounds = getBounds();
//		final Image scaled = new Image(Display.getCurrent(),
//				rawImage.getImageData().scaledTo(bounds.width,bounds.height));
//		graphics.drawImage(scaled, new Point(0,0));
//
		super.paintClientArea(graphics);

	}


	public List<String> getRegionNames() {
		if (regionList==null|| regionList.isEmpty()) return null;
		final List<String> names = new ArrayList<String>(regionList.size());
		for (Region region : regionList) names.add(region.getName());
		return names;
	}


	public void setSelectionProvider(ISelectionProvider provider) {
		this.selectionProvider = provider;
	}
}
