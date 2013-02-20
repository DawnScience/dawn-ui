/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dawnsci.plotting.jreality.print;

import org.dawnsci.plotting.jreality.impl.Plot1DGraphTable;
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
