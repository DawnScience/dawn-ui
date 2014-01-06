package org.dawnsci.slicing.component;

import java.lang.reflect.Field;

import org.dawnsci.common.widgets.celleditor.SpinnerCellEditor;
import org.dawnsci.slicing.api.system.DimsData;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;

public class SpanEditingSupport extends EditingSupport {
	
	private static final Logger logger = LoggerFactory.getLogger(SpanEditingSupport.class);

	private SliceSystemImpl system;

	public SpanEditingSupport(final SliceSystemImpl system, ColumnViewer viewer) {
		super(viewer);
		this.system = system;
	}

	@Override
	protected CellEditor getCellEditor(final Object element) {
		try {
			final  SpinnerCellEditor ret = new SpinnerCellEditor(((TableViewer)getViewer()).getTable(), SWT.BORDER);
			final DimsData data = (DimsData)element;
			int dimension = data.getDimension();
			ret.setMinimum(1);
			final int max = getMax(system.getData().getLazySet(), dimension);
			ret.setMaximum(max);
			ret.getSpinner().setToolTipText("The maximum value of a slice of dimension '"+(dimension+1)+"' is '"+max+"',\nbased on available memory.");
			
			ret.getSpinner().addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
			        data.setSliceSpan(ret.getSpinner().getSelection());
			        TableViewer tviewer = (TableViewer)getViewer();
			       
			        tviewer.update(data, new String[]{"Slice"});
			        if (system.synchronizeSliceData(data)) system.slice(false);
				}				
			});

		    return ret;
		} catch (Exception ne) {
			logger.error("Cannot set bounds of spinner, invalid data!", ne);
			return null;
		}
	}

	/**
	 * Gets the maximum slice of a dataset in a given dimension
	 * which should normally fit in memory.
	 * @param lazySet
	 * @param dimension
	 * @return maximum size of dimension that can be sliced.
	 */
	private static int getMax(ILazyDataset lazySet, int dimension) {
		
		final double size = getSize(lazySet.elementClass());
		final double max = (double)Runtime.getRuntime().maxMemory();
		
        // Firstly if the whole dataset it likely to fit in memory, then we
		// allow it.
		final double space = max/lazySet.getSize();
		
		// If we have room for this whole dataset, then fine
		if (space>=size) return lazySet.getShape()[dimension];
		
		// Otherwize estimate what we can fit in, conservatively
		// First get size of one slice, see it that fits, if not, still return 1.
		double sizeOneSlice = 1; // in bytes eventually
		for (int dim = 0; dim < lazySet.getRank(); dim++) {
			if (dim == dimension) continue;
			sizeOneSlice*=lazySet.getShape()[dim];
		}
		sizeOneSlice*=size;// in bytes now
		double avail = max/sizeOneSlice;
		if (avail<1) return 1;
		
		int maxAllowed = (int)Math.floor(avail);
        return maxAllowed;
	}

	/**
	 * Size in bytes of 1 of given type
	 * @param elementClass
	 * @return
	 */
	private static int getSize(Class<?> elementClass) {
		// If Number will usually have the SIZE attribute
		try {
			Field size = elementClass.getField("SIZE");
			if (size!=null) return size.getInt(null);// static
		} catch (Throwable ne) {
			// Ignored
		}
		return 64;
	}

	@Override
	protected boolean canEdit(Object element) {
		final DimsData data = (DimsData)element;
		return system.isAdvanced() && data.getPlotAxis().isAdvanced();
	}

	@Override
	protected Object getValue(Object element) {
		final DimsData data = (DimsData)element;
		return data.getSliceSpan();
	}

	@Override
	protected void setValue(Object element, Object value) {
		final DimsData data = (DimsData)element;
        data.setSliceSpan((Integer)value);
        system.update(data, false);
	}

}
