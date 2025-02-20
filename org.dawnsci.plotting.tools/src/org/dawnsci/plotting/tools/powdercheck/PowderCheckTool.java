/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.powdercheck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.dawb.common.ui.menu.CheckableActionGroup;
import org.dawb.common.ui.menu.MenuAction;
import org.dawnsci.plotting.actions.ActionBarWrapper;
import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.powdercheck.PowderCheckJob.PowderCheckMode;
import org.dawnsci.plotting.tools.preference.diffraction.DiffractionPreferencePage;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteListener;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.PaletteEvent;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.metadata.MaskMetadata;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.IPageSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.crystallography.CalibrantSelectedListener;
import uk.ac.diamond.scisoft.analysis.crystallography.CalibrantSelectionEvent;
import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationFactory;
import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationStandards;
import uk.ac.diamond.scisoft.analysis.roi.XAxis;

public class PowderCheckTool extends AbstractToolPage {
	
	
	private final static Logger logger = LoggerFactory.getLogger(PowderCheckTool.class);
	
	IPlottingSystem<Composite> system;
	PowderCheckJob updatePlotJob;
	SashForm sashForm;
	TableViewer viewer;
	Action fullImage;
	XAxis xAxis = XAxis.Q;
	private MenuAction calibrantActions;
	private CheckableActionGroup calibrantGroup;
	private Action     calPref;
	private boolean onDialog = false;


	private ITraceListener            traceListener;
	private IPaletteListener          paletteListener;
	private CalibrantSelectedListener calListener;
	
	public PowderCheckTool() {
		try {
			system = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		this.calListener = new CalibrantSelectedListener() {		
			@Override
			public void calibrantSelectionChanged(CalibrantSelectionEvent evt) {
				if (updatePlotJob != null)	updatePlotJob.updateCalibrantLines();
				updateCalibrationActions((CalibrationStandards)evt.getSource());
			}
		};
		
		// TODO Listen to other things.
		this.traceListener = new ITraceListener.Stub() {
			
			@Override
			public void traceAdded(TraceEvent evt) {
				PowderCheckTool.this.update();
			}
			
			@Override
			public void traceUpdated(TraceEvent evt) {
				PowderCheckTool.this.update();
			}
			
			@Override
			public void traceRemoved(TraceEvent evt) {
				PowderCheckTool.this.update();
			}

		};
		
		this.paletteListener = new IPaletteListener.Stub() {
			@Override
			public void maskChanged(PaletteEvent evt) {
				PowderCheckTool.this.update();
			}
		};
	}

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}

