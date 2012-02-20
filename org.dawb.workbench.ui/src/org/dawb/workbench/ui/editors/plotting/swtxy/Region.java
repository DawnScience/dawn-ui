package org.dawb.workbench.ui.editors.plotting.swtxy;

import org.csstudio.swt.xygraph.figures.Axis;
import org.csstudio.swt.xygraph.figures.IAxisListener;
import org.csstudio.swt.xygraph.figures.XYGraph;
import org.csstudio.swt.xygraph.linearscale.Range;
import org.dawb.common.ui.plot.region.AbstractRegion;
import org.dawb.workbench.ui.Activator;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * Shape used for ROIs which has bounds fixed to the graph area.
 * 
 * @author fcp94556
 *
 */
public abstract class Region extends AbstractRegion implements IAxisListener{


	private RegionBean bean;
    private ISelectionProvider selectionProvider;
    private IFigure[] regionObjects;

	public Region(String name, Axis xAxis, Axis yAxis) {
		super();
		this.bean = new RegionBean();
		bean.setName(name);
		bean.setxAxis(xAxis);
		bean.setyAxis(yAxis);
		xAxis.addListener(this);
		yAxis.addListener(this);
	}

	/**
	 * Creates the contents of the selection ie the figure(s)
	 * which make up the selection.
	 * 
	 * @param parent
	 */
	public abstract void createContents(final Figure parent);

	/**
	 * Return the type of region which this provides UI for.
	 * @return
	 */
	public abstract RegionType getRegionType();
	
	/**
	 * If there is a fill figure, this method may be called to 
	 * refill the fill.
	 */
	protected abstract void updateConnectionBounds();
	
	/**
	 * Paint the regions before it is created during the first click
	 * and drag of the user.
	 * 
	 * @param g
	 * @param start
	 * @param end
	 */
	public abstract void paintBeforeAdded(final Graphics g, Rectangle bounds);

	/**
	 * Sets the local of the region in local coordinates not axis ones.
	 * @param bounds
	 */
	public abstract void setLocalBounds(Rectangle bounds);
	
	/**
	 * This cursor is used after the region is created and
	 * before the user has clicked to create it. This is the
	 * path to the cursor, for instance "icons/Cursor-box.png"
	 * @return
	 */
	protected abstract String getCursorPath();

	public void sync(RegionBean bean) {
		setName(bean.getName());
		setShowPosition(bean.isShowPosition());
		setxAxis(bean.getxAxis());
		setyAxis(bean.getyAxis());
		setXyGraph(bean.getXyGraph());
		setRegionColor(bean.getRegionColor());
		setAlpha(bean.getAlpha());
		setVisible(bean.isVisible());
		setMotile(bean.isMotile());
	}
	
	private Cursor cursor;
	/**
	 * A new cursor is created on each call.
	 */
	public Cursor getCursor() {
		if (cursor==null)  {
			Image image = Activator.getImage(getCursorPath());
			cursor = new Cursor(Display.getDefault(), image.getImageData(), 8, 8);
		}
		return cursor;
	}

	
	public void axisRevalidated(Axis axis) {
		updateRegionBounds();
	}
	public void axisRangeChanged(Axis axis, Range old_range, Range new_range) {
		updateRegionBounds();
	}
			
	protected void setRegionObjects(IFigure... objects) {
		this.regionObjects = objects;
	}
	
	public String getName() {
		return bean.getName();
	}
	
	/**
	 * Remove from graph and remove all RegionBoundsListeners.
	 */
	public void remove() {
		if (regionObjects!=null)for (IFigure ob : regionObjects) {
			if (ob.getParent()!=null) ob.getParent().remove(ob);
		}
		if (cursor!=null) cursor.dispose();
		clearListeners();
	}

	protected void clearListeners() {
        getxAxis().removeListenr(this);
        getyAxis().removeListenr(this);
		super.clearListeners();
	}
	
	public void setName(String name) {
		bean.setName(name);
	}


