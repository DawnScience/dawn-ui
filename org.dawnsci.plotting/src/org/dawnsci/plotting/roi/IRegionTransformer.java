/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.roi;

import org.eclipse.dawnsci.analysis.api.roi.IROI;

public interface IRegionTransformer {

	public IROI getROI()  throws Exception;
	
	public Object getValue(IROI value)  throws Exception;

	public String getRendererText();
}
