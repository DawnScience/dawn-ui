/*
 * Copyright (c) 2012, 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.system.dialog;

import java.util.List;

import org.dawnsci.plotting.draw2d.swtxy.RegionArea;
import org.dawnsci.plotting.draw2d.swtxy.RegionBean;
import org.dawnsci.plotting.draw2d.swtxy.XYRegionMemento;
import org.dawnsci.plotting.draw2d.swtxy.selection.AbstractSelectionRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.nebula.visualization.internal.xygraph.undo.XYGraphConfigCommand;
import org.eclipse.nebula.visualization.internal.xygraph.undo.XYGraphMemento;
import org.eclipse.nebula.visualization.internal.xygraph.undo.XYGraphMementoUtil;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;

public class XYRegionConfigCommand extends XYGraphConfigCommand {

	@SuppressWarnings("unused")
	public XYRegionConfigCommand(IXYGraph xyGraph) {
		super(xyGraph, XYRegionMemento::new);

		final RegionArea regionArea = (RegionArea) xyGraph.getPlotArea();
		for (String name : regionArea.getRegionNames()) {
			((XYRegionMemento) getPreviousXYGraphMem()).addRegionMemento(new RegionBean());
			((XYRegionMemento) getAfterXYGraphMem()).addRegionMemento(new RegionBean());
		}
	}

	protected void saveXYGraphPropsToMemento(IXYGraph xyGraph, XYGraphMemento memento){
		XYGraphMementoUtil.saveXYGraphPropsToMemento(xyGraph, memento);
		
		int i=0;
		final List<IRegion>     regionList     = ((RegionArea)xyGraph.getPlotArea()).getRegions();
		final List<RegionBean> regionBeanList = ((XYRegionMemento)memento).getRegionBeanList();
		for(IRegion region : regionList) {
			saveRegionPropsToMemento(region,regionBeanList.get(i));
			++i;
		}
	}
	
	protected void restoreXYGraphPropsFromMemento(IXYGraph xyGraph, XYGraphMemento memento){
		XYGraphMementoUtil.restoreXYGraphPropsFromMemento(xyGraph, memento);

		int i=0;
		for(RegionBean rb : ((XYRegionMemento)memento).getRegionBeanList()) {
			restoreRegionPropsFromMemento(((RegionArea)xyGraph.getPlotArea()).getRegions().get(i), rb);
			++i;
		}

	}
	
	
	private void saveRegionPropsToMemento(IRegion region, RegionBean memento){		
		memento.sync(((AbstractSelectionRegion<?>)region).getBean());
	}
	
	private void restoreRegionPropsFromMemento(IRegion region, RegionBean regionBean){		
		((AbstractSelectionRegion<?>)region).sync(regionBean);	
	}
}
