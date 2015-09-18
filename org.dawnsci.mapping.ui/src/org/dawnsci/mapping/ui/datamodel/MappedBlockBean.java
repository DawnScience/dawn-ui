package org.dawnsci.mapping.ui.datamodel;

public class MappedBlockBean {

	private String name;
	private int rank;
	private String[] axes;
	private String xAxisForRemapping;
	private int xDim;
	private int yDim;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	public String[] getAxes() {
		return axes;
	}
	public void setAxes(String[] axes) {
		this.axes = axes;
	}
	public int getxDim() {
		return xDim;
	}
	public void setxDim(int xDim) {
		this.xDim = xDim;
	}
	public int getyDim() {
		return yDim;
	}
	public void setyDim(int yDim) {
		this.yDim = yDim;
	}
	public String getxAxisForRemapping() {
		return xAxisForRemapping;
	}
	
	public void setxAxisForRemapping(String xAxisForRemapping) {
		this.xAxisForRemapping = xAxisForRemapping;
	}
	
	
}
