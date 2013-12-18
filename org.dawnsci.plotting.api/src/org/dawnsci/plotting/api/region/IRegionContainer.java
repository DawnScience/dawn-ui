/*-
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.api.region;

/**
 * An interface to provide a region from an object which may be containing it.
 * 
 * For instance children of a selection region and/or the selection region itself may be.
 * 
 * @author fcp94556
 *
 */
public interface IRegionContainer {

	public IRegion getRegion();

	public void setRegion(IRegion region);
}