	@Override
	public void createControl(Composite parent) {
		
		ActionBarWrapper actionBarWrapper = null;
		
		if (getSite() == null) {
			parent = new Composite(parent, SWT.NONE);
			parent.setLayout(new GridLayout(1,true));
			actionBarWrapper = ActionBarWrapper.createActionBars(parent, null);
			onDialog = true;
		}
		
		sashForm = new SashForm(parent, SWT.VERTICAL);
		if (getSite() == null) sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

		final IPageSite site = getSite();
		IActionBars actionbars = site!=null?site.getActionBars():actionBarWrapper;
		
		createActions(actionbars);
		
		system.createPlotPart(sashForm, 
				getTitle(), 
				actionbars, 
				PlotType.XY,
				this.getViewPart());

		system.getSelectedYAxis().setAxisAutoscaleTight(true);
		
		getPlottingSystem().addTraceListener(traceListener);
		
		viewer = new TableViewer(sashForm);
		createColumns();
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		viewer.setContentProvider(createContentProvider());
			sashForm.setWeights(new int[]{60,40});

		sashForm.setMaximizedControl(system.getPlotComposite());
		update();

		super.createControl(parent);
	}
	
	
	private void update() {
		if (!onDialog) {
			if (getViewPart()==null) return;
			IWorkbenchPartSite site = getViewPart().getSite();
			if (site == null || !site.getPage().isPartVisible(getViewPart())) return;
		}
		
		IImageTrace im = getImageTrace();
		logger.debug("Update");
		
		if (im == null) {
			//cleanPlottingSystem();
			return;
		}
		
		im.addPaletteListener(paletteListener);
		
		final Dataset ds = DatasetUtils.convertToDataset(im.getData());
		if (ds==null) return;
			
		IDiffractionMetadata m = ds.getFirstMetadata(IDiffractionMetadata.class);

		if (m == null) {
			//TODO nicer error
			logger.error("No Diffraction Metadata");
			return;
		}
		
		Dataset mask = DatasetUtils.convertToDataset(im.getMask());
		
		if (updatePlotJob == null) {
			updatePlotJob= new PowderCheckJob(system);
			updatePlotJob.addJobChangeListener(new JobChangeAdapter() {
				@Override
				public void done(IJobChangeEvent event) {
					
					Display.getDefault().syncExec(new Runnable() {
						
						@Override
						public void run() {
							List<PowderCheckResult> resultsList = updatePlotJob.getResultsList();
							Collections.sort(resultsList, new Comparator<PowderCheckResult>() {

								@Override
								public int compare(PowderCheckResult o1,
										PowderCheckResult o2) {
									return (int) Math.signum(o1.getCalibrantQValue()-o2.getCalibrantQValue());
								}
							});
							
							if (viewer != null && !viewer.getTable().isDisposed()) viewer.setInput(resultsList);
						}
					});
				}
			});
		}
		
		updatePlotJob.cancel();
		updatePlotJob.setAxisMode(xAxis);
		updatePlotJob.setCheckMode(PowderCheckMode.FullImage);
		updatePlotJob.setData(ds, m, mask);
		if (fullImage != null)	fullImage.run();
		
	}
	
