package org.dawb.workbench.plotting.tools;

import org.eclipse.swt.graphics.RGB;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

public class HistoryBean {

	private AbstractDataset xdata;
	private AbstractDataset ydata;
	private RGB             plotColour;
	private String          plotName; // Often the file name
	private String          traceName;
	private boolean         selected;
	
	/**
	 * Human readable trace name
	 * @return
	 */
	public String createTraceName() {
		return getTraceName()+" ("+getPlotName()+")";
	}
	public String getTraceKey() {
		return getTraceName()+":"+getPlotName();
	}
	
	public AbstractDataset getXdata() {
		return xdata;
	}

	public void setXdata(AbstractDataset xdata) {
		this.xdata = xdata;
	}
	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	public String getPlotName() {
		return plotName;
	}
	public void setPlotName(String parentPlotName) {
		this.plotName = parentPlotName;
	}
	public String getTraceName() {
		return traceName;
	}
	public void setTraceName(String originalTraceName) {
		this.traceName = originalTraceName;
	}
	public AbstractDataset getYdata() {
		return ydata;
	}
	public void setYdata(AbstractDataset data) {
		this.ydata = data;
	}
	public RGB getPlotColour() {
		return plotColour;
	}
	public void setPlotColour(RGB plotColour) {
		this.plotColour = plotColour;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((plotColour == null) ? 0 : plotColour.hashCode());
		result = prime * result
				+ ((plotName == null) ? 0 : plotName.hashCode());
		result = prime * result + (selected ? 1231 : 1237);
		result = prime * result
				+ ((traceName == null) ? 0 : traceName.hashCode());
		result = prime * result + ((xdata == null) ? 0 : xdata.hashCode());
		result = prime * result + ((ydata == null) ? 0 : ydata.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HistoryBean other = (HistoryBean) obj;
		if (plotColour == null) {
			if (other.plotColour != null)
				return false;
		} else if (!plotColour.equals(other.plotColour))
			return false;
		if (plotName == null) {
			if (other.plotName != null)
				return false;
		} else if (!plotName.equals(other.plotName))
			return false;
		if (selected != other.selected)
			return false;
		if (traceName == null) {
			if (other.traceName != null)
				return false;
		} else if (!traceName.equals(other.traceName))
			return false;
		if (xdata == null) {
			if (other.xdata != null)
				return false;
		} else if (!xdata.equals(other.xdata))
			return false;
		if (ydata == null) {
			if (other.ydata != null)
				return false;
		} else if (!ydata.equals(other.ydata))
			return false;
		return true;
	}


}
