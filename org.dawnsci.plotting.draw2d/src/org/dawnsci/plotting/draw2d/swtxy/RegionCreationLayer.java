package org.dawnsci.plotting.draw2d.swtxy;

import org.dawnsci.plotting.draw2d.swtxy.selection.AbstractSelectionRegion;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.LayeredPane;
import org.eclipse.draw2d.geometry.PointList;

/**
 * The RegionSelectionLayer is used like a glass pane above the plot to 
 * give a temporary layer on which to draw the region as it is 
 * being added. Once the region is added it goes down to the graph layer (TODO Should there
 * be a separate layer for the regions?)
 * 
 * @author fcp94556
 *
 */
public class RegionCreationLayer extends Layer {
	
	private Figure              regionOverlayArea;
	private RegionArea          regionArea;
	private RegionMouseListener regionListener;
	private LayeredPane         layeredPane;
	
	public RegionCreationLayer(LayeredPane layeredPane, RegionArea regionArea) {
		
		this.layeredPane= layeredPane;
		this.regionArea = regionArea;
		
		this.regionOverlayArea = new Figure() {
			public void paint(Graphics graphics) {
				// Nothing we draw nothing for the transparent figure.
			}
		};
		regionOverlayArea.setOpaque(false);
		add(regionOverlayArea);
		
		regionArea.setRegionLayer(this);
	}

	public void layout() {
		regionOverlayArea.setBounds(regionArea.getBounds());
		super.layout();
	}

	public void setMouseListenerActive(final RegionMouseListener rl, final boolean isActive) {
		this.regionListener = rl;
		if (isActive) {
			layeredPane.add(this);
			this.regionListener = rl;
			regionOverlayArea.setOpaque(true);
			regionOverlayArea.addMouseListener(rl);
			regionOverlayArea.addMouseMotionListener(rl);
			final AbstractSelectionRegion regionBeingAdded = regionListener.getRegionBeingAdded();
			if (regionBeingAdded!=null) {
				regionOverlayArea.setCursor(regionBeingAdded.getRegionCursor());
			}

		} else {
			layeredPane.remove(this);
			this.regionListener = null;
			regionOverlayArea.setOpaque(false);
			regionOverlayArea.removeMouseListener(rl);
			regionOverlayArea.removeMouseMotionListener(rl);
			regionOverlayArea.setCursor(null);
		}
	}
	
	@Override
	protected void paintClientArea(final Graphics graphics) {
		super.paintClientArea(graphics);

		if (regionListener!=null) {
			final AbstractSelectionRegion regionBeingAdded = regionListener.getRegionBeingAdded();
			final PointList regionPoints   = regionListener.getRegionPoints();
			if (regionBeingAdded!=null && regionPoints!=null && regionPoints.size() > 0) {
				regionBeingAdded.paintBeforeAdded(graphics, regionPoints, getBounds());
			}
		}
	}

}
