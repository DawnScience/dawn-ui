package org.dawnsci.jzy3d;

import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.transform.squarifier.ISquarifier;

public class CustomSquarifier implements ISquarifier {
	
	float xScaleFactor = 1;
	float yScaleFactor = 1;
	float zScaleFactor = 1;
	
	@Override
	public Coord3d scale(float x, float y, float z) {
		
		float max = Math.max(Math.max(x, y),z);
		
		return new Coord3d(max/x*xScaleFactor, max/y*yScaleFactor,max/z*zScaleFactor);
	}
	
	
	public void setxScaleFactor(float xScaleFactor) {
		this.xScaleFactor = xScaleFactor;
	}


	public void setyScaleFactor(float yScaleFactor) {
		this.yScaleFactor = yScaleFactor;
	}


	public void setzScaleFactor(float zScaleFactor) {
		this.zScaleFactor = zScaleFactor;
	}

}
