package org.dawnsci.mapping.ui.datamodel;

public class MapBean {

	private String name;
	private String parent;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getParent() {
		return parent;
	}
	public void setParent(String parent) {
		this.parent = parent;
	}
	
	public boolean checkValid() {
		if (name == null) return false;
		if (parent == null) return false;
		return true;
	}
	
}
