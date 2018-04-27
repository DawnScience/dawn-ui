/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.spectrum.ui.processing;

import java.io.File;
import java.util.List;

import org.dawb.common.services.ServiceManager;
import org.dawnsci.spectrum.ui.file.IContain1DData;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistentFile;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;

public class SaveProcess extends AbstractSaveProcess {

	@Override
	public List<IContain1DData> process(List<IContain1DData> list) {
		File                file=null;
		IPersistentFile     pf=null;
		
		try {
			IPersistenceService service = (IPersistenceService) ServiceManager.getService(IPersistenceService.class);
			file = new File(this.path);
			pf = service.createPersistentFile(file.getAbsolutePath());
			IContain1DData data = list.get(0);
			IDataset[] x = new IDataset[] { data.getxDataset() };
			for (IDataset ds : data.getyDatasets()) {
				pf.setData(ds, x);
			}
		} catch (Throwable ne) {
			ne.printStackTrace();
		} finally {
			if (pf!=null) pf.close();
		}
		return null;
	}

	@Override
	protected Dataset process(Dataset x, Dataset y) {
		return null;
	}

	@Override
	protected String getAppendingName() {
		//Should never be called
		return "_save";
	}

	@Override
	public String getDefaultName() {
		return "traceprocessed.hdf5";
	}

}
