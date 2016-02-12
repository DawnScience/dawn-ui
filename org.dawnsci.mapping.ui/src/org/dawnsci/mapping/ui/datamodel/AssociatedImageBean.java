package org.dawnsci.mapping.ui.datamodel;

public class AssociatedImageBean {

	private String name;
	private String[] axes;

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String[] getAxes() {
		return axes;
	}
	public void setAxes(String[] axes) {
		this.axes = axes;
	}
	
	public boolean checkValid() {
		if (name == null) return false;
		if (axes == null) return false;
		if (axes[0] == null) return false;
		if (axes[1] == null) return false; 
		
		return true;
	}
}
