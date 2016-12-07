package org.dawnsci.processing.ui.model;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dawnsci.plotting.roi.AxisPixelROIEditTable;
import org.eclipse.dawnsci.analysis.api.processing.model.AbstractOperationModel;
import org.eclipse.jface.viewers.AbstractTableViewer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.richbeans.widgets.cell.FieldComponentCellEditor;
import org.eclipse.richbeans.widgets.wrappers.BooleanWrapper;
import org.eclipse.richbeans.widgets.wrappers.FloatSpinnerWrapper;
import org.eclipse.richbeans.widgets.wrappers.TextWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParameterEditor extends Composite {
	public ParameterEditor(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */

	public static void main(String[] args) {


		final Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setText("Tester");
		shell.setLayout(new GridLayout());

		final String wspacePath = "/home/clb02321/Desktop/";// ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();

		AxisPixelTableViewModel viewModel = new AxisPixelTableViewModel(wspacePath);

		final Table table = new Table(shell, SWT.FULL_SELECTION | SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
		TableViewer regionViewer = buildAndLayoutTable(table, viewModel);
		regionViewer.setInput(viewModel.getValues());
		shell.pack();
		shell.setSize(600, 300);
		shell.open();

		shell.layout(true, true);

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	private static TableViewer buildAndLayoutTable(final Table table, AxisPixelTableViewModel viewModel) {

		TableViewer tableViewer = new TableViewer(table);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false, 2, 2));

		TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE, 0);
		viewerColumn.getColumn().setText("Name");
		viewerColumn.getColumn().setWidth(80);
		viewerColumn.setLabelProvider(new AxisPixelLabelProvider(viewModel, 0));
		RegionEditingSupport regionEditor = new RegionEditingSupport(viewModel, tableViewer, 0);
		viewerColumn.setEditingSupport(regionEditor);

		TableViewerColumn viewerColumn1 = new TableViewerColumn(tableViewer, SWT.NONE, 1);
		viewerColumn1.getColumn().setText("Value");
		viewerColumn1.getColumn().setWidth(80);
		viewerColumn1.setLabelProvider(new AxisPixelLabelProvider(viewModel, 1));
		RegionEditingSupport regionEditor1 = new RegionEditingSupport(viewModel,tableViewer, 1);
		viewerColumn1.setEditingSupport(regionEditor1);

		TableViewerColumn viewerColumn2 = new TableViewerColumn(tableViewer, SWT.NONE, 2);
		viewerColumn2.getColumn().setText("Value");
		viewerColumn2.getColumn().setWidth(400);
		viewerColumn2.setLabelProvider(new AxisPixelLabelProvider(viewModel, 2));
		RegionEditingSupport regionEditor2 = new RegionEditingSupport(viewModel,tableViewer, 2);
		viewerColumn2.setEditingSupport(regionEditor2);

		return tableViewer;

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}

/**
 * Table viewer label provider
 *
 */
class AxisPixelLabelProvider extends ColumnLabelProvider {

	private int column;
	private AxisPixelTableViewModel viewModel;
	private Iterator<String> nameIterator;
	private int index0;
	private int index1;
	private int index2;
	private String[] stuff;


	public AxisPixelLabelProvider(AxisPixelTableViewModel viewModel, int column) {
		// TODO Auto-generated constructor stub
		this.column = column;
	}

	@Override
	public Image getImage(Object element) {
		return null;
	}

	@Override
	public String getText(Object element) {
		final AxisPixelRowDataModel model = (AxisPixelRowDataModel)element;
		DecimalFormat pointFormat = new DecimalFormat("##0.00###");
		switch (column) {
		case 0:
//			System.out.println("thing");
//			if (this.nameIterator.hasNext()) {
//				return this.nameIterator.next().toString();
////				return "boris";
//			} else {
//				this.nameIterator = viewModel.getPluginDict().keySet().iterator();
//			}
//
//			return "Error 0";
//			System.out.println(this.stuff);
//			String out = this.stuff[index0];
//			System.out.println("index 0"+index0);
//			index0 = index0+1;
			return model.getKey(); 
		case 1:
//			if (this.nameIterator.hasNext()) {
//				//// Class<? extends Object> type = ((Map<String, Object>)
//				//// entry.getValue()).get("value").getClass();
//				Map<String, Object> paramDict = (Map<String, Object>) this.viewModel.getPluginDict()
//						.get(this.nameIterator.next());
//				Object outVal = paramDict.get("value");
//				// System.out.println(outVal.getClass());
//			String key = this.stuff[index1];
//			Map<String, Object> paramDict = (Map<String, Object>) this.viewModel.getPluginDict();
//			Map<String, Object> outValDict = (Map<String, Object>) paramDict.get(key);
//			Object outVal  = outValDict.get("value");
//			index1 = index1+1;
			Object outVal = model.getValue(); 
				if (outVal instanceof Double) {
					return pointFormat.format(outVal);

				}
				if (outVal instanceof Integer) {
					return outVal.toString();

				}

				if (outVal instanceof Boolean) {
					return outVal.toString();
				}

				if (outVal instanceof String) {
					return outVal.toString();
				}

		case 2:
//			if (this.nameIterator.hasNext()) {
//				Map<String, Object> paramDict1 = (Map<String, Object>) this.viewModel.getPluginDict()
//						.get(this.nameIterator.next());
//				Object hint = paramDict1.get("hint");
//				return hint.toString();
//			}
//			return "Error 2";
////			return "Spider";
//			String key1 = this.stuff[index2];
//			Map<String, Object> paramDict1 = (Map<String, Object>) this.viewModel.getPluginDict();
//			Map<String, Object> outValDict1 = (Map<String, Object>) paramDict1.get(key1);
//			String outVal1  = outValDict1.get("hint").toString();
//			index2 = index2+1;
//			return outVal1;
			return model.getDescription();
		default:
			return "No cheese";
		}
	}

	@Override
	public String getToolTipText(Object element) {
		return "";
	}

	@Override
	public void dispose() {
		super.dispose();
	}

}

/**
 * EditingSupport Class
 *
 */
class RegionEditingSupport extends EditingSupport {

	private int column;
	private AxisPixelTableViewModel viewModel;

	public RegionEditingSupport(AxisPixelTableViewModel viewModel, ColumnViewer viewer, int col) {
		super(viewer);
		this.column = col;
		this.viewModel = viewModel;
	}

	@Override
	protected CellEditor getCellEditor(final Object element) {
		System.out.println(this.getValue(element).getClass());
		FieldComponentCellEditor ed = null;
		if (this.getValue(element) instanceof Double) {
			try {

				ed = new FieldComponentCellEditor(((TableViewer) getViewer()).getTable(),
						FloatSpinnerWrapper.class.getName(), SWT.RIGHT);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			}

			final FloatSpinnerWrapper rb = (FloatSpinnerWrapper) ed.getFieldWidget();

			rb.setFormat(rb.getWidth(), 0);
			rb.setFormat(rb.getWidth(), 3);
			rb.setMaximum(Double.MAX_VALUE);
			rb.setMinimum(-Double.MAX_VALUE);
			rb.setButtonVisible(false);
			rb.setActive(true);

			((Spinner) rb.getControl()).addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						System.out.println(rb.getValue());
						setValue(element, rb.getValue(), false);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
			return ed;

		}
		if (this.getValue(element) instanceof Integer) {
			try {

				ed = new FieldComponentCellEditor(((TableViewer) getViewer()).getTable(),
						FloatSpinnerWrapper.class.getName(), SWT.RIGHT);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			}

			final FloatSpinnerWrapper rb = (FloatSpinnerWrapper) ed.getFieldWidget();

			rb.setFormat(rb.getWidth(), 0);
			rb.setFormat(rb.getWidth(), 0);
			rb.setMaximum(Integer.MAX_VALUE);
			rb.setMinimum(-Integer.MAX_VALUE);
			rb.setButtonVisible(false);
			rb.setActive(true);

			((Spinner) rb.getControl()).addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						setValue(element, rb.getValue(), false);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
			return ed;

		}

		if (this.getValue(element) instanceof Boolean) {
			try {

				ed = new FieldComponentCellEditor(((TableViewer) getViewer()).getTable(),
						BooleanWrapper.class.getName(), SWT.RIGHT);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			}

			final BooleanWrapper rb = (BooleanWrapper) ed.getFieldWidget();
			
			rb.setActive(true);

			return ed;
		}

		if (this.getValue(element) instanceof String) {
			try {

				ed = new FieldComponentCellEditor(((TableViewer) getViewer()).getTable(),
						TextWrapper.class.getName(), SWT.RIGHT);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			}

			final TextWrapper rb = (TextWrapper) ed.getFieldWidget();
			rb.setActive(true);
		}
		return ed;

	}

	@Override
	protected boolean canEdit(Object element) {
		if (column == 0)
			return false;
		if (column == 2)
			return false;
		else
			return true;
	}

	@Override
	protected Object getValue(Object element) {
		try {
			return getRowValue(element);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private Object getRowValue(Object element) throws Exception {

		final AxisPixelRowDataModel row = (AxisPixelRowDataModel) element;
		switch (column) {
		case 0:
			return row.getKey();
		case 1:
			return row.getValue();
		case 2:
			return row.getDescription();
		default:
			return null;
		}
	}

	@Override
	protected void setValue(Object element, Object value) {
		try {
			this.setValue(element, value, true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setValue(Object element, Object value, boolean tableRefresh) throws Exception {
		final AxisPixelRowDataModel row = (AxisPixelRowDataModel) element;

		switch (column) {
		case 0:
			row.setKey((String) value);
			break;
		case 1:
			row.setValue(value);
			break;
		case 2:
			row.setDescription((String) value);
			break;

		default:
			break;
		}
		if (tableRefresh) {
			getViewer().refresh();
		}
		// roi = createProfileRoi(profileViewModel.getValues());
		setPluginDictValues(row);

		// setTableValues(roi);
		/// here I should set the parameters in the model
		// // set new region
		// IRegion region = plottingSystem.getRegions().iterator().next();
		// region.setROI(roi);
	}

	private void setPluginDictValues(AxisPixelRowDataModel row) {
		// TODO Auto-generated method stub
		Map<String, Object> pluginDict = this.viewModel.getPluginDict();
		Map<String, Object> entryGroup = (Map<String, Object>) pluginDict.get(row.getKey());
		entryGroup.put("value", row.getValue());
		pluginDict.put(row.getKey(), entryGroup);
		this.viewModel.setPluginDict(pluginDict);
		System.out.println(this.viewModel.getPluginDict());
	}
}

/**
 * Model object for a Region Of Interest row used in an AxisPixel Table
 * 
 * @author wqk87977
 *
 */
class AxisPixelRowDataModel extends AbstractOperationModel {
	private String key;
	private Object value;
	private String description;

	public AxisPixelRowDataModel(String key, Object value, String description) {
		this.key = key;
		this.value = value;
		this.description = description;
	}

	public String getKey() {
		return key;
	}

	public Object getValue() {
		return value;
	}

	public String getDescription() {
		return description;
	}

	public void setKey(String key) {
		String oldValue = this.key;
		this.key = key;
		firePropertyChange("name", oldValue, this.key);
	}

	public void setDescription(String desc) {
		String oldDesc = this.description;
		this.description = desc;
		firePropertyChange("description", oldDesc, this.description);
	}

	public void setValue(Object value) {
		Object oldValue = this.value;
		this.value = value;
		firePropertyChange("start", oldValue, this.value);
	}
}

class AxisPixelTableViewModel {
	private List<AxisPixelRowDataModel> rows = new ArrayList<AxisPixelRowDataModel>();

	private Map<String, Object> pluginDict;

	private static String wspacePath;

	public AxisPixelTableViewModel(String wspacePath) {
		AxisPixelTableViewModel.wspacePath = wspacePath;
		Map<String, Object> pluginDict = null;

		try {
			pluginDict = getMapFromFile();
			this.pluginDict = pluginDict;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (Map.Entry<String, Object> entry : pluginDict.entrySet()) {
			Map<String, Object> info = (Map<String, Object>) entry.getValue();
			rows.add(new AxisPixelRowDataModel(entry.getKey(), info.get("value"), (String) info.get("hint")));
		}

	}

	public Map<String, Object> getPluginDict() {
		return pluginDict;
	}

	public void setPluginDict(Map<String, Object> pluginDict) {
		this.pluginDict = pluginDict;
	}

	public static String getWspacePath() {
		return wspacePath;
	}

	public static void setWspacePath(String wspacePath) {
		AxisPixelTableViewModel.wspacePath = wspacePath;
	}

	public List<AxisPixelRowDataModel> getValues() {
		return rows;
	}

	private static Map<String, Object> getMapFromFile() throws IOException {
		Map<String, Object> pluginDict = null;
		ObjectInputStream in;
		FileInputStream fileIn;

		try {
			fileIn = new FileInputStream(wspacePath + "runtime-uk.ac.diamond.dawn.productPaganinFilter.ser");// just
																												// for
																												// testing
			in = new ObjectInputStream(fileIn);
			pluginDict = (Map<String, Object>) in.readObject();
			in.close();
			fileIn.close();
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pluginDict;
	}
}
