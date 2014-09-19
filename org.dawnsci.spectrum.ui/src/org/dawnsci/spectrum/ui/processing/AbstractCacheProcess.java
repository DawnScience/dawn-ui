/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.spectrum.ui.processing;

import java.util.List;

import org.dawnsci.spectrum.ui.file.IContain1DData;
import org.dawnsci.spectrum.ui.utils.SpectrumUtils;

public abstract class AbstractCacheProcess extends AbstractProcess {

	IContain1DData oCachedData;
	IContain1DData cachedData;
	
	public AbstractCacheProcess(IContain1DData cache) {
		this.oCachedData = cache;
	}
	
	@Override
	public List<IContain1DData> process(List<IContain1DData> list) {

		list.add(oCachedData);
		List<IContain1DData> listCom = SpectrumUtils.getCompatibleDatasets(list);
		this.cachedData = listCom.remove(listCom.size()-1);

		return super.process(listCom);
	}

}
