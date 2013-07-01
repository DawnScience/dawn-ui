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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dawb.common.services.ILoaderService;
import org.dawb.common.ui.parts.PartUtils;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.workbench.ui.Activator;
import org.dawb.workbench.ui.views.DiffractionCalibrationUtils.ManipulateMode;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.trace.IImageTrace;
import org.dawnsci.plotting.api.trace.ITraceListener;
import org.dawnsci.plotting.api.trace.TraceEvent;
import org.dawnsci.plotting.tools.diffraction.DiffractionImageAugmenter;
import org.dawnsci.plotting.tools.diffraction.DiffractionTool;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationFactory;
import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationStandards;
import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
import uk.ac.diamond.scisoft.analysis.io.AbstractFileLoader;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;

/**
 * This view is being replaced by {@link DiffractionCalibrationPlottingView }
 * This listens for a selected editor (of a diffraction image) and allows
 * 
 * 1) selection of calibrant
 * 2) movement, scaling and tilting of rings
 * 3) refinement of fit
 * 4) calibration (other images too?)
 *
 * Should display relevant metadata, allow a number of files to contribute to final calibration
 */
public class DiffractionCalibrationView extends ViewPart {

	private ScrolledComposite sComp;
	private IPlottingSystem currentSystem;
	private DiffractionTableData currentData;
	private List<DiffractionTableData> model = new ArrayList<DiffractionTableData>();
	private Map<IPlottingSystem, DiffractionTraceListener> listeners = new HashMap<IPlottingSystem, DiffractionTraceListener>();
	private ILoaderService service;
	private TableViewer tableViewer;
	private IPartListener2 listener;
	private Button calibrateImages;
	private Button calibrateWD;

	public DiffractionCalibrationView() {
		// FIXME, service might not be there, can this view still work then?
		service = (ILoaderService)PlatformUI.getWorkbench().getService(ILoaderService.class);
	}

