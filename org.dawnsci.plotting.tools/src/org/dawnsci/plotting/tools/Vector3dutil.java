/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */


package org.dawnsci.plotting.tools;

/*
 * creating a dependency to vecmath package from the plotting tools plugin was throwing a linkage error
 * 
 * This class has been created here as a workaround since the current plugin is dependant
 * on gda.libs that holds the vecmath package
 * */

import javax.vecmath.Vector3d;
import uk.ac.diamond.scisoft.analysis.diffraction.QSpace;

public class Vector3dutil {

	Vector3d q = null;

	public Vector3dutil(QSpace qSpace, double x, double y){
		q = qSpace.qFromPixelPosition(x, y);
	}

	public Vector3d getQ(QSpace qSpace, double x, double y ){

		return qSpace.qFromPixelPosition(x, y);
	}
	
	public Object getQMask(QSpace qSpace, double x, double y ){
		
		return (qSpace.qFromPixelPosition(x, y) == null) ? null : new Object();
	}
	
	public double getQx(){

		return q.x;
	}

	public double getQy(){

		return q.y;
	}
	
	public double getQz(){

		return q.z;
	}
	
	public double getQlength(){

		return q.length();
	}
	
	public double getQScatteringAngle(QSpace qSpace){
		return qSpace.scatteringAngle(q);
	}
}
