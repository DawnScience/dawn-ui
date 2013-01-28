package org.dawnsci.plotting.draw2d.swtxy;

import java.util.ArrayList;
import java.util.List;

import org.csstudio.swt.xygraph.undo.XYGraphMemento;

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
