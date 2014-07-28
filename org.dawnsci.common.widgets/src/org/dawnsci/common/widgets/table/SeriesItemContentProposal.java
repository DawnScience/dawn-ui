package org.dawnsci.common.widgets.table;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.fieldassist.IContentProposal;

/**
 * Content proposals for simple functions (not jexl)
 *
 */
public class SeriesItemContentProposal implements IContentProposal, IAdaptable {
	

	private ISeriesItemDescriptor desriptor;

	public SeriesItemContentProposal(ISeriesItemDescriptor functionDescriptor) {
		super();
		this.desriptor = functionDescriptor;
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		return desriptor.getAdapter(adapter);
	}

	@Override
	public String getContent() {
		return desriptor.getName();
	}

	@Override
	public int getCursorPosition() {
		return desriptor.getName().length();
	}

	@Override
	public String getLabel() {
		return desriptor.getName();
	}

	@Override
	public String getDescription() {
		return desriptor.getDescription();
	}

	@Override
	public String toString() {
		return getLabel();
	}

	public Object getDesriptor() {
		return desriptor;
	}
}