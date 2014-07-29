package org.dawnsci.common.widgets.table;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * A content provider that returns the current series plus an additional
 * item on the end which is used to add more items to the series.
 * 
 * @author fcp94556
 *
 */
public class SeriesContentProvider implements IStructuredContentProvider {

	private Collection<ISeriesItemDescriptor> input;
	private boolean lockEditing;

	@Override
	public void dispose() {
		input = null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.input = (Collection<ISeriesItemDescriptor>)newInput;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		
		final Collection<ISeriesItemDescriptor> copy = input!=null && input.size()>0
				                          ? new ArrayList<ISeriesItemDescriptor>(input)
				                          : new ArrayList<ISeriesItemDescriptor>();
		if (!lockEditing) copy.add(ISeriesItemDescriptor.NEW);
		
		return copy.toArray(new ISeriesItemDescriptor[copy.size()]);
	}

	public void setLockEditing(boolean checked) {
		this.lockEditing=checked;
	}

}
