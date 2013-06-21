/*-
 * Copyright 2013 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dawb.workbench.ui.views;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.dawb.common.services.ILoaderService;
import org.dawb.common.ui.plot.PlottingFactory;
import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawb.workbench.ui.Activator;
import org.dawb.workbench.ui.views.DiffractionCalibrationUtils.ManipulateMode;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.tool.IToolPage.ToolPageRole;
import org.dawnsci.plotting.api.tool.IToolPageSystem;
import org.dawnsci.plotting.api.trace.IImageTrace;
import org.dawnsci.plotting.tools.diffraction.DiffractionImageAugmenter;
import org.dawnsci.plotting.tools.diffraction.DiffractionTool;
import org.dawnsci.plotting.util.PlottingUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationFactory;
import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationStandards;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
import uk.ac.diamond.scisoft.analysis.diffraction.DetectorPropertyEvent;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironmentEvent;
import uk.ac.diamond.scisoft.analysis.diffraction.IDetectorPropertyListener;
import uk.ac.diamond.scisoft.analysis.diffraction.IDiffractionCrystalEnvironmentListener;
import uk.ac.diamond.scisoft.analysis.hdf5.HDF5NodeLink;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;

/**
 * This listens for a selected editor (of a diffraction image) and allows
 * 
 * 1) selection of calibrant
 * 2) movement, scaling and tilting of rings
 * 3) refinement of fit
 * 4) calibration (other images too?)
 *
 * Should display relevant metadata, allow a number of files to contribute to final calibration
 */
public class DiffractionCalibrationPlottingView extends ViewPart {

	private static Logger logger = LoggerFactory.getLogger(DiffractionCalibrationPlottingView.class);

	private DiffractionTableData currentData;
	
	// CopyOnWriteArrayList is necessary to avoid ConcurrentModificationExceptions
	// See http://javarevisited.blogspot.co.uk/2010/10/what-is-difference-between-synchronized.html
	// and
	// http://docs.oracle.com/javase/1.5.0/docs/api/java/util/concurrent/CopyOnWriteArrayList.html
	private List<DiffractionTableData> model = new CopyOnWriteArrayList<DiffractionTableData>();
	private ILoaderService service;

	private Composite parent;
	private ScrolledComposite scrollComposite;
	private Composite scrollHolder;
	private TableViewer tableViewer;
	private Button calibrateImages;
	private Button calibrateWD;
	private Combo calibrant;
	private Action deleteAction;

	private IPlottingSystem plottingSystem;

	private String fullPath;
	private String fileName;
	
	private String[] statusString = new String[1];

	private ISelectionChangedListener selectionChangeListener;
	private DropTargetAdapter dropListener;
	private IDiffractionCrystalEnvironmentListener diffractionCrystEnvListener;
	private IDetectorPropertyListener detectorPropertyListener;

	private int dataNumber = -1;
	private List<String> pathsList = new ArrayList<String>();

	public DiffractionCalibrationPlottingView() {
		service = (ILoaderService)PlatformUI.getWorkbench().getService(ILoaderService.class);
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		init(site);
		setSite(site);
		setPartName("Diffraction Calibration View");

		if (memento != null) {
			dataNumber = memento.getInteger("DataNumber");
			for(int i=0; i<dataNumber; i++){
				pathsList.add(i, memento.getString("DataPath"+String.valueOf(i)));
			}
		}
	}

	@Override
	public void saveState(IMemento memento) {
		if (memento != null) {
			int size = tableViewer.getTable().getItems().length;
			memento.putInteger("DataNumber", size);
			for(int i = 0; i < size; i++){
				TableItem item = ((TableItem)tableViewer.getTable().getItem(i));
				DiffractionTableData data = (DiffractionTableData)item.getData();
				memento.putString("DataPath"+String.valueOf(i), data.path);
			}
		}
	}

