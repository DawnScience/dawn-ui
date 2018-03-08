package org.dawnsci.mapping.ui.datamodel;

public interface LockableMapObject extends PlottableMapObject {
	
	public void setLock(Object lock);
	
	public Object getLock();

}
