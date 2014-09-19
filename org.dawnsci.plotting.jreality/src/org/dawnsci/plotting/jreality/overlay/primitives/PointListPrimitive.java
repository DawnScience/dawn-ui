/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.jreality.overlay.primitives;

import de.jreality.geometry.PointSetFactory;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;

/**
 *
 */
public class PointListPrimitive extends PointPrimitive {

	private double[] pointCoords;
	private int numVertices;
	
	public PointListPrimitive(SceneGraphComponent comp) {
		super(comp);
	}

	public PointListPrimitive(SceneGraphComponent comp, 
							  boolean fixedSize) {
		super(comp,fixedSize);
	}

	@Override
	public void setPoint(double x, double y) {
		
	}
	
	public void setPoints(double[] x, double[] y) {
		if (x.length == y.length) {
			pointCoords = new double[x.length*3];
			numVertices = x.length;
			for (int i = 0; i < x.length; i++) {
				pointCoords[i*3] = x[i];
				pointCoords[i*3+1] = y[i];
				pointCoords[i*3+2] = 0.005;
			}
			needToUpdateGeom = true;
		}
	}
	
	@Override
	protected PointSet createPointGeometry() {
		pFactory = new PointSetFactory();
		pFactory.setVertexCount(numVertices);
		pFactory.setVertexCoordinates(pointCoords);
		pFactory.update();
		return pFactory.getPointSet();
	}	
}
