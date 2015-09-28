/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.spectrum.ui.processing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dawnsci.spectrum.ui.dialogs.CombineDialog;
import org.dawnsci.spectrum.ui.file.IContain1DData;
import org.dawnsci.spectrum.ui.utils.Contain1DDataImpl;
import org.dawnsci.spectrum.ui.utils.SpectrumUtils;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

public class CombineProcess extends AbstractProcess {
	
	@Override
	public List<IContain1DData> process(List<IContain1DData> list) {

		list = SpectrumUtils.getCompatibleDatasets(list);
		
		if (list == null) return null;
		
		IDataset x0 = list.get(0).getxDataset();
		
		StringBuilder sb = new StringBuilder();
		sb.append("Comination of: ");
		sb.append("\n");
		IDataset[] all = new IDataset[list.size()];
		
		int count = 0;
		for (IContain1DData file : list) {
			
			sb.append(file.getName() +":");

			for (IDataset ds : file.getyDatasets()) {
				
				sb.append(ds.getName() +":");
				IDataset ds1 = ds.getSliceView().squeeze();
				ds1.setShape(new int[]{1,ds1.getShape()[0]});
				all[count++] = ds;
			}
			sb.deleteCharAt(sb.length()-1);
			sb.deleteCharAt(sb.length()-1);
			sb.append("\n");
		}
		List<IDataset> sets = new ArrayList<IDataset>();
		
		Dataset conc = DatasetUtils.concatenate(all, 0);
		
		conc.setName("Combination");
		sets.add(conc);
		String shortName = "Combine: " + list.get(0).getName() + " to " + list.get(list.size()-1).getName();
		return Arrays.asList(new IContain1DData[] {
				new Contain1DDataImpl(x0, sets, shortName, sb.toString() + "[" + sets.hashCode() + "]") });

	}
	
	@Override
	protected Dataset process(Dataset x, Dataset y) {
		return null;
	}

	@Override
	protected String getAppendingName() {
		//Should not be called
		return "_combined";
	}

}
