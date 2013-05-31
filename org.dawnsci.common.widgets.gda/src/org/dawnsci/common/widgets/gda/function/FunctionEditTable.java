package org.dawnsci.common.widgets.gda.function;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.dawb.common.ui.plot.function.FunctionType;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.fitting.functions.AFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.FunctionFactory;
import uk.ac.gda.richbeans.components.cell.FieldComponentCellEditor;
import uk.ac.gda.richbeans.components.wrappers.BooleanWrapper;
import uk.ac.gda.richbeans.components.wrappers.FloatSpinnerWrapper;
import uk.ac.gda.richbeans.components.wrappers.SpinnerWrapper;

/**
 * A table widget for editing any Function
 * 
 *
 */
public class FunctionEditTable {

	private static final Logger logger = LoggerFactory.getLogger(FunctionEditTable.class);
	
	private TableViewer functionTable;
	private AFunction     function;
	private AFunction     originalFunction;
	private FunctionType  functionType;
	private List<FunctionRow> rows;

	public Control createPartControl(Composite parent) {
		
		this.functionTable = new TableViewer(parent, SWT.FULL_SELECTION | SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
		GridData tableData = new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1);
		tableData.heightHint=200;
		functionTable.getTable().setLayoutData(tableData);
		
		createColumns(functionTable);
		
		final Label clickToEdit = new Label(parent, SWT.WRAP);
		clickToEdit.setText("* Click to change");
		clickToEdit.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 4, 1));
		
		return functionTable.getTable();
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener){
		functionTable.addSelectionChangedListener(listener);
	}
	
	public void removeSelectionChangedListener(ISelectionChangedListener listener){
		functionTable.removeSelectionChangedListener(listener);
	}

	/**
	 * Can be called also to change to editing a different function.
	 * @param function
	 * @param functionType - may be null
	 */
	public void setFunction(final AFunction function, final FunctionType functionType) {
		
		this.setFunctionType(functionType);
		this.originalFunction = function!=null ? function : null;
		this.function         = function!=null ? function : null;
				
		this.rows = createFunctionRows(function);
		
		functionTable.setContentProvider(new IStructuredContentProvider() {
			
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { }
			
			@Override
			public void dispose() { }
			
			@Override
			public Object[] getElements(Object inputElement) {
				return rows.toArray(new FunctionRow[rows.size()]);
			}			
		});
		
		functionTable.setInput(rows.get(0));
	}

	private void createColumns(TableViewer viewer) {
		
		ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		
		viewer.setColumnProperties(new String[] { "Name", "value", "min", "max", "Fixed"});
		
		TableViewerColumn var = new TableViewerColumn(viewer, SWT.LEFT, 0);
		var.getColumn().setText("Name");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new FunctionLabelProvider(0));
		
		var = new TableViewerColumn(viewer, SWT.LEFT, 1);
		var.getColumn().setText("value");
		var.getColumn().setWidth(120);
		FunctionEditingSupport functionEditor = new FunctionEditingSupport(viewer, 1);
		var.setEditingSupport(functionEditor);
		var.setLabelProvider(new DelegatingStyledCellLabelProvider(new FunctionLabelProvider(1, functionEditor)));
		
		var = new TableViewerColumn(viewer, SWT.LEFT, 2);
		var.getColumn().setText("min");
		var.getColumn().setWidth(120);
		functionEditor = new FunctionEditingSupport(viewer, 2);
		var.setEditingSupport(functionEditor);
		var.setLabelProvider(new DelegatingStyledCellLabelProvider(new FunctionLabelProvider(2, functionEditor)));
		
		var = new TableViewerColumn(viewer, SWT.LEFT, 3);
		var.getColumn().setText("max");
		var.getColumn().setWidth(120);
		functionEditor = new FunctionEditingSupport(viewer, 3);
		var.setEditingSupport(functionEditor);
		var.setLabelProvider(new DelegatingStyledCellLabelProvider(new FunctionLabelProvider(3, functionEditor)));
		
		// column 4 = checkbox
		var = new TableViewerColumn(viewer, SWT.LEFT, 4);
		var.getColumn().setText("Fixed");
		var.getColumn().setWidth(120);
		functionEditor = new FunctionEditingSupport(viewer, 4);
		var.setEditingSupport(functionEditor);
		var.setLabelProvider(new DelegatingStyledCellLabelProvider(new FunctionLabelProvider(4, functionEditor)));

		//functionTable.getTable().setHeaderVisible(false);

	}
	
	public void cancelEditing() {
		this.functionTable.cancelEditing();
	}
	
	public class FunctionEditingSupport extends EditingSupport {

		private int column;
		
		public FunctionEditingSupport(ColumnViewer viewer,  int col) {
			super(viewer);
			this.column = col;
		}
		@Override
		protected CellEditor getCellEditor(final Object element) {
			
			FieldComponentCellEditor ed = null;
			
			if(column<4){
				try {
					ed = new FieldComponentCellEditor(((TableViewer)getViewer()).getTable(), 
						                     FloatSpinnerWrapper.class.getName(), SWT.RIGHT);
				} catch (ClassNotFoundException e) {
					logger.error("Cannot get FieldComponentCellEditor for "+SpinnerWrapper.class.getName(), e);
					return null;
				}
			
				final FloatSpinnerWrapper rb = (FloatSpinnerWrapper)ed.getFieldWidget();
				if (rb.getPrecision() < 5)
					rb.setFormat(rb.getWidth(), 5);

				rb.setMaximum(Double.MAX_VALUE);
				rb.setMinimum(-Double.MAX_VALUE);

				rb.setButtonVisible(false);
				rb.setActive(true);
//				rb.setFormat(10, 5);

				((Spinner)rb.getControl()).addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						setValue(element, rb.getValue(), false);
					}
				});
			}else{
				try{
					ed = new FieldComponentCellEditor(((TableViewer)getViewer()).getTable(), BooleanWrapper.class.getName(), SWT.LEFT);
				}catch (ClassNotFoundException e) {
					logger.error("Cannot get FieldComponentCellEditor for "+BooleanWrapper.class.getName(), e);
					return null;
				}
				final BooleanWrapper bw = (BooleanWrapper)ed.getFieldWidget();
				bw.setVisible(true);
				bw.setActive(true);
				((Button)bw.getControl()).addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						setValue(element, bw.getValue(), false);
					}
				});
			}
			
			return ed;
		}

		@Override
		protected boolean canEdit(Object element) {
			final FunctionRow row = (FunctionRow)element;
			if(column<4){
				double val = 0;
				
				if (!row.isEnabled()) return false;
				val = row.getParameter(column-1);
				return !Double.isNaN(val);
			}else{
				return true;
			}
		}

		@Override
		protected Object getValue(Object element) {
			final FunctionRow row = (FunctionRow)element;
			if(column<4){
				return (double)row.getParameter(column-1);
			}else{
				return row.isFixed();
			}
			
		}

		@Override
		protected void setValue(Object element, Object value) {
			this.setValue(element, value, true);
		}
		
		protected void setValue(Object element, Object value, boolean tableRefresh) {
			final FunctionRow row = (FunctionRow)element;
			if(column<4){
				final Number    val = (Number)value;
				
				row.setParameter(val.doubleValue(), column-1);
				
				if (tableRefresh) {
					getViewer().refresh();
				}
				
				function = createFunction(rows, row);
			}else{ // for the last column (checkbox)
				final boolean    fixed = (Boolean)value;
				
				row.setIsFixed(fixed);
				
				if (tableRefresh) {
					getViewer().refresh();
				}
				
				function = createFunction(rows, row);
			}
			
		}

	}

	public class FunctionLabelProvider extends ColumnLabelProvider implements IStyledLabelProvider {
		
		private int column;
		private NumberFormat format;
		private FunctionEditingSupport editor;
		
		public FunctionLabelProvider(int col) {
			this(col, null);
		}
		public FunctionLabelProvider(int col, final FunctionEditingSupport editor) {
			this.column = col;
			this.format = NumberFormat.getNumberInstance();
			this.editor = editor;
		}
		
		public String getText(Object element) {
			
			final FunctionRow row = (FunctionRow)element;
			switch (column) {
				case 0:
					return row.getParamName();
				case 1:
					if (Double.isNaN(row.getParameter(0))) return "-";
					return format.format(row.getParameter(0));
				
				case 2:
					if (Double.isNaN(row.getParameter(1))) return "-";
					return format.format(row.getParameter(1));
				
				case 3:
					if (Double.isNaN(row.getParameter(2))) return "-";
					return format.format(row.getParameter(2));
				case 4:
					if(row.isFixed())
						return "true";
					else
						return "false";
			}
			return "";
		}

		@Override
		public String getToolTipText(Object element){
			final FunctionRow row = (FunctionRow)element;
			return row.getDescription();
		}
		
		@Override
		public StyledString getStyledText(Object element) {
			final StyledString ret = new StyledString(getText(element));
			if (editor!=null && editor.canEdit(element)) {
				ret.append(new StyledString("*", StyledString.QUALIFIER_STYLER));
			}
			return ret;
		}
	}

	private List<FunctionRow> createFunctionRows(AFunction function) {
		
		final List<FunctionRow> ret = new ArrayList<FunctionEditTable.FunctionRow>();
		int numberParam = function.getNoOfParameters();
		for(int i=0; i<numberParam; i++){
			String paramName = function.getParameterName(i);
			String functionDescription = function.getDescription();
			boolean isfixed = function.getParameter(i).isFixed();
			double value = function.getParameterValue(i);
			double minValue = function.getParameter(i).getLowerLimit();
			double maxValue = function.getParameter(i).getUpperLimit();
			ret.add(new FunctionRow(paramName, functionDescription, isfixed, value, minValue, maxValue));
		}
		
		return ret;
	}

	public AFunction createFunction(List<FunctionRow> rows, FunctionRow changed) {
			
		try {
			AFunction ret = FunctionFactory.getFunction(function.getName()); 
			
			final int count  = function.getNoOfParameters();
			for (int i = 0; i < count; i++) {
				ret.getParameter(i).setValue(rows.get(i).getParameter(0));
				ret.getParameter(i).setLowerLimit(rows.get(i).getParameter(1));
				ret.getParameter(i).setUpperLimit(rows.get(i).getParameter(2));
				ret.getParameter(i).setFixed(rows.get(i).isFixed());
			}	
			return ret;
			
		} catch (Exception ne) {
			ne.printStackTrace();
			//logger.error("Unable to create function "+function.getName(), ne);
		}

		return null;
	}

	public void dispose() {
		function=null;
		originalFunction=null;
		setFunctionType(null);
		rows.clear();
		rows=null;
	}
	
	private final static class FunctionRow {
		private String name;
		private String paramName;
		private String description;
		private double[] params;
		private boolean enabled=true;
		private boolean fixed = false;
		
		public FunctionRow(String name, String description, boolean fixed, double... parameters) {
			this.paramName     = name;
			this.description     = description;
			this.fixed  = fixed;
			this.params = parameters;
		}
		@SuppressWarnings("unused")
		public String getName() {
			return name;
		}
		public String getDescription() {
			return description;
		}
		public String getParamName() {
			return paramName;
		}
		@SuppressWarnings("unused")
		public double[] getParameters() {
			return params;
		}

		public boolean isFixed() {
			return fixed;
		}
		
		public void setIsFixed(boolean value) {
			fixed = value;
		}

		public double getParameter(int index) {
			return params[index];
		}
		public void setParameter(double value, int index) {
			params[index] = value;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((description == null) ? 0 : description.hashCode());
			result = prime * result + ((paramName == null) ? 0 : paramName.hashCode());
			for(int i=0;i<params.length;i++){
				long temp = Double.doubleToLongBits(params[i]);
				result = prime * result + (int) (temp ^ (temp >>> 32));
			}
			return result;
		}
		@Override
		public boolean equals(Object obj) {
//			if (this == obj)
//				return true;
//			if (obj == null)
//				return false;
//			if (getClass() != obj.getClass())
//				return false;
//			FunctionRow other = (FunctionRow) obj;
//			if (name == null) {
//				if (other.name != null)
//					return false;
//			} else if (!name.equals(other.name))
//				return false;
//			if (unit == null) {
//				if (other.unit != null)
//					return false;
//			} else if (!unit.equals(other.unit))
//				return false;
//			if (Double.doubleToLongBits(xLikeVal) != Double
//					.doubleToLongBits(other.xLikeVal))
//				return false;
//			if (Double.doubleToLongBits(yLikeVal) != Double
//					.doubleToLongBits(other.yLikeVal))
//				return false;
			return true;
		}

		public boolean isEnabled() {
			return enabled;
		}

		@SuppressWarnings("unused")
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
    }

	public AFunction getOriginalFunction() {
		return originalFunction;
	}
	public AFunction getFunction() {
		return function;
	}

	public FunctionType getFunctionType() {
		return functionType;
	}

	public void setFunctionType(FunctionType functionType) {
		this.functionType = functionType;
	}
}
