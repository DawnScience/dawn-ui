/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.jreality.tool;

import java.util.EventListener;

/**
 * AreaSelectEventListener allows to listen to any successful rectangle area selection that have been
 * done inside the 3D framework, the AreaSelectEvent contains the rectangle coordinates in DataSet space
 */

public interface AreaSelectEventListener extends EventListener {

	/**
	 * An area has been selected
	 * @param event AreaSelectEvent object
	 */
	public void areaSelected(AreaSelectEvent event);
	
}
