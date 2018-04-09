package org.dawnsci.jzy3d;

import java.util.List;

import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.plot3d.builder.concrete.WaterfallTessellator;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.primitives.LineStrip;
import org.jzy3d.plot3d.primitives.Polygon;
import org.jzy3d.plot3d.primitives.Shape;

/**
 * A drawable Waterfall using <a href="">Matlab style</a>
 * 
 * @see WaterfallTessellator
 * 
 * @author Jacob Filik
 */
public class WaterfallComposite extends Shape {

	public void add(ColoredWireframePolygon outline, Shape fill) {
		
//		List<AbstractDrawable> drawables = fill.getDrawables();
//		for (AbstractDrawable dr : drawables) {
//			if (dr instanceof Polygon) {
//				Polygon p = (Polygon)dr;
//				p.setPolygonOffsetFactor(-1);
//				p.setPolygonOffsetUnit(1);
//				p.setPolygonOffsetFillEnable(true);
//				p.setWireframeDisplayed(true);
//				p.setColor(Color.BLUE);
//				p.setFaceDisplayed(true);
//			}
//		}
//		
//		outline.setPolygonOffsetFillEnable(true);
//		outline.setPolygonOffsetFactor(10);
//		outline.setPolygonOffsetUnit(1);
//		outline.setFaceDisplayed(true);
//		outline.setColor(Color.BLUE);
//		outline.setWireframeColor(Color.BLACK)
//		outline.setFaceDisplayed(false);
//		fill.setFaceDisplayed(false);
		add(fill);
		add(outline);
	}
	
	@Override
	public void setWireframeWidth(float width) {
		if (getDrawables() == null) return;
		for (AbstractDrawable d : getDrawables()) {
			if (d instanceof ColoredWireframePolygon) {
				((ColoredWireframePolygon) d).setWireframeWidth(width);
			}
		}
	}
	
	@Override
	public void setColorMapper(ColorMapper mapper) {
		for (AbstractDrawable d : getDrawables()) {
			if (d instanceof ColoredWireframePolygon) {
				((ColoredWireframePolygon) d).setColorMapper(mapper);
			}
		}
	}
	
	@Override
	public void setColor(Color color) {
		for (AbstractDrawable d : getDrawables()) {
			if (d instanceof ColoredWireframePolygon) {
				((ColoredWireframePolygon) d).setWireframeColor(color);
			}
		}
	}

}
