package org.dawb.workbench.ui.editors.plotting.swtxy;

import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.csstudio.swt.xygraph.figures.PlotArea;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class RegionArea extends PlotArea {

	public RegionArea(XYRegionGraph xyGraph) {
		super(xyGraph);
	}
		
	final private List<RegionFigure> regionList = new ArrayList<RegionFigure>();

	@Override
	protected void layout() {
	    final Rectangle clientArea = getClientArea();
		for(RegionFigure region : regionList){
			if(region != null && region.isVisible())
				region.setBounds(clientArea);			
		}		

		super.layout();
	}
	

	public void addRegion(final RegionFigure region){
		regionList.add(region);
		region.setxyGraph(xyGraph);
		add(region);
		revalidate();
	}


	public boolean removeRegion(final RegionFigure region){
	    final boolean result = regionList.remove(region);
		//if(!region.isFree()) region.getTrace().getDataProvider().removeDataProviderListener(region);
		if(result){
			remove(region);
			revalidate();
		}
		return result;
	}
	
	public List<RegionFigure> getRegionList() {
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
}
