/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.processing.ui.processing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dawnsci.common.widgets.table.ISeriesItemDescriptor;
import org.dawnsci.common.widgets.table.ISeriesItemFilter;
import org.dawnsci.processing.ui.Activator;
import org.dawnsci.processing.ui.model.OperationDescriptor;
import org.dawnsci.processing.ui.slice.IOperationErrorInformer;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class OperationFilter implements ISeriesItemFilter {
	
	private static final Logger logger = LoggerFactory.getLogger(OperationFilter.class);
	
	private IOperationService service;
	private IOperationErrorInformer informer;
	
	public OperationFilter() {
		this.service     = (IOperationService)Activator.getService(IOperationService.class);
	}
	
	@Override
	public Collection<ISeriesItemDescriptor> getDescriptors(String contents, int position, ISeriesItemDescriptor previous) {
		// TODO use previous
		try {
			
			final Collection<String>                ops = service.getRegisteredOperations();
			final Collection<ISeriesItemDescriptor> ret = new ArrayList<ISeriesItemDescriptor>(7);
			
			if (previous == null && informer != null && informer.getTestData() != null) {
				int rank = informer.getTestData().getRank();
				
				for (String id : ops) {
					final OperationDescriptor des = new OperationDescriptor(id, service);
					if (des.getSeriesObject().getOutputRank().getRank() == rank) {
						previous= des;
						break;
					}
				}

			}
			
			
			
			for (String id : ops) {
				final OperationDescriptor des = new OperationDescriptor(id, service);
				if (!des.isVisible()) continue;
				if (contents!=null && !des.getName().toLowerCase().contains(contents.toLowerCase())) continue;
				if (!des.isCompatibleWith(previous)) continue;
				ret.add(des);
			}
			return ret;
			
		} catch (Exception e) {
			logger.error("Cannot get operations!", e);
			return null;
		}
	}

	public List<OperationDescriptor> createDescriptors(List<String> ids) {
		List<OperationDescriptor> descriptions = new ArrayList<OperationDescriptor>();
		for (String id : ids) {
			final OperationDescriptor des = new OperationDescriptor(id, service);
			if (!des.isVisible()) continue;
			descriptions.add(des);
		}
		return descriptions;
	}
	
	public void setOperationErrorInformer(IOperationErrorInformer informer) {
		this.informer = informer;
	}
}
