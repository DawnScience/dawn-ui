package org.dawnsci.slicing.component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dawnsci.common.widgets.celleditor.CComboCellEditor;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.slicing.api.system.AxisType;
import org.eclipse.dawnsci.slicing.api.system.DimsData;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TypeEditingSupport extends EditingSupport {

	private static final Logger logger = LoggerFactory.getLogger(TypeEditingSupport.class);
	
	private CComboCellEditor   typeEditor;
	private SliceSystemImpl    system;
	private boolean valueChangeAllowed = false;

	public TypeEditingSupport(final SliceSystemImpl system, ColumnViewer viewer) {
		
		super(viewer);
		this.system = system;
		
		typeEditor = new CComboCellEditor(((TableViewer)viewer).getTable(), getAxisItems(), SWT.READ_ONLY) {
			protected int getDoubleClickTimeout() {
				return 0;
			}	

			public void activate() {
				String[] items = getAxisItems();
				if (!Arrays.equals(this.getCombo().getItems(), items)) {
					this.getCombo().setItems(items);
				}
				super.activate();
			}
		};
		final CCombo combo = typeEditor.getCombo();
		combo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				
				if (!typeEditor.isActivated()) return;
				final String   value = combo.getText();
				
				valueChangeAllowed = true;
				
				final DimsData data  = (DimsData)((IStructuredSelection)getViewer().getSelection()).getFirstElement();
				if (value==null || "".equals(value) || AxisType.SLICE.getLabel().equals(value)) {
					setPlotAxis(data, AxisType.SLICE);
					return; // Bit of a bodge
				}
				
				setPlotAxis(data, AxisType.forLabel(value));
			}
		});
	}

	@Override
	protected CellEditor getCellEditor(Object element) {	
		
		final StringBuilder text = new StringBuilder("'Type' defines how a data dimension should be treated.\n");
		text.append("For instance as an X direction in the plotted data or as a slice.\n\n");
		
		try {
			// We append something about the current slice type.
			Method dims = system.getSliceType().getClass().getMethod("getDimensions");
			int    size = (Integer)dims.invoke(system.getSliceType());
				
			switch (size) {
			case 1:
				text.append("Please define one X-axis ");
				break;
			case 2:
				text.append("Please define two axes, one X and one Y ");
				break;
			case 3:
				text.append("Please define three axes, one X, one Y and one Z ");
				break;
			}
			
			text.append("for this dataset '"+system.getLazyDataset().getName()+"'");
	
			if (system.getLazyDataset().getRank()>size) {
				text.append("\nother 'types' should be slices or ranges");
			}
			text.append(".");
			text.append("\n\nAn error message will be shown below the table if the types are not consistent.");
			text.append("\n\nYou can choose the 'Advanced slicing' option from the toolbar above to see more options.");
			text.append("\nFor example mean, median, sum... etc."); 
		} catch (Exception ne) {
			ne.printStackTrace();
		}
		
		Hinter.showHint(typeEditor, text.toString());
		
		return typeEditor;
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		final DimsData data = (DimsData)element;
		
		AxisType axis = data.getPlotAxis();
		final Enum sliceType = system.getSliceType();
		if (sliceType==PlotType.IMAGE && system.isReversedImage()) {
			if (axis==AxisType.X) {
				axis=AxisType.Y;
			} else if (axis==AxisType.Y) {
				axis=AxisType.X;
			}
		}
			
		final String[] items = typeEditor.getCombo().getItems();
        for (int i = 0; i < items.length; i++) {
			if (items[i].equals(axis.getLabel())) return i;
		}
		
		return -1;
	}
	
	@Override
	protected void setValue(Object element, Object value) {
        // Does nothing, we only deal with PlotType from modify listener.
	}

	protected void setPlotAxis(final DimsData data, final AxisType value) {
		
		if (!valueChangeAllowed) return; // Stops focus lost doing a change.
		try {
			if (data==null) return;
			
			AxisType axis = value;
			final Enum sliceType = system.getSliceType();
			if (sliceType==PlotType.XY && axis!=AxisType.RANGE && !axis.isAdvanced()) {
				axis = axis.hasValue() ? AxisType.SLICE : AxisType.X;
			}
			if (sliceType==PlotType.IMAGE && system.isReversedImage()) {
				if (axis==AxisType.X) {
					axis=AxisType.Y;
				} else if (axis==AxisType.Y) {
					axis=AxisType.X;
				}
			}
			if (axis==data.getPlotAxis()) return;
			
			data.setPlotAxis(axis);
			if (axis.isAdvanced()) system.setAdvancedColumnsVisible(true);
			system.getDimsDataList().removeLargeStacks(system, 100);
			system.updateAxesChoices();
			system.update(data, false);
			system.fireDimensionalListeners();
		} finally {
			valueChangeAllowed = false;
		}
	}	
	

	/**
	 * TODO Get a better design for this by forcing the
	 * sliceType to contain a method.
	 * @return String[] of choices for drop down in table.
	 */
	@SuppressWarnings("unused")
	protected String[] getAxisItems() {
	
		List<String> ret = new ArrayList<String>(3);

		Enum sliceType = system.getSliceType();
		try {
			final Method dimCountMethod = sliceType.getClass().getMethod("getDimensions");
			final int dimCount = (Integer)dimCountMethod.invoke(sliceType);
		
			switch(dimCount) {
			case 1:
				ret.add(AxisType.X.getLabel());
				break;
			case 2:
				final IPlottingSystem psystem = system.getPlottingSystem();
			    if (sliceType==PlotType.XY_STACKED) {
					ret.add(AxisType.X.getLabel());
					ret.add(AxisType.Y_MANY.getLabel());
			    } else if (system.isReversedImage()) {
					ret.add(AxisType.Y.getLabel());
					ret.add(AxisType.X.getLabel());
				} else {
					ret.add(AxisType.X.getLabel());
					ret.add(AxisType.Y.getLabel());
				}
				break;
			case 3:
				ret.add(AxisType.X.getLabel());
				ret.add(AxisType.Y.getLabel());
				ret.add(AxisType.Z.getLabel());
				break;
			}
		} catch (Throwable ne) {
            logger.error("Cannot find the getDimensions method in "+sliceType.getClass());
			ret.add(AxisType.X.getLabel());
			ret.add(AxisType.Y.getLabel());
			ret.add(AxisType.Z.getLabel());
		}

		final int rank = (system.getLazyDataset()!=null) ? system.getLazyDataset().getRank() : Integer.MAX_VALUE;
		if (rank>ret.size()) {
			if (system.getRangeMode().isRange()) ret.add(AxisType.RANGE.getLabel());
			ret.add(AxisType.SLICE.getLabel());
			
			if (!system.getRangeMode().isRange() && system.isAdvanced()) { // Allow mathematics
				for (AxisType a : AxisType.getMathsTypes()) ret.add(a.getLabel());
			}
		}
		return ret.toArray(new String[ret.size()]);
	}

	public void updateChoices() {
		typeEditor.setItems(getAxisItems());
	}


}
