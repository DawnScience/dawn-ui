/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawnsci.plotting.api;

public enum PlotType {
    
	IMAGE(2), 
    
    SURFACE(3), 
    
    XY(1), 
        
    XY_STACKED(1),

    XY_STACKED_3D(3);
    
	
	
	
	private final int rank;

	private PlotType(int rank) {
    	this.rank = rank;
	}
	
	public static PlotType forSliceIndex(int type) {
		switch(type) {
		case 0:
			return IMAGE;
		case 1:
			return SURFACE;
		}
		return null;
	}

	public boolean is1D() {
		return rank==1;
	}
	public boolean is2D() {
		return rank==2;
	}
	public boolean is3D() {
		return rank==3;
	}
	public boolean is1Dor2D() {
		return is1D()||is2D();
	}
}
