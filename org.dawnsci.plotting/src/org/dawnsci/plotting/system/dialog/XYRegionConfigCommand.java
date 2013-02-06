package org.dawnsci.plotting.system.dialog;

import java.util.List;
import java.util.Map;

import org.csstudio.swt.xygraph.figures.XYGraph;
import org.csstudio.swt.xygraph.undo.XYGraphConfigCommand;
import org.csstudio.swt.xygraph.undo.XYGraphMemento;
import org.dawnsci.plotting.draw2d.swtxy.RegionArea;
import org.dawnsci.plotting.draw2d.swtxy.RegionBean;
import org.dawnsci.plotting.draw2d.swtxy.XYRegionMemento;
import org.dawnsci.plotting.draw2d.swtxy.selection.AbstractSelectionRegion;

public class XYRegionConfigCommand extends XYGraphConfigCommand {

	public XYRegionConfigCommand(XYGraph xyGraph) {
		
		super(xyGraph);
		
		previousXYGraphMem = new XYRegionMemento();		
		afterXYGraphMem = new XYRegionMemento();
		
		createDefaultSettings();
		
		final Map<String,AbstractSelectionRegion> regionList     = ((RegionArea)xyGraph.getPlotArea()).getRegionMap();
		for(String name : regionList.keySet()){
			((XYRegionMemento)previousXYGraphMem).addRegionMemento(new RegionBean());
			((XYRegionMemento)afterXYGraphMem).addRegionMemento(new RegionBean());
		}	

	}

	
	protected void saveXYGraphPropsToMemento(XYGraph xyGraph, XYGraphMemento memento){
		super.saveXYGraphPropsToMemento(xyGraph, memento);
		
		int i=0;
		final List<AbstractSelectionRegion>     regionList     = ((RegionArea)xyGraph.getPlotArea()).getRegions();
		final List<RegionBean> regionBeanList = ((XYRegionMemento)memento).getRegionBeanList();
		for(AbstractSelectionRegion region : regionList) {
			saveRegionPropsToMemento(region,regionBeanList.get(i));
			++i;
		}
	}
	
	protected void restoreXYGraphPropsFromMemento(XYGraph xyGraph, XYGraphMemento memento){
		super.restoreXYGraphPropsFromMemento(xyGraph, memento);

		int i=0;
		for(RegionBean rb : ((XYRegionMemento)memento).getRegionBeanList()) {
			restoreRegionPropsFromMemento(((RegionArea)xyGraph.getPlotArea()).getRegions().get(i), rb);
			++i;
		}

	}
	
	
	private void saveRegionPropsToMemento(AbstractSelectionRegion region, RegionBean memento){		
		memento.sync(region.getBean());
	}
	
	private void restoreRegionPropsFromMemento(AbstractSelectionRegion region, RegionBean regionBean){		
		region.sync(regionBean);	
	}
}
