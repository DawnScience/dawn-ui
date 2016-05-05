package org.dawnsci.isosurface.isogui;

public enum Type {
	ISO_SURFACE("Iso surface"),
	VOLUME("Volume");
	
	private String name;

	Type(String name){
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
