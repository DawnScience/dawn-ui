/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.draw2d.swtxy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.visualization.xygraph.undo.XYGraphMemento;

public class XYRegionMemento extends XYGraphMemento {

	private List<RegionBean> regionBeanList;
	
	public XYRegionMemento() {
		super();
		this.regionBeanList = new ArrayList<RegionBean>();
	}

	public void addRegionMemento(RegionBean regionBean) {
		regionBeanList.add(regionBean);
	}

	public List<RegionBean> getRegionBeanList() {
		return regionBeanList;
	}

}
