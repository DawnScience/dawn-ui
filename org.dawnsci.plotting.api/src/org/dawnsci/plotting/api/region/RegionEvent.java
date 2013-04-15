package org.dawnsci.plotting.api.region;

import java.util.EventObject;

/**
 * Event with source of the IRegion affected.
 * 
 * @author fcp94556
 *
 */
public class RegionEvent extends EventObject {

	public RegionEvent(Object source) {
		super(source);
	}
	
	public IRegion getRegion() {
		return (IRegion)getSource();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 3121767937881041584L;

}
