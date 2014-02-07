package org.dawnsci.plotting.draw2d.swtxy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawnsci.plotting.api.trace.IVectorTrace;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
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
    private int[]          arrowColor = new int[]{0,0,0};
    private int[]          circleColor= new int[]{0,0,0};
    private PaletteData    arrowPalette;

	private VectorNormalization vectorNormalizationType = VectorNormalization.LINEAR;
    private ArrowConfiguration  arrowPosition           = ArrowConfiguration.THROUGH_CENTER;
    private ArrowHistogram      arrowHistogram          = ArrowHistogram.FIXED_COLOR;

	private int maximumArrowSize = 20;

	private IDataset       vectors;
	private IDataset       normalizedMagnitude;
	private IDataset       normalizedAngle;
	private List<IDataset> axes;
	private Axis xAxis;
	private Axis yAxis;
	private PolygonDecoration polyline;
    private Map<RGB, Color> colorMap;
	
	public VectorTrace(String traceName, Axis xAxis, Axis yAxis) {
		setName(traceName);
		this.xAxis = xAxis;
		this.yAxis = yAxis;

		this.polyline = new PolygonDecoration();
		
        PointList pl = new PointList();

        pl.addPoint(0, 0);
        pl.addPoint(-3, 1);
        pl.addPoint(-3, -1);
        polyline.setTemplate(pl);

		add(polyline);
		
		colorMap = new HashMap<RGB, Color>(3);
	}

	@Override
	public void dispose() {
		// TODO Check this is called
		for (Color col : colorMap.values()) {
			col.dispose();
		}
		colorMap.clear();
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
			paintArrows(graphics);
		} finally {
			graphics.popState();
		}
	}

	private Color getArrowSWTColor(final int x, final int y) {
		
		RGB key = new RGB(arrowColor[0], arrowColor[1], arrowColor[2]);
		if (arrowPalette!=null) {
			int pos = -1;
			if (getArrowHistogram()==ArrowHistogram.COLOR_BY_MAGNITUDE) {
				pos = (int)Math.round(252*normalizedMagnitude.getDouble(x,y));
			} else {
				pos = (int)Math.round(252*normalizedAngle.getDouble(x,y));
			}
			if (pos>-1) {
				key = arrowPalette.getRGB(pos);
			}
		} 
		
		Color cached = colorMap.get(key);
		if (cached!=null) return cached;
		
		cached = new Color(Display.getDefault(), key);
		colorMap.put(key,  cached);
		return cached;
	}
	
	private Color getCircleSWTColor() {
		
		RGB key = new RGB(circleColor[0], circleColor[1], circleColor[2]);
		
		Color cached = colorMap.get(key);
		if (cached!=null) return cached;

		cached = new Color(Display.getDefault(), key);
		colorMap.put(key,  cached);
		return cached;
	}

	private void paintArrows(Graphics graphics) {
		
		final int[] shape = vectors.getShape();
		for (int x = 0; x < shape[0]; x++) {
			for (int y = 0; y < shape[1]; y++) {
				final double mag       = getMaximumArrowSize()*normalizedMagnitude.getDouble(x,y);
				final double angle     = vectors.getDouble(x,y,1);
				final double xloc      = axes!=null ? axes.get(0).getDouble(x) : x;
				final double yloc      = axes!=null ? axes.get(1).getDouble(y) : y;

				// TODO Make normalize magnitude
				graphics.setForegroundColor(getArrowSWTColor(x,y));
				graphics.setBackgroundColor(getArrowSWTColor(x,y));
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
		
		final Point one;
		final Point two;
		
		if (arrowPosition==ArrowConfiguration.THROUGH_CENTER) {
			final int xD = (int)Math.round(l*Math.sin(theta));
			final int yD = (int)Math.round(l*Math.cos(theta));
		    one = new Point(x-xD, y-yD);
		    two = new Point(x+xD, y+yD);
		} else {
			final int xD = (int)Math.round(length*Math.sin(theta));
			final int yD = (int)Math.round(length*Math.cos(theta));
			one = new Point(x,y);
		    two = new Point(x+xD, y+yD);
		    
		    if (arrowPosition==ArrowConfiguration.TO_CENTER_WITH_CIRCLE) {
		    	graphics.pushState();
				graphics.setForegroundColor(getCircleSWTColor());
				graphics.setBackgroundColor(getCircleSWTColor());
		    	graphics.fillOval(one.x-1, one.y-1, 4, 4);
		    	graphics.popState();
		    }
		}
		
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

	/**
	 * Normalizes the arrow length to the pixel value defined in maximumArrowSize
	 */
	private void normalize() {
		
		int[] shape = vectors.getShape();
        normalizedMagnitude = AbstractDataset.zeros(new int[]{shape[0], shape[1]}, ADataset.FLOAT);
        normalizedAngle     = AbstractDataset.zeros(new int[]{shape[0], shape[1]}, ADataset.FLOAT);
        
        double maxMag  = -Double.MAX_VALUE;
        double maxAng  = -Double.MAX_VALUE;
		for (int x = 0; x < shape[0]; x++) {
			for (int y = 0; y < shape[1]; y++) {
				double mag = vectors.getDouble(x,y,0);
				maxMag = Math.max(mag, maxMag);
				
				double ang = vectors.getDouble(x,y,1);
				maxAng = Math.max(ang, maxAng);
			}
		}
		
		for (int x = 0; x < shape[0]; x++) {
			for (int y = 0; y < shape[1]; y++) {
				
				double mag   = vectors.getDouble(x,y,0);
				try {
			        double ratio = getVectorNormalization()==VectorNormalization.LOGARITHMIC
			        		     ? Math.log(mag) / Math.log(maxMag)
				                 : mag / maxMag;
			        
			        normalizedMagnitude.set(ratio, x, y);
				} catch (Throwable ne) {
					normalizedMagnitude.set(0, x, y);
				}
				
				double ang   = vectors.getDouble(x,y,1);
				try {
			        double ratio = getVectorNormalization()==VectorNormalization.LOGARITHMIC
			        		     ? Math.log(ang) / Math.log(maxAng)
				                 : ang / maxAng;
			        
			        normalizedAngle.set(ratio, x, y);
				} catch (Throwable ne) {
					normalizedAngle.set(0, x, y);
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
		arrowColor = rgb;
		repaint();
	}

	@Override
	public int[] getArrowColor() {
		return arrowColor;
	}


	public VectorNormalization getVectorNormalization() {
		return vectorNormalizationType;
	}

	public void setVectorNormalization(VectorNormalization vectorNormalizationType) {
		this.vectorNormalizationType = vectorNormalizationType;
		normalize();
		repaint();
	}

	public int getMaximumArrowSize() {
		return maximumArrowSize;
	}

	public void setMaximumArrowSize(int maximumArrowSize) {
		this.maximumArrowSize = maximumArrowSize;
		repaint();
	}
    
    public ArrowConfiguration getArrowConfiguration() {
		return arrowPosition;
	}

	public void setArrowConfiguration(ArrowConfiguration arrowPosition) {
		this.arrowPosition = arrowPosition;
		repaint();
	}
    
    public int[] getCircleColor() {
		return circleColor;
	}

    @Override
	public void setCircleColor(int... circleColor) {
		this.circleColor = circleColor;
	}

	public PaletteData getArrowPalette() {
		return arrowPalette;
	}

	public void setArrowPalette(PaletteData arrowPalette) {
		this.arrowPalette = arrowPalette;
		repaint();
	}

	public ArrowHistogram getArrowHistogram() {
		return arrowHistogram;
	}

	public void setArrowHistogram(ArrowHistogram arrowHistogram) {
		this.arrowHistogram = arrowHistogram;
	}

}
