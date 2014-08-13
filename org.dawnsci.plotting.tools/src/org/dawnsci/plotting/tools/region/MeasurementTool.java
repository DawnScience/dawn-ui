package org.dawnsci.plotting.tools.region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.wizard.persistence.PersistenceExportWizard;
import org.dawb.common.ui.wizard.persistence.PersistenceImportWizard;
import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.preference.RegionEditorConstants;
import org.dawnsci.plotting.tools.preference.RegionEditorPreferencePage;
import org.dawnsci.plotting.tools.region.MeasurementLabelProvider.LabelType;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.progress.UIJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.Dataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetFactory;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.roi.IROI;

/**
 * This tool shows the measurements of selected regions.
 * 
 * @author fcp94556
 *
 */
public class MeasurementTool extends AbstractToolPage implements IRegionListener, IROIListener {

	protected IROI roi;
	private ITraceListener axesTraceListener;
	private double xCalibratedAxisFactor=Double.NaN;
	private double yCalibratedAxisFactor=Double.NaN;
	private String unitName;

	protected static final Logger logger = LoggerFactory.getLogger(MeasurementTool.class);
	
	private   Composite     composite;
	protected TableViewer   viewer;

	private RegionColorListener viewUpdateListener;
	
	/**
	 * A map to store dragBounds which are not the official bounds
	 * of the selection until the user lets go.
	 */
	private Map<String,IROI> dragBounds;
	private ITraceListener traceListener;

	public MeasurementTool() {
		super();
		dragBounds = new HashMap<String,IROI>(7);

		Activator.getPlottingPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (isActive()) {
					if(isInterestedProperty(event))
						viewer.refresh();
				}
 			}

