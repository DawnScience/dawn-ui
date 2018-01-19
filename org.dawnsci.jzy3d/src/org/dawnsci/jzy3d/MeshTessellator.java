package org.dawnsci.jzy3d;

import java.util.ArrayList;
import java.util.List;

import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.Polygon;
import org.jzy3d.plot3d.primitives.Shape;

public class MeshTessellator {
	
	
	public static Shape buildShape(float[] x, float[] y, float[] z) {
		Shape s = new Shape();
		s.add(getSquarePolygonsOnCoordinates(x,y,z));
		return s;
	}
	
	public static List<AbstractDrawable> getSquarePolygonsOnCoordinates(float[] x, float[] y, float[] z){
		List<AbstractDrawable> polygons = new ArrayList<AbstractDrawable>();
		
		for(int xi=0; xi<x.length-1; xi++){
			for(int yi=0; yi<y.length-1; yi++){
				// Compute quad making a polygon 
				Point p[] = getRealQuadStandingOnPoint(xi, yi, x, y, z);
				// Store quad
				AbstractDrawable quad = newQuad(p);
                polygons.add(quad);
			}
		}	
		return polygons;
	}
	
	private static Point[] getRealQuadStandingOnPoint(int xi, int yi, float[] x, float[] y, float[] z){
		Point p[]  = new Point[4];
		
		int nx = x.length;
		int ny = y.length;
		
		int pos = yi+xi*y.length;
		int posNext = yi+(xi+1)*y.length;
		
		p[0] = new Point(new Coord3d(x[xi],   y[yi],   z[pos]    ));
		p[1] = new Point(new Coord3d(x[xi+1], y[yi],   z[posNext]  ));
		p[2] = new Point(new Coord3d(x[xi+1], y[yi+1], z[posNext+1]));
		p[3] = new Point(new Coord3d(x[xi],   y[yi+1], z[pos+1]  ));

		return p;
	}
	
	private static AbstractDrawable newQuad(Point p[]){
	    Polygon quad = new Polygon();
        for(int pi=0; pi<p.length; pi++)
            quad.add(p[pi]);
        return quad;
	}

}
