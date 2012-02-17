package org.dawb.workbench.ui.editors.plotting.swtxy;

import org.csstudio.swt.xygraph.figures.Axis;
import org.csstudio.swt.xygraph.figures.XYGraph;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Shape;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Color;
import org.dawb.common.ui.plot.region.IRegion;

/**
 * Shape used for ROIs which has bounds fixed to the graph area.
 * 
 * @author fcp94556
 *
 */
public abstract class Region implements IRegion{


	private RegionBean bean;
    private ISelectionProvider selectionProvider;
    private IFigure[] regionObjects;

	public Region(String name, Axis xAxis, Axis yAxis) {
		super();
		this.bean = new RegionBean();
		bean.setName(name);
		bean.setxAxis(xAxis);
		bean.setyAxis(yAxis);
	}

	public abstract void createContents(final Figure parent);


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

	protected void setRegionObjects(IFigure... objects) {
		this.regionObjects = objects;
	}
	
	public String getName() {
		return bean.getName();
	}
	
	public void remove() {
		if (regionObjects!=null)for (IFigure ob : regionObjects) {
			if (ob.getParent()!=null) ob.getParent().remove(ob);
		}
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

		bean.setyAxis(yAxis);
	}

}
