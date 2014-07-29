package org.dawnsci.processing.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dawnsci.common.widgets.table.ISeriesItemDescriptor;
import org.dawnsci.common.widgets.table.ISeriesItemFilter;
import org.eclipse.ui.internal.contexts.ContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.processing.IOperationService;

public final class OperationFilter implements ISeriesItemFilter {
	
	private static final Logger logger = LoggerFactory.getLogger(OperationFilter.class);
	
	private IOperationService service;

	public OperationFilter() {
		this.service     = (IOperationService)Activator.getService(IOperationService.class);
	}
	
	@Override
	public Collection<ISeriesItemDescriptor> getDescriptors(String contents, int position, ISeriesItemDescriptor previous) {
		// TODO use previous
		try {
			final Collection<String>                ops = service.getRegisteredOperations();
			final Collection<ISeriesItemDescriptor> ret = new ArrayList<ISeriesItemDescriptor>(7);
			
			for (String id : ops) {
				final OperationDescriptor des = new OperationDescriptor(id, service);
				if (!des.isVisible()) continue;
				if (contents!=null && !des.getName().toLowerCase().startsWith(contents.toLowerCase())) continue;
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
}
