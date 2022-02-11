package org.dawnsci.mapping.ui.datamodel;

public class AssociatedImageStackBean extends AssociatedImageBean {
	
	
	public boolean checkValid() {
		if (name == null) return false;
		if (axes == null) return false;
		
		return true;
	}

}
