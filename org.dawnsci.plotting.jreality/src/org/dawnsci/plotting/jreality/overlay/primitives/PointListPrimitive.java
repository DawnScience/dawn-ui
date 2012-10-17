/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
