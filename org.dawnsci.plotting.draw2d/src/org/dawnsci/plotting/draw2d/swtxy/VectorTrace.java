package org.dawnsci.plotting.draw2d.swtxy;

import java.util.List;

import org.csstudio.swt.xygraph.figures.Axis;
import org.dawnsci.plotting.api.trace.IVectorTrace;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.Triangle;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import uk.ac.diamond.scisoft.analysis.dataset.ADataset;
import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

/**
 * An implementation of VectorTrace based on the draw2d Figure
 * 
 * TODO Not curently implemented for rotated images.
 * 
 * TODO No downsampling currently implemented.
 * 
 * @author fcp94556
 *
 */
public class VectorTrace extends Figure implements IVectorTrace {

	private String         name;
	private String         dataName;
	private boolean        visible;
	private boolean        userTrace;
	private Object         userObject;
    private int[]          arrowColor=new int[]{0,0,0};
    
    private VectorNormalizationType vectorNormalizationType=VectorNormalizationType.LINEAR;
    private int maximumArrowSize = 20;

	private IDataset       vectors;
	private IDataset       normalizedMagnitude;
	private List<IDataset> axes;
	private Axis xAxis;
	private Axis yAxis;
	private PolygonDecoration polyline;

	
	public VectorTrace(String traceName, Axis xAxis, Axis yAxis) {
		setName(traceName);
		this.xAxis = xAxis;
		this.yAxis = yAxis;

		this.polyline = new PolygonDecoration();
		
        PointList pl = new PointList();

        pl.addPoint(0, 0);
        pl.addPoint(-2, 1);
        pl.addPoint(-2, -1);
        polyline.setTemplate(pl);

		add(polyline);
	}

	@Override
	public void dispose() {
		if (cachedColor!=null) cachedColor.dispose();
	}
	
	/**
	 * This figure simply paints the vectors.
	 * 
	 * TODO Downsampling!
	 */
	public void paint(Graphics graphics) {
		if (getLocalBackgroundColor() != null)
			graphics.setBackgroundColor(getLocalBackgroundColor());
		if (getLocalForegroundColor() != null)
			graphics.setForegroundColor(getLocalForegroundColor());

		graphics.pushState();
		try {
			graphics.setForegroundColor(getColor());
			graphics.setBackgroundColor(getColor());
			paintArrows(graphics);
		} finally {
			graphics.popState();
		}
	}

	private Color cachedColor = null;
	private Color getColor() {
		if (cachedColor==null) cachedColor = new Color(Display.getDefault(), arrowColor[0], arrowColor[1], arrowColor[2]);
		return cachedColor;
	}

	private void paintArrows(Graphics graphics) {
		
		final int[] shape = vectors.getShape();
		for (int x = 0; x < shape[0]; x++) {
			for (int y = 0; y < shape[1]; y++) {
				final double mag       = normalizedMagnitude.getDouble(x,y);
				final double angle     = vectors.getDouble(x,y,1);
				final double xloc      = axes!=null ? axes.get(0).getDouble(x) : x;
				final double yloc      = axes!=null ? axes.get(1).getDouble(y) : y;

				// TODO Make normalize magnitude
				paintArrow(graphics, xloc, yloc, mag, angle);
			}
		}
		
	}

	/**
	 *             
	 * @param xloc  - location of center of arrow
	 * @param yloc  - location of center of arrow
	 * @param length  - size of arrow, normalized from magnitude of vector.
	 * @param theta - anti-clockwise angle from 12 O'clock in radians.
	 */
	private void paintArrow(Graphics graphics, double xloc, double yloc, double length, double theta) {
		
		if (!xAxis.getRange().inRange(xloc) && !yAxis.getRange().inRange(yloc)) {
			return; // Nothing to draw
		}
		final int x    = xAxis.getValuePosition(xloc, false);
		final int y    = yAxis.getValuePosition(yloc, false);
		final double l = length/2d;
		
		final int xD = (int)Math.round(l*Math.sin(theta));
		final int yD = (int)Math.round(l*Math.cos(theta));
		
		final Point one = new Point(x-xD, y-yD);
		final Point two = new Point(x+xD, y+yD);
		
		graphics.drawLine(one, two);
		polyline.setLocation(one);
		polyline.setReferencePoint(two);
		polyline.setScale(l/3d, l/3d);
		
		polyline.paintFigure(graphics);
	}

	@Override
	public boolean setData(IDataset vectors, List<IDataset> axes) {
		this.vectors = vectors;
		this.axes    = axes;
		normalize();
		repaint();
		return true;
	}

	private void normalize() {
		
		int[] shape = vectors.getShape();
        normalizedMagnitude = AbstractDataset.zeros(new int[]{shape[0], shape[1]}, ADataset.FLOAT);
        
        double max  = -Double.MAX_VALUE;
		for (int x = 0; x < shape[0]; x++) {
			for (int y = 0; y < shape[1]; y++) {
				double val = vectors.getDouble(x,y,0);
				max = Math.max(val, max);
			}
		}
		
		for (int x = 0; x < shape[0]; x++) {
			for (int y = 0; y < shape[1]; y++) {
				
				double val   = vectors.getDouble(x,y,0);
				try {
			        double ratio = getVectorNormalizationType()==VectorNormalizationType.LOGARITHMIC
			        		    ? Math.log(val) / Math.log(max)
				                : val / max;
			        
			        normalizedMagnitude.set(ratio*getMaximumArrowSize(), x, y);
				} catch (Throwable ne) {
					normalizedMagnitude.set(1, x, y);
				}
			}
		}
	}

	@Override
	public List<IDataset> getAxes(){
		return axes;
	}
	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDataName() {
		return dataName;
	}

	public void setDataName(String dataName) {
		this.dataName = dataName;
	}

	public IDataset getData() {
		return vectors;
	}

	public boolean isUserTrace() {
		return userTrace;
	}

	public void setUserTrace(boolean userTrace) {
		this.userTrace = userTrace;
	}

	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	@Override
	public boolean is3DTrace() {
		return false;
	}


	@Override
	public int getRank() {
		return 2;
	}


	@Override
	public void setArrowColor(int... rgb) {
		cachedColor= null;
		arrowColor = rgb;
		repaint();
	}

	@Override
	public int[] getArrowColor() {
		return arrowColor;
	}


	public VectorNormalizationType getVectorNormalizationType() {
		return vectorNormalizationType;
	}

	public void setVectorNormalizationType(VectorNormalizationType vectorNormalizationType) {
		this.vectorNormalizationType = vectorNormalizationType;
		normalize();
		repaint();
	}

	public int getMaximumArrowSize() {
		return maximumArrowSize;
	}

	public void setMaximumArrowSize(int maximumArrowSize) {
		this.maximumArrowSize = maximumArrowSize;
		normalize();
		repaint();
	}

}
