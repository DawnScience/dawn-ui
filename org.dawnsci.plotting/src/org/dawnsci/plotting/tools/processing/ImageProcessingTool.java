package org.dawnsci.plotting.tools.processing;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.PlottingFactory;
import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.region.IROIListener;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegion.RegionType;
import org.dawnsci.plotting.api.region.IRegionListener;
import org.dawnsci.plotting.api.region.ROIEvent;
import org.dawnsci.plotting.api.region.RegionEvent;
import org.dawnsci.plotting.api.region.RegionUtils;
import org.dawnsci.plotting.api.tool.AbstractToolPage;
import org.dawnsci.plotting.api.tool.IAuxiliaryToolDataset;
import org.dawnsci.plotting.api.tool.IToolPageSystem;
import org.dawnsci.plotting.api.trace.IImageTrace;
import org.dawnsci.plotting.api.trace.ILineTrace;
import org.dawnsci.plotting.api.trace.ITrace;
import org.dawnsci.plotting.api.trace.ITraceListener;
import org.dawnsci.plotting.api.trace.TraceEvent;
import org.dawnsci.plotting.tools.profile.ProfileType;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.roi.IROI;

/**
 * Abstract Image Processing Tool
 * This tool has two profile plotting systems and a composite of controls
 * @author wqk87977
 *
 */
public abstract class ImageProcessingTool extends AbstractToolPage  implements IROIListener, IAuxiliaryToolDataset {
	
	private static Logger logger = LoggerFactory.getLogger(ImageProcessingTool.class);

	protected AbstractPlottingSystem normProfilePlotSystem;
	protected AbstractPlottingSystem preNormProfilePlotSystem;

	private HashMap<String, Collection<ITrace>> registeredTraces;
	private IRegionListener regionListener;
	private Composite profileContentComposite;
	private NormProfileJob updateNormProfile;
	private PreNormProfileJob updatePreNormProfile;
	private ITraceListener traceListener;

	private HashMap<String, IDataset> auxiliaryDatasets = new HashMap<String, IDataset>();

	private IRegion region;

	public ImageProcessingTool(){

		this.registeredTraces = new HashMap<String,Collection<ITrace>>(7);
		try {
			normProfilePlotSystem = PlottingFactory.createPlottingSystem();
			preNormProfilePlotSystem = PlottingFactory.createPlottingSystem();

			updatePreNormProfile = new PreNormProfileJob();
			updatePreNormProfile.addJobChangeListener(new JobChangeAdapter() {
					@Override
					public void done(IJobChangeEvent event) {
						super.done(event);
						final IStatus s = event.getResult();
						if (s.isOK()) {
							if(region== null)return;
							updateNormProfile(region, region.getROI(), false);
						}
					}
				});
			updateNormProfile = new NormProfileJob();

			this.regionListener = new IRegionListener.Stub() {
				@Override
				public void regionRemoved(RegionEvent evt) {
					if (evt.getRegion()!=null) {
						evt.getRegion().removeROIListener(ImageProcessingTool.this);
						clearTraces(evt.getRegion());
					}
				}
				@Override
				public void regionsRemoved(RegionEvent evt) {
					//clears traces if all regions removed
					final Collection<IRegion> regions = getPlottingSystem().getRegions();
					if (regions == null || regions.isEmpty()) {
						registeredTraces.clear();
						normProfilePlotSystem.clear();
						preNormProfilePlotSystem.clear();
					}
				}
				@Override
				public void regionAdded(RegionEvent evt) {}
				@Override
				public void regionCreated(RegionEvent evt) {
					if (evt.getRegion()!=null) {
						evt.getRegion().addROIListener(ImageProcessingTool.this);
					}
				}
				protected void update(RegionEvent evt) {}
			};

			this.traceListener = new ITraceListener.Stub() {
				@Override
				public void tracesAdded(TraceEvent evt) {
					if (!(evt.getSource() instanceof List<?>)) {
						return;
					}
					ImageProcessingTool.this.updateNormProfile(null, null, false);
				}
				@Override
				protected void update(TraceEvent evt) {
					ImageProcessingTool.this.updateNormProfile(null, null, false);
				}
			};
		} catch (Exception e) {
			logger.error("Cannot get plotting system!", e);
		}
	}

