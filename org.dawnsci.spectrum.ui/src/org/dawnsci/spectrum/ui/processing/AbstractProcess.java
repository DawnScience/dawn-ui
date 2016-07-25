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
import java.util.List;

import org.dawnsci.spectrum.ui.file.IContain1DData;
import org.dawnsci.spectrum.ui.utils.Contain1DDataImpl;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;

public abstract class AbstractProcess {
	
	public List<IContain1DData> process(List<IContain1DData> list) {

		List<IContain1DData> output = new ArrayList<IContain1DData>();

		for (IContain1DData data : list) {

			List<IDataset> out = new ArrayList<IDataset>();

			Dataset x = DatasetUtils.convertToDataset(data.getxDataset());

			for (IDataset y : data.getyDatasets()) {
				out.add(process(x, DatasetUtils.convertToDataset(y)));
			}

			output.add(new Contain1DDataImpl(x, out, data.getName() + getAppendingName(), data.getLongName() + getAppendingName()));
		}

		return output;
	}
	
	protected abstract Dataset process(Dataset x, Dataset y);
	
	protected abstract String getAppendingName();
	
//	public List<IContain1DData> getDatasetList() {
//		return list;
//	}

}
