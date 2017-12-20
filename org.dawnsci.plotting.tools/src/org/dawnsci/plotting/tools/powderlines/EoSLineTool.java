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
import org.dawnsci.plotting.tools.powderlines.PowderLinesModel.PowderLineCoord;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

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

			// New, multiple file loading
			PowderLinesModel nyModel = new EoSLinesModel();
			nyModel.setWavelength(theTool.model.getWavelength());
			nyModel.setCoords(PowderLineCoord.D_SPACING);
			nyModel.setLines((DoubleDataset) d);
			theTool.addMaterialModel(nyModel);
			
			theTool.refresh(true);
//			theTool.setLines((DoubleDataset)d);
			
		}
		
	}
	
	private void setMySlice(Dataset data, IDataHolder dh, int n, int index, String name) {
		data.setSlice(DatasetUtils.convertToDataset(dh.getDataset(name)), new int[]{0, index}, new int[]{n, index+1}, new int[]{1, 1});
	}

	static class EosDetailsComposite extends Composite {
		static final String modulusSymbol = "B"; // Could also be K
		static final String modulusString = modulusSymbol+"₀";
		static final String modulusDerivativeString = modulusSymbol + "₀′";
		String pressureUnits = "Pa";
		double pressureMultiplier = 1;
		Text k0;
		Text k0prime;
		Text v;
		Text v0;
		Text ll0;
		
		public EosDetailsComposite(Composite parent, int style) {
			super(parent, style);
			
			redraw();
		}
		
		public void setPressureMultiplierMagnitude(int magnitude) {
			pressureMultiplier = Math.pow(10., magnitude);
			// "terapascals" is probably sufficient
			String[] prefices = new String[] {"", "da", "h", "k", "", "", "M", "", "", "G", "", "", "T"};
			
			if (magnitude < 0 || magnitude > 12 || prefices[magnitude].equals("")) {
				// not a valid multiplier
				System.err.println("10^" + magnitude + " is not a valid SI prefix scale.");
			} else {
				pressureUnits = prefices[magnitude]+"Pa";
			}
		}
	
		@Override
		public void redraw() {
			GridLayout layout = new GridLayout(11, false);
			this.setLayout(layout);
			
			// Modulus
			Label modulusLabel = new Label(this, SWT.RIGHT);
			modulusLabel.setText(modulusString);
			modulusLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
			k0 = new Text(this, SWT.SINGLE | SWT.LEFT);
			k0.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
			k0.setEditable(false);
			Label modulusUnits = new Label(this, SWT.LEFT);
			modulusUnits.setText(pressureUnits);
			modulusUnits.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			
			// Empty text for a spacer
			Text spacer = new Text(this, SWT.SINGLE);
			spacer.setEditable(false);
			
			// Modulus derivate
			Label derivLabel = new Label(this, SWT.RIGHT);
			derivLabel.setText(modulusDerivativeString);
			derivLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
			
			k0prime = new Text(this, SWT.SINGLE | SWT.LEFT);
			k0prime.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
			k0prime.setEditable(false);

			Label derivUnits = new Label(this, SWT.LEFT);
			derivUnits.setText("");
			derivUnits.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			
			// Empty text for a spacer
			spacer = new Text(this, SWT.SINGLE);
			spacer.setEditable(false);
			
			Label ll0Label = new Label(this, SWT.RIGHT);
			ll0Label.setText("l/l₀");
			ll0Label.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
			
			ll0 = new Text(this, SWT.SINGLE | SWT.LEFT);
			ll0.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
			ll0.setEditable(false);
			
			Label ll0Units = new Label(this, SWT.LEFT);
			ll0Units.setText("");
			ll0Units.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

			super.redraw();

		}
	}
}