	protected String getRegionName() {
		return "Processed Region";
	}

	@Override
	public void createControl(Composite parent) {
		profileContentComposite = new Composite(parent, SWT.NONE);
		profileContentComposite.setLayout(new GridLayout(1, true));
		GridUtils.removeMargins(profileContentComposite);
		final Action reselect = new Action("Create new region to process", getImageDescriptor()) {
			public void run() {
				createNewRegion();
			}
		};
		if (getSite().getActionBars() != null){
			getSite().getActionBars().getToolBarManager().add(new Separator("org.dawb.workbench.plotting.tools.profile.newProfileGroup"));
			getSite().getActionBars().getToolBarManager().insertAfter("org.dawb.workbench.plotting.tools.profile.newProfileGroup", reselect);
			getSite().getActionBars().getToolBarManager().add(new Separator("org.dawb.workbench.plotting.tools.profile.newProfileGroupAfter"));
		}

		SashForm mainSashForm = new SashForm(profileContentComposite, SWT.VERTICAL);
		mainSashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		mainSashForm.setBackground(new Color(parent.getDisplay(), 192, 192, 192));
		// top part
		Composite profilePlotComposite = new Composite(mainSashForm, SWT.BORDER);
		profilePlotComposite.setLayout(new FillLayout());
		
		normProfilePlotSystem.createPlotPart(profilePlotComposite, 
				 getTitle(), 
				 getSite().getActionBars(), 
				 PlotType.XY,
				 null);
		configureNormPlottingSystem(normProfilePlotSystem);
		// Unused actions removed for tool
		normProfilePlotSystem.getPlotActionSystem().remove("org.dawb.workbench.plotting.rescale");
		normProfilePlotSystem.getPlotActionSystem().remove("org.dawb.workbench.plotting.plotIndex");
		normProfilePlotSystem.getPlotActionSystem().remove("org.dawb.workbench.plotting.plotX");
		normProfilePlotSystem.setXfirst(true);
		normProfilePlotSystem.setRescale(true);

		//bottom part
		SashForm bottomSashForm = new SashForm(mainSashForm, SWT.HORIZONTAL);
		bottomSashForm.setBackground(new Color(parent.getDisplay(), 192, 192, 192));

		Composite displayComp = new Composite(bottomSashForm, SWT.NONE);
		displayComp.setLayout(new GridLayout(1, false));
		GridUtils.removeMargins(displayComp);
		ActionBarWrapper actionBarWrapper = ActionBarWrapper.createActionBars(displayComp, null);
		Composite displayPlotComp  = new Composite(displayComp, SWT.BORDER);
		displayPlotComp.setLayout(new FillLayout());
		displayPlotComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		preNormProfilePlotSystem.createPlotPart(displayPlotComp, 
												 "User display", 
												 actionBarWrapper, 
												 PlotType.XY, 
												 null);
		configurePreNormPlottingSystem(preNormProfilePlotSystem);
		Composite controlComp = new Composite(bottomSashForm, SWT.NONE);
		controlComp.setLayout(new GridLayout(1, false));
		controlComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createControlComposite(controlComp);
		bottomSashForm.setWeights(new int[] { 2, 1});
		mainSashForm.setWeights(new int[]{1, 1});
		parent.layout();
	}

	/**
	 * Composite to create the controls
	 * @param parent
	 */
	protected abstract void createControlComposite(Composite parent);

