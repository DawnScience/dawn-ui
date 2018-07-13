package org.dawnsci.plotting.tools.reduction;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.dawnsci.plotting.tools.Activator;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DataReduction2DToolSpectraRegionComposite extends DataReduction2DToolObservableResourceComposite {

	public static final String SPECTRA_REGION_TRACE_SHOULD_ADD = "spectraRegionTraceShouldAdd";
	public static final String SPECTRA_REGION_TRACE_SHOULD_REMOVE = "spectraRegionTraceShouldRemove";

	private CheckboxTableViewer spectraRegionTableViewer;

	private final DataBindingContext dataBindingCtx = new DataBindingContext();

	private final IObservableList  spectraRegionList = new WritableList(new ArrayList<DataReduction2DToolSpectraRegionDataNode>(), DataReduction2DToolSpectraRegionDataNode.class);
	private final IObservableList selectedRegionSpectraList = new WritableList(new ArrayList<DataReduction2DToolSpectraRegionDataNode>(), DataReduction2DToolSpectraRegionDataNode.class);
	private final IObservableSet checkedRegionSpectraList = new WritableSet(new HashSet<DataReduction2DToolSpectraRegionDataNode>(), DataReduction2DToolSpectraRegionDataNode.class);

	private final DataReduction2DToolModel toolPageModel;
	
	private static final Logger logger = LoggerFactory.getLogger(DataReduction2DToolSpectraRegionComposite.class);
	
	@Override
	protected void disposeResource() {
		// nothing to dispose of...
	}

	public DataReduction2DToolSpectraRegionComposite(Composite parent, int style, DataReduction2DToolModel toolPageModel) {
		super(parent, style);
		this.toolPageModel = toolPageModel;
		this.toolPageModel.setSpectraRegionTableComposite(this);
		setup();
		doBinding();
	}
	
	public IObservableSet getCheckedRegionSpectraList() {
		return checkedRegionSpectraList;
	}

	public IObservableList getSelectedRegionSpectraList() {
		return selectedRegionSpectraList;
	}

	public IObservableList getSpectraRegionList() {
		return spectraRegionList;
	}
	
	private void doBinding() {
		spectraRegionList.addListChangeListener(event -> 
			event.diff.accept(new ListDiffVisitor() {
				@Override
				public void handleRemove(int index, Object element) {
					DataReduction2DToolSpectraRegionDataNode spectraRegion = (DataReduction2DToolSpectraRegionDataNode) element;
					spectraRegion.getRegion().removeROIListener(spectraRegion);
					spectraRegion.removePropertyChangeListener(DataReduction2DToolSpectraRegionDataNode.SPECTRA_CHANGED, spectraChangedListener);
					if (checkedRegionSpectraList.contains(spectraRegion)) {
						checkedRegionSpectraList.remove(spectraRegion);
					}
					DataReduction2DToolSpectraRegionComposite.this.firePropertyChange(SPECTRA_REGION_TRACE_SHOULD_REMOVE, null, spectraRegion);
				}

				@Override
				public void handleAdd(int index, Object element) {
					DataReduction2DToolSpectraRegionDataNode spectraRegion = (DataReduction2DToolSpectraRegionDataNode) element;
					DataReduction2DToolSpectraRegionComposite.this.firePropertyChange(SPECTRA_REGION_TRACE_SHOULD_ADD, null, spectraRegion);
					spectraRegion.addPropertyChangeListener(DataReduction2DToolSpectraRegionDataNode.SPECTRA_CHANGED, spectraChangedListener);
					checkedRegionSpectraList.add(spectraRegion);
				}
			})
		);

		dataBindingCtx.bindList(
				ViewerProperties.multipleSelection().observe(spectraRegionTableViewer), selectedRegionSpectraList);
		dataBindingCtx.bindSet(
				ViewersObservables.observeCheckedElements(spectraRegionTableViewer, DataReduction2DToolSpectraRegionDataNode.class),
				checkedRegionSpectraList);
	}

	private final PropertyChangeListener spectraChangedListener = evt -> {
		if (evt.getPropertyName().equals(DataReduction2DToolSpectraRegionDataNode.SPECTRA_CHANGED)) {
			DataReduction2DToolSpectraRegionDataNode spectraRegion = (DataReduction2DToolSpectraRegionDataNode) evt.getSource();
			if (spectraRegionTableViewer != null && spectraRegionTableViewer.getChecked(spectraRegion)) {
				DataReduction2DToolSpectraRegionComposite.this.firePropertyChange(SPECTRA_REGION_TRACE_SHOULD_REMOVE, null, spectraRegion);
				DataReduction2DToolSpectraRegionComposite.this.firePropertyChange(SPECTRA_REGION_TRACE_SHOULD_ADD, null, spectraRegion);
			}
		}
	};

	private void setup() {
		setLayout(DataReduction2DToolHelper.createGridLayoutWithNoMargin(1, false));

		createToolbarForSpectraRegionTable(this);

		spectraRegionTableViewer = CheckboxTableViewer.newCheckList(
				this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		Table spectraRegionTable = spectraRegionTableViewer.getTable();
		spectraRegionTable.setHeaderVisible(true);
		spectraRegionTable.setLinesVisible(true);
		spectraRegionTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		TableViewerColumn colRegionName = new TableViewerColumn(spectraRegionTableViewer, SWT.NONE);
		colRegionName.getColumn().setText("Region name");
		colRegionName.getColumn().setWidth(100);

		TableViewerColumn colStartSpectrumIndex = new TableViewerColumn(spectraRegionTableViewer, SWT.NONE);
		colStartSpectrumIndex.getColumn().setText("Start");
		colStartSpectrumIndex.getColumn().setWidth(40);

		TableViewerColumn colEndSpectrumIndex = new TableViewerColumn(spectraRegionTableViewer, SWT.NONE);
		colEndSpectrumIndex.getColumn().setText("End");
		colEndSpectrumIndex.getColumn().setWidth(40);

		TableViewerColumn colRegionDesc = new TableViewerColumn(spectraRegionTableViewer, SWT.NONE);
		colRegionDesc.getColumn().setText("Description");
		colRegionDesc.getColumn().setWidth(60);

		ObservableListContentProvider contentProvider = new ObservableListContentProvider();
		IObservableSet knownElements = contentProvider.getKnownElements();

		final IObservableMap startColumn = BeanProperties.value(DataReduction2DToolSpectraRegionDataNode.class,
				DataReduction2DToolSpectraRegionDataNode.START).observeDetail(knownElements);
		final IObservableMap endColumn = BeanProperties.value(DataReduction2DToolSpectraRegionDataNode.class,
				DataReduction2DToolSpectraRegionDataNode.END).observeDetail(knownElements);

		IObservableMap[] labelMaps = {startColumn, endColumn};

		spectraRegionTableViewer.setContentProvider(contentProvider);
		spectraRegionTableViewer.setLabelProvider(new ObservableMapLabelProvider(labelMaps) {
			@Override
			public String getColumnText(Object element, int columnIndex) {
				DataReduction2DToolSpectraRegionDataNode spectraRegionToolDataModel = (DataReduction2DToolSpectraRegionDataNode) element;
				switch (columnIndex) {
				case 0: return spectraRegionToolDataModel.getRegion().getLabel();
				case 1: return Integer.toString(spectraRegionToolDataModel.getStart().getIndex());
				case 2: return Integer.toString(spectraRegionToolDataModel.getEnd().getIndex());
				case 3: return spectraRegionToolDataModel.toString();
				default : return "Unkown column";
				}
			}
		});

		final MenuManager menuManager = new MenuManager();
		Menu menu = menuManager.createContextMenu(spectraRegionTableViewer.getTable());
		menuManager.addMenuListener(manager -> {
			if(spectraRegionTableViewer.getSelection().isEmpty()) {
				return;
			}
			menuManager.add(removeRegionAction);
		});
		menuManager.setRemoveAllWhenShown(true);
		// Set the MenuManager
		spectraRegionTableViewer.getTable().setMenu(menu);

		spectraRegionTableViewer.setInput(spectraRegionList);
	}
	
	private final Action removeRegionAction = new Action("Remove") {
		@Override
		public void run() {
			if(spectraRegionTableViewer.getSelection() instanceof IStructuredSelection) {
				IStructuredSelection selection = (IStructuredSelection) spectraRegionTableViewer.getSelection();
				Iterator<?> iterator = selection.iterator();
				while (iterator.hasNext()) {
					toolPageModel.getDataImagePlotting().removeRegion(((DataReduction2DToolSpectraRegionDataNode) iterator.next()).getRegion());
				}
			}
		}
	};
	
	private void createToolbarForSpectraRegionTable(Composite regionTableParent) {
		ToolBar toolBar = new ToolBar(regionTableParent, SWT.HORIZONTAL);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		ToolItem selectAllToolItem = new ToolItem(toolBar, SWT.PUSH);
		selectAllToolItem.setText("");
		selectAllToolItem.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ADD));
		selectAllToolItem.addListener(SWT.Selection, event -> {
			for (TableItem item : spectraRegionTableViewer.getTable().getItems()) {
				if (!item.getChecked()) {
					spectraRegionTableViewer.setChecked(item.getData(), true);
					fireCheckSelectionEvent(event, item);
				}
			}
		});

		ToolItem unSelectAllToolItem = new ToolItem(toolBar, SWT.PUSH);
		unSelectAllToolItem.setText("");
		unSelectAllToolItem.setToolTipText("Clear region selection");
		unSelectAllToolItem.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ETOOL_CLEAR));
		unSelectAllToolItem.addListener(SWT.Selection, event -> {
			for (TableItem item : spectraRegionTableViewer.getTable().getItems()) {
				if (item.getChecked()) {
					spectraRegionTableViewer.setChecked(item.getData(), false);
					fireCheckSelectionEvent(event, item);
				}
			}
		});

		final ToolItem saveNexusToolItem = new ToolItem(toolBar, SWT.PUSH);
		saveNexusToolItem.setText("");
		saveNexusToolItem.setToolTipText("Export reduced dataset");
		saveNexusToolItem.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ETOOL_SAVEAS_EDIT));
		saveNexusToolItem.addListener(SWT.Selection, event -> {
			try {
				toolPageModel.averageSpectrumAndExport(saveNexusToolItem.getDisplay());
			} catch (Exception e) {
				logger.error("Export exception", e);
				Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage());
				ErrorDialog.openError(DataReduction2DToolSpectraRegionComposite.this.getShell(), "Error exporting data", "Could not export data to file", status);
			}
		});
	}
	
	private void fireCheckSelectionEvent(Event event, TableItem item) {
		SelectionEvent checkEvent = new SelectionEvent(event);
		checkEvent.detail = SWT.CHECK;
		checkEvent.item = item;
		spectraRegionTableViewer.handleSelect(checkEvent);
	}
	
	public void populateSpectraRegion(List<IRegion> plottedRegions) {
		for (IRegion region : toolPageModel.getDataImagePlotting().getRegions()) {
			if (plottedRegions.contains(region) && region.getUserObject() != null) {
				DataReduction2DToolSpectraRegionDataNode spectraRegion;
				if (region.getUserObject() instanceof DataReduction2DToolAvgSpectraRegionDataNode) {
					spectraRegion = new DataReduction2DToolAvgSpectraRegionDataNode(region, toolPageModel, ((DataReduction2DToolAvgSpectraRegionDataNode) region.getUserObject()).getRegionData());
				}
				else {
					spectraRegion = new DataReduction2DToolSpectraRegionDataNode(region, toolPageModel, ((DataReduction2DToolSpectraRegionDataNode) region.getUserObject()).getRegionData());
				}
				spectraRegionList.add(spectraRegion);
			}
		}
	}
	
	public void clearRegionData() {
		//toolPageModel.getDataImagePlotting().clearRegions();
		spectraRegionList.clear();
	}
}
