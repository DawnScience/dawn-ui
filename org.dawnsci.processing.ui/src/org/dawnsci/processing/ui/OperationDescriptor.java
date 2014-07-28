package org.dawnsci.processing.ui;

import org.dawnsci.common.widgets.table.ISeriesItemDescriptor;

import uk.ac.diamond.scisoft.analysis.processing.IOperation;
import uk.ac.diamond.scisoft.analysis.processing.IOperationService;

public class OperationDescriptor implements ISeriesItemDescriptor {

	private IOperation        operation;
	private String            id;
	private IOperationService service;
	public OperationDescriptor(String id, IOperationService service) {
		this.id      = id;
		this.service = service;
	}

	@Override
	public IOperation getSeriesObject() throws InstantiationException {
		if (operation==null) {
			try {
				operation = service.create(id);
			} catch (Exception e) {
				throw new InstantiationException(e.getMessage());
			}
		}
		return operation;
	}

	@Override
	public String getName() {
		return id; // TODO FIXME
	}

	@Override
	public String getDescription() {
		try {
			return getSeriesObject().getOperationDescription();
		} catch (InstantiationException e) {
			return e.getMessage();
		}
	}

	@Override
	public String getLongDescription() {
		try {
			return "TODO getLongDescription(): "+getSeriesObject().getOperationDescription();
		} catch (InstantiationException e) {
			return e.getMessage();
		}
	}

	@Override
	public Object getAdapter(Class clazz) {
		// TODO Auto-generated method stub
		return null;
	}

}
