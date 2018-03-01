package org.dawnsci.plotting.tools.reduction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.dawnsci.plotting.tools.Activator;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DataReduction2DToolSpectraTableComposite extends DataReduction2DToolObservableResourceComposite {

	protected static final Logger logger = LoggerFactory.getLogger(DataReduction2DToolSpectraTableComposite.class);

	public static final String NEW_REGION_PROP_NAME = "newRegion";
	private static final String SPECTRUM_NODE = "spectrumNode";

	private final Image stackoffsetChangeIcon = Activator.getImage("icons/ui-slider.png");

	private final List<DataReduction2DToolSpectrumDataNode> selectedSpectraList = new ArrayList<>();

	private Table spectraTable;
	private final DataReduction2DToolModel toolPageModel;
	
	private final InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(),
			"", "Enter number", "", newText -> {
		try {
			int avgValue = Integer.parseInt(newText);
			if (avgValue < 1) {
				return "Invalid number";
			}
		} catch(NumberFormatException e) {
			return "Not a number";
		}
		return null;
	});

	private final Action createPlotEveryIntervalAction = new Action("Select spectrum for every") {
		@Override
		public void run() {
			if (dlg.open() == Window.OK) {
				int intervalToPlot = Integer.parseInt(dlg.getValue());
				int counter = 0;
					selectedSpectraList.clear();
				for (DataReduction2DToolSpectrumDataNode spectrumNode : toolPageModel.getSpectrumDataNodes()) {
					if (counter % intervalToPlot == 0) {
						selectedSpectraList.add(spectrumNode);
					}
					counter++;
				}

				BusyIndicator.showWhile(Display.getCurrent(), () -> {
					TableItem[] items = spectraTable.getItems();
					TableItem[] array = IntStream.range(0, items.length)
							.filter(i -> i % intervalToPlot == 0)
							.mapToObj(i -> items[i])
							.toArray(TableItem[]::new);
					spectraTable.setSelection(array);
					// fire of event
					spectraTable.notifyListeners(SWT.Selection, new Event());
				});
			}
		}
	};

	private final Action createRegionAction = new Action("Create region") {
		@Override
		public void run() {
			DataReduction2DToolRegionData region = findSpectraAndCreateRegion();
			if (!validateRegion(region))
				return;
				
			DataReduction2DToolSpectraRegionDataNode spectraRegion;
			try {
				spectraRegion = new DataReduction2DToolSpectraRegionDataNode(createRegionROI(region.getStartIndex(), region.getEndIndex()), toolPageModel, region);
				addRegionAction(spectraRegion);
			} catch (Exception e) {
				e.printStackTrace();
				DataReduction2DToolHelper.showError("Unable to create regions for spectra", e.getMessage());
			}
		}
	};

	private final Action createRegionAvgAction = new Action("Create region and average") {
		@Override
		public void run() {
			DataReduction2DToolRegionData region = findSpectraAndCreateRegion();
			if (!validateRegion(region))
				return;
				
			try {
				DataReduction2DToolAvgSpectraRegionDataNode spectraRegion = new DataReduction2DToolAvgSpectraRegionDataNode(createRegionROI(region.getStartIndex(), region.getEndIndex()), toolPageModel, region);
				addRegionAction(spectraRegion);
			} catch (Exception e) {
				e.printStackTrace();
				DataReduction2DToolHelper.showError("Unable to create regions for spectra", e.getMessage());
			}
		}
	};

	public void clearSelectedSpectraList() {
		selectedSpectraList.clear();
	}
	
	public void setAxisColumnName(String name) {
		spectraTable.getColumn(1).setText(name);
	}
	
	private final Action createRegionAvgEveryAction = new Action("Create region and average every...") {
		@Override
		public void run() {
			if (dlg.open() == Window.OK) {
				try {
					int noOfSpectraToAvg = Integer.parseInt(dlg.getValue());
					DataReduction2DToolRegionData region = findSpectraAndCreateRegion();
					for (int i = 0 ; i < region.getnSpectra() ; i += noOfSpectraToAvg) {
						int newEndIndex;
						int newStartIndex;
						try {
							newStartIndex = region.getNodes().get(i).getIndex();
							newEndIndex = region.getNodes().get(i + noOfSpectraToAvg - 1).getIndex();
						} catch (IndexOutOfBoundsException e) {
							continue;
						}
						// create new region
						DataReduction2DToolRegionData newRegion = new DataReduction2DToolRegionData(newStartIndex, newEndIndex, region.getNodes().subList(i, i + noOfSpectraToAvg));
						if (!validateRegion(newRegion))
							continue;
							
						DataReduction2DToolAvgSpectraRegionDataNode spectraRegion = new DataReduction2DToolAvgSpectraRegionDataNode(createRegionROI(newStartIndex, newEndIndex), toolPageModel, newRegion);
						addRegionAction(spectraRegion);
					}
				} catch (Exception e) {
					e.printStackTrace();
					DataReduction2DToolHelper.showError("Unable to create regions for spectra", e.getMessage());
				}
			}
		}
	};
	
	private final Action deleteSpectraAction = new Action("Delete selected spectra") {
		@Override
		public void run() {
			TableItem[] selection = spectraTable.getSelection();
			Arrays.sort(selection, tableItemComparator);
			IObservableList spectraRegionList = toolPageModel.getSpectraRegionTableComposite().getSpectraRegionList();
			Arrays.stream(selection).forEachOrdered(item -> {
				DataReduction2DToolSpectrumDataNode spectrumDataNode = (DataReduction2DToolSpectrumDataNode) item.getData(SPECTRUM_NODE);
				int index = spectrumDataNode.getIndex();
				// check if this node is not already within a region!
				for (Object data : spectraRegionList) {
					DataReduction2DToolSpectraRegionDataNode node = (DataReduction2DToolSpectraRegionDataNode) data;
					int start = node.getStart().getIndex();
					int end = node.getEnd().getIndex();
					if (index >= start && index <= end) {
						DataReduction2DToolHelper.showWarning("Cannot delete spectrum " + Integer.toString(index), "This index is already in use by " + node.getRegion().getName() + ". Delete the region first and try again.");
						return;
					}
				}
					
				toolPageModel.getDeletedIndices().add(index);
				toolPageModel.getSpectrumDataNodes().remove(spectrumDataNode);
				// remove from tree
				item.dispose();
			});
			// remove spectra from plot after deleting them
			// this is done simply by firing a selection event
			spectraTable.notifyListeners(SWT.Selection, new Event());
		}
	};
	
	public List<DataReduction2DToolSpectrumDataNode> getSelectedSpectraList() {
		return selectedSpectraList;
	}

	private void addRegionAction(DataReduction2DToolSpectraRegionDataNode spectraRegion) {
		firePropertyChange(NEW_REGION_PROP_NAME, null, spectraRegion);
		toolPageModel.getDataImagePlotting().addRegion(spectraRegion.getRegion());
		Collection<IRegion> regions = toolPageModel.getDataImagePlotting().getRegions();
		logger.debug("current regions in createRegion: {}", regions.stream().map(myregion -> myregion.getName()).collect(Collectors.joining(", ")));
	}
	
	private IRegion createRegionROI(int startIndex, int endIndex) throws Exception {
		IRegion region = createRegion();
		region.setROI(new RectangularROI(0, startIndex, 100, endIndex - startIndex + 1, 0));
		return region;
	}

	private IRegion createRegion() throws Exception {
		IPlottingSystem<?> plotting = toolPageModel.getDataImagePlotting();
		IRegion region = plotting.createRegion(RegionUtils.getUniqueName("Region", plotting), IRegion.RegionType.YAXIS);
		region.setRegionColor(ColorConstants.blue);
		region.setPlotType(plotting.getPlotType());
		region.setShowLabel(true);
		return region;
	}

	private boolean validateRegion(DataReduction2DToolRegionData newRegion) {
		IObservableList spectraRegionList = toolPageModel.getSpectraRegionTableComposite().getSpectraRegionList();
		
		int newStart = newRegion.getStartIndex();
		int newEnd = newRegion.getEndIndex();
		
		for (Object data : spectraRegionList) {
				DataReduction2DToolSpectraRegionDataNode node = (DataReduction2DToolSpectraRegionDataNode) data;
				int start = node.getStart().getIndex();
				int end = node.getEnd().getIndex();
				if (newEnd >= start && newStart <= end) {
					DataReduction2DToolHelper.showWarning("Cannot add region with bounds " + Integer.toString(newStart) + ":" + Integer.toString(newEnd), "These indices are already in use by " + node.getRegion().getName() + ". Delete the region first and try again.");
					return false;
				}
		}
		return true;
	}
	
	private static final Comparator<TableItem> tableItemComparator = (a,b) -> {
		Object dataA = a.getData(SPECTRUM_NODE);
		Object dataB = b.getData(SPECTRUM_NODE);

		// ensure non-SpectrumDataNodes end up at the end
		if (dataA == null)
			return 1;
		if (dataB == null)
			return -1;

		return ((DataReduction2DToolSpectrumDataNode) dataA).getIndex() - ((DataReduction2DToolSpectrumDataNode) dataB).getIndex();
	};
	
	public DataReduction2DToolSpectraTableComposite(Composite parent, int style, DataReduction2DToolModel toolPageModel) {
		super(parent, style);
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		this.toolPageModel = toolPageModel;
		this.toolPageModel.setSpectraTableComposite(this);
		setup();
	}
	@Override
	protected void disposeResource() {
		stackoffsetChangeIcon.dispose();
	}

	private void setup() {
		createToolbarForSpectraTable(this);
		spectraTable = new Table(this, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL /* | SWT.VIRTUAL */);
		spectraTable.setHeaderVisible(true);
		spectraTable.setLinesVisible(true);
		spectraTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		TableColumn nameColumn = new TableColumn(spectraTable, SWT.LEFT);
		nameColumn.setText("Name");
		nameColumn.setWidth(155);

		TableColumn timeColumn = new TableColumn(spectraTable, SWT.CENTER);
		timeColumn.setText("Y-Axis"); // FIXME
		timeColumn.setWidth(120);
		
		final MenuManager menuManager = new MenuManager();
		Menu menu = menuManager.createContextMenu(spectraTable);
		menuManager.addMenuListener(manager -> {
			menuManager.add(createPlotEveryIntervalAction);

			TableItem[] selection = spectraTable.getSelection();
			if(selection.length == 0) {
				return;
			}

			menuManager.add(createRegionAction);
			menuManager.add(createRegionAvgAction);
			menuManager.add(createRegionAvgEveryAction);
			menuManager.add(deleteSpectraAction);
		});
		menuManager.setRemoveAllWhenShown(true);
		spectraTable.setMenu(menu);
		
		for (DataReduction2DToolSpectrumDataNode spectrumNode : toolPageModel.getSpectrumDataNodes()) {
			TableItem tableItem = new TableItem(spectraTable, SWT.NONE);
			tableItem.setText(0, spectrumNode.toString());
			tableItem.setText(1, DataReduction2DToolHelper.roundDoubletoString(spectrumNode.getAxisValue()));
			// TODO: add support for multiple columns for multiple axes
			tableItem.setData(SPECTRUM_NODE, spectrumNode);
		}
	}

	private void createToolbarForSpectraTable(final Composite treeParent) {
		ToolBar toolBar = new ToolBar(treeParent, SWT.HORIZONTAL);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final ToolItem stackToggle = new ToolItem(toolBar, SWT.PUSH);
		stackToggle.setImage(stackoffsetChangeIcon);
		stackToggle.setToolTipText(Double.toString(toolPageModel.getTraceStack()));
		stackToggle.addListener(SWT.Selection, event -> {
			InputDialog dlg = new InputDialog(stackToggle.getDisplay().getActiveShell(), "Stack offset", "Enter new offset", Double.toString(toolPageModel.getTraceStack()), newText ->  {
				try {
					double value = Double.parseDouble(newText);
					if (value >= 0) {
						return null;
					}
					return "Only positive number are allowed";
				} catch (NumberFormatException e) {
					return "Invalid input";
				}
			});
			if (dlg.open() ==  Window.OK) {
				toolPageModel.setTraceStack(Double.parseDouble(dlg.getValue()));
				stackToggle.setToolTipText(Double.toString(toolPageModel.getTraceStack()));
			}
		});

	}

	private DataReduction2DToolRegionData findSpectraAndCreateRegion() {
		TableItem[] selection = spectraTable.getSelection();
		// sort selection with respect to index
		Arrays.sort(selection, tableItemComparator);
		List<DataReduction2DToolSpectrumDataNode> nodes = new ArrayList<>();
		for (TableItem item : selection) {
			Object data = item.getData(SPECTRUM_NODE);
			if (data == null || !DataReduction2DToolSpectrumDataNode.class.isInstance(data))
				continue;
				
			DataReduction2DToolSpectrumDataNode spectrum = (DataReduction2DToolSpectrumDataNode) data;

			nodes.add(spectrum);
		}
		return new DataReduction2DToolRegionData(nodes.get(0).getIndex(), nodes.get(nodes.size() - 1).getIndex(), nodes);
	}
	
	public Table getSpectraTable() {
		return spectraTable;
	}
}