	@Override
	public void createPartControl(final Composite parent) {
		parent.setLayout(new FillLayout());

		// add part listener for relevant editors and views
		listener = new IPartListener2() {
			@Override
			public void partVisible(IWorkbenchPartReference partRef) {
				IWorkbenchPart part = partRef.getPart(false);
				System.err.println("part visible: " + part.getTitle());
				IPlottingSystem system = PartUtils.getPlottingSystem(part);
				if (system != null) {
					String altPath = part instanceof IEditorPart ? EclipseUtils.getFilePath(((IEditorPart) part).getEditorInput()) : null;
					setData(altPath, system);
				}
			}
			
			@Override
			public void partOpened(IWorkbenchPartReference partRef) {
				IWorkbenchPart part = partRef.getPart(false);
				System.err.println("part opened: " + part.getTitle());
			}
			
			@Override
			public void partInputChanged(IWorkbenchPartReference partRef) {
				IWorkbenchPart part = partRef.getPart(false);
				System.err.println("input changed: " + part.getTitle());
			}
			
			@Override
			public void partHidden(IWorkbenchPartReference partRef) {
			}
			
			@Override
			public void partDeactivated(IWorkbenchPartReference partRef) {
			}
			
			@Override
			public void partClosed(IWorkbenchPartReference partRef) {
				IWorkbenchPart part = partRef.getPart(false);
				IPlottingSystem system = PartUtils.getPlottingSystem(part);
				if (system != null) {
					removePlottingSystem(system);
				}
			}
			
			@Override
			public void partBroughtToTop(IWorkbenchPartReference partRef) {
			}
			
			@Override
			public void partActivated(IWorkbenchPartReference partRef) {
				IWorkbenchPart part = partRef.getPart(false);
				System.err.println("part activated: " + part.getTitle());
			}
		};
		getSite().getPage().addPartListener(listener);

		// make entire view a scrolled composite
		sComp = new ScrolledComposite(parent, SWT.V_SCROLL);

		Composite sHolder = new Composite(sComp, SWT.NONE);
		RowLayout rl = new RowLayout(SWT.VERTICAL);
		rl.fill = true;
		sHolder.setLayout(rl);

		Composite gHolder = new Composite(sHolder, SWT.NONE);
		gHolder.setLayout(new GridLayout(4, false));

		// create calibrant combo
		Label l = new Label(gHolder, SWT.NONE);
		l.setText("Calibrant");
		l.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		final Combo calibrant = new Combo(gHolder, SWT.READ_ONLY);
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
//		calibrant.setText("Please select a calibrant..."); // won't work with read-only
		calibrant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));

		final Display display = parent.getDisplay();

		// create motion buttons cluster
		l = new Label(gHolder, SWT.NONE);
		l = new Label(gHolder, SWT.NONE);
		Button b = new Button(gHolder, SWT.ARROW | SWT.UP);
		b.setToolTipText("Move rings up");
		b.addMouseListener(new RepeatingMouseAdapter(display, new SlowFastRunnable() {
			@Override
			public void run() {
				DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.UP, isFast());
			}
		}));
		b.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, false, false));
		l = new Label(gHolder, SWT.NONE);

		l = new Label(gHolder, SWT.NONE);
		b = new Button(gHolder, SWT.ARROW | SWT.LEFT);
		b.setToolTipText("Shift rings left");
		b.addMouseListener(new RepeatingMouseAdapter(display, new SlowFastRunnable() {
			@Override
			public void run() {
				DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.LEFT, isFast());
			}
		}));
		b.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		l = new Label(gHolder, SWT.NONE);
		b = new Button(gHolder, SWT.ARROW | SWT.RIGHT);
		b.setToolTipText("Shift rings right");
		b.addMouseListener(new RepeatingMouseAdapter(display, new SlowFastRunnable() {
			@Override
			public void run() {
				DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.RIGHT, isFast());
			}
		}));
		b.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		l = new Label(gHolder, SWT.NONE);
		l = new Label(gHolder, SWT.NONE);
		b = new Button(gHolder, SWT.ARROW | SWT.DOWN);
		b.setToolTipText("Move rings down");
		b.addMouseListener(new RepeatingMouseAdapter(display, new SlowFastRunnable() {
			@Override
			public void run() {
				DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.DOWN, isFast());
			}
		}));
		b.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));
		l = new Label(gHolder, SWT.NONE);

		l = new Label(gHolder, SWT.NONE);
		l = new Label(gHolder, SWT.NONE);
		l = new Label(gHolder, SWT.NONE);
		l = new Label(gHolder, SWT.NONE);

		l = new Label(gHolder, SWT.NONE);
		b = new Button(gHolder, SWT.PUSH);
		b.setText("-");
		b.setToolTipText("Make rings smaller");
		b.addMouseListener(new RepeatingMouseAdapter(display, new SlowFastRunnable() {
			@Override
			public void run() {
				DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.SHRINK, isFast());
			}

			@Override
			public void stop() {
				refreshTable();
			}
		}));
		b.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		l = new Label(gHolder, SWT.NONE);
		b = new Button(gHolder, SWT.PUSH);
		b.setText("+");
		b.setToolTipText("Make rings larger");
		b.addMouseListener(new RepeatingMouseAdapter(display, new SlowFastRunnable() {
			@Override
			public void run() {
				DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.ENLARGE, isFast());
			}

			@Override
			public void stop() {
				refreshTable();
			}
		}));
		b.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

		l = new Label(gHolder, SWT.NONE);
		b = new Button(gHolder, SWT.PUSH);
		b.setText("Squash");
		b.setToolTipText("Make rings more circular");
		b.addMouseListener(new RepeatingMouseAdapter(display, new SlowFastRunnable() {
			@Override
			public void run() {
				DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.SQUASH, isFast());
			}

			@Override
			public void stop() {
				refreshTable();
			}
		}));
		b.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		l = new Label(gHolder, SWT.NONE);
		b = new Button(gHolder, SWT.PUSH);
		b.setText("Elongate");
		b.setToolTipText("Make rings more elliptical");
		b.addMouseListener(new RepeatingMouseAdapter(display, new SlowFastRunnable() {
			@Override
			public void run() {
				DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.ELONGATE, isFast());
			}

			@Override
			public void stop() {
				refreshTable();
			}
		}));
		b.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

		l = new Label(gHolder, SWT.NONE);
		b = new Button(gHolder, SWT.PUSH);
		b.setImage(Activator.getImage("icons/arrow_rotate_anticlockwise.png"));
		b.setToolTipText("Rotate rings anti-clockwise");
		b.addMouseListener(new RepeatingMouseAdapter(display, new SlowFastRunnable() {
			@Override
			public void run() {
				DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.ANTICLOCKWISE, isFast());
			}

			@Override
			public void stop() {
				refreshTable();
			}
		}));
		b.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		l = new Label(gHolder, SWT.NONE);
		b = new Button(gHolder, SWT.PUSH);
		b.setImage(Activator.getImage("icons/arrow_rotate_clockwise.png"));
		b.setToolTipText("Rotate rings clockwise");
		b.addMouseListener(new RepeatingMouseAdapter(display, new SlowFastRunnable() {
			@Override
			public void run() {
				DiffractionCalibrationUtils.changeRings(currentData, ManipulateMode.CLOCKWISE, isFast());
			}

			@Override
			public void stop() {
				refreshTable();
			}
		}));
		b.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

		l = new Label(gHolder, SWT.NONE);
		l = new Label(gHolder, SWT.NONE);
		l = new Label(gHolder, SWT.NONE);
		l = new Label(gHolder, SWT.NONE);

		l = new Label(gHolder, SWT.NONE);
		b = new Button(gHolder, SWT.PUSH);
		b.setText("Find rings in image");
		b.setToolTipText("Use pixel values to find rings in image near calibration rings");
		b.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Job findRingsJob = DiffractionCalibrationUtils.findRings(display, currentSystem, currentData);
				if(findRingsJob == null) return;
				findRingsJob.addJobChangeListener(new JobChangeAdapter(){
					@Override
					public void done(IJobChangeEvent event){
					display.asyncExec(new Runnable() {
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

		l = new Label(gHolder, SWT.NONE);
		l = new Label(gHolder, SWT.NONE);
		l = new Label(gHolder, SWT.NONE);
		l = new Label(gHolder, SWT.NONE);

//		gHolder.setSize(gHolder.computeSize(SWT.DEFAULT, SWT.DEFAULT));
//		gHolder.layout();

		// table of images and found rings
		tableViewer = new TableViewer(sHolder, SWT.NONE);
//		tableViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true, 5, 5));
		createColumns(tableViewer);
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.setContentProvider(new MyContentProvider());
		tableViewer.setLabelProvider(new MyLabelProvider());
		tableViewer.setInput(model);
		tableViewer.refresh();

		calibrateImages = new Button(sHolder, SWT.PUSH);
		calibrateImages.setText("Calibrate chosen images");
		calibrateImages.setToolTipText("Calibrate detector in chosen images");
		calibrateImages.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DiffractionCalibrationUtils.calibrateImages(display, currentSystem, model, currentData);
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						refreshTable();
						setCalibrateButtons();
					}
				});
			}
		});
		calibrateImages.setEnabled(false);

		calibrateWD = new Button(sHolder, SWT.PUSH);
		calibrateWD.setText("Calibrate wavelength");
		calibrateWD.setToolTipText("Calibrate wavelength from images chosen in table");
		calibrateWD.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DiffractionCalibrationUtils.calibrateWavelength(display, model, currentData);
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						refreshTable();
					}
				});
			}
		});
		calibrateWD.setEnabled(false);

		//		sHolder.setSize(sHolder.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		sHolder.layout();
		sComp.setContent(sHolder);
		sComp.setExpandHorizontal(true);
		sComp.setExpandVertical(true);
		sComp.setMinSize(sHolder.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		sComp.layout();
	}

	class DiffractionTraceListener extends ITraceListener.Stub {
		private DiffractionTableData data;
		
		public DiffractionTraceListener(DiffractionTableData mydata) {
			setData(mydata);
		}

		public void setData(DiffractionTableData mydata) {
			data = mydata;
		}

		@Override
		public void traceRemoved(TraceEvent evt) {
			data.path = null;
		}

		@Override
		protected void update(TraceEvent evt) {
			if (data == null || data.system == null)
				return;

			IImageTrace image = DiffractionCalibrationUtils.getImageTrace(data.system);
			if (image == null)
				return;

			System.err.println("We have an image, Houston!");

			DiffractionImageAugmenter aug = data.augmenter;
			if (aug == null) {
				aug = new DiffractionImageAugmenter(data.system);
				data.augmenter = aug;
			}
			aug.activate();
			if (data.path == null) {
				String path = getPathFromTrace(image);
				data.path = path;
				data.name = path.substring(path.lastIndexOf(File.separatorChar) + 1);
				data.md = DiffractionTool.getDiffractionMetadata(image.getData(), data.path, service, null);
			}
			if (data.md == null)
				data.md = DiffractionTool.getDiffractionMetadata(image.getData(), data.path, service, null);

			aug.setDiffractionMetadata(data.md);
			refreshTable();
			DiffractionCalibrationUtils.drawCalibrantRings(currentData.augmenter);
		}
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

	private static final Image TICK = Activator.getImageDescriptor("icons/tick.png").createImage();

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
				return TICK;
			return null;
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

		public MyEditingSupport(TableViewer viewer) {
			super(viewer);
			tv = viewer;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new CheckboxCellEditor(null, SWT.CHECK);
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			DiffractionTableData data = (DiffractionTableData) element;
			return data.use;
		}

		@Override
		protected void setValue(Object element, Object value) {
			DiffractionTableData data = (DiffractionTableData) element;
			data.use = (Boolean) value;
			tv.refresh();

			setCalibrateButtons();
		}
		
	}

	private void createColumns(TableViewer tv) {
		TableViewerColumn tvc = new TableViewerColumn(tv, SWT.NONE);
		tvc.setEditingSupport(new MyEditingSupport(tv));
		TableColumn tc = tvc.getColumn();
		tc.setText("Use");
		tc.setWidth(40);

		String[] headings = { "Image", "# of rings", "Distance", "Wavelength" };
		for (String h : headings) {
			tvc = new TableViewerColumn(tv, SWT.NONE);
			tc = tvc.getColumn();
			tc.setText(h);
			tc.setWidth(60);
		}
	}

	private String getPathFromTrace(IImageTrace trace) {
		String path = null;
		if (trace != null) {
			path = trace.getData().getName();
			if (!new File(path).canRead()) {
				int i = path.lastIndexOf(AbstractFileLoader.FILEPATH_DATASET_SEPARATOR);
				path = i < 0 ? null : path.substring(0, i);
			}
		}
		return path;
	}

	private void setData(String path, IPlottingSystem system) {
		if (path == null) { // cope with PlotView
			path = getPathFromTrace(DiffractionCalibrationUtils.getImageTrace(system));
		}

		DiffractionTableData data = null;
		if (path != null) {
			for (DiffractionTableData d : model) {
				if (path.equals(d.path)) {
					data = d;
					break;
				}
			}
		}
		if (system == currentSystem && data == currentData)
			return;

		if (currentSystem != null && currentData != null) {
			DiffractionImageAugmenter aug = currentData.augmenter;
			if (aug != null)
				aug.deactivate();
		}
		currentSystem = system;
		if (currentSystem == null)
			return;

		if (data == null) {
			data = new DiffractionTableData();
			model.add(data);
			if (path != null && new File(path).canRead()) {
				data.path = path;
				data.name = path.substring(path.lastIndexOf(File.separatorChar) + 1);
			}
		}
		currentData = data;
		data.system = currentSystem;

		DiffractionTraceListener listener = data.listener;
		if (listener == null) {
			listener = listeners.get(currentSystem);
			if (listener == null) {
				listener = new DiffractionTraceListener(data);
				data.listener = listener;
				currentSystem.addTraceListener(listener);
				listeners.put(currentSystem, listener);
			}
		} else {
			listener.setData(data);
		}

		if (data.augmenter != null) {
			data.augmenter.activate();
			DiffractionCalibrationUtils.drawCalibrantRings(currentData.augmenter);
		}
		refreshTable();
		// highlight current image
		tableViewer.setSelection(new StructuredSelection(data), true);
	}

	private void refreshTable() {
		tableViewer.refresh();
		
//		for (TableColumn c : tableViewer.getTable().getColumns()) {
//			c.pack();
//		}
//		tableViewer.getControl().getParent().layout();
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

	private void removePlottingSystem(IPlottingSystem system) {
		DiffractionTraceListener listener = listeners.remove(system);
		if (listener != null)
			system.removeTraceListener(listener);

		for (DiffractionTableData d : model) {
			if (d.system == system) {
				d.system = null;
				d.augmenter = null;
			}
		}
		refreshTable();
	}

	@Override
	public void dispose() {
		super.dispose();
		getSite().getPage().removePartListener(listener);
		Iterator<DiffractionTableData> it = model.iterator();
		while(it.hasNext()){
			DiffractionTableData d = it.next();
			model.remove(d);
			if(d.augmenter != null) d.augmenter.deactivate();
		}
	}

	@Override
	public void setFocus() {
		sComp.getContent().setFocus();
		// this should be done with a listener...
		tableViewer.refresh();
		DiffractionCalibrationUtils.drawCalibrantRings(currentData.augmenter);
	}
}


