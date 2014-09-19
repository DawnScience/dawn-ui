/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.jreality.overlay;
import org.dawnsci.plotting.jreality.tool.AreaSelectEventListener;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.OverlayConsumer;

/**
 * Overlay1D Consumer interface allows drawing overlays on a 1D Graph plot it
 * needs to register to a OverlayProvider
 */
public interface Overlay1DConsumer extends OverlayConsumer,
		AreaSelectEventListener {

}
