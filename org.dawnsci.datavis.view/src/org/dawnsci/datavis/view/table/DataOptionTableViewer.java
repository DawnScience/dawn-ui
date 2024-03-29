/*-
 * Copyright (c) 2019 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.datavis.view.table;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import org.dawnsci.datavis.api.IRecentPlaces;
import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.DataOptionsDataset;
import org.dawnsci.datavis.model.DataOptionsSlice;
import org.dawnsci.datavis.model.DataOptionsUtils;
import org.dawnsci.datavis.model.IFileController;
import org.dawnsci.datavis.model.ILoadedFileInitialiser;
import org.dawnsci.datavis.view.DataOptionsUIUtils;
import org.dawnsci.datavis.view.ExpressionDialog;
import org.dawnsci.datavis.view.parts.CopyToClipboardAction;
import org.dawnsci.january.ui.utils.SelectionUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.expressions.IExpressionService;
import org.eclipse.dawnsci.plotting.api.ProgressMonitorWrapper;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.january.IMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import uk.ac.diamond.osgi.services.ServiceProvider;

public class DataOptionTableViewer {

	private TableViewer tableViewer;
	private Image ticked;
	private Image unticked;
	private Composite tableComposite;
	private boolean useShortName;
	
	private boolean reverse = false;

	/**
	 * Set to true to display short names
	 * @param useShortName
	 */
	public void setUseShortName(boolean useShortName) {
		this.useShortName = useShortName;
	}

	IFileController controller;
	ILoadedFileInitialiser initialiser;
	
	public DataOptionTableViewer(IFileController controller, ILoadedFileInitialiser initialiser){
		this.controller = controller;
		this.initialiser = initialiser;
	}

	public void dispose(){
		ticked.dispose();
		unticked.dispose();
	}

	public Control getControl() {
		return tableComposite;
	}

	public Table getTable() {
		return tableViewer.getTable();
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener){
		tableViewer.addSelectionChangedListener(listener);
	}

	public IStructuredSelection getStructuredSelection() {
		return tableViewer.getStructuredSelection();
	}

	public void setInput(List<DataOptions> input) {
		sort(input);
		tableViewer.setInput(input);
	}

	public void refresh() {
		tableViewer.refresh();
	}

	public void setSelection(ISelection selection, boolean reveal) {
		tableViewer.setSelection(selection, reveal);
	}

	/**
	 * @return if viewer has no input
	 */
	public boolean isEmpty() {
		return tableViewer.getInput() == null;
	}

	/**
	 * Reselect current selection
	 */
	public void reselect() {
		setSelection(getStructuredSelection(), true);
	}
	
	
	public void createControl(Composite parent) {
		Text text = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH);
		text.setMessage("Filter Datasets");
		text.setToolTipText("Only show datasets whose name contains the entered text. Does not remove plotted datasets.");
		text.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		tableComposite = new Composite(parent, SWT.None);
		tableViewer = new TableViewer(tableComposite, SWT.FULL_SELECTION | SWT.BORDER);
		tableViewer.getTable().setHeaderVisible(true);
		ColumnViewerToolTipSupport.enableFor(tableViewer);
		DataOptionViewerFilter dovf = new DataOptionViewerFilter();
		tableViewer.addFilter(dovf);
		
		text.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				dovf.setFilterString(text.getText());
				tableViewer.refresh();
			}
		});

		ticked = AbstractUIPlugin.imageDescriptorFromPlugin("org.dawnsci.datavis.view", "icons/ticked.png").createImage();
		unticked = AbstractUIPlugin.imageDescriptorFromPlugin("org.dawnsci.datavis.view", "icons/unticked.gif").createImage();

		tableViewer.setContentProvider(new ArrayContentProvider());

		MenuManager menuMgr = new MenuManager();
		Menu menu = menuMgr.createContextMenu(tableViewer.getControl());
		menuMgr.addMenuListener(new IMenuListener() {

			@Override
			public void menuAboutToShow(IMenuManager manager) {
				
				DataOptions dataOptions = SelectionUtils.getFirstFromSelection(tableViewer.getSelection(), DataOptions.class);
				if (dataOptions != null) {
					
					if (dataOptions.getName() != null) {
						manager.add(new CopyToClipboardAction(dataOptions.getName()));
					}
					
					Action a = new Action("Take view") {
						@Override
						public void run() {
							dataOptions.getParent().addVirtualOption(DataOptionsUtils.buildView(dataOptions));
							setInput(dataOptions.getParent().getDataOptions());
						}
					};

					manager.add(a);

					DataOptionsAction avr = new DataOptionsAction("Average", "Calculating average...", dataOptions, DataOptionsUtils::average);

					manager.add(avr);

					DataOptionsAction sum = new DataOptionsAction("Sum", "Calculating sum...", dataOptions, DataOptionsUtils::sum);

					manager.add(sum);

					Action exprAction = new Action("Apply expression...") {
						@Override
						public void run() {
							IExpressionService service = ServiceProvider.getService(IExpressionService.class);
							ExpressionDialog dialog = new ExpressionDialog(tableComposite.getShell(), service, dataOptions, controller);
							int retVal = dialog.open();
							if (retVal == Window.OK) {
								DataOptions result = dialog.getResult();
								if (result != null ) {
									dataOptions.getParent().addVirtualOption(result);
									if (initialiser != null)
										initialiser.initialise(dataOptions.getParent());
									
									setInput(dataOptions.getParent().getDataOptions());
								}
							}
						}
					};
					
					manager.add(exprAction);

					if (dataOptions instanceof DataOptionsSlice || dataOptions instanceof DataOptionsDataset) {
						Action r = new Action("Remove view") {
							@Override
							public void run() {
								if (dataOptions.isSelected() && dataOptions.getParent().isSelected()) {
									controller.setDataSelected(dataOptions, false);
								}
								dataOptions.getParent().removeVirtualOption(dataOptions.getName());
								setInput(dataOptions.getParent().getDataOptions());

							}
						};
						
						Action save = new Action("Save to Nexus...") {
							@Override
							public void run() {
								IRecentPlaces service = ServiceProvider.getService(IRecentPlaces.class);
								
								DataOptionsUIUtils.saveToFile(dataOptions, service == null ? null : service.getCurrentDefaultDirectory());
							}
						};
						
						manager.add(r);
						manager.add(save);
					}
				}
			}
		});

		menuMgr.setRemoveAllWhenShown(true);
		tableViewer.getControl().setMenu(menu);

		TableViewerColumn check   = new TableViewerColumn(tableViewer, SWT.CENTER, 0);
		check.setEditingSupport(new CheckBoxEditSupport(tableViewer));
		check.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return "";
			}

			@Override
			public Image getImage(Object element) {
				return ((DataOptions)element).isSelected() ? ticked : unticked;
			}

		});

		check.getColumn().setWidth(28);

		TableViewerColumn name = new TableViewerColumn(tableViewer, SWT.LEFT);
		name.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public Color getForeground(Object element) {
				if (element instanceof DataOptionsSlice || element instanceof DataOptionsDataset) {
					return ColorConstants.gray;
				}
				return null;
			}

			@Override
			public String getText(Object element) {
				DataOptions d = (DataOptions) element;
				return useShortName ? d.getShortName() : d.getName();
			}

			@Override
			public String getToolTipText(Object element) {
				return ((DataOptions) element).getName();
			}
		});

		name.getColumn().setText("Dataset Name");
		name.getColumn().setWidth(200);
		name.getColumn().addSelectionListener(new SelectionAdapter() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				reverse = !reverse;
				Object input = tableViewer.getInput();
				
				if (input instanceof List<?>) {
					sort((List<DataOptions>) input);
				}
				
				tableViewer.getTable().setSortDirection(reverse ? SWT.UP : SWT.DOWN);
				tableViewer.getTable().setSortColumn(name.getColumn());
				tableViewer.refresh();
				
			}
		});

		TableViewerColumn shape = new TableViewerColumn(tableViewer, SWT.CENTER);
		shape.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				try {
					return Arrays.toString(((DataOptions)element).getLazyDataset().getShape());
				} catch (Exception e) {
					return "[x]";
				}

			}
		});

		shape.getColumn().setText("Shape");
		shape.getColumn().setWidth(200);

		TableColumnLayout columnLayout = new TableColumnLayout();
		columnLayout.setColumnData(check.getColumn(), new ColumnPixelData(24));
		columnLayout.setColumnData(name.getColumn(), new ColumnWeightData(70,20));
		columnLayout.setColumnData(shape.getColumn(), new ColumnWeightData(30,20));

		tableComposite.setLayout(columnLayout);
	}
	
	private void sort(List<DataOptions> list) {

		if (list == null) {
			return;
		}

		list.sort((DataOptions o1, DataOptions o2) -> {

			//put "not from file" datasets at bottom
			if (!o1.isFromFile() ^ !o2.isFromFile()) {
				return o1.isFromFile() ? -1 : 1; 
			}

			int c = o1.getName().compareTo(o2.getName());

			if (reverse) c *= -1;

			return c;
		});
	}

	private class CheckBoxEditSupport extends EditingSupport {

		public CheckBoxEditSupport(ColumnViewer viewer) {
			super(viewer);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			CheckboxCellEditor edit = new CheckboxCellEditor(getTable());
			edit.setValue(((DataOptions)element).isSelected());
			return edit;
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			if (element instanceof DataOptions) return ((DataOptions)element).isSelected();
			return null;
		}

		@Override
		protected void setValue(Object element, Object value) {
			if (element instanceof DataOptions && value instanceof Boolean){
				controller.setDataSelected((DataOptions)element, (Boolean)value);
			}
		}
	}

	private class DataOptionsAction extends Action {

		private DataOptions dataOptions;
		private BiFunction<DataOptions, IMonitor, DataOptions> function;
		private String task;

		public DataOptionsAction(String name, String task, DataOptions dataOptions, BiFunction<DataOptions, IMonitor, DataOptions> function) {
			super(name);
			this.dataOptions = dataOptions;
			this.function = function;
			this.task = task;
		}

		@Override
		public void run() {

			try {
				PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {

					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						monitor.beginTask(task, DataOptionsUtils.getNumberOfSlices(dataOptions));
						DataOptions out = function.apply(dataOptions,new ProgressMonitorWrapper(monitor));
						if (out == null) {
							//cancelled
							return;
						}
						dataOptions.getParent().addVirtualOption(out);
						Display.getDefault().asyncExec(() -> tableViewer.setInput(dataOptions.getParent().getDataOptions()));
					}
				});
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


		}

	}
}