class SlowFastRunnable implements Runnable {
	private boolean fast = false;

	public void setFast(boolean fast) {
		this.fast = fast;
	}

	public boolean isFast() {
		return fast;
	}

	@Override
	public void run() {
	}

	public void stop() {
	}
}

class RepeatingMouseAdapter extends MouseAdapter {
	private final EventRepeater repeater;

	public RepeatingMouseAdapter(Display display, SlowFastRunnable task) {
		repeater = new EventRepeater(display, task);
	}

	@Override
	public void mouseDown(MouseEvent e) {
		repeater.setStarter(e);
		repeater.run();
	}

	@Override
	public void mouseUp(MouseEvent e) {
		repeater.stop();
	}
}

class EventRepeater implements Runnable {
	boolean stop;
	Display display;
	static int slow = 200;
	static int mid = 40;
	static int fast = 8;
	static int[] threshold = {4, 8};
	int[] count;
	private SlowFastRunnable task;

	public EventRepeater(Display display, SlowFastRunnable task) {
		this.display = display;
		this.task = task;
		stop = true;
	}

	MouseEvent first;
	public void setStarter(MouseEvent me) {
		first = me;
		stop = false;
		count = threshold.clone();
		task.setFast(false);
	}

	@Override
	public void run() {
		if (!stop) {
			task.run();
//			System.out.printf(".");
			if (count[0] >= 0) {
				count[0]--;
				display.timerExec(slow, this);
			} else if (count[1] >= 0) {
				count[1]--;
				display.timerExec(mid, this);
			} else {
				task.setFast(true);
				display.timerExec(fast, this);
			}
		}
	}

	public void stop() {
		stop = true;
		task.stop();
	}
}
