/*
 * Copyright (c) 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.tools.powderlines;

import org.dawnsci.plotting.tools.ServiceLoader;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

public class EoSLineTool extends PowderLineTool {

	
	protected class LoadAction extends PowderLineTool.LoadAction {
		
		// This version loads JCPDS files exclusively
		@Override
		public void run() {
			FileDialog chooser = new FileDialog(theShell, SWT.OPEN);
			String chosenFile = chooser.open();
			
			ILoaderService loaderService = ServiceLoader.getLoaderService();
			IDataHolder dataHolder = null;
			// Get the data from the file
			try {
				dataHolder = loaderService.getData(chosenFile, null);
			
			} catch (Exception e) {
				if (chosenFile != null)
					System.err.println("EoSLineTool: Could not read line data from " + chosenFile + ".");
				return;
			}
			// Get the d, i, h, k, l Datasets from the file
			Dataset d = DatasetUtils.convertToDataset(dataHolder.getDataset("d"));
			
			if (d == null) {
				logger.info("EoSLineTool: No valid d-spacing data in file " + chosenFile + ".");
				return;
			}
			if (d.getDType() != Dataset.FLOAT) {
				logger.info("EoSLineTool: No valid double data found in file " + chosenFile + ".");
				return;
			}
			// If d does exist, then perhaps we shall assume that the rest do, too.
			Dataset i = DatasetUtils.convertToDataset(dataHolder.getDataset("i"));
			int n = i.getSize();
			Dataset hkl = DatasetFactory.zeros(n, 3);
			String[] axes = new String[] {"h", "k", "l"};
			for (int j=0; j<3; j++)
				setMySlice(hkl, dataHolder, n, j, axes[j]);

			theTool.setLines((DoubleDataset)d);
			
		}
		
	}
	
	private void setMySlice(Dataset data, IDataHolder dh, int n, int index, String name) {
		data.setSlice(DatasetUtils.convertToDataset(dh.getDataset(name)), new int[]{0, index}, new int[]{n, index+1}, new int[]{1, 1});
	}
}
