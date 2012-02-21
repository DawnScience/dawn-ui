package org.dawb.workbench.plotting.system.swtxy;

import java.util.List;
import java.util.Map;

import org.csstudio.swt.xygraph.figures.XYGraph;
import org.csstudio.swt.xygraph.undo.XYGraphConfigCommand;
import org.csstudio.swt.xygraph.undo.XYGraphMemento;

public class XYRegionConfigCommand extends XYGraphConfigCommand {

	public XYRegionConfigCommand(XYGraph xyGraph) {
		
		super(xyGraph);
		
		previousXYGraphMem = new XYRegionMemento();		
		afterXYGraphMem = new XYRegionMemento();
		
		createDefaultSettings();
		
		final Map<String,Region> regionList     = ((RegionArea)xyGraph.getPlotArea()).getRegionMap();
		for(String name : regionList.keySet()){
			((XYRegionMemento)previousXYGraphMem).addRegionMemento(new RegionBean());
			((XYRegionMemento)afterXYGraphMem).addRegionMemento(new RegionBean());
		}	

	}

	
	protected void saveXYGraphPropsToMemento(XYGraph xyGraph, XYGraphMemento memento){
		super.saveXYGraphPropsToMemento(xyGraph, memento);
		
		int i=0;
		final List<Region>     regionList     = ((RegionArea)xyGraph.getPlotArea()).getRegions();
		final List<RegionBean> regionBeanList = ((XYRegionMemento)memento).getRegionBeanList();
		for(Region region : regionList) {
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
	
	
	private void saveRegionPropsToMemento(Region region, RegionBean memento){		
		memento.sync(region.getBean());
	}
	
	private void restoreRegionPropsFromMemento(Region region, RegionBean regionBean){		
		region.sync(regionBean);	
	}
}
