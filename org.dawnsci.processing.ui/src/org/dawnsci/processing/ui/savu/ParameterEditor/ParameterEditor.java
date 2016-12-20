package org.dawnsci.processing.ui.savu.ParameterEditor;

//import org.dawnsci.processing.ui.savu.SelectionLister;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

public class ParameterEditor extends Composite {

	private TableViewer tableViewer;
	public String pluginName="PaganinFilter";
	public String pluginPath;
	public Integer pluginRank;
	private Composite parent;

	public String getPluginName() {
		return pluginName;
	}


	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}


	public String getPluginPath() {
		return pluginPath;
	}


	public void setPluginPath(String pluginPath) {
		this.pluginPath = pluginPath;
	}


	public Integer getPluginRank() {
		return pluginRank;
	}


	public void setPluginRank(Integer pluginRank) {
		this.pluginRank = pluginRank;
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

		ParameterEditor editor = new ParameterEditor(shell, SWT.NONE);
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

	public ParameterEditor(Composite parent, int style) {
		super(parent,style);
		this.parent = parent;
	}


	public void initialiseTable(ParameterEditorTableViewModel viewModel) {
		
		System.out.println(getPluginName());
		System.out.println(viewModel.getPluginDict());
		final Table table = new Table(parent, SWT.FULL_SELECTION | SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
		TableViewer regionViewer = buildAndLayoutTable(table, viewModel);
		regionViewer.setInput(viewModel.getValues());
	}
	public void setpluginName(String pluginName) {
		this.pluginName = pluginName;
	}

	public String getpluginName() {
		return this.pluginName;
	}
	
	private TableViewer buildAndLayoutTable(final Table table, ParameterEditorTableViewModel viewModel) {

		tableViewer = new TableViewer(table);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false, 2, 2));

		TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE, 0);
		viewerColumn.getColumn().setText("Name");
		viewerColumn.getColumn().setWidth(80);
		viewerColumn.setLabelProvider(new ParameterEditorLabelProvider(viewModel, 0));
		ParameterEditingSupport regionEditor = new ParameterEditingSupport(viewModel, tableViewer, 0);
		viewerColumn.setEditingSupport(regionEditor);

		TableViewerColumn viewerColumn1 = new TableViewerColumn(tableViewer, SWT.NONE, 1);
		viewerColumn1.getColumn().setText("Value");
		viewerColumn1.getColumn().setWidth(80);
		viewerColumn1.setLabelProvider(new ParameterEditorLabelProvider(viewModel, 1));
		ParameterEditingSupport regionEditor1 = new ParameterEditingSupport(viewModel,tableViewer, 1);
		viewerColumn1.setEditingSupport(regionEditor1);

		TableViewerColumn viewerColumn2 = new TableViewerColumn(tableViewer, SWT.NONE, 2);
		viewerColumn2.getColumn().setText("Description");
		viewerColumn2.getColumn().setWidth(400);
		viewerColumn2.setLabelProvider(new ParameterEditorLabelProvider(viewModel, 2));
		ParameterEditingSupport regionEditor2 = new ParameterEditingSupport(viewModel,tableViewer, 2);
		viewerColumn2.setEditingSupport(regionEditor2);

		return tableViewer;

	}

	public void update(ParameterEditorTableViewModel model) {
		tableViewer.setInput(null);
		tableViewer.setInput(model.getValues());
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}


//	public void addSelectionListener(SelectionLister selectionLister) {
//		// TODO Auto-generated method stub
//		
//	}

}
