package org.dawnsci.plotting.draw2d.swtxy;

import org.dawnsci.plotting.api.region.RegionEvent;
import org.dawnsci.plotting.draw2d.swtxy.selection.AbstractSelectionRegion;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RegionMouseListener extends MouseMotionListener.Stub implements MouseListener {
	private static final Logger logger = LoggerFactory.getLogger(RegionMouseListener.class);

	/**
	 * 
	 */
	private int last = -1; // index of point that is being dragged around
	private final int maxLast; // region allows multiple mouse button presses
	private final int minLast;
	private boolean isDragging;

	private static final int MIN_DIST = 2;
	
	private final RegionArea          regionArea;
	private PointList                 regionPoints;
	private AbstractSelectionRegion   regionBeingAdded;
	private RegionCreationLayer      regionLayer;

	public RegionMouseListener(RegionCreationLayer    regionLayer,
			                   RegionArea              regionArea, 
			                   AbstractSelectionRegion regionBeingAdded, 
			                   final int minPresses, final int maxPresses) {
		
		this.regionLayer      = regionLayer;
		this.regionArea       = regionArea;
		this.regionBeingAdded = regionBeingAdded;
		
		minLast = minPresses - 1;
		maxLast = maxPresses - 1;
		this.regionPoints = new PointList(2);
		isDragging = false;
	}

	@Override
	public void mousePressed(MouseEvent me) {
		final Point loc = me.getLocation();
		if (isDragging) {
			isDragging = false;
			if (maxLast > 0 && last >= maxLast) {
//				System.err.println("End with last = " + last + " / " + maxLast);
				releaseMouse();
			} else if (me.button == 2) {
				this.regionPoints.removePoint(last--);
//				System.err.println("End with last-- = " + last + " / " + maxLast);
				releaseMouse();
			}
		} else {
			if (last > 0 && loc.getDistance(this.regionPoints.getPoint(last)) <= MIN_DIST) {
//				System.err.println("Cancel with last = " + last + " / " + maxLast);
				if (maxLast >= 0 || last >= minLast) {
					releaseMouse();
				} else {
					logger.warn("Not enough points!");
				}
			} else {
				this.regionPoints.addPoint(loc);
				last++;
//				System.err.println("Added on press (from non-drag), now last = " + last);
				isDragging = maxLast == 0;
			}
		}

		me.consume();
		this.regionArea.repaint();
	}

	@Override
	public void mouseReleased(MouseEvent me) {
		if (isDragging) {
			isDragging = false;
			if (maxLast >= 0 && last >= maxLast) {
//				System.err.println("Release with last = " + last + " / " + maxLast);
				releaseMouse();
			}
			me.consume();
			this.regionArea.repaint();
		}
	}

	@Override
	public void mouseDoubleClicked(MouseEvent me) {
	}

	@Override
	public void mouseDragged(final MouseEvent me) {
		mouseMoved(me);
	}

	@Override
	public void mouseMoved(final MouseEvent me) {
		if (last < 0)
			return;

		final Point loc = me.getLocation();
		if (isDragging) {
			this.regionPoints.setPoint(loc, last);
			me.consume();
			this.regionArea.repaint();
		} else if (loc.getDistance(this.regionPoints.getPoint(last)) > MIN_DIST) {
			this.regionPoints.addPoint(loc);
			last++;
			isDragging = true;
			me.consume();
			this.regionArea.repaint();
//			System.err.println("Added on move, last = " + last);
		}
	}

	@Override
	public void mouseExited(final MouseEvent me) {
		// mouseReleased(me);
	}

	private void releaseMouse() {
		this.regionLayer.setMouseListenerActive(this, false);
		if (this.regionArea.regionListener == this) {
			this.regionArea.regionListener = null;
		} else {
			this.regionArea.clearRegionTool(); // Actually something has gone wrong if this happens.
		}
		this.regionArea.setCursor(null);

		this.regionArea.addRegion(this.regionBeingAdded, false);
		this.regionArea.getRegionGraph().getOperationsManager().addCommand(new AddRegionCommand(regionArea.getRegionGraph(), this.regionBeingAdded));

		this.regionBeingAdded.setLocalBounds(this.regionPoints, this.regionArea.getBounds());

		this.regionArea.fireRegionAdded(new RegionEvent(this.regionBeingAdded));

		this.regionBeingAdded = null;
		this.regionPoints = null;
	}

	public AbstractSelectionRegion getRegionBeingAdded() {
		return regionBeingAdded;
	}

	public PointList getRegionPoints() {
		return regionPoints;
	}
}