package org.dawb.workbench.ui.editors.plotting.swtxy;

import java.util.List;

import org.csstudio.swt.xygraph.figures.XYGraph;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.UpdateManager;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Class from SWT examples which allows a draw2D figure to move.
 * @author fcp94556
 *
 */
public class FigureMover implements MouseListener, MouseMotionListener {

	private final IFigure figure;
	private Rectangle bounds;
	private boolean translateChildren;
	private XYGraph xyGraph;
	private Dimension cumulativeOffset;
	private Point startLocation;
   
	public FigureMover(XYGraph xyGraph, IFigure figure, boolean translateChildren) {
		this(xyGraph, figure, figure, translateChildren);
	}

	public FigureMover(XYGraph xyGraph, IFigure figure, IFigure listener, boolean translateChildren) {
		this.figure = figure;
		listener.addMouseListener(this);
		listener.addMouseMotionListener(this);
		this.translateChildren = translateChildren;
		this.xyGraph = xyGraph;
	}

	@Override
	public void mouseDragged(MouseEvent event) {
		if (location == null) return;
		Point newLocation = event.getLocation();
		if (newLocation == null) return;
		
		this.cumulativeOffset = newLocation.getDifference(startLocation);

		Dimension offset = newLocation.getDifference(location);
		if (offset.width == 0 && offset.height == 0) return;
		location = newLocation;
		UpdateManager updateMgr = figure.getUpdateManager();
		LayoutManager layoutMgr = figure.getParent().getLayoutManager();
		bounds = figure.getBounds();
		updateMgr.addDirtyRegion(figure.getParent(), bounds);
		
		this.bounds = bounds.getCopy().translate(offset.width, offset.height);
		if (layoutMgr!=null) layoutMgr.setConstraint(figure, bounds);
		
		if (translateChildren) {
			final List<IFigure> children = figure.getChildren();
			for (int i = 0; i < children.size(); i++)
				((IFigure) children.get(i)).translate(offset.width, offset.height);
		} else {
		    figure.translate(offset.width, offset.height);
		}
	
		updateMgr.addDirtyRegion(figure.getParent(), bounds);
		
		event.consume();
	}

	@Override
	public void mouseEntered(MouseEvent me) {
		// TODO Auto-generated method stub

	}
	@Override
	public void mouseExited(MouseEvent me) {
		// TODO Auto-generated method stub

	}
	@Override
	public void mouseHover(MouseEvent me) {
		// TODO Auto-generated method stub

	}
	@Override
	public void mouseMoved(MouseEvent me) {
		// TODO Auto-generated method stub

	}
	private Point location;
	@Override
	public void mousePressed(MouseEvent event) {
		location = event.getLocation();
		startLocation = event.getLocation();
		event.consume();
	}
	@Override
	public void mouseReleased(MouseEvent event) {
		if (location == null) return;
		xyGraph.getOperationsManager().addCommand(new MoverCommand(figure, cumulativeOffset, translateChildren));
		location = null;
		
		event.consume();
	}
	@Override
	public void mouseDoubleClicked(MouseEvent me) {
		// TODO Auto-generated method stub

	}

	public void startMoving(MouseEvent me) {
		location = me.getLocation();
	}

}