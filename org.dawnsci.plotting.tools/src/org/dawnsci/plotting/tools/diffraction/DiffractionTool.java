/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.diffraction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.TreeNode;

import org.dawb.common.ui.menu.CheckableActionGroup;
import org.dawb.common.ui.menu.MenuAction;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.ui.viewers.TreeNodeContentProvider;
import org.dawb.common.ui.wizard.persistence.PersistenceExportWizard;
import org.dawb.common.ui.wizard.persistence.PersistenceImportWizard;
import org.dawnsci.common.widgets.tree.ClearableFilteredTree;
import org.dawnsci.common.widgets.tree.DelegatingProviderWithTooltip;
import org.dawnsci.common.widgets.tree.IResettableExpansion;
import org.dawnsci.common.widgets.tree.NodeFilter;
import org.dawnsci.common.widgets.tree.NodeLabelProvider;
import org.dawnsci.common.widgets.tree.NumericNode;
import org.dawnsci.common.widgets.tree.UnitEditingSupport;
import org.dawnsci.common.widgets.tree.ValueEditingSupport;
import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.preference.DiffractionDefaultsPreferencePage;
import org.dawnsci.plotting.tools.preference.detector.DiffractionDetectorPreferencePage;
import org.dawnsci.plotting.tools.preference.diffraction.DiffractionPreferencePage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.diffraction.DetectorProperties;
import org.eclipse.dawnsci.analysis.api.diffraction.DetectorPropertyEvent;
import org.eclipse.dawnsci.analysis.api.diffraction.DiffractionCrystalEnvironment;
import org.eclipse.dawnsci.analysis.api.diffraction.DiffractionCrystalEnvironmentEvent;
import org.eclipse.dawnsci.analysis.api.diffraction.IDetectorPropertyListener;
import org.eclipse.dawnsci.analysis.api.diffraction.IDiffractionCrystalEnvironmentListener;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IPeak;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularFitROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.EllipticalFitROI;
import org.eclipse.dawnsci.analysis.dataset.roi.EllipticalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolylineROI;
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.ProgressMonitorWrapper;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.january.dataset.BooleanDataset;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.metadata.IMetadata;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.osgi.services.ServiceProvider;
import uk.ac.diamond.scisoft.analysis.crystallography.CalibrantSelectedListener;
import uk.ac.diamond.scisoft.analysis.crystallography.CalibrantSelectionEvent;
import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationFactory;
import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationStandards;
import uk.ac.diamond.scisoft.analysis.crystallography.HKL;
import uk.ac.diamond.scisoft.analysis.diffraction.PowderRingsUtils;
import uk.ac.diamond.scisoft.analysis.diffraction.QSpace;

public class DiffractionTool extends AbstractToolPage implements CalibrantSelectedListener, IResettableExpansion, IROIListener {

	private static final Logger logger = LoggerFactory.getLogger(DiffractionTool.class);
	
	private ClearableFilteredTree filteredTree;
	private TreeViewer      viewer;
	private Composite       control;
	private DiffractionTreeModel model;
	private ILoaderService  service;
	private Label statusMessage;
	private String[] statusString = new String[1];
	
	private static DiffractionTool      activeDiffractionTool=null;
	
	//Region and region listener added for 1-click beam centring
	private IRegion               tmpRegion;
	private IRegionListener       regionListener;
	private ITraceListener.Stub   traceListener;
	private IROIListener roiListener;
	private IDetectorPropertyListener detpropListener;
	private IDiffractionCrystalEnvironmentListener difcrysListener;
	
	private int lastWavelengthUnit = 0;
	
	protected DiffractionImageAugmenter augmenter;

	/**
	 * Boolean used for Powder diffraction view
	 */
	private boolean hide = false;