	@Override
	public void createPartControl(final Composite parent) {
		parent.setLayout(new FillLayout());

		this.parent = parent;

		// selection change listener for table viewer
		selectionChangeListener = new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection is = event.getSelection();
				if(is instanceof StructuredSelection){
					StructuredSelection structSelection = (StructuredSelection)is;
					DiffractionTableData selectedData = (DiffractionTableData)structSelection.getFirstElement();
					if(selectedData == null) return;
					IImageTrace image = DiffractionCalibrationUtils.getImageTrace(plottingSystem);
					if(image == null) return;
					// do nothing if image already plotted
					if(image.getData().equals(selectedData.image)) return;

					if(selectedData.image != null)
						PlottingUtils.plotData(plottingSystem, selectedData.name, selectedData.image);

					// set Ring data
					setData(selectedData.path, selectedData.image);

					DiffractionImageAugmenter aug = selectedData.augmenter;
					if (aug == null) {
						aug = new DiffractionImageAugmenter(plottingSystem);
						selectedData.augmenter = aug;
					}
					aug.activate();
					if (selectedData.path == null && fullPath != null) {
						String path = fullPath;
						selectedData.path = path;
						selectedData.name = path.substring(path.lastIndexOf(File.separatorChar) + 1);
						selectedData.md = DiffractionTool.getDiffractionMetadata(selectedData.image, selectedData.path, service, statusString);
					}
					if (selectedData.md == null)
						selectedData.md = DiffractionTool.getDiffractionMetadata(selectedData.image, selectedData.path, service, statusString);
					if(selectedData.md != null){
						aug.setDiffractionMetadata(selectedData.md);
					}

					DiffractionCalibrationUtils.hideFoundRings(plottingSystem);

					DiffractionCalibrationUtils.drawCalibrantRings(aug);
				}
			}
		};

		diffractionCrystEnvListener = new IDiffractionCrystalEnvironmentListener() {
			@Override
			public void diffractionCrystalEnvironmentChanged(
					DiffractionCrystalEnvironmentEvent evt) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						tableViewer.refresh();
					}
				});
			}
		};

		detectorPropertyListener = new IDetectorPropertyListener() {
			@Override
			public void detectorPropertiesChanged(DetectorPropertyEvent evt) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						tableViewer.refresh();
					}
				});
			}
		};

		dropListener = new DropTargetAdapter() {
			@Override
			public void drop(DropTargetEvent event) {
				Object dropData = event.data;
				if (dropData instanceof IResource[]) {
					IResource[] res = (IResource[])dropData;
					for (int i = 0; i < res.length; i++) {
						loadDataAndDrawCalibrants(res[i].getRawLocation().toOSString(), null);
					}
				}
				if(dropData instanceof TreeSelection) {
					TreeSelection selectedNode = (TreeSelection) dropData;
					Object obj[] = selectedNode.toArray();
					for (int i = 0; i < obj.length; i++) {
						if(obj[i] instanceof HDF5NodeLink){
							HDF5NodeLink node = (HDF5NodeLink) obj[i];
							if (node == null) return;
							loadDataAndDrawCalibrants(node.getFile().getFilename(), node.getFullName());

						} else if (obj[i] instanceof IFile){
							IFile file = (IFile) obj[i];
							loadDataAndDrawCalibrants(file.getLocation().toOSString(), null);
						}
					}
				}
				if(dropData instanceof String[]){
					String[] selectedData = (String[]) dropData;
					for (int i = 0; i < selectedData.length; i++) {
						loadDataAndDrawCalibrants(selectedData[i], null);
					}
				}
				tableViewer.getTable().deselectAll();
			}
		};

		deleteAction = new Action("Delete item", Activator.getImageDescriptor("icons/delete_obj.png")) {
			@Override
			public void run(){
				StructuredSelection selection = (StructuredSelection)tableViewer.getSelection();
				DiffractionTableData selectedData = (DiffractionTableData)selection.getFirstElement();
				if(model.size()>1)
					for (DiffractionTableData data : model) {
						if(data.name.equals(selectedData.name)){
							model.remove(data);
							data.md.getDetector2DProperties().removeDetectorPropertyListener(detectorPropertyListener);
							data.md.getDiffractionCrystalEnvironment().removeDiffractionCrystalEnvironmentListener(diffractionCrystEnvListener);
							tableViewer.refresh();
							break;
						}
					}
				if(!model.isEmpty()){
					PlottingUtils.plotData(plottingSystem, model.get(0).name, model.get(0).image);
					DiffractionTableData newSelectedData = (DiffractionTableData)tableViewer.getElementAt(0);
					StructuredSelection newSelection = new StructuredSelection(newSelectedData);
					tableViewer.setSelection(newSelection);
				}
			}
		};

		// main sashform which contains the left sash and the plotting system
		SashForm mainSash = new SashForm(parent, SWT.HORIZONTAL);
		mainSash.setBackground(new Color(parent.getDisplay(), 192, 192, 192));
		mainSash.setLayout(new FillLayout());

		// left sasfhform which contains the diffraction calibration controls and the diffraction tool
		SashForm leftSash = new SashForm(mainSash, SWT.VERTICAL);
		leftSash.setBackground(new Color(parent.getDisplay(), 192, 192, 192));
		leftSash.setLayout(new GridLayout(1, false));
		leftSash.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Composite controlComp = new Composite(leftSash, SWT.NONE);
		controlComp.setLayout(new GridLayout(1, false));
		
		Label instructionLabel = new Label(controlComp, SWT.WRAP);
		instructionLabel.setText("Drag/drop a file/data to the table below, choose a type of calibrant, " +
								 "modify the rings using the controls below and tick the checkbox near to the corresponding " +
								 "image before pressing the calibration buttons.");
		instructionLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));

		// make a scrolled composite
		scrollComposite = new ScrolledComposite(controlComp, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		scrollComposite.setLayout(new GridLayout(1, false));
		scrollComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		scrollHolder = new Composite(scrollComposite, SWT.NONE);

		GridLayout gl = new GridLayout(1, false);
		scrollHolder.setLayout(gl);
		scrollHolder.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true));

		// table of images and found rings
		tableViewer = new TableViewer(scrollHolder, SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		createColumns(tableViewer);
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLinesVisible(true);
		tableViewer.getTable().setToolTipText("Drag/drop file(s)/data to this table");
		tableViewer.setContentProvider(new MyContentProvider());
		tableViewer.setLabelProvider(new MyLabelProvider());
		tableViewer.setInput(model);
		tableViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableViewer.addSelectionChangedListener(selectionChangeListener);
		tableViewer.refresh();
		final MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
				if (!selection.isEmpty()) {
					deleteAction.setText("Delete "
							+ ((DiffractionTableData) selection.getFirstElement()).name);
					mgr.add(deleteAction);
				}
			}
		});
		tableViewer.getControl().setMenu(mgr.createContextMenu(tableViewer.getControl()));
		//add drop support
		DropTarget dt = new DropTarget(tableViewer.getControl(), DND.DROP_MOVE| DND.DROP_DEFAULT| DND.DROP_COPY);
		dt.setTransfer(new Transfer[] { TextTransfer.getInstance (), FileTransfer.getInstance(), 
										ResourceTransfer.getInstance(), LocalSelectionTransfer.getTransfer()});
		dt.addDropListener(dropListener);

		Composite calibrantHolder = new Composite(scrollHolder, SWT.NONE);
		calibrantHolder.setLayout(new GridLayout(2, false));
		calibrantHolder.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));

		Group controllerHolder = new Group(calibrantHolder, SWT.BORDER);
		controllerHolder.setText("Calibrant selection and positioning");
		controllerHolder.setLayout(new GridLayout(2, false));
		controllerHolder.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

		// create calibrant combo
		Label l = new Label(controllerHolder, SWT.NONE);
		l.setText("Calibrant:");
		l.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		calibrant = new Combo(controllerHolder, SWT.READ_ONLY);
		final CalibrationStandards standards = CalibrationFactory.getCalibrationStandards();
		calibrant.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				standards.setSelectedCalibrant(calibrant.getItem(calibrant.getSelectionIndex()));
				DiffractionCalibrationUtils.drawCalibrantRings(currentData.augmenter);
			}
		});
		for (String c : standards.getCalibrantList()) {
			calibrant.add(c);
		}
		String s = standards.getSelectedCalibrant();
		if (s != null) {
			calibrant.setText(s);
		}
		calibrant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		Composite padComp = new Composite(controllerHolder, SWT.BORDER);
		padComp.setLayout(new GridLayout(5, false));
		padComp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		padComp.setToolTipText("Move calibrant");

		l = new Label(padComp, SWT.NONE);
		l = new Label(padComp, SWT.NONE);
		Button upButton = new Button(padComp, SWT.ARROW | SWT.UP);
		upButton.setToolTipText("Move rings up");
		upButton.addMouseListener(new RepeatingMouseAdapter(parent.getDisplay(), new SlowFastRunnable() {
			@Override
			public void run() {
				DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.UP, isFast());
			}
		}));
		upButton.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, false, false));
		l = new Label(padComp, SWT.NONE);
		l = new Label(padComp, SWT.NONE);

		l = new Label(padComp, SWT.NONE);
		Button leftButton = new Button(padComp, SWT.ARROW | SWT.LEFT);
		leftButton.setToolTipText("Shift rings left");
		leftButton.addMouseListener(new RepeatingMouseAdapter(parent.getDisplay(), new SlowFastRunnable() {
			@Override
			public void run() {
				DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.LEFT, isFast());
			}
		}));
		leftButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		l = new Label(padComp, SWT.NONE);
		Button rightButton = new Button(padComp, SWT.ARROW | SWT.RIGHT);
		rightButton.setToolTipText("Shift rings right");
		rightButton.addMouseListener(new RepeatingMouseAdapter(parent.getDisplay(), new SlowFastRunnable() {
			@Override
			public void run() {
				DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.RIGHT, isFast());
			}
		}));
		rightButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		l = new Label(padComp, SWT.NONE);

		l = new Label(padComp, SWT.NONE);
		l = new Label(padComp, SWT.NONE);
		Button downButton = new Button(padComp, SWT.ARROW | SWT.DOWN);
		downButton.setToolTipText("Move rings down");
		downButton.addMouseListener(new RepeatingMouseAdapter(parent.getDisplay(), new SlowFastRunnable() {
			@Override
			public void run() {
				DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.DOWN, isFast());
			}
		}));
		downButton.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));
		l = new Label(padComp, SWT.NONE);
		l = new Label(padComp, SWT.NONE);

		Composite actionComp = new Composite(controllerHolder, SWT.NONE);
		actionComp.setLayout(new GridLayout(3, false));
		actionComp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

		Composite sizeComp = new Composite(actionComp, SWT.BORDER);
		sizeComp.setLayout(new GridLayout(1, false));
		sizeComp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		sizeComp.setToolTipText("Change size");

		Button plusButton = new Button(sizeComp, SWT.PUSH);
		plusButton.setText("+");
		plusButton.setToolTipText("Make rings larger");
		plusButton.addMouseListener(new RepeatingMouseAdapter(parent.getDisplay(), new SlowFastRunnable() {
			@Override
			public void run() {
				DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.ENLARGE, isFast());
			}
			@Override
			public void stop() {
				refreshTable();
			}
		}));
		plusButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		Button minusButton = new Button(sizeComp, SWT.PUSH);
		minusButton.setText("-");
		minusButton.setToolTipText("Make rings smaller");
		minusButton.addMouseListener(new RepeatingMouseAdapter(parent.getDisplay(), new SlowFastRunnable() {
			@Override
			public void run() {
				DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.SHRINK, isFast());
			}
			@Override
			public void stop() {
				refreshTable();
			}
		}));
		minusButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

		Composite shapeComp = new Composite(actionComp, SWT.BORDER);
		shapeComp.setLayout(new GridLayout(1, false));
		shapeComp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		shapeComp.setToolTipText("Change shape");

		Button elongateButton = new Button(shapeComp, SWT.PUSH);
		elongateButton.setText("Elongate");
		elongateButton.setToolTipText("Make rings more elliptical");
		elongateButton.addMouseListener(new RepeatingMouseAdapter(parent.getDisplay(), new SlowFastRunnable() {
			@Override
			public void run() {
				DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.ELONGATE, isFast());
			}
			@Override
			public void stop() {
				refreshTable();
			}
		}));
		elongateButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		Button squashButton = new Button(shapeComp, SWT.PUSH | SWT.FILL);
		squashButton.setText("Squash");
		squashButton.setToolTipText("Make rings more circular");
		squashButton.addMouseListener(new RepeatingMouseAdapter(parent.getDisplay(), new SlowFastRunnable() {
			@Override
			public void run() {
				DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.SQUASH, isFast());
			}
			@Override
			public void stop() {
				refreshTable();
			}
		}));
		squashButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

		Composite rotateComp = new Composite(actionComp, SWT.BORDER);
		rotateComp.setLayout(new GridLayout(1, false));
		rotateComp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		rotateComp.setToolTipText("Change rotation");

		Button clockButton = new Button(rotateComp, SWT.PUSH);
		clockButton.setImage(Activator.getImage("icons/arrow_rotate_clockwise.png"));
		clockButton.setToolTipText("Rotate rings clockwise");
		clockButton.addMouseListener(new RepeatingMouseAdapter(parent.getDisplay(), new SlowFastRunnable() {
			@Override
			public void run() {
				DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.CLOCKWISE, isFast());
			}
			@Override
			public void stop() {
				refreshTable();
			}
		}));
		clockButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		Button antiClockButton = new Button(rotateComp, SWT.PUSH);
		antiClockButton.setImage(Activator.getImage("icons/arrow_rotate_anticlockwise.png"));
		antiClockButton.setToolTipText("Rotate rings anti-clockwise");
		antiClockButton.addMouseListener(new RepeatingMouseAdapter(parent.getDisplay(), new SlowFastRunnable() {
			@Override
			public void run() {
				DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.ANTICLOCKWISE, isFast());
			}
			@Override
			public void stop() {
				refreshTable();
			}
		}));
		antiClockButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

		Composite calibrateComp = new Composite(calibrantHolder, SWT.NONE);
		calibrateComp.setLayout(new GridLayout(1, false));
		calibrateComp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));

		Button findRingButton = new Button(calibrateComp, SWT.PUSH);
		findRingButton.setText("Find rings in image");
		findRingButton.setToolTipText("Use pixel values to find rings in image near calibration rings");
		findRingButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		findRingButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Job findRingsJob = DiffractionCalibrationUtils.findRings(parent, plottingSystem, currentData);
				if(findRingsJob == null) return;
				findRingsJob.addJobChangeListener(new JobChangeAdapter(){
					@Override
					public void done(IJobChangeEvent event){
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								if (currentData.nrois > 0) {
									setCalibrateButtons();
								}
								refreshTable();
							}
						});
					}
				});
				findRingsJob.schedule();
			}
		});

		calibrateImages = new Button(calibrateComp, SWT.PUSH);
		calibrateImages.setText("Calibrate chosen images");
		calibrateImages.setToolTipText("Calibrate detector in chosen images");
		calibrateImages.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		calibrateImages.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DiffractionCalibrationUtils.calibrateImages(parent, plottingSystem, model, currentData);
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						refreshTable();
						setCalibrateButtons();
					}
				});
			}
		});
		calibrateImages.setEnabled(false);

		calibrateWD = new Button(calibrateComp, SWT.PUSH);
		calibrateWD.setText("Calibrate wavelength");
		calibrateWD.setToolTipText("Calibrate wavelength from images chosen in table");
		calibrateWD.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		calibrateWD.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DiffractionCalibrationUtils.calibrateWavelength(parent, model, currentData);
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						refreshTable();
					}
				});
			}
		});
		calibrateWD.setEnabled(false);

		scrollHolder.layout();
		scrollComposite.setContent(scrollHolder);
		scrollComposite.setExpandHorizontal(true);
		scrollComposite.setExpandVertical(true);
		scrollComposite.setMinSize(scrollHolder.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		scrollComposite.layout();
		// end of Diffraction Calibration controls
		
		// start plotting system
		Composite plotComp = new Composite(mainSash, SWT.NONE);
		plotComp.setLayout(new GridLayout(1, false));
		plotComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		try {
			ActionBarWrapper actionBarWrapper = ActionBarWrapper.createActionBars(plotComp, null);
			plottingSystem = PlottingFactory.createPlottingSystem();
			plottingSystem.createPlotPart(plotComp, "", actionBarWrapper, PlotType.IMAGE, this);
			plottingSystem.setTitle("");
			plottingSystem.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		} catch (Exception e1) {
			logger.error("Could not create plotting system:"+ e1);
		}

		// try to load the previous data saved in the memento
		for(int i = 0; i<dataNumber; i++){
			if(!pathsList.get(i).endsWith(".nxs")){
				loadDataAndDrawCalibrants(pathsList.get(i), null);
			}
		}
		tableViewer.getTable().deselectAll();

		// start diffraction tool 
		Composite diffractionToolComp = new Composite(leftSash, SWT.BORDER);
		diffractionToolComp.setLayout(new FillLayout());
		try {
			final IToolPageSystem toolSystem = (IToolPageSystem)plottingSystem.getAdapter(IToolPageSystem.class);
			// Show tools here, not on a page.
			toolSystem.setToolComposite(diffractionToolComp);
			toolSystem.setToolVisible("org.dawb.workbench.plotting.tools.diffraction.Diffraction", ToolPageRole.ROLE_2D, null);
		} catch (Exception e2) {
			logger.error("Could not open diffraction tool:"+ e2);
		}

		//mainSash.setWeights(new int[] { 1, 2});
	}

	private void loadDataAndDrawCalibrants(String filePath, String dataFullName) {
		// Test if the selection has already been loaded and is in the model
		DiffractionTableData data = null;
		this.fullPath = filePath;
		if(fullPath == null) return;

		for (DiffractionTableData d : model) {
			if (fullPath.equals(d.path)) {
				data = d;
				break;
			}
		}

		IDataset image = null;
		if(data == null){
			image = PlottingUtils.loadData(filePath, dataFullName);
			int j = fullPath.lastIndexOf(System.getProperty("file.separator")); // TODO File.separator
			if (image == null) return;
			if(fileName == null)
				fileName = image.getName();
			fileName = j > 0 ? fullPath.substring(j + 1) : null;
			image.setName(fileName+":"+image.getName());
		}
		else{
			image = data.image;
		}
		if(image == null) return;
		
		PlottingUtils.plotData(plottingSystem, image.getName(), image);

		// set Ring data
		setData(fullPath, image);

		// update PlottingSystem
		data = null;
		if (fullPath != null) {
			for (DiffractionTableData d : model) {
				if (fullPath.equals(d.path)) {
					data = d;
					break;
				}
			}
		}
		if (data == null) return;

		System.err.println("We have an image, Houston!");

		DiffractionImageAugmenter aug = data.augmenter;
		if (aug == null) {
			aug = new DiffractionImageAugmenter(plottingSystem);
			data.augmenter = aug;
		}
		aug.activate();
		if (data.path == null && fullPath != null) {
			String path = fullPath;
			data.path = path;
			data.name = path.substring(path.lastIndexOf(File.separatorChar) + 1);
			data.md = DiffractionTool.getDiffractionMetadata(data.image, data.path, service, statusString);
		}
		if (data.md == null)
			data.md = DiffractionTool.getDiffractionMetadata(data.image, data.path, service, statusString);
		if(data.md != null){
			aug.setDiffractionMetadata(data.md);
			data.md.getDetector2DProperties().addDetectorPropertyListener(detectorPropertyListener);
			data.md.getDiffractionCrystalEnvironment().addDiffractionCrystalEnvironmentListener(diffractionCrystEnvListener);
		}

		refreshTable();
		DiffractionCalibrationUtils.hideFoundRings(plottingSystem);

		DiffractionCalibrationUtils.drawCalibrantRings(currentData.augmenter);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class key) {
		if (key==IPlottingSystem.class) {
			return plottingSystem;
		} else if (key==IToolPageSystem.class) {
			return plottingSystem.getAdapter(IToolPageSystem.class);
		}
		return super.getAdapter(key);
	}

	class MyContentProvider implements IStructuredContentProvider {
		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement == null) {
				return null;
			}
			return ((List<?>) inputElement).toArray();
		}
	}

	private static final Image TICKED = Activator.getImageDescriptor("icons/ticked.png").createImage();
	private static final Image UNTICKED = Activator.getImageDescriptor("icons/unticked.gif").createImage();

	class MyLabelProvider implements ITableLabelProvider {
		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return true;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex != 0)
				return null;
			if (element == null)
				return null;

			DiffractionTableData data = (DiffractionTableData) element;
			if (data.use)
				return TICKED;
			return UNTICKED;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (columnIndex == 0)
				return null;
			if (element == null)
				return null;

			DiffractionTableData data = (DiffractionTableData) element;
			if (columnIndex == 1) {
				return data.name;
			} else if (columnIndex == 2) {
				if (data.rois == null)
					return null;
				return String.valueOf(data.nrois);
			}

			IDiffractionMetadata md = data.md;
			if (md == null)
				return null;

			if (columnIndex == 3) {
				DetectorProperties dp = md.getDetector2DProperties();
				if (dp == null)
					return null;
				return String.format("%.2f", dp.getDetectorDistance());
			} else if (columnIndex == 4) {
				DiffractionCrystalEnvironment ce = md.getDiffractionCrystalEnvironment();
				if (ce == null)
					return null;
				return String.format("%.3f", ce.getWavelength());
			}
			return null;
		}
	}

	class MyEditingSupport extends EditingSupport {
		private TableViewer tv;
		private int column;

		public MyEditingSupport(TableViewer viewer, int col) {
			super(viewer);
			tv = viewer;
			this.column = col;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new CheckboxCellEditor(null, SWT.CHECK);
		}

		@Override
		protected boolean canEdit(Object element) {
			if(column == 0)
				return true;
			else
				return false;
		}

		@Override
		protected Object getValue(Object element) {
			DiffractionTableData data = (DiffractionTableData) element;
			return data.use;
		}

		@Override
		protected void setValue(Object element, Object value) {
			if(column == 0){
				DiffractionTableData data = (DiffractionTableData) element;
				data.use = (Boolean) value;
				tv.refresh();

				setCalibrateButtons();
			}
		}
		
	}

	private void createColumns(TableViewer tv) {
		TableViewerColumn tvc = new TableViewerColumn(tv, SWT.NONE);
		tvc.setEditingSupport(new MyEditingSupport(tv, 0));
		TableColumn tc = tvc.getColumn();
		tc.setText("Use");
		tc.setWidth(40);

		tvc = new TableViewerColumn(tv, SWT.NONE);
		tc = tvc.getColumn();
		tc.setText("Image");
		tc.setWidth(200);
		tvc.setEditingSupport(new MyEditingSupport(tv, 1));
		
		tvc = new TableViewerColumn(tv, SWT.NONE);
		tc = tvc.getColumn();
		tc.setText("#rings");
		tc.setWidth(70);
		tvc.setEditingSupport(new MyEditingSupport(tv, 2));
		
		tvc = new TableViewerColumn(tv, SWT.NONE);
		tc = tvc.getColumn();
		tc.setText("Distance");
		tc.setWidth(70);
		tvc.setEditingSupport(new MyEditingSupport(tv, 3));
		
		tvc = new TableViewerColumn(tv, SWT.NONE);
		tc = tvc.getColumn();
		tc.setText("Wavelength");
		tc.setWidth(70);
		tvc.setEditingSupport(new MyEditingSupport(tv, 4));
	}

	private void setData(String path, IDataset image) {
		if (path == null) return;
		if(image == null) return;
		DiffractionTableData data = null;
		if (path != null) {
			for (DiffractionTableData d : model) {
				if (path.equals(d.path)) {
					data = d;
					break;
				}
			}
		}
		if (data != null && data == currentData) return;

		if (currentData != null) {
			DiffractionImageAugmenter aug = currentData.augmenter;
			if (aug != null)
				aug.deactivate();
		}

		if (data == null) {
			data = new DiffractionTableData();
			model.add(data);
			if (new File(path).canRead()) {
				data.path = path;
				data.name = path.substring(path.lastIndexOf(File.separatorChar) + 1);
				data.image = image;
			}
		}
		currentData = data;

		if (data.augmenter != null) {
			data.augmenter.activate();
		}
	}

	private void refreshTable() {
		if(tableViewer == null) return;
		tableViewer.refresh();
		for (TableColumn c : tableViewer.getTable().getColumns()) {
			c.pack();
		}
		tableViewer.getControl().getParent().layout();
		// reset the scroll composite
		Rectangle r = scrollHolder.getClientArea();
		scrollComposite.setMinSize(scrollHolder.computeSize(r.width, SWT.DEFAULT));
		scrollHolder.layout();
	}

	private void setCalibrateButtons() {
		// enable/disable calibrate button according to use column
		int used = 0;
		for (DiffractionTableData d : model) {
			if (d.use) {
				used++;
			}
		}
		calibrateImages.setEnabled(used > 0);
		calibrateWD.setEnabled(used > 2);
	}

	private void removeListeners() {
		tableViewer.removeSelectionChangedListener(selectionChangeListener);
		Iterator<DiffractionTableData> it = model.iterator();
		while(it.hasNext()){
			DiffractionTableData d = it.next();
			model.remove(d);
			if(d.augmenter != null) d.augmenter.deactivate();
			d.md.getDetector2DProperties().removeDetectorPropertyListener(detectorPropertyListener);
			d.md.getDiffractionCrystalEnvironment().removeDiffractionCrystalEnvironmentListener(diffractionCrystEnvListener);
		}
		logger.debug("model emptied");
	}

	@Override
	public void dispose() {
		super.dispose();
		removeListeners();
		// FIXME Clear 
	}

	@Override
	public void setFocus() {
		if(parent != null && !parent.isDisposed())
			parent.setFocus();
	}
}