	/**
	 * Create a set of Radio buttons given a list of Actions
	 * @param parent
	 * @param actions
	 * @throws Exception
	 */
	protected void createRadioControls(Composite parent, List<Entry<String, Action>> actions) throws Exception{
		if(actions == null) return;
		int i = 0;
		for (final Entry<String, Action> action : actions) {
			final Button radioButton = new Button(parent, SWT.RADIO);
			radioButton.setText(action.getKey());
			radioButton.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					widgetDefaultSelected(e);
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					if(((Button)e.getSource()).getSelection())
						action.getValue().run();
				}
			});
			if(i == 0)
				radioButton.setSelection(true);
			i++;
		}
	}

	/**
	 * Create a set of action items in a Combo box given a list of actions
	 * @param parent
	 * @param actions
	 * @throws Exception
	 */
	protected void createComboControls(Composite parent, final List<Entry<String, Action>> actions) throws Exception{
		if(actions == null) return;
		final Combo comboButton = new Combo(parent, SWT.BORDER);
		for (final Entry<String, Action> action : actions) {
			comboButton.add(action.getKey());
		}
		
		comboButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				widgetDefaultSelected(e);
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				int index = ((Combo)e.getSource()).getSelectionIndex();
				actions.get(index).getValue().run();
			}
		});
		comboButton.select(0);
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class clazz) {
		if (clazz == IToolPageSystem.class) {
			return profileContentComposite;
		} else {
			return super.getAdapter(clazz);
		}
	}

	@Override
	public final ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}

	@Override
	public void setFocus() {
		if (getControl()!=null && !getControl().isDisposed()) {
			getControl().setFocus();
		}
	}

	@Override
	public void activate() {
		super.activate();
		if (getPlottingSystem()!=null) {
			getPlottingSystem().addTraceListener(traceListener);
			getPlottingSystem().addRegionListener(regionListener);
		}
		setRegionsActive(true);

		createNewRegion();

	}

	@Override
	public void addDataset(IDataset data) {
		if(data == null) return;
		auxiliaryDatasets.put(data.getName(), data);
	}

	@Override
	public void removeDataset(IDataset data) {
		if(data == null) return;
		auxiliaryDatasets.remove(data.getName());
	}

	private final void createNewRegion() {
		// Start with a selection of the right type
		try {
			IRegion region = getPlottingSystem().createRegion(RegionUtils.getUniqueName(getRegionName(), getPlottingSystem()), getCreateRegionType());
			region.setUserObject(getMarker());
		} catch (Exception e) {
			logger.error("Cannot create region for profile tool!");
		}
	}

	/**
	 * The object used to mark this profile as being part of this tool.
	 * By default just uses package string.
	 * @return
	 */
	private Object getMarker() {
		return getToolPageRole().getClass().getName().intern();
	}

	public boolean isRegionTypeSupported(RegionType type) {
		return (type==RegionType.BOX)||(type==RegionType.PERIMETERBOX)||(type==RegionType.XAXIS)||(type==RegionType.YAXIS);
	}

	protected RegionType getCreateRegionType() {
		return RegionType.BOX;
	}

	@Override
	public void deactivate() {
		super.deactivate();
		if (getPlottingSystem()!=null) {
			getPlottingSystem().removeRegionListener(regionListener);
			getPlottingSystem().removeTraceListener(traceListener);
		}
		setRegionsActive(false);

		if(normProfilePlotSystem != null){
			normProfilePlotSystem.clear();
		}
		if(preNormProfilePlotSystem != null){
			preNormProfilePlotSystem.clear();
		}
	}
	
	private void setRegionsActive(boolean active) {
		if (getPlottingSystem()!=null) {
			final Collection<IRegion> regions = getPlottingSystem().getRegions();
			if (regions!=null) for (IRegion iRegion : regions) {
				if (active) {
					iRegion.addROIListener(this);
				} else {
					iRegion.removeROIListener(this);
				}
				if (iRegion.getUserObject()==getMarker()) {
					if (active) {
						iRegion.setVisible(active);
					} else {
						// If the plotting system has changed dimensionality
						// to something not compatible with us, remove the region.
						// TODO Change to having getRank() == rank 
						if (getToolPageRole().is2D() && !getPlottingSystem().is2D()) {
							iRegion.setVisible(active);
						} else if (getPlottingSystem().is2D() && !getToolPageRole().is2D()) {
							iRegion.setVisible(active);
						}
					}
				}
			}
		}
	}

	@Override
	public Control getControl() {
		return profileContentComposite;
	}

	@Override
	public void dispose() {
		deactivate();
		registeredTraces.clear();
		if (normProfilePlotSystem!=null) normProfilePlotSystem.dispose();
		normProfilePlotSystem = null;
		if (preNormProfilePlotSystem!=null) preNormProfilePlotSystem.dispose();
		preNormProfilePlotSystem = null;
		super.dispose();
	}

	@Override
	public IPlottingSystem getToolPlottingSystem() {
		return normProfilePlotSystem;
	}
	@Override
	public void roiSelected(ROIEvent evt) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isStaticTool() {
		return true;
	}

	protected void clearTraces(final IRegion region) {
		final String name = region.getName();
		Collection<ITrace> registered = this.registeredTraces.get(name);
		if (registered!=null) for (ITrace iTrace : registered) {
			normProfilePlotSystem.removeTrace(iTrace);
			preNormProfilePlotSystem.removeTrace(iTrace);
		}
	}

	protected void registerTraces(final IRegion region, final Collection<ITrace> traces) {
		final String name = region.getName();
		Collection<ITrace> registered = this.registeredTraces.get(name);
		if (registered==null) {
			registered = new HashSet<ITrace>(7);
			registeredTraces.put(name, registered);
		}
		for (ITrace iTrace : traces) iTrace.setUserObject(ProfileType.PROFILE);
		registered.addAll(traces);

		// Used to set the line on the image to the same color as the plot for line profiles only.
		if (!traces.isEmpty()) {
			final ITrace first = traces.iterator().next();
			if (isRegionTypeSupported(RegionType.LINE) && first instanceof ILineTrace && region.getName().startsWith(getRegionName())) {
				getControl().getDisplay().syncExec(new Runnable() {
					public void run() {
						region.setRegionColor(((ILineTrace)first).getTraceColor());
					}
				});
			}
		}
	}

	@Override
	public void roiDragged(ROIEvent evt) {
		region = (IRegion)evt.getSource();
		updatePreNormProfile(region, evt.getROI(), true);
	}

	@Override
	public void roiChanged(ROIEvent evt) {
		region = (IRegion)evt.getSource();
		updatePreNormProfile(region, region.getROI(), false);
	}

	/**
	 * Updates the normalization profiles
	 */
	protected synchronized void updateProfiles(){
		if (!isActive()) return;
		if(getRegion()== null)return;
		if(!isRegionTypeSupported(getRegion().getRegionType())) return;
		if (!getRegion().isUserRegion()) return;
		updatePreNormProfile.profile(getRegion(), getRegion().getROI(), false);
	}

	protected synchronized void updateNormProfile(IRegion r, IROI rb, boolean isDrag) {
		if (!isActive()) return;
		if (r!=null) {
			if(!isRegionTypeSupported(r.getRegionType())) return;
			if (!r.isUserRegion()) return;
		}
		updateNormProfile.profile(r, rb, isDrag);
	}

	protected synchronized void updatePreNormProfile(IRegion r, IROI rb, boolean isDrag) {
		if (!isActive()) return;
		if (r!=null) {
			if(!isRegionTypeSupported(r.getRegionType())) return;
			if (!r.isUserRegion()) return;
		}
		updatePreNormProfile.profile(r, rb, isDrag);
	}

	/**
	 * 
	 * @param plotter
	 */
	protected abstract void configureNormPlottingSystem(AbstractPlottingSystem plotter);

	/**
	 * 
	 * @param plotter
	 */
	protected abstract void configurePreNormPlottingSystem(AbstractPlottingSystem plotter);

	/**
	 * Creates the resulting normalization profile
	 * @param image
	 * @param region
	 * @param roi - may be null
	 * @param monitor
	 */
	protected abstract void createNormProfile(IImageTrace image, 
													  IRegion region, 
													  IROI roi, 
													  boolean tryUpdate, 
													  boolean isDrag,
													  IProgressMonitor monitor);

	/**
	 * Creates the pre-normalization profile
	 * @param image
	 * @param region
	 * @param roi - may be null
	 * @param monitor
	 */
	protected abstract void createPreNormProfile(IImageTrace image, 
													  IRegion region, 
													  IROI roi, 
													  boolean tryUpdate, 
													  boolean isDrag,
													  IProgressMonitor monitor);

	protected IRegion getRegion(){
		return region;
	}

	private final class NormProfileJob extends Job {
		private   IRegion                currentRegion;
		private   IROI                currentROI;
		private   boolean                isDrag;

		NormProfileJob() {
			super(getRegionName()+" update");
			setSystem(true);
			setUser(false);
			setPriority(Job.INTERACTIVE);
		}

		public void profile(IRegion r, IROI rb, boolean isDrag) {
			this.currentRegion = r;
			this.currentROI    = rb;
			this.isDrag        = isDrag;
			schedule();
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				if (!isActive()) return Status.CANCEL_STATUS;
				final Collection<ITrace> traces= getPlottingSystem().getTraces(IImageTrace.class);	
				IImageTrace image = traces!=null && traces.size()>0 ? (IImageTrace)traces.iterator().next() : null;
	
				if (monitor.isCanceled()) return  Status.CANCEL_STATUS;
				if (image==null) {
					normProfilePlotSystem.clear();
					return Status.OK_STATUS;
				}
				// if the current region is null try and update quickly (without creating 1D)
				// if the trace is in the registered traces object
				if (currentRegion==null) {
					final Collection<IRegion> regions = getPlottingSystem().getRegions();
					if (regions!=null) {
						for (IRegion iRegion : regions) {
							if (!iRegion.isUserRegion()) continue;
							if (monitor.isCanceled()) return  Status.CANCEL_STATUS;
							createNormProfile(image, iRegion, iRegion.getROI(), true, isDrag, monitor);
						}
					} else {
						normProfilePlotSystem.clear();
					}
				} else {
					if (monitor.isCanceled()) return  Status.CANCEL_STATUS;
					createNormProfile(image, currentRegion, currentROI!=null?currentROI:currentRegion.getROI(), 
							true, isDrag, monitor);
				}
				if (monitor.isCanceled()) return Status.CANCEL_STATUS;
				normProfilePlotSystem.repaint();
				return Status.OK_STATUS;
			} catch (Throwable ne) {
				logger.error("Internal error processing profile! ", ne);
				return Status.CANCEL_STATUS;
			}
		}
	}

	private final class PreNormProfileJob extends Job {
		private   IRegion                currentRegion;
		private   IROI                currentROI;
		private   boolean                isDrag;

		PreNormProfileJob() {
			super(getRegionName()+" pre norm update");
			setSystem(true);
			setUser(false);
			setPriority(Job.INTERACTIVE);
		}

		public void profile(IRegion r, IROI rb, boolean isDrag) {
			this.currentRegion = r;
			this.currentROI    = rb;
			this.isDrag        = isDrag;
			schedule();
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				if (!isActive()) return Status.CANCEL_STATUS;
	
				final Collection<ITrace> traces= getPlottingSystem().getTraces(IImageTrace.class);	
				IImageTrace image = traces!=null && traces.size()>0 ? (IImageTrace)traces.iterator().next() : null;
	
				if (monitor.isCanceled()) return  Status.CANCEL_STATUS;
				if (image==null) {
					preNormProfilePlotSystem.clear();
					return Status.OK_STATUS;
				}
				if (currentRegion==null) {
					final Collection<IRegion> regions = getPlottingSystem().getRegions();
					if (regions!=null) {
						for (IRegion iRegion : regions) {
							if (!iRegion.isUserRegion()) continue;
							if (monitor.isCanceled()) return  Status.CANCEL_STATUS;
							if (registeredTraces.containsKey(iRegion.getName())) {
								createPreNormProfile(image, iRegion, iRegion.getROI(), true, isDrag, monitor);
							} else {
								createPreNormProfile(image, iRegion, iRegion.getROI(), false, isDrag, monitor);
							}
						}
					} else {
						registeredTraces.clear();
						preNormProfilePlotSystem.clear();
					}
				} else {
					if (monitor.isCanceled()) return  Status.CANCEL_STATUS;
					createPreNormProfile(image, currentRegion, currentROI!=null?currentROI:currentRegion.getROI(), 
							true, isDrag, monitor);
				}
				if (monitor.isCanceled()) return Status.CANCEL_STATUS;
				preNormProfilePlotSystem.repaint();
				return Status.OK_STATUS;
			} catch (Throwable ne) {
				logger.error("Internal error processing profile! ", ne);
				return Status.CANCEL_STATUS;
			}
		}
	}
}