	private Label label;

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}
	
	
	public DiffractionTool() {
		super();
		
		this.traceListener = new ITraceListener.Stub() {
			protected void update(TraceEvent evt) {
				if (getImageTrace()!=null) createDiffractionModel(true);
				updateIntensity();
			}
		};
		
		this.service = ServiceProvider.getService(ILoaderService.class);
	}

	protected void updateIntensity() {
		try {
			if (model==null) return;
			model.setIntensityValues(getImageTrace());
		} catch (Exception e) {
			logger.error("Updating intensity values!", e);
		}
	}

	@Override
	public void createControl(final Composite parent) {
		
		this.control = new Composite(parent, SWT.NONE);
		control.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		control.setLayout(new GridLayout(1, false));
		GridUtils.removeMargins(control);
	
		this.filteredTree = new ClearableFilteredTree(control, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, new NodeFilter(this), true, "Enter search string to filter the tree.\nThis will match on name, value or units");		
		viewer = filteredTree.getViewer();
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createColumns(viewer);
		viewer.setContentProvider(new TreeNodeContentProvider()); // Swing tree nodes
		viewer.getTree().setLinesVisible(true);
		viewer.getTree().setHeaderVisible(true);
		
		Composite status = new Composite(control, SWT.NONE);
		status.setLayoutData(new GridData(SWT.FILL, GridData.FILL, true, false));
		status.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		status.setLayout(new GridLayout(2, true));
		GridUtils.removeMargins(status);
	
		statusMessage = new Label(status, SWT.LEFT);
		statusMessage.setLayoutData(new GridData(SWT.FILL, GridData.CENTER, true, false));
		ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
		RGB rgb = colorRegistry.getRGB(JFacePreferences.QUALIFIER_COLOR);
		if (rgb == null)
			rgb = new RGB(128, 128, 128);
		statusMessage.setForeground(new Color(statusMessage.getDisplay(), rgb));
		statusMessage.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		if (statusString != null && statusString[0] != null)
			statusMessage.setText(statusString[0]);

		label = new Label(status, SWT.RIGHT);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		label.setForeground(new Color(label.getDisplay(), rgb));
		label.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		label.setText("* Click to change value  ");
		
		createDiffractionModel(false);
		createActions();
		createListeners();
		//false by default
		if (!hide)
			createToolPageActions();

		super.createControl(parent);
	}
	
	public void activate() {
		super.activate();
		createDiffractionModel(true);
		
		IPlottingSystem<Composite> plotting = getPlottingSystem();
		if (plotting != null) {
			if (regionListener != null)
				plotting.addRegionListener(regionListener);
			if (traceListener != null)
				plotting.addTraceListener(traceListener);
			if (tmpRegion != null) {
				tmpRegion.setVisible(true);
			}
		}

		if (augmenter!=null) augmenter.activate();
		CalibrationFactory.addCalibrantSelectionListener(this);
		activeDiffractionTool = this;
		
		if (calibrantActions != null && calibrantActions.getSelectedAction() != null) {
			calibrantActions.getSelectedAction().run();
		}
		
		final IDiffractionMetadata dmd = getDiffractionMetaData();
		if (viewer!=null && viewer.getInput()!=null && model!=null && dmd!=null && dmd.getDetector2DProperties()!=null && dmd.getOriginalDiffractionCrystalEnvironment()!=null) {
			try {
			    viewer.refresh();
			} catch (Throwable ne) {
				// Sometimes model could not be resolved at this point.
			}
		}
	}

	@Override
	public void deactivate() {
		
		if (!isActive()) {return;}
		
		super.deactivate();
		IPlottingSystem<Composite> plotting = getPlottingSystem();
		if (plotting != null) {
			plotting.removeRegionListener(regionListener);
			if (traceListener != null)
				plotting.removeTraceListener(traceListener);
			if (tmpRegion != null) {
				tmpRegion.setVisible(false);
			}
		}

		CalibrationFactory.removeCalibrantSelectionListener(this);
		if (augmenter!=null) augmenter.deactivate(service.getLockedDiffractionMetaData()!=null);
		if (activeDiffractionTool==this) activeDiffractionTool = null;
		if (model!=null) model.deactivate();
		
		IDiffractionMetadata data  = getDiffractionMetaData();
		if (data!=null && data.getDetector2DProperties()!=null && data.getDiffractionCrystalEnvironment()!=null) {
			data.getDetector2DProperties().removeDetectorPropertyListener(detpropListener);
			data.getDiffractionCrystalEnvironment().removeDiffractionCrystalEnvironmentListener(difcrysListener);
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		if (model!=null) model.dispose();
		if (augmenter != null) augmenter.dispose();
		if (statusMessage != null && !statusMessage.isDisposed()) {
			Color color = statusMessage.getForeground();
			if (color != null)
				color.dispose();
		}
		if (label != null && !label.isDisposed()) {
			Color color = label.getForeground();
			if (color != null)
				color.dispose();
		}
	}

	private void createDiffractionModel(boolean force) {
		
		if (!force && model!=null)  return;
		
		if (force && model!=null) {
			TreeNode node = model.getNode("/experimental information/wavelength");
			if (node != null) {
				try {
					lastWavelengthUnit = ((NumericNode<?>)node).getUnitIndex();
				} catch (Exception e) {
					//nothing
				}
				
			}
			model.dispose();
			model= null;
		}
		if (viewer==null)           return;
		statusString[0] = null;
		IDiffractionMetadata data = null;
		try {
			data = getDiffractionMetaData();
			if (data==null || data.getDetector2DProperties()==null || data.getDiffractionCrystalEnvironment()==null) {
				return;
			}
			model = new DiffractionTreeModel(data, hide);
			
			model.setViewer(viewer);
			model.activate();
			if (augmenter != null) {
				augmenter.setDiffractionMetadata(data);
			}

			updateIntensity();

			detpropListener = new IDetectorPropertyListener() {
				@Override
				public void detectorPropertiesChanged(DetectorPropertyEvent evt) {
					if (evt.getSource() instanceof DetectorProperties)
						DiffractionDefaultMetadata.setPersistedDetectorPropertieValues((DetectorProperties)evt.getSource());

				}
			};

			difcrysListener =new IDiffractionCrystalEnvironmentListener() {
				@Override
				public void diffractionCrystalEnvironmentChanged(
						DiffractionCrystalEnvironmentEvent evt) {
					if (evt.getSource() instanceof DiffractionCrystalEnvironment)
						DiffractionDefaultMetadata.setPersistedDiffractionCrystalEnvironmentValues((DiffractionCrystalEnvironment)evt.getSource());

				}
			};
			
			data.getDetector2DProperties().addDetectorPropertyListener(detpropListener);
			data.getDiffractionCrystalEnvironment().addDiffractionCrystalEnvironmentListener(difcrysListener);
			
			TreeNode node = model.getNode("/experimental information/wavelength");
			if (node != null) {
				try {
					((NumericNode<?>)node).setUnitIndex(lastWavelengthUnit);
				} catch (Exception e) {
					//nothing
				}
				
			}
			
		} catch (Exception e) {
			logger.error("Cannot create model!", e);
			return;
		}
			
		viewer.setInput(model.getRoot());
		model.activate();
		
		resetExpansion();
		if (getSite() != null) getSite().setSelectionProvider(viewer);

	}

	@Override
	public void resetExpansion() {
		try {
			if (model == null) return;
			final List<?> top = model.getRoot().getChildren();
			for (Object element : top) {
			   filteredTree.expand(element);
			}
		} catch (Throwable ne) {
			// intentionally silent
		}
	}

	private IDiffractionMetadata getDiffractionMetaData() {
		IDataset image = getImageTrace() == null ? null : getImageTrace().getData();
		if (image == null) return null;
		IWorkbenchPart part = getPart();
		String altPath = null;
		if(part instanceof IEditorPart){
			altPath = EclipseUtils.getFilePath(((IEditorPart) part).getEditorInput());
		} else if (part instanceof IViewPart){
			try {
				IMetadata md = image.getFirstMetadata(IMetadata.class);
				if(md != null)
					altPath = md.getFilePath();
			} catch (Exception e) {
				logger.debug("Exception getting the image metadata", e);
			}
		}
		//Add the meta data to the data set
		IDiffractionMetadata md = DiffractionUtils.getDiffractionMetadata(image, altPath, service, statusString);
		image.setMetadata(md);
		return md;
	}


	private TreeViewerColumn defaultColumn;
	
	private void createColumns(TreeViewer viewer) {
				
		viewer.setColumnProperties(new String[] { "Name", "Original", "Value", "Unit" });
		ColumnViewerToolTipSupport.enableFor(viewer);

		TreeViewerColumn var = new TreeViewerColumn(viewer, SWT.LEFT, 0);
		var.getColumn().setText("Name"); // Selected
		var.getColumn().setWidth(260);
		var.setLabelProvider(new NodeLabelProvider(0));
		
		var = new TreeViewerColumn(viewer, SWT.LEFT, 1);
		var.getColumn().setText("Original"); // Selected
		var.getColumn().setWidth(0);
		var.getColumn().setResizable(false);
		var.setLabelProvider(new DelegatingProviderWithTooltip(new NodeLabelProvider(1)));
		defaultColumn = var;
		
		var = new TreeViewerColumn(viewer, SWT.LEFT, 2);
		var.getColumn().setText("Value"); // Selected
		var.getColumn().setWidth(100);
		var.setLabelProvider(new DelegatingProviderWithTooltip(new NodeLabelProvider(2)));
		var.setEditingSupport(new ValueEditingSupport(viewer));

		var = new TreeViewerColumn(viewer, SWT.LEFT, 3);
		var.getColumn().setText("Unit"); // Selected
		var.getColumn().setWidth(90);
		var.setLabelProvider(new DelegatingProviderWithTooltip(new NodeLabelProvider(3)));
		var.setEditingSupport(new UnitEditingSupport(viewer));
	}

	/**
	 * 
	 * @return model
	 */
	public DiffractionTreeModel getModel() {
		return model;
	}
	
	/**
	 * 
	 * @return augmenter
	 */
	public DiffractionImageAugmenter getAugmenter() {
		return augmenter;
	}

	private TreeNode   copiedNode;
	private MenuAction calibrantActions;
	private CheckableActionGroup calibrantGroup;
	private Action     calPref;

	private Action refine;
	private Action findOuter;
	private Action calibrate;


	private static Action lock;

	private void createActions() {

		final Action exportMeta = new Action("Export metadata to file", Activator.getImageDescriptor("icons/save_edit.png")) {
			@Override
			public void run() {
				try {
					IWizard wiz = EclipseUtils.openWizard(PersistenceExportWizard.ID, false);
					WizardDialog wd = new  WizardDialog(Display.getCurrent().getActiveShell(), wiz);
					wd.setTitle(wiz.getWindowTitle());
					wd.open();
				} catch (Exception e) {
					logger.error("Problem opening export!", e);
				}
			}
		};
		
		final Action importMeta = new Action("Import metadata from file", Activator.getImageDescriptor("icons/import_wiz.png")) {
			@Override
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
		
		final Action showDefault = new Action("Show the original/default value column", Activator.getImageDescriptor("icons/plot-tool-diffraction-default.gif")) {
			@Override
			public void run() {
				defaultColumn.getColumn().setWidth(isChecked()?80:0);
				defaultColumn.getColumn().setResizable(!isChecked());
			}
		};
		showDefault.setChecked(false);
		
		final Action reset = new Action("Reset selected field", Activator.getImageDescriptor("icons/reset.gif")) {
			@Override
			public void run() {
				final TreeNode node = (TreeNode)((StructuredSelection)viewer.getSelection()).getFirstElement();
				if (node instanceof NumericNode) {
					((NumericNode<?>)node).reset();
					viewer.refresh(node);
				}
			}
		};
		final Action resetAll = new Action("Reset all fields", Activator.getImageDescriptor("icons/reset_red.png")) {
			@Override
			public void run() {
				
				boolean ok = MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Confirm Reset All", "Are you sure that you would like to reset all values?");
				if (!ok) return;
				filteredTree.clearText();
				if (service.getLockedDiffractionMetaData()!=null) {
					model.reset();
					viewer.refresh();
			        resetExpansion();
				} else {
					model.reset();
					createDiffractionModel(true);
					viewer.refresh();
				}
			}
		};
		
		final Action copy = new Action("Copy value", Activator.getImageDescriptor("icons/copy.gif")) {
			@Override
			public void run() {
				copiedNode = (TreeNode)((StructuredSelection)viewer.getSelection()).getFirstElement();
			}
		};

		final Action paste = new Action("Paste value", Activator.getImageDescriptor("icons/paste.gif")) {
			@Override
			public void run() {
				if (copiedNode!=null) {
					Object object = ((StructuredSelection)viewer.getSelection()).getFirstElement();
					if (object instanceof NumericNode) {
						NumericNode<?> nn = (NumericNode<?>)object;
						if (!nn.isEditable()) {
							MessageDialog.openWarning(Display.getDefault().getActiveShell(), "Cannot paste", "The item '"+nn.getLabel()+"' is not writable.\n\nPlease choose a different value to paste to.");
							return;
						}
						
						try {
						    nn.mergeValue(copiedNode);
						} catch (Throwable e) {
							try {
								if (EclipseUtils.getActivePage().findView("org.eclipse.pde.runtime.LogView")==null) {
								    EclipseUtils.getActivePage().showView("org.eclipse.pde.runtime.LogView");
								}
							} catch (PartInitException pe) {
								// Ignored.
							}
							IStatus status = new Status(IStatus.INFO, Activator.PLUGIN_ID, "Cannot past into '"+nn.getLabel()+"'", e);
							Activator.getPluginLog().log(status);
						}
						viewer.refresh(object);
					}
				}
			}
		};
		
		final Action centre = new Action("One-click beam centre", IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				logger.debug("1-click clicked");

				IPlottingSystem<Composite> plotter = getPlottingSystem();
				if (plotter == null) {
					logger.debug("No plotting system found");
					return;
				}

				try {
					clearRegions(plotter);
					if (tmpRegion != null) {
						plotter.removeRegion(tmpRegion);
					}
					tmpRegion = plotter.createRegion(RegionUtils.getUniqueName("BeamCentrePicker", plotter), IRegion.RegionType.POINT);
					tmpRegion.setUserRegion(false);
					tmpRegion.setVisible(false);
					refine.setEnabled(true);
				} catch (Exception e) {
					logger.error("Cannot add beam centre", e);
				}

			}
		};
		centre.setImageDescriptor(Activator.getImageDescriptor("icons/centre.png"));
		
		final Action fitRing = new Action("Fit ring", IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				logger.debug("Fit ring clicked");

				IPlottingSystem<Composite> plotter = getPlottingSystem();
				if (plotter == null) {
					logger.debug("No plotting system found");
					return;
				}

				try {
					clearRegions(plotter);
					if (tmpRegion != null) {
						plotter.removeRegion(tmpRegion);
					}
					tmpRegion = plotter.createRegion(RegionUtils.getUniqueName("RingPicker", plotter), IRegion.RegionType.ELLIPSEFIT);
					tmpRegion.setShowLabel(false);
					tmpRegion.setUserRegion(false);
					tmpRegion.addROIListener(roiListener);
					findOuter.setEnabled(true);
					refine.setEnabled(true);
				} catch (Exception e) {
					logger.error("Cannot add ring", e);
				}
			}
		};
		fitRing.setImageDescriptor(Activator.getImageDescriptor("icons/eclipsecirclepoints.png"));
		fitRing.setToolTipText("Select 3 or 4 points on ring to fit a circle or 5 points or more for an ellipse");

		refine = new Action("Refine beam centre", IAction.AS_PUSH_BUTTON) {
			
			class Compare implements Comparator<IPeak> {

				@Override
				public int compare(IPeak o1, IPeak o2) {
					if (o1.getPosition() > o2.getPosition()) {
						return 1;
					}
					if (o1.getPosition() < o2.getPosition()) {
						return -1;
					}
					return 0;
				}

			}
			
			
			@SuppressWarnings("unchecked")
			private List<IPeak> loadPeaks() {
				IToolPage radialTool = getToolSystem().getToolPage(
						"org.dawb.workbench.plotting.tools.radialProfileTool");
				IToolPage fittingTool = ((IToolPageSystem)radialTool.getToolPlottingSystem()).getToolPage(
						"org.dawb.workbench.plotting.tools.fittingTool");
				if (fittingTool != null) {
					List<IPeak> fittedPeaks = (List<IPeak>) fittingTool.getAdapter(IPeak.class);

					if (fittedPeaks != null) {
						Collections.sort(fittedPeaks, new Compare());

						ArrayList<IPeak> peaks = new ArrayList<IPeak>(fittedPeaks.size());
						if (peaks != null && peaks.size() > 0)
							peaks.clear();
						for (IPeak peak : fittedPeaks) {
							peaks.add(peak);
						}
						return peaks;
					}
				}
				
				return null;
			}

			@Override
			public void run() {
				final IPlottingSystem<Composite> plotter = getPlottingSystem();
				final IImageTrace t = getImageTrace();
				if (tmpRegion.getRegionType() == RegionType.ELLIPSEFIT || tmpRegion.getRegionType() == RegionType.CIRCLEFIT) {
					final Display display = control.getDisplay();
					if (t != null) {
						Job job = new Job("Circle fit refinement") {
							@Override
							protected IStatus run(final IProgressMonitor monitor) {
								IROI roi = runEllipseFit(monitor, display, plotter, t, tmpRegion.getROI(), true, RADIAL_DELTA);
								if (roi == null)
									return Status.CANCEL_STATUS;
								
								return drawRing(monitor, display, plotter, roi, true);
							}
						};
						job.setPriority(Job.SHORT);
//						job.setUser(true);
						job.schedule();

					}
					return;
				}
				try {
					
					Collection<IRegion> regions = plotter.getRegions(RegionType.SECTOR);
					if (regions.size() == 0) {
						throw new IllegalStateException();
					}
					SectorROI sroi = (SectorROI) regions.iterator().next().getROI();
					Dataset dataset = DatasetUtils.convertToDataset(t.getData());
					Dataset mask    = DatasetUtils.convertToDataset(t.getMask());
					final BeamCenterRefinement beamOffset = new BeamCenterRefinement(dataset, mask, sroi);
					List<IPeak> peaks = loadPeaks();
					if (peaks==null) throw new Exception("Cannot find peaks!");
					beamOffset.setInitPeaks(peaks);
					
					beamOffset.optimize(getDiffractionMetaData().getDetector2DProperties().getBeamCentreCoords());
				} catch (Throwable ne) {
					
					/**
					 * Long discussion with Iralki on this. The algorithm must be set up in a particular way to 
					 * run at the moment. 
					 */
					ConfigurableMessageDialog dialog = new ConfigurableMessageDialog(Display.getDefault().getActiveShell(),
							"Experimental Refinement Algorithm Incomplete",
							null,
							"Could not read peak positions to start refinement.\nThis tool is still under development. To perform the refinement, use the following procedure:\n\n"+
							"1. Open the 'Diffraction' tool in a dedicated view (action on the right of the toolbar).\n"+
							"2. Open the 'Radial Profile' tool (from the plot containing the image).\n" +
							"3. Select a sector which bisects the rings wanted.\n"+
							"4. In the 'Radial Profile' tool press 'Lock to Metadata' button.\n"+
							"5. Select 'q' from the 'Select x axis values' list in the 'Radial Profile' toolbar.\n"+
							"6. In the 'Radial Profile' tool select peak fitting.\n"+
							"7. Set up a peak fit on all the rings which the radial profile found.\n"+
							"8. Now run the refine action in the diffraction tool again.\n\n"+
							"Please note that the algorithm may not converge. A job is run for the refinement which may be stopped.\n"+
							"Please contact your support representative for more training/help with refinement.\n\n"+
							"(NOTE: This dialog can be kept open as a guide while doing the proceedure.)",
							MessageDialog.INFORMATION,
							new String[]{IDialogConstants.OK_LABEL},
							0);
					dialog.setShellStyle(SWT.SHELL_TRIM|SWT.MODELESS);
					dialog.open();
				}
			}
		};
		refine.setImageDescriptor(Activator.getImageDescriptor("icons/refine.png"));
		refine.setEnabled(false);

		findOuter = new Action("Find outer rings", IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				logger.debug("Find outer rings clicked");

				if (tmpRegion.getRegionType() == RegionType.ELLIPSEFIT || tmpRegion.getRegionType() == RegionType.CIRCLEFIT) {
					final IPlottingSystem<Composite> plotter = getPlottingSystem();
					final IImageTrace t = getImageTrace();
					final Display display = control.getDisplay();
					if (t != null) {
						Job job = new Job("Ellipse rings finding") {
							@Override
							protected IStatus run(final IProgressMonitor monitor) {
								IROI roi = tmpRegion.getROI();
								boolean circle = roi instanceof CircularROI;
								roi = runEllipseFit(monitor, display, plotter, t, roi, circle, RADIAL_DELTA);
								if (roi == null)
									return Status.CANCEL_STATUS;

								IStatus stat = drawRing(monitor, display, plotter, roi, circle);
								if (stat.isOK()) {
									stat = runFindOuterRings(monitor, display, plotter, t, roi);
								}
								return stat;
							}
						};
						job.setPriority(Job.SHORT);
//						job.setUser(true);
						job.schedule();
					}
					return;
				} else {
					ConfigurableMessageDialog dialog = new ConfigurableMessageDialog(Display.getDefault().getActiveShell(),
							"Rings locator - no initial ring",
							null,
							"Please define an initial ring",
							MessageDialog.INFORMATION,
							new String[]{IDialogConstants.OK_LABEL},
							0);
					dialog.setShellStyle(SWT.SHELL_TRIM|SWT.MODELESS);
					dialog.open();

				}
			}
		};
		findOuter.setImageDescriptor(Activator.getImageDescriptor("icons/findmorerings.png"));
		findOuter.setToolTipText("Find outer rings");
		findOuter.setEnabled(false);

		calibrate = new Action("Calibrate against standard", IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				CalibrationStandards standards = CalibrationFactory.getCalibrationStandards();
				String name = standards.getSelectedCalibrant();
				if (name != null) {
					logger.debug("Calibrating against {}", name);
					final List<HKL> spacings = standards.getCalibrationPeakMap(name).getHKLs();

					final IPlottingSystem<Composite> plotter = getPlottingSystem();
					final IImageTrace t = getImageTrace();
					final Display display = control.getDisplay();
					if (t != null) {
						Job job = new Job("Calibrating detector") {
							@Override
							protected IStatus run(final IProgressMonitor monitor) {
								return runCalibrateDetector(monitor, display, plotter, spacings);
							}
						};
						job.setPriority(Job.SHORT);
//						job.setUser(true);
						job.schedule();

					}
					return;
				} else {
					ConfigurableMessageDialog dialog = new ConfigurableMessageDialog(Display.getDefault().getActiveShell(),
							"Calibrator - no standard selected",
							null,
							"Please define calibrant",
							MessageDialog.INFORMATION,
							new String[]{IDialogConstants.OK_LABEL},
							0);
					dialog.setShellStyle(SWT.SHELL_TRIM|SWT.MODELESS);
					dialog.open();

				}
			}
		};
		calibrate.setImageDescriptor(Activator.getImageDescriptor("icons/findmorerings.png"));
		calibrate.setToolTipText("Calibrate detector using rings - this is an experimental feature and does not work robustly");
		calibrate.setEnabled(false);

		if (lock==null) lock = new Action("Lock the diffraction data and apply it to newly opened files.\nThis will also leave the rings on the image when the tool is deactivated.",IAction.AS_CHECK_BOX) {
		    @Override
			public void run() {
		    	if (isChecked()) {
		    		IDiffractionMetadata data = activeDiffractionTool.getDiffractionMetaData().clone();
		    		service.setLockedDiffractionMetaData(data);
		    	} else {
		    		// Also clears cached data because we may have
		    		// changed the meta data.
		    		service.setLockedDiffractionMetaData(null);
		    	}
		    	activeDiffractionTool.createDiffractionModel(true);
			}
		};
		lock.setImageDescriptor(Activator.getImageDescriptor("icons/lock.png"));

		this.calPref = new Action("Configure Calibrants...") {
			@Override
			public void run() {
				try {
					PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), DiffractionPreferencePage.ID, null, null);
					if (pref != null) pref.open();
				} catch (IllegalStateException e) {
					logger.error(e.getMessage());
				}
			}
		};
		
		Action configDetectors = new Action("Configure Detectors...") {
			@Override
			public void run() {
				try {
					PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), DiffractionDetectorPreferencePage.ID, null, null);
					if (pref != null) pref.open();
				} catch (IllegalStateException e) {
					logger.error(e.getMessage());
				}
			}
		};
		
		Action configDefaultMeta = new Action("Configure Default Metadata...") {
			@Override
			public void run() {
				try {
					PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), DiffractionDefaultsPreferencePage.ID, null, null);
					if (pref != null) pref.open();
				} catch (IllegalStateException e) {
					logger.error(e.getMessage());
				}
			}
		};
		

		this.calibrantActions = new MenuAction("Calibrants");
		calibrantActions.setImageDescriptor(Activator.getImageDescriptor("/icons/calibrant_rings.png"));
		this.calibrantGroup = new CheckableActionGroup();
		updateCalibrationActions(CalibrationFactory.getCalibrationStandards());		
		

		MenuAction dropdown = new MenuAction("Resolution rings");
	    dropdown.setImageDescriptor(Activator.getImageDescriptor("/icons/resolution_rings.png"));
	    
	    if (!hide) {
	    	augmenter = new DiffractionImageAugmenter(getPlottingSystem());
	    	augmenter.addActions(dropdown);
	    }

	    if (getSite() == null) return;
	    
		IToolBarManager toolMan = getSite().getActionBars().getToolBarManager();
		final MenuManager     menuMan = new MenuManager();

		// false by default
		if (!hide) {
			toolMan.add(importMeta);
			toolMan.add(exportMeta);
			toolMan.add(new Separator());
			toolMan.add(lock);
			toolMan.add(new Separator());
			toolMan.add(dropdown);
			toolMan.add(calibrantActions);
			toolMan.add(new Separator());
			toolMan.add(centre);
			toolMan.add(fitRing);
			toolMan.add(refine);
			toolMan.add(findOuter);
			toolMan.add(calibrate);
			toolMan.add(new Separator());
			toolMan.add(reset);
			toolMan.add(resetAll);
			toolMan.add(new Separator());
			toolMan.add(showDefault);
			toolMan.add(new Separator());
		}
		
		menuMan.add(dropdown);
		menuMan.add(centre);
		menuMan.add(fitRing);
		menuMan.add(refine);
		menuMan.add(findOuter);
		menuMan.add(calibrate);
		menuMan.add(new Separator());
		menuMan.add(reset);
		menuMan.add(resetAll);
		menuMan.add(new Separator());
		menuMan.add(copy);
		menuMan.add(paste);
		menuMan.add(new Separator());
		menuMan.add(showDefault);
		menuMan.add(new Separator());
		menuMan.add(calPref);

		final Menu menu = menuMan.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);

		if (!hide) {
			getSite().getActionBars().getMenuManager().add(new Separator());
			getSite().getActionBars().getMenuManager().add(calPref);
			getSite().getActionBars().getMenuManager().add(configDetectors);
			getSite().getActionBars().getMenuManager().add(configDefaultMeta);
			getSite().getActionBars().getMenuManager().add(new Separator());
		}
	}

	public void hideToolBar(boolean hide) {
		this.hide = hide;
	}

	private static final String RING_PREFIX = "Ring";
	private void clearRegions(IPlottingSystem<Composite> plotter) {
		Collection<IRegion> regions = plotter.getRegions();
		for (IRegion r : regions) {
			String n = r.getName();
			if (n.startsWith(RING_PREFIX))
				plotter.removeRegion(r);
		}
	}

	private static final double ARC_LENGTH = 8;
	private static final double RADIAL_DELTA = 10;
	private static final int MAX_POINTS = 200;

	public static IROI runEllipseFit(final IProgressMonitor monitor, Display display, final IPlottingSystem<Composite> plotter, IImageTrace t, IROI roi, final boolean circle, double radialDelta) {
		if (roi == null)
			return null;

		String shape = circle ? "circle" : "ellipse";
		final ProgressMonitorWrapper mon = new ProgressMonitorWrapper(monitor);
		monitor.beginTask("Refine " + shape + " fit", IProgressMonitor.UNKNOWN);
		monitor.subTask("Find POIs near initial " + shape);
		Dataset image = DatasetUtils.convertToDataset(t.getData());
		BooleanDataset mask = (BooleanDataset) t.getMask();
		PolylineROI points;
		EllipticalFitROI efroi;
		monitor.subTask("Fit POIs");
		points = roi instanceof CircularROI ? PowderRingsUtils.findPOIsNearCircle(mon, image, mask, (CircularROI) roi, ARC_LENGTH, radialDelta, MAX_POINTS)
				: PowderRingsUtils.findPOIsNearEllipse(mon, image, mask, (EllipticalROI) roi, ARC_LENGTH, radialDelta, MAX_POINTS);
		if (points.getNumberOfPoints() < 3) {
			throw new IllegalArgumentException("Could not find enough points to trim");
		}

		monitor.subTask("Trim POIs");
		efroi = PowderRingsUtils.fitAndTrimOutliers(mon, points, 2, circle);
		logger.debug("Found {}...", efroi);

		int npts = efroi.getPoints().getNumberOfPoints();
		int lpts;
		do {
			lpts = npts;
			points = PowderRingsUtils.findPOIsNearEllipse(mon, image, mask, efroi);

			efroi = PowderRingsUtils.fitAndTrimOutliers(mon, points, 2, circle);
			npts = efroi.getPoints().getNumberOfPoints(); 
		} while (lpts > npts);

		if (monitor.isCanceled())
			return null;

		final IROI froi = circle ? new CircularFitROI(efroi.getPoints()) : efroi;
		monitor.worked(1);
		logger.debug("Fitted {} from peaks: {}", shape, froi);

		return froi;
	}
	
	private IStatus drawRing(final IProgressMonitor monitor, Display display, final IPlottingSystem<Composite> plotter, final IROI froi, final boolean circle) {
		final boolean[] status = {true};
		display.syncExec(new Runnable() {

			@Override
			public void run() {
				try {
					long t = - System.currentTimeMillis();
					IRegion region = plotter.createRegion(RegionUtils.getUniqueName("Pixel peaks", plotter), circle ? RegionType.CIRCLEFIT : RegionType.ELLIPSEFIT);
					region.setROI(froi);
					region.setRegionColor(circle ? ColorConstants.cyan : ColorConstants.orange);
					plotter.removeRegion(tmpRegion);
					monitor.subTask("Add region");
					tmpRegion = region;
					tmpRegion.setUserRegion(false);
					tmpRegion.addROIListener(roiListener);
					roiListener.roiSelected(new ROIEvent(tmpRegion, froi)); // trigger beam centre update
					plotter.addRegion(region);
					System.err.println("Time taken " + (t + System.currentTimeMillis()) + "ms");
					monitor.worked(1);
					findOuter.setEnabled(true);
				} catch (Exception e) {
					status[0] = false;
				}
			}
		});

		return status[0] ? Status.OK_STATUS : Status.CANCEL_STATUS;
	}

	private IStatus runFindOuterRings(final IProgressMonitor monitor, Display display, final IPlottingSystem<Composite> plotter, IImageTrace t, IROI roi) {
		final ProgressMonitorWrapper mon = new ProgressMonitorWrapper(monitor);
		monitor.beginTask("Find elliptical rings", IProgressMonitor.UNKNOWN);
		monitor.subTask("Find rings");
		if (roi instanceof CircularFitROI) {
			roi = new EllipticalFitROI(((CircularFitROI) roi).getPoints(), true);
		}
		final List<EllipticalROI> ells = PowderRingsUtils.findOtherEllipses(mon, DatasetUtils.convertToDataset(t.getData()), (BooleanDataset) t.getMask(), (EllipticalROI) roi);
		final boolean[] status = {true};
		display.syncExec(new Runnable() {

			@Override
			public void run() {
				try {
					int emax = ells.size();
					if (emax > 0)
						plotter.removeRegion(tmpRegion);
					for (int i = 0; i < emax; i++) {
						monitor.subTask("Add region: " + i);
						EllipticalROI e = ells.get(i);
						logger.debug("Ellipse from peaks: {}, {}", i, e);
						IRegion region = plotter.createRegion(RegionUtils.getUniqueName(RING_PREFIX, plotter), e instanceof EllipticalFitROI ? RegionType.ELLIPSEFIT : RegionType.ELLIPSE);
						region.setMobile(false);
						region.setROI(e);
						region.setRegionColor(ColorConstants.orange);
						region.setUserRegion(false);
						plotter.addRegion(region);
						monitor.worked(1);
					}
					// TODO set beam centre in case of all circles
					calibrate.setEnabled(true);
					findOuter.setEnabled(false);
				} catch (Exception e) {
					status[0] = false;
				}
			}
		});

		return status[0] ? Status.OK_STATUS : Status.CANCEL_STATUS;
	}

	private IStatus runCalibrateDetector(final IProgressMonitor monitor, Display display, final IPlottingSystem<Composite> plotter, List<HKL> spacings) {
		final ProgressMonitorWrapper mon = new ProgressMonitorWrapper(monitor);
		monitor.beginTask("Calibrate detector from rings", IProgressMonitor.UNKNOWN);
		monitor.subTask("Find rings");
		Collection<IRegion> regions = plotter.getRegions();
		List<EllipticalROI> rois = new ArrayList<EllipticalROI>();
		for (IRegion r : regions) {
			String n = r.getName();
			if (n.startsWith(RING_PREFIX))
				rois.add((EllipticalROI) r.getROI());
		}
		monitor.worked(1);
		
		monitor.subTask("Fit detector");
		try {
			IDiffractionMetadata md = getDiffractionMetaData();
			final DetectorProperties det = md.getDetector2DProperties();
			final DiffractionCrystalEnvironment env = md.getDiffractionCrystalEnvironment();
//			final QSpace q = PowderRingsUtils.fitEllipsesToQSpace(mon, det, env, rois, spacings, true);
			final QSpace q = PowderRingsUtils.fitEllipsesToQSpace(mon, det, env, rois, spacings, false);
//			final QSpace q = PowderRingsUtils.fitAllEllipsesToQSpace(mon, det, env, rois, spacings, false);
			if (q == null)
				return Status.CANCEL_STATUS;
			display.syncExec(new Runnable() {
				@Override
				public void run() {
					det.setGeometry(q.getDetectorProperties());
					env.setWavelength(q.getWavelength());
				}
			});
		} catch (Exception e) {
			return Status.CANCEL_STATUS;
		}

		return Status.OK_STATUS;
	}
	
	private void updateCalibrationActions(final CalibrationStandards standards) {
		this.calibrantActions.clear();
		this.calibrantGroup.clear();
		final String selected = standards.getSelectedCalibrant();
		Action selectedAction=null;
		for (final String calibrant : standards.getCalibrantList()) {
			final Action calibrantAction = new Action(calibrant, IAction.AS_CHECK_BOX) {
				@Override
				public void run() {
					standards.setSelectedCalibrant(calibrant, true);
				}
			};
			calibrantGroup.add(calibrantAction);
			if (selected!=null&&selected.equals(calibrant)) selectedAction = calibrantAction;
			calibrantActions.add(calibrantAction);
		}
		calibrantActions.addSeparator();
		calibrantActions.add(calPref);
		if (selected!=null) selectedAction.setChecked(true);
	}

	private void createListeners() {
		
		this.regionListener = new IRegionListener.Stub() {
			@Override
			public void regionAdded(RegionEvent evt) {
				//test if our region
				if (evt.getRegion() == tmpRegion) {
//					logger.debug("Region added (type: {})", tmpRegion.getRegionType());
					double[] point = tmpRegion.getROI().getPointRef();
//					logger.debug("Clicked here X: {} Y : {}", point[0], point[1]);

					if (tmpRegion.getRegionType() == RegionType.POINT)
						getPlottingSystem().removeRegion(tmpRegion);
					IDiffractionMetadata data = getDiffractionMetaData();
					DetectorProperties detprop = data.getDetector2DProperties();
					detprop.setBeamCentreCoords(point);
					if (augmenter != null && !augmenter.isShowingBeamCenter()) {
						augmenter.drawBeamCentre(true);
					}
//					tmpRegion.setShowLabel(true);
				}
				if (evt.getRegion() != null)
					evt.getRegion().addROIListener(DiffractionTool.this);
			}

			@Override
			public void regionRemoved(RegionEvent evt) {
				IRegion region = evt.getRegion();
				if (region!=null) {
					region.removeROIListener(DiffractionTool.this);
				}
			}

			@Override
			public void regionCreated(RegionEvent evt) {
				IRegion region = evt.getRegion();
				if (region!=null) {
					region.addROIListener(DiffractionTool.this);
				}
			}

			@Override
			public void regionsRemoved(RegionEvent evt) {
				IWorkbenchPage page = null;
				try {
					page =  PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				} catch (IllegalStateException e) {
					logger.error(e.getMessage());
				}
				if(page != null){
					Iterator<IRegion> it = getPlottingSystem().getRegions().iterator();
					while(it.hasNext()){
						IRegion region = it.next();
						region.removeROIListener(DiffractionTool.this);
					}	
				}
			}
		};

		roiListener = new IROIListener.Stub() {
			@Override
			public void update(ROIEvent evt) {
				IROI r = evt.getROI();
				if (r instanceof CircularFitROI || (r instanceof EllipticalFitROI && ((EllipticalFitROI) r).isCircular())) {
					double[] point = r.getPointRef();
//					logger.debug("ROI moved here X: {} Y : {}", point[0], point[1]);
					IDiffractionMetadata data = getDiffractionMetaData();
					DetectorProperties detprop = data.getDetector2DProperties();
					detprop.setBeamCentreCoords(point);
					if (augmenter != null && !augmenter.isShowingBeamCenter()) {
						augmenter.drawBeamCentre(true);
					}
				}
			}
		};
	}

	@Override
	public Control getControl() {
		return control;
	}

	public void refresh() {
		viewer.refresh();
	}

	@Override
	public void setFocus() {
		if (viewer!=null && !viewer.getControl().isDisposed()) viewer.getControl().setFocus();
	}

	@Override
	public void calibrantSelectionChanged(CalibrantSelectionEvent evt) {
		updateCalibrationActions((CalibrationStandards)evt.getSource());
	}

	@Override
	public void roiDragged(ROIEvent evt) {
		updateBeamCentre(evt);
	}

	@Override
	public void roiChanged(ROIEvent evt) {
		updateBeamCentre(evt);
	}

	@Override
	public void roiSelected(ROIEvent evt) {}
	
	private void updateBeamCentre(ROIEvent evt) {
//		IROI roi = evt.getROI();
//		if(roi == null)return;
//		PointROI eroi = roi instanceof PointROI ? (PointROI)roi : null;		
//		if(eroi == null) return;
//		if (!(evt.getSource() instanceof IRegion)) return;
//		
//		IRegion point = (IRegion)evt.getSource();
//		Object ob = point.getUserObject();
//		if (ob == null) return;
//		
//		String ringType = ob.toString(); // TODO this is hacky!
//		if (!"CALIBRANT".equals(ringType) && !"BEAM_POSITION_HANDLE".equals(ringType)) return;
//		
//		double ptx = eroi.getPointX();
//		double pty = eroi.getPointY();
//		IDiffractionMetadata data = getDiffractionMetaData();
//		DetectorProperties detprop = data.getDetector2DProperties();
//		detprop.setBeamCentreCoords(new double[]{ptx, pty});
	}

}