	public XYGraph getXyGraph() {
		return bean.getXyGraph();
	}


	public void setXyGraph(XYGraph xyGraph) {
		bean.setXyGraph(xyGraph);
	}

	public Color getRegionColor() {
		return bean.getRegionColor();
	}


	public void setRegionColor(Color regionColor) {
		if (regionObjects!=null) for (IFigure ob : regionObjects) {
			if (ob!=null) {
				ob.setForegroundColor(regionColor);
				ob.setBackgroundColor(regionColor);
			}
		}
		bean.setRegionColor(regionColor);
	}


	public boolean isShowPosition() {
		return bean.isShowPosition();
	}


	public void setShowPosition(boolean showPosition) {
		if (regionObjects!=null) for (IFigure ob : regionObjects) {
			if (ob instanceof SelectionRectangle) {
				((SelectionRectangle)ob).setShowPosition(showPosition);
			}
		}
		bean.setShowPosition(showPosition);
	}

	public void setAlpha(int alpha) {
		if (regionObjects!=null) for (IFigure ob : regionObjects) {
			if (ob instanceof SelectionRectangle) {
				((SelectionRectangle)ob).setAlpha(alpha);
			}
			if (ob instanceof Shape) {
				((Shape)ob).setAlpha(alpha);
			}
		}
		bean.setAlpha(alpha);
	}
	public int getAlpha() {
		return bean.getAlpha();
	}

	public RegionBean getBean() {
		return bean;
	}

	public ISelectionProvider getSelectionProvider() {
		return selectionProvider;
	}

	public void setSelectionProvider(ISelectionProvider selectionProvider) {
		this.selectionProvider = selectionProvider;
	}

	public boolean isVisible() {
		return bean.isVisible();
	}

	public void setVisible(boolean visible) {
		if (regionObjects!=null) for (IFigure ob : regionObjects) {
			if (ob instanceof SelectionRectangle) {
				((SelectionRectangle)ob).setVisible(visible&&isMotile());
			} else {
			    if (ob!=null) ob.setVisible(visible);
			}
		}
		bean.setVisible(visible);
	}

	public boolean isMotile() {
		return bean.isMotile();
	}

	public void setMotile(boolean motile) {
		if (regionObjects!=null) for (IFigure ob : regionObjects) {
			if (ob instanceof SelectionRectangle) {
				((SelectionRectangle)ob).setVisible(motile);
			} else if (ob instanceof RegionFillFigure) {
				((RegionFillFigure)ob).setMotile(motile);
			}
		}
		bean.setMotile(motile);
	}


	public void repaint() {
		if (regionObjects!=null) for (IFigure ob : regionObjects) {
			if (ob!=null) ob.repaint();
		}
	}

	public Axis getxAxis() {
		return bean.getxAxis();
	}

	public void setxAxis(Axis xAxis) {
		if (regionObjects!=null) for (IFigure ob : regionObjects) {
			if (ob instanceof SelectionRectangle) {
				((SelectionRectangle)ob).setxAxis(xAxis);
			}
		}
		getxAxis().removeListenr(this);
		xAxis.addListener(this);
		bean.setxAxis(xAxis);
	}

	public Axis getyAxis() {
		return bean.getyAxis();
	}

	public void setyAxis(Axis yAxis) {
		if (regionObjects!=null) for (IFigure ob : regionObjects) {
			if (ob instanceof SelectionRectangle) {
				((SelectionRectangle)ob).setyAxis(yAxis);
			}
		}
		getyAxis().removeListenr(this);
		yAxis.addListener(this);
		bean.setyAxis(yAxis);
	}
	
	public TranslationListener createRegionNotifier() {
		return new TranslationListener() {
			@Override
			public void translateBefore(TranslationEvent evt) {
				
			}
			@Override
			public void translationAfter(TranslationEvent evt) {
				
			}
			@Override
			public void translationCompleted(TranslationEvent evt) {
				createRegionBounds(true);
				fireRegionBoundsChanged(getRegionBounds());
			}
			
		};
	}

}
