package org.dawb.workbench.plotting.tools.history;

import java.util.List;

import org.eclipse.swt.graphics.RGB;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

class HistoryBean {
	
	// Image compare
	private AbstractDataset       data;
	private List<AbstractDataset> axes;
	private ImageOperator    operator;
	
	// 1D history
	private AbstractDataset xdata;
	private AbstractDataset ydata;	
	private RGB             plotColour;
	
	// Anyone
	private String          plotName; // Often the file name
	private String          traceName;
	private boolean         selected;
	private boolean         modifiable=true;
	
	/**
	 * Human readable trace name
	 * @return
	 */
	public String createTraceName() {
		return getTraceName()+" ("+getPlotName()+")";
	}
	public String getTraceKey() {
		if (fixedImageKey!=null) return fixedImageKey;
		createFixedKey(true);
		return fixedImageKey;
	}
	
	private String fixedImageKey; // Once the key is generated, it is used in a map and cannot change.
	public void setFixedImageKey(String key) {
		this.fixedImageKey = key;
	}

	public AbstractDataset getXdata() {
		return xdata;
	}

	public AbstractDataset getData() {
		return data;
	}
	public void setData(AbstractDataset data) {
		this.data = data;
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
		createFixedKey(false);
	}
	private void createFixedKey(boolean force) {
		if (fixedImageKey==null && traceName!=null && plotName!=null) {
			fixedImageKey = getTraceName()+":"+getPlotName();
		}
		if (force && fixedImageKey==null) { // Nulls allowed
			fixedImageKey = getTraceName()+":"+getPlotName();
		}
	}
	public String getTraceName() {
		return traceName;
	}
	public void setTraceName(String originalTraceName) {
		this.traceName = originalTraceName;
		createFixedKey(false);
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
		result = prime * result + ((axes == null) ? 0 : axes.hashCode());
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + (modifiable ? 1231 : 1237);
		result = prime * result
				+ ((operator == null) ? 0 : operator.hashCode());
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
		if (axes == null) {
			if (other.axes != null)
				return false;
		} else if (!axes.equals(other.axes))
			return false;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (modifiable != other.modifiable)
			return false;
		if (operator != other.operator)
			return false;
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
	public List<AbstractDataset> getAxes() {
		return axes;
	}
	public void setAxes(List<AbstractDataset> axes) {
		this.axes = axes;
	}
	public ImageOperator getOperator() {
		return operator;
	}
	public void setOperator(ImageOperator operator) {
		this.operator = operator;
	}
	public boolean isModifiable() {
		return modifiable;
	}
	public void setModifiable(boolean modifiable) {
		this.modifiable = modifiable;
	}


}