			private boolean isInterestedProperty(PropertyChangeEvent event) {
				final String propName = event.getProperty();
				return RegionEditorConstants.POINT_FORMAT.equals(propName) ||
						RegionEditorConstants.INTENSITY_FORMAT.equals(propName) ||
						RegionEditorConstants.SUM_FORMAT.equals(propName);
			}
		});

		traceListener = new ITraceListener.Stub() {
			@Override
			public void traceUpdated(TraceEvent evt) {
				if (viewer != null && viewer.getControl().isDisposed())
					return;
				viewer.refresh();
			}
			
			@Override
			public void traceRemoved(TraceEvent evt) {
				if (viewer != null && viewer.getControl().isDisposed())
					return;
				viewer.refresh();
			}
			
			@Override
			public void traceCreated(TraceEvent evt) {
				if (viewer != null && viewer.getControl().isDisposed())
					return;
				viewer.refresh();
			}
			
			@Override
			public void traceAdded(TraceEvent evt) {
				if (viewer != null && viewer.getControl().isDisposed())
					return;
				viewer.refresh();
			}
		};
	}

	@Override
	public void createControl(Composite parent) {
		
		this.composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());

		viewer = new TableViewer(composite, SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		createColumns(viewer);
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		createActions();
		getSite().setSelectionProvider(viewer);
		
		viewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// TODO Auto-generated method stub
			}			
			@Override
			public void dispose() {
				// TODO Auto-generated method stub
			}		
			@Override
			public Object[] getElements(Object inputElement) {
				
				final Collection<IRegion> regions = getPlottingSystem().getRegions();
				if (regions==null || regions.isEmpty()) return new Object[]{"-"};
				
				final List<IRegion> okRegions = new ArrayList<IRegion>();
				for (IRegion iRegion : regions) {
					if (isRegionOk(iRegion)) okRegions.add(iRegion);
				}
				
				return okRegions.toArray(new IRegion[okRegions.size()]);
			}
		});
		viewer.setInput(new Object());
		this.viewUpdateListener = new RegionColorListener();
		activate();
	}
	
	protected boolean isRegionOk(IRegion iRegion) {
		return iRegion.isVisible() && iRegion.isUserRegion();
	}

	protected IAction getReselectAction() {
		final Action reselect = new Action("Create new "+getRegionTypeName(), getImageDescriptor()) {
			public void run() {
				createNewRegion(true);
			}
		};
		return reselect;
	}

	protected void createActions() {
		getSite().getActionBars().getToolBarManager().add(getReselectAction());
		getSite().getActionBars().getToolBarManager().add(new Separator());

		if (getToolPageRole()==ToolPageRole.ROLE_2D) {
			final Action calibrate = new Action("Calibrate axes using a measurement and apply these axes to other plots.\nThese axes can then be applied to other plots by keeping the\nmeasurement tool open using 'open in a dedicated view'.", IAction.AS_PUSH_BUTTON) {
				public void run() {
					MeasurementCalibrationDialog dialog = new MeasurementCalibrationDialog(MeasurementTool.this);
					dialog.open();
				}
			};
			calibrate.setImageDescriptor(Activator.getImageDescriptor("icons/measurement_calibrate.png"));
			
			final Action applyCalibrated = new Action("Apply calibrated axes to any images opened while this tool is active.", IAction.AS_CHECK_BOX) {
				public void run() {
					updateCalibrateTraceListener(isChecked());
				}
			};
			applyCalibrated.setChecked(true);
			applyCalibrated.setImageDescriptor(Activator.getImageDescriptor("icons/axis.png"));
			updateCalibrateTraceListener(true);
			
			
			getSite().getActionBars().getToolBarManager().add(calibrate);
			getSite().getActionBars().getToolBarManager().add(applyCalibrated);
			getSite().getActionBars().getToolBarManager().add(new Separator());
		}

		final Action exportRegion = new Action("Export region to file", Activator.getImageDescriptor("icons/mask-export-wiz.png")) {
			public void run() {
				try {
					IWizard wiz = EclipseUtils.openWizard(PersistenceExportWizard.ID, false);
					WizardDialog wd = new  WizardDialog(Display.getCurrent().getActiveShell(), wiz);
					wd.setTitle(wiz.getWindowTitle());
					wd.open();
				} catch (Exception e) {
					logger.error("Problem opening import!", e);
				}
			}
		};

		final Action importRegion = new Action("Import region from file", Activator.getImageDescriptor("icons/mask-import-wiz.png")) {
			public void run() {
				try {
					IWizard wiz = EclipseUtils.openWizard(PersistenceImportWizard.ID, false);
					WizardDialog wd = new  WizardDialog(Display.getCurrent().getActiveShell(), wiz);
					wd.setTitle(wiz.getWindowTitle());
					wd.open();
				} catch (Exception e) {
					logger.error("Problem opening import!", e);
				}
			}			
		};

		final Action copy = new Action("Copy region values to clipboard", Activator.getImageDescriptor("icons/plot-tool-measure-copy.png")) {
			public void run() {
				if (!isActive()) return;
				final IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
				if (!(sel.getFirstElement() instanceof IRegion)) return;
				if (sel!=null && sel.getFirstElement()!=null) {
					final IRegion region = (IRegion)sel.getFirstElement();
					if (region==null||region.getROI()==null) return;
					final IROI bounds = region.getROI();
					if (bounds.getPointRef()==null) return;
					
					final Clipboard cb = new Clipboard(composite.getDisplay());
					TextTransfer textTransfer = TextTransfer.getInstance();
					cb.setContents(new Object[]{region.getName()+"  "+bounds}, new Transfer[]{textTransfer});
				}
			}
		};
		copy.setToolTipText("Copies the region values as text to clipboard which can then be pasted externally.");

		final Action delete = new Action("Delete selected region", Activator.getImageDescriptor("icons/RegionDelete.png")) {
			public void run() {
				if (!isActive()) return;
				final IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
				if (!(sel.getFirstElement() instanceof IRegion)) return;
				if (sel!=null && sel.getFirstElement()!=null) {
					final IRegion region = (IRegion)sel.getFirstElement();
					getPlottingSystem().removeRegion(region);
				}
			}
		};
		delete.setToolTipText("Delete selected region, if there is one.");

		final Action show = new Action("Show all vertex values", Activator.getImageDescriptor("icons/plot-tool-measure-vertices.png")) {
			public void run() {
				if (!isActive()) return;
				final Object[] oa = ((IStructuredContentProvider)viewer.getContentProvider()).getElements(null);
				for (Object object : oa) {
					if (object instanceof IRegion) ((IRegion)object).setShowPosition(true);
				}
			}
		};
		show.setToolTipText("Show vertices in all visible regions");

		final Action clear = new Action("Show no vertex values", Activator.getImageDescriptor("icons/plot-tool-measure-clear.png")) {
			public void run() {
				if (!isActive()) return;
				final Object[] oa = ((IStructuredContentProvider)viewer.getContentProvider()).getElements(null);
				for (Object object : oa) {
					if (object instanceof IRegion) ((IRegion)object).setShowPosition(false);
				}
			}
		};
		clear.setToolTipText("Clear all vertices shown in the plotting");

		final Action preferences = new Action("Preferences...") {
			public void run() {
				if (!isActive()) return;
				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), RegionEditorPreferencePage.ID, null, null);
				if (pref != null) pref.open();
			}
		};
		preferences.setToolTipText("Open Region Editor preferences");

		getSite().getActionBars().getToolBarManager().add(importRegion);
		getSite().getActionBars().getToolBarManager().add(exportRegion);
		getSite().getActionBars().getToolBarManager().add(new Separator());
		getSite().getActionBars().getToolBarManager().add(copy);
		getSite().getActionBars().getMenuManager().add(copy);
		getSite().getActionBars().getToolBarManager().add(delete);
		getSite().getActionBars().getMenuManager().add(delete);
		final Separator sep = new Separator(getClass().getName()+".separator1");
		getSite().getActionBars().getToolBarManager().add(sep);
		getSite().getActionBars().getMenuManager().add(sep);
		getSite().getActionBars().getToolBarManager().add(show);
		getSite().getActionBars().getMenuManager().add(show);
		getSite().getActionBars().getToolBarManager().add(clear);
		getSite().getActionBars().getMenuManager().add(clear);
		getSite().getActionBars().getMenuManager().add(preferences);
		createRightClickMenu();
	}

	/**
	 * Add or remove a trace listener which applies the calibrated images
	 * @param b
	 */
	private void updateCalibrateTraceListener(boolean addListener) {
		if (addListener) {
			axesTraceListener = new ITraceListener.Stub() {
				@Override
				public void tracesUpdated(TraceEvent evt) {
					updateAxes(evt);
				}
				public void traceAdded(TraceEvent evt) {
					updateAxes(evt);
				}
				protected void updateAxes(TraceEvent evt) {
					applyCalibration();
				}			
			};
			getPlottingSystem().addTraceListener(axesTraceListener);
			applyCalibration();

		} else {
			getPlottingSystem().removeTraceListener(axesTraceListener);
			axesTraceListener = null;
			final IImageTrace image = getImageTrace();
			if (image!=null) image.setAxes(null, true);
		}
		getPlottingSystem().repaint();
	}

	private void applyCalibration() {
		final IImageTrace trace = getImageTrace();
		if (trace!=null && !Double.isNaN(xCalibratedAxisFactor) && !Double.isNaN(yCalibratedAxisFactor)
				        && xCalibratedAxisFactor>0              && yCalibratedAxisFactor>0) {
			final IDataset data = trace.getData();
			trace.setAxes(Arrays.asList(getCalibratedAxis(xCalibratedAxisFactor, data.getShape()[1]), 
					                    getCalibratedAxis(yCalibratedAxisFactor, data.getShape()[0])), 
					                    true);
		}
	}

	private IDataset getCalibratedAxis(double factor, int size) {
		Dataset axis = DatasetFactory.createRange(size, Dataset.FLOAT64);
		axis.imultiply(factor);
		axis.setName(unitName);
		return axis;
	}

	private void createRightClickMenu() {
		final MenuManager menuManager = new MenuManager();
		for (IContributionItem item : getSite().getActionBars()
				.getMenuManager().getItems())
			menuManager.add(item);
		viewer.getControl().setMenu(
				menuManager.createContextMenu(viewer.getControl()));
	}

	private void createColumns(final TableViewer viewer) {
		ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);

		TableViewerColumn var = new TableViewerColumn(viewer, SWT.LEFT, 0);
		var.getColumn().setText("Name");
		var.getColumn().setWidth(120);
		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.ROINAME));

		var = new TableViewerColumn(viewer, SWT.CENTER, 1);
		var.getColumn().setText("Region Type");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.ROITYPE));

		var = new TableViewerColumn(viewer, SWT.LEFT, 2);
		var.getColumn().setText("dx");
		var.getColumn().setWidth(80);
		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.DX));

		var = new TableViewerColumn(viewer, SWT.LEFT, 3);
		var.getColumn().setText("dy");
		var.getColumn().setWidth(80);
		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.DY));

		var = new TableViewerColumn(viewer, SWT.LEFT, 4);
		var.getColumn().setText("length");
		var.getColumn().setWidth(80);
		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.LENGTH));

		var = new TableViewerColumn(viewer, SWT.LEFT, 5);
		var.getColumn().setText("Coordinates");
		var.getColumn().setWidth(500);
		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.ROISTRING));
	}

	@Override
	public void activate() {
		super.activate();
		if (viewer!=null && viewer.getControl().isDisposed()) return;
		
		getPlottingSystem().addTraceListener(traceListener);

		if (viewUpdateListener!=null) viewer.addSelectionChangedListener(viewUpdateListener);

		try {
			try {
				getPlottingSystem().addRegionListener(this);
				final Collection<IRegion> regions = getPlottingSystem().getRegions();
				for (IRegion iRegion : regions)
					iRegion.addROIListener(this);
				
				if (!isDedicatedView()) {
					createNewRegion(false);
				}
			} catch (Exception e) {
				logger.error("Cannot add region listeners!", e);
			}		
			
			if (viewer!=null) {
				try {
					viewer.refresh();
				} catch (Throwable ignored) {
					// Can happen when model invalid.
				}
			}
			
		} catch (Exception e) {
			logger.error("Cannot put the selection into fitting region mode!", e);
		}
		if (getPlottingSystem()!=null && axesTraceListener!=null) try {
			getPlottingSystem().addTraceListener(axesTraceListener);
		} catch (Exception e) {
			logger.error("Cannot add trace listener!", e);
		}
	}

	@Override
	public void deactivate() {
		super.deactivate();

		if (viewer != null && !viewer.getControl().isDisposed() && viewUpdateListener!=null) {
			viewer.removeSelectionChangedListener(viewUpdateListener);
			viewUpdateListener.resetSelectionColor();
		}
		if (dragBounds!=null) dragBounds.clear();
		if (getPlottingSystem()!=null) try {
			getPlottingSystem().removeTraceListener(traceListener);
			getPlottingSystem().removeRegionListener(this);
			final Collection<IRegion> regions = getPlottingSystem().getRegions();
			if (regions!=null) for (IRegion iRegion : regions) iRegion.removeROIListener(this);
		} catch (Exception e) {
			logger.error("Cannot remove region listeners!", e);
		}
		if (getPlottingSystem()!=null && axesTraceListener!=null) try {
			getPlottingSystem().removeTraceListener(axesTraceListener);
		} catch (Exception e) {
			logger.error("Cannot remove trace listener!", e);
		}
	}

	private void createNewRegion(boolean force) {
		try {
			if (!force) {
				// We check to see if the region type preferred is already there
				final Collection<IRegion> regions = getPlottingSystem().getRegions();
				for (IRegion iRegion : regions) {
					if (iRegion.isUserRegion() && iRegion.isVisible()) {
						// We have one already, do not go into create mode :)
						if (iRegion.getRegionType() == IRegion.RegionType.LINE) return;
					}
				}
			}
			getPlottingSystem().createRegion(RegionUtils.getUniqueName("Measurement", getPlottingSystem()), IRegion.RegionType.LINE);
		} catch (Exception e) {
			logger.error("Cannot create line region for selecting in measurement tool!", e);
		}
	}

	@Override
	public void setFocus() {
        if (viewer!=null && !viewer.getControl().isDisposed()) viewer.getControl().setFocus();
	}
	
	public void dispose() {
		super.dispose();
	}


	@Override
	public Control getControl() {
		return composite;
	}

	@Override
	public void regionCreated(RegionEvent evt) {
		IRegion region = evt.getRegion();
		region.setAlpha(51); // 20%
	}
	@Override
	public void regionCancelled(RegionEvent evt) {
	}

	@Override
	public void regionNameChanged(RegionEvent evt, String oldName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void regionAdded(RegionEvent evt) {
		if (!isActive()) return;
		IRegion region = evt.getRegion();
		if (region != null && region.getROI() != null) {
			region.addROIListener(this);
			region.getROI().setPlot(true);
			// set the Region isActive flag
			region.setActive(true);
		}
		if (viewer!=null) viewer.refresh();
	}

	@Override
	public void regionRemoved(RegionEvent evt) {
		if (!isActive()) return;
		if (viewer!=null) viewer.refresh();
		if (evt.getRegion()!=null) {
			evt.getRegion().removeROIListener(this);
		}
	}
	@Override
	public void regionsRemoved(RegionEvent evt) {
		if (!isActive()) return;
		if (viewer!=null) viewer.refresh();
	}
	
	@Override
	public void roiDragged(ROIEvent evt) {
		viewer.cancelEditing();
		if (!isActive()) return;
		updateRegion(evt);
	}

	@Override
	public void roiChanged(ROIEvent evt) {
		if (!isActive()) return;
		updateRegion(evt);
		if((IRegion)evt.getSource() == null) return;
		updateColorSelection((IRegion)evt.getSource());
	}
	@Override
	public void roiSelected(ROIEvent evt) {

	}

	private void updateColorSelection(IRegion region){
		Collection<IRegion> regions = getPlottingSystem().getRegions();
		for (IRegion iRegion : regions) {
			if(region.getName().equals(iRegion.getName())){
				iRegion.setRegionColor(ColorConstants.red);
			} else {
				if(iRegion.isActive()) iRegion.setRegionColor(ColorConstants.green);
				else if (!iRegion.isActive()) iRegion.setRegionColor(ColorConstants.gray);
			}
		}
		TableItem[] regionItems = viewer.getTable().getItems();
		for (TableItem tableItem : regionItems) {
			IRegion myRegion = (IRegion)tableItem.getData();
			if(region.getName().equals(myRegion.getName())){
				viewer.getTable().setSelection(tableItem);
				break;
			}
		}
	}

	private RegionBoundsUIJob updateJob;
	/**
	 * Uses cancellable UIJob
	 * 
	 * @param evt
	 */
	private void updateRegion(final ROIEvent evt) {
		if(viewer == null) return;
		if(viewer.isCellEditorActive()) return; 
		if (updateJob==null) {
			updateJob = new RegionBoundsUIJob();
			updateJob.setPriority(UIJob.INTERACTIVE);
			//updateJob.setUser(false);
		}
		updateJob.setEvent(evt);
		updateJob.cancel();
		updateJob.schedule();
	}
	
	private final class RegionBoundsUIJob extends UIJob {
		
		private ROIEvent evt;
		RegionBoundsUIJob() {
			super("Measurement update");
		}
		
		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (viewer!=null) {
				if(monitor.isCanceled())	return Status.CANCEL_STATUS;

				IRegion  region = (IRegion)evt.getSource();
				IROI rb = evt.getROI();

				if(monitor.isCanceled())	return Status.CANCEL_STATUS;
				dragBounds.put(region.getName(), rb);

				if(monitor.isCanceled())	return Status.CANCEL_STATUS;
				viewer.refresh(region);
			}
			return Status.OK_STATUS;
		}
		
		void setEvent(ROIEvent evt) {
			this.evt = evt;
		}
	};

	public IROI getROI(IRegion region) {
		if (dragBounds!=null&&dragBounds.containsKey(region.getName()))
			return dragBounds.get(region.getName());
		return region.getROI();
	}

	/**
	 * get point in axis coords
	 * @param coords
	 * @return
	 */
	public double[] getAxisPoint(ICoordinateSystem coords, double... vals) {
		if (coords==null) return vals;
		try {
			return coords.getValueAxisLocation(vals);
		} catch (Exception e) {
			return vals;
		}
	}

	/**
	 * get point in image coords
	 * @param coords
	 * @return
	 */
	public double[] getImagePoint(ICoordinateSystem coords, double... vals) {
		if (coords==null) return vals;
		try {
			return coords.getAxisLocationValue(vals);
		} catch (Exception e) {
			return vals;
		}
	}

	public IROI getRoi() {
		return roi;
	}

	public void setRoi(IROI roi) {
		this.roi = roi;
	}

	@Override
	public ToolPageRole getToolPageRole() {
		if (getToolId()!=null) {
			if (getToolId().endsWith("1d")) return ToolPageRole.ROLE_1D;
			if (getToolId().endsWith("2d")) return ToolPageRole.ROLE_2D;
		}
		return ToolPageRole.ROLE_1D;
	}
	
	@Override
	public boolean isStaticTool() {
		return getToolPageRole()==ToolPageRole.ROLE_2D;
	}

	private String getRegionTypeName() {
		return "measurement";
	}

	public IToolPage cloneTool() throws Exception {
		IToolPage tp = super.cloneTool();
		((MeasurementTool)tp).xCalibratedAxisFactor = xCalibratedAxisFactor;
		((MeasurementTool)tp).yCalibratedAxisFactor = yCalibratedAxisFactor;
		
		return tp;
	}

	/**
	 * must be two axes in array.
	 * @param axes
	 */
	public void setCalibratedAxes(String unitName, double... axes) {
		this.unitName = unitName;
		if (axes==null || axes.length!=2) {
			xCalibratedAxisFactor=Double.NaN;
			yCalibratedAxisFactor=Double.NaN;
			return;
		}
		xCalibratedAxisFactor = axes[0];
		yCalibratedAxisFactor = axes[1];
		viewer.refresh();
	}

	public double getxCalibratedAxisFactor() {
		return xCalibratedAxisFactor;
	}

	public void setxCalibratedAxisFactor(double xCalibratedAxisFactor) {
		this.xCalibratedAxisFactor = xCalibratedAxisFactor;
	}

	public double getyCalibratedAxisFactor() {
		return yCalibratedAxisFactor;
	}

	public void setyCalibratedAxisFactor(double yCalibratedAxisFactor) {
		this.yCalibratedAxisFactor = yCalibratedAxisFactor;
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}
}
