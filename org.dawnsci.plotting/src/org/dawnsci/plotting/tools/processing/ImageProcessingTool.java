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
import org.eclipse.core.runtime.jobs.Job;
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
 * 
 * @author wqk87977
 *
 */
public abstract class ImageProcessingTool extends AbstractToolPage  implements IROIListener, IAuxiliaryToolDataset {
	
	private static Logger logger = LoggerFactory.getLogger(ImageProcessingTool.class);

	protected AbstractPlottingSystem profilePlottingSystem;
	protected AbstractPlottingSystem displayPlottingSystem;

	private List<Entry<String,Action>> radioActions;
	private List<Entry<String, Action>> comboActions;

	private HashMap<String, Collection<ITrace>> registeredTraces;
	private IRegionListener regionListener;
	private Composite profileContentComposite;
	private ProfileJob updateProfiles;
	private ITraceListener traceListener;

	private HashMap<String, IDataset> auxiliaryDatasets = new HashMap<String, IDataset>();

	public ImageProcessingTool(){

		this.registeredTraces = new HashMap<String,Collection<ITrace>>(7);
		try {
			profilePlottingSystem = PlottingFactory.createPlottingSystem();
			displayPlottingSystem = PlottingFactory.createPlottingSystem();
			updateProfiles = new ProfileJob();
			this.regionListener = new IRegionListener.Stub() {
				@Override
				public void regionRemoved(RegionEvent evt) {
					if (evt.getRegion()!=null) {
						evt.getRegion().removeROIListener(ImageProcessingTool.this);
					}
				}
				@Override
				public void regionsRemoved(RegionEvent evt) {
					//clears traces if all regions removed
					final Collection<IRegion> regions = getPlottingSystem().getRegions();
					if (regions == null || regions.isEmpty()) {
						registeredTraces.clear();
						profilePlottingSystem.clear();
						displayPlottingSystem.clear();
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
					ImageProcessingTool.this.update(null, null, false);
				}
				@Override
				protected void update(TraceEvent evt) {
					ImageProcessingTool.this.update(null, null, false);
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
		
		profilePlottingSystem.createPlotPart(profilePlotComposite, 
				 getTitle(), 
				 getSite().getActionBars(), 
				 PlotType.XY,
				 null);
		configurePlottingSystem(profilePlottingSystem);
		// Unused actions removed for tool
		profilePlottingSystem.getPlotActionSystem().remove("org.dawb.workbench.plotting.rescale");
		profilePlottingSystem.getPlotActionSystem().remove("org.dawb.workbench.plotting.plotIndex");
		profilePlottingSystem.getPlotActionSystem().remove("org.dawb.workbench.plotting.plotX");
		profilePlottingSystem.setXfirst(true);
		profilePlottingSystem.setRescale(true);

		//bottom part
		SashForm bottomSashForm = new SashForm(mainSashForm, SWT.HORIZONTAL);
		bottomSashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		bottomSashForm.setBackground(new Color(parent.getDisplay(), 192, 192, 192));

		Composite displayComp = new Composite(bottomSashForm, SWT.NONE);
		displayComp.setLayout(new GridLayout(1, false));
		GridUtils.removeMargins(displayComp);
		ActionBarWrapper actionBarWrapper = ActionBarWrapper.createActionBars(displayComp, null);
		Composite displayPlotComp  = new Composite(displayComp, SWT.BORDER);
		displayPlotComp.setLayout(new FillLayout());
		displayPlotComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		displayPlottingSystem.createPlotPart(displayPlotComp, 
												 "User display", 
												 actionBarWrapper, 
												 PlotType.XY, 
												 null);
		configureDisplayPlottingSystem(displayPlottingSystem);
		Composite radioControlComp = new Composite(bottomSashForm, SWT.NONE);
		radioControlComp.setLayout(new GridLayout(1, false));
		radioControlComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		try {
			createRadioControls(radioControlComp, getRadioActions());
			createComboControls(radioControlComp, getComboActionS());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error while creating Controls:"+e);
		}

		mainSashForm.setWeights(new int[]{1, 1});
		parent.layout();
	}

	protected void createRadioControls(Composite parent, List<Entry<String, Action>> actions) throws Exception{
		if(actions == null) return;
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
		}
	}

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
	}

	public List<Entry<String,Action>> getRadioActions() {
		return radioActions;
	}

	public void setRadioActions(List<Entry<String,Action>> radioActions) {
		this.radioActions = radioActions;
	}

	public List<Entry<String, Action>> getComboActionS() {
		return comboActions;
	}

	public void setComboActions(List<Entry<String,Action>> comboActions) {
		this.comboActions = comboActions;
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

	private RegionType getCreateRegionType() {
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

		if(profilePlottingSystem != null){
			profilePlottingSystem.clear();
		}
		if(displayPlottingSystem != null){
			displayPlottingSystem.clear();
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
		if (profilePlottingSystem!=null) profilePlottingSystem.dispose();
		profilePlottingSystem = null;
		if (displayPlottingSystem!=null) displayPlottingSystem.dispose();
		displayPlottingSystem = null;
		super.dispose();
	}

	@Override
	public IPlottingSystem getToolPlottingSystem() {
		return profilePlottingSystem;
	}
	@Override
	public void roiSelected(ROIEvent evt) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isStaticTool() {
		return true;
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
		update((IRegion)evt.getSource(), evt.getROI(), true);
	}

	@Override
	public void roiChanged(ROIEvent evt) {
		final IRegion region = (IRegion)evt.getSource();
		update(region, region.getROI(), false);
	}

	protected synchronized void update(IRegion r, IROI rb, boolean isDrag) {
		if (!isActive()) return;
		if (r!=null) {
			if(!isRegionTypeSupported(r.getRegionType())) return;
			if (!r.isUserRegion()) return;
		}
		updateProfiles.profile(r, rb, isDrag);
	}

	/**
	 * 
	 * @param plotter
	 */
	protected abstract void configurePlottingSystem(AbstractPlottingSystem plotter);

	/**
	 * 
	 * @param plotter
	 */
	protected abstract void configureDisplayPlottingSystem(AbstractPlottingSystem plotter);

	/**
	 * Creates the profile
	 * @param image
	 * @param region
	 * @param roi - may be null
	 * @param monitor
	 */
	protected abstract void createProfile(IImageTrace image, 
													  IRegion region, 
													  IROI roi, 
													  boolean tryUpdate, 
													  boolean isDrag,
													  IProgressMonitor monitor);

	/**
	 * Creates the display profile
	 * @param image
	 * @param region
	 * @param roi - may be null
	 * @param monitor
	 */
	protected abstract void createDisplayProfile(IImageTrace image, 
													  IRegion region, 
													  IROI roi, 
													  boolean tryUpdate, 
													  boolean isDrag,
													  IProgressMonitor monitor);

	private final class ProfileJob extends Job {
		private   IRegion                currentRegion;
		private   IROI                currentROI;
		private   boolean                isDrag;

		ProfileJob() {
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
					profilePlottingSystem.clear();
					displayPlottingSystem.clear();
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
							if (registeredTraces.containsKey(iRegion.getName())) {
								createProfile(image, iRegion, iRegion.getROI(), true, isDrag, monitor);
								createDisplayProfile(image, iRegion, iRegion.getROI(), true, isDrag, monitor);
							} else {
								createProfile(image, iRegion, iRegion.getROI(), false, isDrag, monitor);
								createDisplayProfile(image, iRegion, iRegion.getROI(), false, isDrag, monitor);
							}
						}
					} else {
						registeredTraces.clear();
						profilePlottingSystem.clear();
						displayPlottingSystem.clear();
					}
				} else {
	
					if (monitor.isCanceled()) return  Status.CANCEL_STATUS;
					createProfile(image, currentRegion, currentROI!=null?currentROI:currentRegion.getROI(), 
									true, isDrag, monitor);
					createDisplayProfile(image, currentRegion, currentROI!=null?currentROI:currentRegion.getROI(), 
							true, isDrag, monitor);
				}
				if (monitor.isCanceled()) return Status.CANCEL_STATUS;
				profilePlottingSystem.repaint();
				displayPlottingSystem.repaint();
				return Status.OK_STATUS;
			} catch (Throwable ne) {
				logger.error("Internal error processing profile! ", ne);
				return Status.CANCEL_STATUS;
			}
		}
	}
}
