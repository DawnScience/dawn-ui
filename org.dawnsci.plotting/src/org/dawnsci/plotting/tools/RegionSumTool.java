package org.dawnsci.plotting.tools;

import java.text.DecimalFormat;
import java.util.Collection;

import org.dawb.common.ui.menu.CheckableActionGroup;
import org.dawb.common.ui.menu.MenuAction;
import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.widgets.FontExtenderWidget;
import org.dawb.common.util.number.DoubleUtils;
import org.dawnsci.plotting.Activator;
import org.dawnsci.plotting.api.region.IROIListener;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegion.RegionType;
import org.dawnsci.plotting.api.region.IRegionListener;
import org.dawnsci.plotting.api.region.ROIEvent;
import org.dawnsci.plotting.api.region.RegionEvent;
import org.dawnsci.plotting.api.region.RegionUtils;
import org.dawnsci.plotting.api.tool.AbstractToolPage;
import org.dawnsci.plotting.api.tool.ToolPageFactory;
import org.dawnsci.plotting.api.trace.IImageTrace;
import org.dawnsci.plotting.api.trace.ITraceListener;
import org.dawnsci.plotting.api.trace.TraceEvent;
import org.dawnsci.plotting.views.RegionSumView;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IPageSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

/**
 * Region sum tool displays the sum of a RectangularROI
 * 
 * @author wqk87977
 *
 */
public class RegionSumTool extends AbstractToolPage implements IROIListener {

	private final static Logger logger = LoggerFactory.getLogger(RegionSumTool.class);

	private FontExtenderWidget sumDisplay;

	private int precision = 3;

	private String sumStr = "";

	private SumJob sumJob;

	private IRegionListener regionListener;

	private Composite parent;

	private boolean isSciNotation;

	private RectangularROI currentROI;

	private DecimalFormat sciNotationFormat = new DecimalFormat("0.######E0");

	private DecimalFormat normNotationFormat = new DecimalFormat("###.#####");

	private IRegion region;

	private ITraceListener traceListener;

	public RegionSumTool(){
		this.sumJob = new SumJob();
		this.regionListener = new IRegionListener.Stub() {
			@Override
			public void regionRemoved(RegionEvent evt) {
				if (evt.getRegion()!=null) {
					evt.getRegion().removeROIListener(RegionSumTool.this);
				}
			}
			
			@Override
			public void regionsRemoved(RegionEvent evt) {}
			
			@Override
			public void regionAdded(RegionEvent evt) {
				if (evt.getRegion()!=null) {
//					RegionSumTool.this.update(image, null, null, false);
				}
			}
			
			@Override
			public void regionCreated(RegionEvent evt) {
				if (evt.getRegion()!=null) {
					evt.getRegion().addROIListener(RegionSumTool.this);
				}
			}
			
			protected void update(RegionEvent evt) {
//				RegionSumTool.this.update(image, null, null, false);
			}
		};

		this.traceListener = new ITraceListener.Stub() {
			@Override
			public void tracesAdded(TraceEvent evt) {}

			@Override
			protected void update(TraceEvent evt) {
				RegionSumTool.this.update(region, currentROI, false);
			}
		};
	}

	@Override
	public void createControl(Composite parent) {
		this.parent = parent;
		createActions(getSite());
		sumDisplay = new FontExtenderWidget(parent, SWT.FILL, "Sum");
	}

	private void createActions(IPageSite site) {

		final Clipboard cb = new Clipboard(parent.getDisplay());
		final Action fontChooser = new Action("Configure Font", IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				FontDialog dlg = new FontDialog(parent.getShell());

				FontData fontData = dlg.open();
				if (fontData != null) {
					if (sumDisplay.getFont() != null)
						sumDisplay.getFont().dispose();
					sumDisplay.setFont(new Font(parent.getShell().getDisplay(), fontData));
					sumDisplay.update(sumDisplay.getText());
				}
			}
		};
		fontChooser.setToolTipText("Configure Font");
		fontChooser.setText("Font");
		fontChooser.setImageDescriptor(Activator.getImageDescriptor("icons/font.gif"));

