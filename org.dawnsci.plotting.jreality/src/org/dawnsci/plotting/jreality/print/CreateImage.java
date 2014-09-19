/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.jreality.print;

import org.eclipse.dawnsci.plotting.api.jreality.impl.Plot1DGraphTable;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Display;

import de.jreality.ui.viewerapp.AbstractViewerApp;

/**
 * CreateImage Class used to create a Thread because of the amount of time it eventually takes to create an image
 */
public class CreateImage implements Runnable {
	private AbstractViewerApp viewerApp;
	private Display device;
	private Plot1DGraphTable legendTable;
	private PrinterData printerData;
	private static Image image;
	private int resolution;

	public CreateImage(AbstractViewerApp viewerApp, Display device, Plot1DGraphTable legendTable,
			PrinterData printerData, int resolution) {
		this.viewerApp = viewerApp;
		this.device = device;
		this.legendTable = legendTable;
		this.printerData = printerData;
		this.resolution = resolution;
	}

	@Override
	public void run() {
		setImage(PlotExportUtil.createImage(this.viewerApp, this.device, this.legendTable, this.printerData,
				this.resolution));
	}

	public static Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		CreateImage.image = image;
	}
}
