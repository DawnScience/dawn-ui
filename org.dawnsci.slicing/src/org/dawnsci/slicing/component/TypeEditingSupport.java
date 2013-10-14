package org.dawnsci.slicing.component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dawnsci.common.widgets.celleditor.CComboCellEditor;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.slicing.api.system.DimsData;
import org.dawnsci.slicing.api.system.AxisType;
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
			
			/**
			 * Do nothing, do not reapply any value.
			 */
			public void focusLost() {
				//super.focusLost();
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
				if (AxisType.RANGE.getLabel().equals(value)) {
					setPlotAxis(data, AxisType.RANGE);
					return; // Bit of a bodge
				}
				
				for (AxisType pa : AxisType.values()) {
					if (value.equals(pa.getLabel())) {
						setPlotAxis(data, pa);
						return; // Bit of a bodge
					}
				}
			}
		});
	}

	@Override
	protected CellEditor getCellEditor(Object element) {			
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
			if (sliceType==PlotType.XY && axis!=AxisType.RANGE) {
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
			if (axis==AxisType.Y_MANY) axis = AxisType.Y; // Y_MANY does not currrently require different slice code.
			
			data.setPlotAxis(axis);
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
			if (system.isRangesAllowed()) ret.add(AxisType.RANGE.getLabel());
			ret.add(AxisType.SLICE.getLabel());
		}
		return ret.toArray(new String[ret.size()]);
	}

	public void updateChoices() {
		typeEditor.setItems(getAxisItems());
	}


}