		final Action sumCopy = new Action("Copy Sum", IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				TextTransfer textTransfer = TextTransfer.getInstance();
				cb.setContents(new Object[] { sumDisplay.getText() },
						new Transfer[] { textTransfer });
			}
		};
		sumCopy.setToolTipText("Copy Sum to Clipboard");
		sumCopy.setText("Copy");
		sumCopy.setImageDescriptor(Activator.getImageDescriptor("icons/copy.gif"));

		final Action sciNotation = new Action("Scientific Notation", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				// update the Region Sum
				String str = "";
				for(int j = 0; j<precision; j++) str += "#";
				DecimalFormat sciNotationFormat = new DecimalFormat("0."+str+"E0");
				if (currentROI != null)
					update(null, currentROI, false);
				double value = DoubleUtils.roundDouble(Double.valueOf(sumStr), precision);
				if(isChecked()){
					isSciNotation = true;
					sumStr = sciNotationFormat.format(value);
					sumDisplay.update(sumStr);
				} else {
					isSciNotation = false;
					sumStr = normNotationFormat.format(value);
					sumDisplay.update(sumStr);
				}
			}
		};
		sciNotation.setToolTipText("Toggle On/Off Scientific Notation");
		sciNotation.setText("E");
		sciNotation.setImageDescriptor(Activator.getImageDescriptor("icons/SciNotation.png"));

		final MenuAction decimalDropDown = new MenuAction("Decimal Precision");
		CheckableActionGroup group      = new CheckableActionGroup();
		IAction selectedAction  = null;
		for(int i=0; i<10; i++){
			final int precis = i;
			Action decimalPrecision = new Action(String.valueOf(i), IAction.AS_CHECK_BOX) {
				@Override
				public void run() {
					precision = precis;
					String str = "";
					for(int j = 0; j<precis; j++) str += "#";
					DecimalFormat sciNotationFormat = new DecimalFormat("0."+str+"E0");
					// update the Region Sum
					if (currentROI != null)
						update(null, currentROI, false);
					double value = DoubleUtils.roundDouble(Double.valueOf(sumStr), precis);
					if(sciNotation.isChecked()){
						sumStr = sciNotationFormat.format(value);
					} else {
						sumStr = normNotationFormat.format(value);
					}
					sumDisplay.update(sumStr);
					setChecked(true);
				}
			};
			// select 3 by default
			if(i == precision)
				selectedAction = decimalPrecision;
			
			decimalDropDown.add(decimalPrecision);
			group.add(decimalPrecision);
		}
		if (selectedAction!=null) selectedAction.setChecked(true);
		decimalDropDown.setToolTipText("Adjust decimal precision for non-scientific notation");
		decimalDropDown.setText("#.#");
		decimalDropDown.setImageDescriptor(Activator.getImageDescriptor("icons/edit_decimal.png"));

		final Action autoResize = new Action("Automatic Resize", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				if(isChecked()){
					sumDisplay.setAutoResize(true);
				} else {
					sumDisplay.setAutoResize(false);
				}
				sumDisplay.update(sumStr);
			}
		};
		autoResize.setToolTipText("Toggle On/Off the Font automatic resize");
		autoResize.setText("Auto");
		autoResize.setImageDescriptor(Activator.getImageDescriptor("icons/FontAutoResize.png"));

		// if site is null, the tool has been called programmatically
		if(site != null){
			getSite().getActionBars().getToolBarManager().removeAll();
			IToolBarManager toolMan = getSite().getActionBars().getToolBarManager();
			MenuManager menuMan = new MenuManager();
			toolMan.add(fontChooser);
			menuMan.add(fontChooser);
			toolMan.add(sumCopy);
			menuMan.add(sumCopy);
			toolMan.add(sciNotation);
			menuMan.add(sciNotation);
			toolMan.add(decimalDropDown);
			menuMan.add(decimalDropDown);
			toolMan.add(autoResize);
			menuMan.add(autoResize);
		} else {
			//Action to open the tool in a separate view
			final Action openViewTool = new Action("Open Tool in a separate view", IAction.AS_PUSH_BUTTON) {
				@Override
				public void run() {
					try {
						// If view already opened do nothing
						IViewReference[] viewRefs = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
						for (IViewReference iViewReference : viewRefs) {
							if(iViewReference.getId().equals("uk.ac.diamond.scisoft.arpes.regionSumView")) return;
						}
						RegionSumTool roiSumProfile = (RegionSumTool)ToolPageFactory.getToolPage("org.dawb.workbench.plotting.tools.regionSumTool");
						roiSumProfile.setToolSystem((AbstractPlottingSystem)getPlottingSystem());
						roiSumProfile.setPlottingSystem((AbstractPlottingSystem)getPlottingSystem());
						roiSumProfile.setTitle("Region_Sum_View");
						roiSumProfile.setToolId(String.valueOf(roiSumProfile.hashCode()));
						RegionSumView viewPart = (RegionSumView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("uk.ac.diamond.scisoft.arpes.regionSumView");
						roiSumProfile.createControl(viewPart.getComposite());
						roiSumProfile.activate();
						// update the sum profile
						roiSumProfile.createProfile(getImageTrace(), region, currentROI, true, false, null);
						// refresh the layout
						viewPart.getComposite().layout();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			openViewTool.setToolTipText("Open Tool in a separate view");
			openViewTool.setText("Open");
			openViewTool.setImageDescriptor(Activator.getImageDescriptor("icons/openbrwsr.gif"));

			ToolBarManager toolMan = new ToolBarManager(SWT.FLAT | SWT.LEFT | SWT.WRAP);
			Control tb = toolMan.createControl(parent);
			tb.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

			MenuManager menuMan = new MenuManager();
			Control mb = toolMan.createControl(parent);
			mb.setLayoutData(new GridData(SWT.FLAT | SWT.RIGHT, SWT.TOP, true, false));

			toolMan.add(fontChooser);
			menuMan.add(fontChooser);
			toolMan.add(sumCopy);
			menuMan.add(sumCopy);
			toolMan.add(sciNotation);
			menuMan.add(sciNotation);
			toolMan.add(decimalDropDown);
			menuMan.add(decimalDropDown);
			toolMan.add(autoResize);
			menuMan.add(autoResize);
			toolMan.add(openViewTool);
			menuMan.add(openViewTool);

			toolMan.update(true);
			menuMan.update(true);
		}
	}

	@Override
	public void deactivate() {
		super.deactivate();
		if (getPlottingSystem()!=null) {
			getPlottingSystem().removeTraceListener(traceListener);
			getPlottingSystem().removeRegionListener(regionListener);
		}
		setRegionsActive(false);
	}

	@Override
	public void activate() {
		super.activate();
		if (getPlottingSystem()!=null) {
			getPlottingSystem().addTraceListener(traceListener);
			getPlottingSystem().addRegionListener(regionListener);
		}
		setRegionsActive(true);
		sumDisplay.update(sumDisplay.getText());
		//createNewRegion();
	}

	@SuppressWarnings("unused")
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
	protected Object getMarker() {
		return getToolPageRole().getClass().getName().intern();
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
		return sumDisplay;
	}

	@Override
	public void dispose() {
		deactivate();
		if (sumDisplay!=null) sumDisplay.dispose();
		sumDisplay = null;
		super.dispose();
	}

	public boolean isRegionTypeSupported(RegionType type) {
		return type==RegionType.BOX||type==RegionType.PERIMETERBOX;
	}

	public RegionType getCreateRegionType() {
		return RegionType.BOX;
	}

	/**
	 * 
	 * @param image
	 * @param region
	 * @param roi - may be null
	 * @param monitor
	 */
	public void createProfile(final IImageTrace image, 
			                              IRegion region, 
			                              ROIBase roi, 
			                              boolean tryUpdate, 
			                              boolean isDrag,
			                              IProgressMonitor monitor){
		if (monitor!= null && monitor.isCanceled()) return;
		if (image==null) return;
		
		if (!isRegionTypeSupported(region.getRegionType())) return;

		final RectangularROI bounds = (RectangularROI) (roi==null ? region.getROI() : roi);
		if (bounds==null) return;
		if (!region.isVisible()) return;

		if (monitor!= null && monitor.isCanceled()) return;
		// sum profile
		updateSum(image, bounds, region, tryUpdate, monitor);
	}

	private void updateSum(final IImageTrace image, 
						final RectangularROI bounds, 
						IRegion region, 
						boolean tryUpdate, 
						IProgressMonitor monitor){
		int xStartPt = (int) bounds.getPoint()[0];
		int yStartPt = (int) bounds.getPoint()[1];
		int xStopPt = (int) bounds.getEndPoint()[0];
		int yStopPt = (int) bounds.getEndPoint()[1];
		int xInc = bounds.getPoint()[0]<bounds.getEndPoint()[0] ? 1 : -1;
		int yInc = bounds.getPoint()[1]<bounds.getEndPoint()[1] ? 1 : -1;

		if(image == null) return;
		AbstractDataset dataRegion = (AbstractDataset)image.getData();

		if(dataRegion == null) return;
		try {
			dataRegion = dataRegion.getSlice(
					new int[] { yStartPt, xStartPt },
					new int[] { yStopPt, xStopPt },
					new int[] {yInc, xInc});
			if (monitor!= null && monitor.isCanceled()) return;
		} catch (IllegalArgumentException e) {
			logger.debug("Error getting region data:"+ e);
		}
		//round the Sum to n decimal
		double value = DoubleUtils.roundDouble((Double)dataRegion.sum(true), precision);

		if(isSciNotation){
			sumStr = sciNotationFormat.format(value);
		} else {
			sumStr = normNotationFormat.format(value);
		}

		if (monitor!= null && monitor.isCanceled()) return;

		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				sumDisplay.update(sumStr);
			}
		});
	}

	@Override
	public void roiSelected(ROIEvent evt) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isStaticTool() {
		return true;
	}

	@Override
	public ToolPageRole getToolPageRole() {
			return ToolPageRole.ROLE_2D;
	}

	@Override
	public void roiDragged(ROIEvent evt) {
		if(evt.getROI() instanceof RectangularROI){
			currentROI = (RectangularROI)evt.getROI();
			update((IRegion)evt.getSource(), currentROI, true);
		}
	}

	@Override
	public void roiChanged(ROIEvent evt) {
		region = (IRegion)evt.getSource();
		if(region.getROI() instanceof RectangularROI){
			currentROI = (RectangularROI)region.getROI();
			update(region, currentROI, false);
		}
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

	protected String getRegionName() {
		return "Sum";
	}

	protected synchronized void update(IRegion r, RectangularROI rb, boolean isDrag) {
		if (!isActive()) return;
		if (r!=null) {
			if(!isRegionTypeSupported(r.getRegionType())) return; // Nothing to do.
			if (!r.isUserRegion()) return; // Likewise
		}
		if(rb == null) return;
		sumJob.profile(r, rb, isDrag);
	}

	private final class SumJob extends Job {
		
		private   IRegion                currentRegion;
		private   RectangularROI currentROI;
		private   boolean                isDrag;

		SumJob() {
			super(getRegionName()+" update");
			setSystem(true);
			setUser(false);
			setPriority(Job.INTERACTIVE);
		}

		public void profile(IRegion r, RectangularROI rb, boolean isDrag) {
			this.currentRegion = r;
			this.currentROI    = rb;
			this.isDrag        = isDrag;
			schedule();
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
//			Collection<ITrace> traces = getPlottingSystem().getTraces();
				
//			Iterator<ITrace> it = traces.iterator();
//			if (monitor.isCanceled()) return Status.CANCEL_STATUS;
//			while (it.hasNext()) {
//				ITrace iTrace = (ITrace) it.next();
//				if(iTrace instanceof IImageTrace){
					updateSum(getImageTrace(), currentROI, currentRegion, isDrag, monitor);
//				}
//			}
			return Status.OK_STATUS;
		}
	}
}