	private void createActions(IActionBars actionbars) {
		
		final MenuAction modeSelect = new MenuAction("Select Check Mode");
		
		fullImage = new Action("Full Image") {
			@Override
			public void run() {
				modeSelect.setSelectedAction(this);
				sashForm.setMaximizedControl(system.getPlotComposite());
				if (updatePlotJob == null) {
					update();
				}
				
				if (updatePlotJob == null) return;
				updatePlotJob.cancel();
				updatePlotJob.setCheckMode(PowderCheckMode.FullImage);
				updatePlotJob.schedule();
			}
		};
		fullImage.setToolTipText("Integrate the entire image, showing lines at calibrant positions");
		fullImage.setImageDescriptor(Activator.getImageDescriptor("icons/pixel.png"));
		
		modeSelect.add(fullImage);
		modeSelect.setSelectedAction(fullImage);
		
		final Action quad = new Action("Sections") {
			@Override
			public void run() {
				modeSelect.setSelectedAction(this);
				sashForm.setMaximizedControl(system.getPlotComposite());
				if (updatePlotJob == null) update();
				updatePlotJob.cancel();
				updatePlotJob.setCheckMode(PowderCheckMode.Quadrants);
				updatePlotJob.schedule();
			}
		};
		
		quad.setToolTipText("Integrate the 4 quadrants, showing lines at calibrant positions");
		quad.setImageDescriptor(Activator.getImageDescriptor("icons/CalibrationCheck.png"));
		
		modeSelect.add(quad);
		
		final Action peakfit = new Action("Peak Fit") {
			@Override
			public void run() {
				modeSelect.setSelectedAction(this);
				
				Display.getDefault().syncExec(new Runnable() {
					
					@Override
					public void run() {
						viewer.getTable().clearAll();
					}
				});
				
				sashForm.setMaximizedControl(null);
				if (updatePlotJob == null) update();
				updatePlotJob.cancel();
				updatePlotJob.setCheckMode(PowderCheckMode.PeakFit);
				updatePlotJob.setAxisMode(xAxis);
				updatePlotJob.schedule();
				
				
			}
		};
		
		peakfit.setToolTipText("Integrate the entire image, peak fit, and compare with calibrant positions");
		peakfit.setImageDescriptor(Activator.getImageDescriptor("icons/peakfit.png"));
		modeSelect.add(peakfit);
		
		final MenuAction axisSelect= new MenuAction("Select Axis");
		
		final Action qAction = new Action("Q") {
			@Override
			public void run() {
				axisSelect.setSelectedAction(this);
				xAxis = XAxis.Q;
				updateAndRun();
				
			}
		};
		
		
		final Action tthAction = new Action("2\u03b8") {
			@Override
			public void run() {
				axisSelect.setSelectedAction(this);
				xAxis = XAxis.ANGLE;
				updateAndRun();
				
			}
		};
		
		final Action cake = new Action("Cake") {
			@Override
			public void run() {
				modeSelect.setSelectedAction(this);
				sashForm.setMaximizedControl(system.getPlotComposite());
				updatePlotJob.cancel();
				updatePlotJob.setCheckMode(PowderCheckMode.Cake);
				updatePlotJob.schedule();
			}
		};
		
		cake.setImageDescriptor(Activator.getImageDescriptor("icons/cake.png"));
		
		this.calPref = new Action("Configure Calibrants...") {
			@Override
			public void run() {
				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), DiffractionPreferencePage.ID, null, null);
				if (pref != null) pref.open();
			}
		};
		
		this.calibrantActions = new MenuAction("Calibrants");
		this.calibrantGroup   = new CheckableActionGroup();
		updateCalibrationActions(CalibrationFactory.getCalibrationStandards());	
		
		cake.setToolTipText("2D integration");
		
		modeSelect.add(cake);
		
		axisSelect.add(qAction);
		axisSelect.add(tthAction);
		axisSelect.setSelectedAction(qAction);
		
		actionbars.getToolBarManager().add(modeSelect);
		actionbars.getToolBarManager().add(axisSelect);
		actionbars.getMenuManager().add(modeSelect);
		actionbars.getMenuManager().add(axisSelect);
		actionbars.getMenuManager().add(this.calibrantActions);
		
	}
	
	private void cleanPlottingSystem(){
		if (system != null) {
			system.reset();
		}
	}
	
	private void updateAndRun() {
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				viewer.getTable().clearAll();
				setColumnNames();
			}
		});
		updatePlotJob.cancel();
		updatePlotJob.setAxisMode(xAxis);
		updatePlotJob.schedule();
		
	}
	
	@Override
	public void activate() {
		
		if (isActive()) return;
		
		CalibrationFactory.addCalibrantSelectionListener(calListener);
		getPlottingSystem().addTraceListener(traceListener);
		boolean wasActive = isActive();
		super.activate();
		if (!wasActive) update();		
	}

	@Override
	public void deactivate() {
		
		if (updatePlotJob != null) updatePlotJob.cancel();
		
		CalibrationFactory.removeCalibrantSelectionListener(calListener);
		getPlottingSystem().removeTraceListener(traceListener);
		super.deactivate();
		cleanPlottingSystem();
	}
	
	@Override
	public void dispose() {
		deactivate();
		if (system!=null) system.dispose();
		system = null;
		super.dispose();
	}
	
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class clazz) {
		if (clazz == IToolPageSystem.class) {
			return system;
		} else if (clazz == IPlottingSystem.class) {
		    return system;
		} else {
			return super.getAdapter(clazz);
		}
	}
	
	
	@Override
	public Control getControl() {
		return sashForm;
		//if (system != null) return system.getPlotComposite();
		//return null;
	}

	@Override
	public void setFocus() {
		if (system != null) system.setFocus();
	}
	
	private void updateCalibrationActions(final CalibrationStandards standards) {
		this.calibrantActions.clear();
		this.calibrantGroup.clear();
		final String selected = standards.getSelectedCalibrant();
		Action selectedAction=null;
		for (final String calibrant : standards.getCalibrantList()) {
			final Action calibrantAction = new Action(calibrant, IAction.AS_CHECK_BOX) {
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
	
	private void setColumnNames() {
		TableColumn[] columns = viewer.getTable().getColumns();
		
		String unit = null;
		String name = null;
		
		if (xAxis == XAxis.Q) {
			name = "Q ";
			unit = "(1/\u00c5)";
		} else if (xAxis == XAxis.ANGLE) {
			name = "2\u03b8 ";
			unit = "(degrees)";
		}
		
		columns[0].setText("Calibrant " + name + unit);
		columns[1].setText("Peak Position " + unit);
		columns[2].setText("Peak Width " + unit);
		columns[4].setText("Delta "  +name + unit);
	}
	
	private void createColumns() {

		List<TableViewerColumn> ret = new ArrayList<TableViewerColumn>(9);

		TableViewerColumn var   = new TableViewerColumn(viewer, SWT.LEFT, 0);
		var.getColumn().setText("Calibrant (1/\u00c5)");
		var.getColumn().setWidth(150);
		var.setLabelProvider(new PowderLabelProvider(0));
		ret.add(var);

		var   = new TableViewerColumn(viewer, SWT.CENTER, 1);
		var.getColumn().setText("Peak Position (1/\u00c5)");
		var.getColumn().setWidth(170);
		var.setLabelProvider(new PowderLabelProvider(1));
		ret.add(var);

		var   = new TableViewerColumn(viewer, SWT.CENTER, 2);
		var.getColumn().setText("Peak Width (1/\u00c5)");
		var.getColumn().setWidth(170);
		var.setLabelProvider(new PowderLabelProvider(2));
		ret.add(var);

		var   = new TableViewerColumn(viewer, SWT.CENTER, 3);
		var.getColumn().setText("Relative Error");
		var.getColumn().setToolTipText("1 - calibrated value/standard value.");
		var.getColumn().setWidth(150);
		var.setLabelProvider(new PowderLabelProvider(3));
		ret.add(var);
		
		var   = new TableViewerColumn(viewer, SWT.CENTER, 4);
		var.getColumn().setText("Delta (1/\u00c5)");
		var.getColumn().setToolTipText("Standard value minus calibrated value.");
		var.getColumn().setWidth(150);
		var.setLabelProvider(new PowderLabelProvider(4));
		ret.add(var);

		setColumnNames();
	}
	
	public class PowderLabelProvider extends ColumnLabelProvider {
		
		private int column;
		
		public PowderLabelProvider(int i) {
			this.column = i;
		}
		
		@Override
		public String getText(Object element) {
			
			if (element==null) return "";
			if (!(element instanceof PowderCheckResult)) return "";
			final PowderCheckResult  result  = (PowderCheckResult)element;
			
			double q = result.getCalibrantQValue();
			double qExp = result.getPeak().getParameter(0).getValue();
			double diff = 1-(qExp/q);
			
			switch(column) {
			case 0:
				return String.format("%.6g",q);
			case 1:
				return String.format("%.6g",qExp);
			case 2:
				return String.format("%.4g",result.getPeak().getParameter(1).getValue());
			case 3:
				return String.format("%.3g",diff);
			case 4:
				return String.format("%.3g",q-qExp);
			default:
				return "";
			}
		}
		
	}
	
	private IContentProvider createContentProvider() {
		return new IStructuredContentProvider() {
			@Override
			public void dispose() {
			}
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

			@Override
			public Object[] getElements(Object inputElement) {

				if (inputElement instanceof List<?> && !((List<?>)inputElement).isEmpty()) {
					if (((List<?>)inputElement).get(0) instanceof PowderCheckResult) {
						return ((List<?>)inputElement).toArray();
					}
				}
				
				return new Object[]{1};
			}
		};
	}
}
