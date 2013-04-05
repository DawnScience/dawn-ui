package org.dawnsci.plotting.tools.profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.IPlottingSystem;
import org.dawb.common.ui.plot.PlotType;
import org.dawb.common.ui.plot.PlottingFactory;
import org.dawb.common.ui.plot.axis.IAxis;
import org.dawb.common.ui.plot.region.IROIListener;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.region.IRegionListener;
import org.dawb.common.ui.plot.region.ROIEvent;
import org.dawb.common.ui.plot.region.RegionEvent;
import org.dawb.common.ui.plot.region.RegionUtils;
import org.dawb.common.ui.plot.tool.AbstractToolPage;
import org.dawb.common.ui.plot.tool.IToolPageSystem;
import org.dawb.common.ui.plot.tool.ToolPageFactory;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.plot.trace.ILineTrace;
import org.dawb.common.ui.plot.trace.IPaletteListener;
import org.dawb.common.ui.plot.trace.ITrace;
import org.dawb.common.ui.plot.trace.ITraceListener;
import org.dawb.common.ui.plot.trace.PaletteEvent;
import org.dawb.common.ui.plot.trace.TraceEvent;
import org.dawb.common.ui.widgets.ROIWidget;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Slice;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile.BoxLineType;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

/**
 * Perimeter Box tool that creates a tool page with 3 plotting systems and a composite:<br>
 * - a zoom profile<br>
 * - an XY plot that shows the line profiles of the vertical edges of the box<br>
 * - an XY plot that show the line profiles of the horizontal edges of the box<br>
 * - a composite with table viewers used to display and edit the box X/Y points and width/height.<br>
 * 
 * @author wqk87977
 *
 */
public class PerimeterBoxProfileTool extends AbstractToolPage  implements IROIListener {

	private final static Logger logger = LoggerFactory.getLogger(PerimeterBoxProfileTool.class);
	
	private AbstractPlottingSystem zoomProfilePlottingSystem;
	private AbstractPlottingSystem verticalProfilePlottingSystem;
	private AbstractPlottingSystem horizontalProfilePlottingSystem;
	private ITraceListener         traceListener;
	private IRegionListener        regionListener;
	private IPaletteListener       paletteListener;
	private ProfileJob             updateProfiles;
	private Map<String,Collection<ITrace>> registeredTraces;

	private ROIWidget myROIWidget;

	private ROIWidget verticalProfileROIWidget;

	private ROIWidget horizontalProfileROIWidget;

	private IAxis xPixelAxisHorizontal;
	private IAxis yPixelAxisHorizontal;
	private IAxis xPixelAxisVertical;
	private IAxis yPixelAxisVertical;

	private Composite profileContentComposite;

	private AbstractToolPage roiSumProfile;

	public PerimeterBoxProfileTool() {
		
		this.registeredTraces = new HashMap<String,Collection<ITrace>>(7);
		try {
			zoomProfilePlottingSystem = PlottingFactory.createPlottingSystem();
			verticalProfilePlottingSystem = PlottingFactory.createPlottingSystem();
			horizontalProfilePlottingSystem = PlottingFactory.createPlottingSystem();
			updateProfiles = new ProfileJob();

			this.paletteListener = new IPaletteListener.Stub() {
				@Override
				public void maskChanged(PaletteEvent evt) {
					update(null, null, false);
				}
				@Override
				public void imageOriginChanged(PaletteEvent evt) {
					update(null, null, false);
				}
			};

			this.traceListener = new ITraceListener.Stub() {
				@Override
				public void tracesAdded(TraceEvent evt) {
					
					if (!(evt.getSource() instanceof List<?>)) {
						return;
					}
					if (getImageTrace()!=null) getImageTrace().addPaletteListener(paletteListener);
					PerimeterBoxProfileTool.this.update(null, null, false);
				}
				@Override
				protected void update(TraceEvent evt) {
					PerimeterBoxProfileTool.this.update(null, null, false);
				}
			};

			this.regionListener = new IRegionListener.Stub() {
				@Override
				public void regionRemoved(RegionEvent evt) {
					if (evt.getRegion()!=null) {
						evt.getRegion().removeROIListener(PerimeterBoxProfileTool.this);
					}
				}
				@Override
				public void regionsRemoved(RegionEvent evt) {
					//clears traces if all regions removed
					final Collection<IRegion> regions = getPlottingSystem().getRegions();
					if (regions == null || regions.isEmpty()) {
						registeredTraces.clear();
						zoomProfilePlottingSystem.clear();
						verticalProfilePlottingSystem.clear();
						horizontalProfilePlottingSystem.clear();
					}
				}
				@Override
				public void regionAdded(RegionEvent evt) {
					if (evt.getRegion()!=null) {
						PerimeterBoxProfileTool.this.update(null, null, false);
					}
				}
				@Override
				public void regionCreated(RegionEvent evt) {
					if (evt.getRegion()!=null) {
						evt.getRegion().addROIListener(PerimeterBoxProfileTool.this);
					}
				}
				protected void update(RegionEvent evt) {
					PerimeterBoxProfileTool.this.update(null, null, false);
				}
			};
		} catch (Exception e) {
			logger.error("Cannot get plotting system!", e);
		}
	}

	@Override
	public void createControl(Composite parent) {

		profileContentComposite = new Composite(parent, SWT.NONE);
		profileContentComposite.setLayout(new GridLayout(1, true));
		SashForm sashForm = new SashForm(profileContentComposite, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sashForm.setBackground(new Color(parent.getDisplay(), 192, 192, 192));

		SashForm sashForm2 = new SashForm(sashForm, SWT.VERTICAL);
		sashForm2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sashForm2.setBackground(new Color(parent.getDisplay(), 192, 192, 192));
		SashForm sashForm3 = new SashForm(sashForm, SWT.VERTICAL);
		sashForm3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sashForm3.setBackground(new Color(parent.getDisplay(), 192, 192, 192));

		try {
			// we create the zool plot part
			zoomProfilePlottingSystem.createPlotPart(sashForm2, getTitle()+" zoom", 
					 getSite()!=null?getSite().getActionBars():null, PlotType.IMAGE, null);
			zoomProfilePlottingSystem.setXfirst(true);
			zoomProfilePlottingSystem.setRescale(true);
			zoomProfilePlottingSystem.setKeepAspect(false);

			// We create the profiles plot part and set the axis
			verticalProfilePlottingSystem.createPlotPart(sashForm3, getTitle()+" vertical", null, PlotType.XY, null);
			if (xPixelAxisVertical == null)
				this.xPixelAxisVertical = verticalProfilePlottingSystem.getSelectedXAxis();
			if (yPixelAxisVertical == null) {
				verticalProfilePlottingSystem.getSelectedYAxis().setTitle("Intensity");
				this.yPixelAxisVertical = verticalProfilePlottingSystem.getSelectedYAxis();
			}
			verticalProfilePlottingSystem.setTitle("Vertical profiles");

			horizontalProfilePlottingSystem.createPlotPart(sashForm2, getTitle()+" horizontal", null, PlotType.XY, null);
			if (xPixelAxisHorizontal == null)
				this.xPixelAxisHorizontal = horizontalProfilePlottingSystem.getSelectedXAxis();
			if (yPixelAxisHorizontal == null) {
				horizontalProfilePlottingSystem.getSelectedYAxis().setTitle("Intensity");
				this.yPixelAxisHorizontal = horizontalProfilePlottingSystem.getSelectedYAxis();
			}
			horizontalProfilePlottingSystem.setTitle("Horizontal profiles");

			//start: we create the ROI information composite
			final ScrolledComposite scrollComposite = new ScrolledComposite(sashForm3, SWT.H_SCROLL | SWT.V_SCROLL);
			final Composite contentComposite = new Composite(scrollComposite, SWT.FILL);
			contentComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
			contentComposite.setLayout(new GridLayout(1, false));

			ExpansionAdapter expansionAdapter = new ExpansionAdapter() {
				@Override
				public void expansionStateChanged(ExpansionEvent e) {
					logger.trace("regionsExpander");
					Rectangle r = scrollComposite.getClientArea();
					scrollComposite.setMinSize(contentComposite.computeSize(r.width, SWT.DEFAULT));
					contentComposite.layout();
				}
			};

			Label metadataLabel = new Label(contentComposite, SWT.NONE);
			metadataLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
			metadataLabel.setAlignment(SWT.CENTER);
			metadataLabel.setText("Region Of Interest Information");

			//main roi
			ExpandableComposite mainRegionInfoExpander = new ExpandableComposite(contentComposite, SWT.NONE);
			mainRegionInfoExpander.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
			mainRegionInfoExpander.setLayout(new GridLayout(1, false));
			mainRegionInfoExpander.setText("Colour Box ROI");

			Composite mainRegionComposite = new Composite(mainRegionInfoExpander, SWT.NONE);
			GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
			mainRegionComposite.setLayout(new GridLayout(1, false));
			mainRegionComposite.setLayoutData(gridData);

			myROIWidget = new ROIWidget(mainRegionComposite, (AbstractPlottingSystem) getPlottingSystem(), "Perimeter Box region editor");
			myROIWidget.createWidget();
			myROIWidget.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					ROIBase newRoi = myROIWidget.getROI();
					String regionName = myROIWidget.getRegionName();
					IRegion region = getPlottingSystem().getRegion(regionName);
					if(region != null){
						region.setROI(newRoi);
					}
				}
			});

			Group regionSumGroup = new Group(mainRegionComposite, SWT.NONE);
			regionSumGroup.setText("Sum");
			gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
			regionSumGroup.setLayout(new GridLayout(1, false));
			regionSumGroup.setLayoutData(gridData);
			roiSumProfile = (AbstractToolPage)ToolPageFactory.getToolPage("org.dawb.workbench.plotting.tools.regionSumTool");
			roiSumProfile.setToolSystem((AbstractPlottingSystem) getPlottingSystem());
			roiSumProfile.setPlottingSystem((AbstractPlottingSystem) getPlottingSystem());
			roiSumProfile.setTitle("Region_Sum");
			//roiSumProfile.setPart((IViewPart)getGuiManager());
			roiSumProfile.setToolId(String.valueOf(roiSumProfile.hashCode()));
			roiSumProfile.createControl(regionSumGroup);
			roiSumProfile.activate();

			mainRegionInfoExpander.setClient(mainRegionComposite);
			mainRegionInfoExpander.addExpansionListener(expansionAdapter);
			mainRegionInfoExpander.setExpanded(true);

			//vertical xaxis roi
			ExpandableComposite verticalRegionInfoExpander = new ExpandableComposite(contentComposite, SWT.NONE);
			verticalRegionInfoExpander.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
			verticalRegionInfoExpander.setLayout(new GridLayout(1, false));
			verticalRegionInfoExpander.setText("Vertical Profile ROI");

			Composite verticalProfileComposite = new Composite(verticalRegionInfoExpander, SWT.NONE);
			verticalProfileComposite.setLayout(new GridLayout(1, false));
			verticalProfileComposite.setLayoutData(gridData);

			verticalProfileROIWidget = new ROIWidget(verticalProfileComposite, verticalProfilePlottingSystem, "Left/Right region editor");
			verticalProfileROIWidget.setIsProfile(true);
			verticalProfileROIWidget.createWidget();
			verticalProfileROIWidget.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					ROIBase newRoi = verticalProfileROIWidget.getROI();
					String regionName = verticalProfileROIWidget.getRegionName();
					IRegion region = verticalProfilePlottingSystem.getRegion(regionName);
					if(region != null){
						region.setROI(newRoi);
					}
				}
			});
			verticalRegionInfoExpander.setClient(verticalProfileComposite);
			verticalRegionInfoExpander.addExpansionListener(expansionAdapter);
			verticalRegionInfoExpander.setExpanded(false);
			
			//horizontal xaxis roi
			ExpandableComposite horizontalRegionInfoExpander = new ExpandableComposite(contentComposite, SWT.NONE);
			horizontalRegionInfoExpander.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
			horizontalRegionInfoExpander.setLayout(new GridLayout(1, false));
			horizontalRegionInfoExpander.setText("Horizontal Profile ROI");

			Composite horizontalProfileComposite = new Composite(horizontalRegionInfoExpander, SWT.NONE);
			horizontalProfileComposite.setLayout(new GridLayout(1, false));
			horizontalProfileComposite.setLayoutData(gridData);

			horizontalProfileROIWidget = new ROIWidget(horizontalProfileComposite, horizontalProfilePlottingSystem, "Bottom/Up region editor");
			horizontalProfileROIWidget.setIsProfile(true);
			horizontalProfileROIWidget.createWidget();
			horizontalProfileROIWidget.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					ROIBase newRoi = horizontalProfileROIWidget.getROI();
					String regionName = horizontalProfileROIWidget.getRegionName();
					IRegion region = horizontalProfilePlottingSystem.getRegion(regionName);
					if(region != null){
						region.setROI(newRoi);
					}
				}
			});
			horizontalRegionInfoExpander.setClient(horizontalProfileComposite);
			horizontalRegionInfoExpander.addExpansionListener(expansionAdapter);
			horizontalRegionInfoExpander.setExpanded(false);

			scrollComposite.setContent(contentComposite);
			scrollComposite.setExpandHorizontal(true);
			scrollComposite.setExpandVertical(true);
			scrollComposite.addControlListener(new ControlAdapter() {
				@Override
				public void controlResized(ControlEvent e) {
					Rectangle r = scrollComposite.getClientArea();
					scrollComposite.setMinSize(contentComposite.computeSize(r.width, SWT.DEFAULT));
				}
			});
			//end Roi information composite

			sashForm.setWeights(new int[]{1, 1});

			//activate();
		} catch (Exception e) {
			logger.error("Cannot locate any Abstract plotting System!", e);
		}
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
		update(null, null, false);
		if (getPlottingSystem()!=null) {
			getPlottingSystem().addTraceListener(traceListener);
			getPlottingSystem().addRegionListener(regionListener);
			// We try to listen to the image mask changing and reprofile if it does.
			if (getImageTrace()!=null) getImageTrace().addPaletteListener(paletteListener);
		}
		setRegionsActive(true);

		createNewRegion();

		if(myROIWidget != null)
			myROIWidget.addRegionListener((AbstractPlottingSystem)getPlottingSystem());
		if(verticalProfileROIWidget != null)
			verticalProfileROIWidget.addRegionListener(verticalProfilePlottingSystem);
		if(horizontalProfileROIWidget != null)
			horizontalProfileROIWidget.addRegionListener(horizontalProfilePlottingSystem);
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

	private boolean isRegionTypeSupported(RegionType type) {
		return (type==RegionType.BOX)||(type==RegionType.PERIMETERBOX)||(type==RegionType.XAXIS)||(type==RegionType.YAXIS);
	}

	private RegionType getCreateRegionType() {
		return RegionType.PERIMETERBOX;
	}

	@Override
	public void deactivate() {
		super.deactivate();
		if (getPlottingSystem()!=null) {
			getPlottingSystem().removeTraceListener(traceListener);
			getPlottingSystem().removeRegionListener(regionListener);
			if (getImageTrace()!=null) getImageTrace().removePaletteListener(paletteListener);
		}
		setRegionsActive(false);

		if(myROIWidget != null)
			myROIWidget.dispose();
		if(roiSumProfile != null)
			roiSumProfile.dispose();
		if(verticalProfileROIWidget != null)
			verticalProfileROIWidget.dispose();
		if(horizontalProfileROIWidget != null)
			horizontalProfileROIWidget.dispose();
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
		if (zoomProfilePlottingSystem!=null) zoomProfilePlottingSystem.dispose();
		zoomProfilePlottingSystem = null;
		if (verticalProfilePlottingSystem!=null) verticalProfilePlottingSystem.dispose();
		verticalProfilePlottingSystem = null;
		if (horizontalProfilePlottingSystem!=null) horizontalProfilePlottingSystem.dispose();
		horizontalProfilePlottingSystem = null;
		super.dispose();
	}

	/**
	 * 
	 * @param image
	 * @param region
	 * @param roi - may be null
	 * @param monitor
	 */
	private void createProfile(final IImageTrace image, 
			                              IRegion region, 
			                              ROIBase roi, 
			                              boolean tryUpdate, 
			                              boolean isDrag,
			                              IProgressMonitor monitor){
		if (monitor.isCanceled()) return;
		if (image==null) return;
		
		if (!isRegionTypeSupported(region.getRegionType())) return;

		final RectangularROI bounds = (RectangularROI) (roi==null ? region.getROI() : roi);
		if (bounds==null) return;
		if (!region.isVisible()) return;

		if (monitor.isCanceled()) return;
		
		//start zoom profile
		final int yInc = bounds.getPoint()[1]<bounds.getEndPoint()[1] ? 1 : -1;
		final int xInc = bounds.getPoint()[0]<bounds.getEndPoint()[0] ? 1 : -1;
		try {
			AbstractDataset slice = null;
			// If the region is out of the image bounds (left and top) we set the start points to 0
			if((int) bounds.getPoint()[0]<0 && (int) bounds.getPoint()[1]>=0)
				slice = image.getData().getSlice(new int[] { (int) bounds.getPoint()[1], 0 },
						new int[] { (int) bounds.getEndPoint()[1], (int) bounds.getEndPoint()[0] },
						new int[] {yInc, xInc});
			else if ((int) bounds.getPoint()[1]<0 && (int) bounds.getPoint()[0]>=0)
				slice = image.getData().getSlice(new int[] { 0, (int) bounds.getPoint()[0] },
						new int[] { (int) bounds.getEndPoint()[1], (int) bounds.getEndPoint()[0] },
						new int[] {yInc, xInc});
			else if((int) bounds.getPoint()[0]<0 && (int) bounds.getPoint()[1]<0)
				slice = image.getData().getSlice(new int[] { 0, 0 },
						new int[] { (int) bounds.getEndPoint()[1], (int) bounds.getEndPoint()[0] },
						new int[] {yInc, xInc});
			else
				slice = image.getData().getSlice(new int[] { (int) bounds.getPoint()[1], (int) bounds.getPoint()[0] },
						new int[] { (int) bounds.getEndPoint()[1], (int) bounds.getEndPoint()[0] },
						new int[] {yInc, xInc});

			slice.setName(region.getName());
			// Calculate axes to have real values not size
			AbstractDataset yLabels = null;
			AbstractDataset xLabels = null;
			if (image.getAxes()!=null) {
				AbstractDataset xl = image.getAxes().get(0);
				if (xl!=null) xLabels = getLabelsFromLabels(xl, bounds, 0);
				AbstractDataset yl = image.getAxes().get(1);
				if (yl!=null) yLabels = getLabelsFromLabels(yl, bounds, 1);
			}

			if (yLabels==null) yLabels = IntegerDataset.arange(bounds.getPoint()[1], bounds.getEndPoint()[1], yInc);
			if (xLabels==null) xLabels = IntegerDataset.arange(bounds.getPoint()[0], bounds.getEndPoint()[0], xInc);

			final IImageTrace zoom_trace = (IImageTrace)zoomProfilePlottingSystem.updatePlot2D(slice, Arrays.asList(new AbstractDataset[]{xLabels, yLabels}), monitor);
			registerTraces(region, Arrays.asList(new ITrace[]{zoom_trace}));
			Display.getDefault().syncExec(new Runnable()  {
				public void run() {
					zoom_trace.setPaletteData(image.getPaletteData());
				}
			});
			
		} catch (IllegalArgumentException ne) {
			// Occurs when slice outside
			logger.trace("Slice outside bounds of image!", ne);
		} catch (Throwable ne) {
			logger.warn("Problem slicing image in "+getClass().getSimpleName(), ne);
		}
		//end zoom profile

		// vertical and horizontal profiles
		updateProfile(verticalProfilePlottingSystem, image, bounds, region, BoxLineType.VERTICAL_TYPE, tryUpdate, monitor);
		updateProfile(horizontalProfilePlottingSystem, image, bounds, region, BoxLineType.HORIZONTAL_TYPE, tryUpdate, monitor);
	}

	private void updateProfile(AbstractPlottingSystem profilePlottingSystem, 
						final IImageTrace image, 
						final RectangularROI bounds, 
						IRegion region, 
						final BoxLineType type, 
						boolean tryUpdate, 
						IProgressMonitor monitor){

		AbstractDataset[] box = ROIProfile.boxLine(image.getData(), image.getMask(), bounds, true, type);

		if (box==null) return;

		String traceName1 = "", traceName2 = "";
		if(type == BoxLineType.HORIZONTAL_TYPE){
			traceName1 = "Top Profile";
			traceName2 = "Bottom Profile";
		} else if(type == BoxLineType.VERTICAL_TYPE){
			traceName1 = "Left Profile";
			traceName2 = "Right Profile";
		}

		final AbstractDataset line1 = box[0];
		line1.setName(traceName1);
		AbstractDataset xi = IntegerDataset.arange(line1.getSize());
		final AbstractDataset x_indices = xi;

		final AbstractDataset line2 = box[1];
		line2.setName(traceName2);
		AbstractDataset yi = IntegerDataset.arange(line2.getSize());
		final AbstractDataset y_indices = yi;

		final List<AbstractDataset> boxesLines = new ArrayList<AbstractDataset>(2);
		boxesLines.add(line1);
		boxesLines.add(line2);

		final ILineTrace x_trace = (ILineTrace)profilePlottingSystem.getTrace(traceName1);
		final ILineTrace y_trace = (ILineTrace)profilePlottingSystem.getTrace(traceName2);
		final List<ILineTrace> traces = new ArrayList<ILineTrace>(2);
		traces.add(x_trace);
		traces.add(y_trace);

		if (tryUpdate && x_trace!=null && y_trace!=null) {
			Control control = getControl();
			if(control != null && !control.isDisposed()) {
				control.getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						List<AbstractDataset> axes = image.getAxes();
						if(axes != null){
							if(type == BoxLineType.VERTICAL_TYPE){
								updateAxes(traces, boxesLines, axes.get(1), bounds.getPointY(), type);
								x_trace.setTraceColor(ColorConstants.blue);
								y_trace.setTraceColor(ColorConstants.red);
							} else if (type == BoxLineType.HORIZONTAL_TYPE){
								updateAxes(traces, boxesLines, axes.get(0), bounds.getPointX(), type);
								x_trace.setTraceColor(ColorConstants.darkGreen);
								y_trace.setTraceColor(ColorConstants.orange);
							}
						} else { //if no axes we set them manually according to the data shape
							int[] shapes = image.getData().getShape();
							if(type == BoxLineType.VERTICAL_TYPE){
								int[] verticalAxis = new int[shapes[1]];
								for(int i = 0; i < verticalAxis.length; i ++){
									verticalAxis[i] = i;
								}
								AbstractDataset vertical = new IntegerDataset(verticalAxis, shapes[1]);
								updateAxes(traces, boxesLines, vertical, bounds.getPointY(), type);
								x_trace.setTraceColor(ColorConstants.blue);
								y_trace.setTraceColor(ColorConstants.red);
							} else if (type == BoxLineType.HORIZONTAL_TYPE){
								int[] horizontalAxis = new int[shapes[0]];
								for(int i = 0; i < horizontalAxis.length; i ++){
									horizontalAxis[i] = i;
								}
								AbstractDataset horizontal = new IntegerDataset(horizontalAxis, shapes[0]);
								updateAxes(traces, boxesLines, horizontal, bounds.getPointX(), type);
								x_trace.setTraceColor(ColorConstants.darkGreen);
								y_trace.setTraceColor(ColorConstants.orange);
							}
						}
					}
				});
			}
		} else {
			if(type == BoxLineType.HORIZONTAL_TYPE){
				profilePlottingSystem.setSelectedXAxis(xPixelAxisHorizontal);
				profilePlottingSystem.setSelectedYAxis(yPixelAxisHorizontal);
			} else if (type == BoxLineType.VERTICAL_TYPE){
				profilePlottingSystem.setSelectedXAxis(xPixelAxisVertical);
				profilePlottingSystem.setSelectedYAxis(yPixelAxisVertical);
			}

			Collection<ITrace> plotted = profilePlottingSystem.updatePlot1D(x_indices, Arrays.asList(new AbstractDataset[]{line1}), monitor);
			registerTraces(region, plotted);

			plotted = profilePlottingSystem.updatePlot1D(y_indices, Arrays.asList(new AbstractDataset[]{line2}), monitor);
			registerTraces(region, plotted);	
		}
	}

	/**
	 * Updates the profile axes according to the ROI start point
	 * @param profiles
	 * @param boxLines
	 * @param originalAxis
	 * @param axis
	 * @param startPoint
	 * @param type
	 */
	private void updateAxes(List<ILineTrace> profiles, List<AbstractDataset> boxesLines, 
			AbstractDataset axis, double startPoint, BoxLineType type){
		// shift the xaxis by yStart
		try {
			double xStart = axis.getElementDoubleAbs((int)Math.round(startPoint));
			double min = axis.getDouble(0);

			axis = new DoubleDataset(axis);
			xStart = axis.getElementDoubleAbs((int)Math.round(startPoint));
			min = axis.getDouble(0);
			axis.iadd(xStart-min);

			profiles.get(0).setData(axis.getSlice(new Slice(0, boxesLines.get(0).getShape()[0], 1)), boxesLines.get(0));
			profiles.get(1).setData(axis.getSlice(new Slice(0, boxesLines.get(1).getShape()[0], 1)), boxesLines.get(1));

			double max = axis.getDouble(axis.argMax());
			if(type == BoxLineType.HORIZONTAL_TYPE){
				xPixelAxisHorizontal.setTitle(axis.getName());
				createXAxisBoxRegion(horizontalProfilePlottingSystem, new RectangularROI(min, 0, (max-min)/2, 100, 0), "X_Axis_box");
			} else if(type == BoxLineType.VERTICAL_TYPE){
				xPixelAxisVertical.setTitle(axis.getName());
				createXAxisBoxRegion(verticalProfilePlottingSystem, new RectangularROI(min, 0, (max-min)/2, 100, 0), "X_Axis_box");
			}
		} catch (ArrayIndexOutOfBoundsException ae) {
			//do nothing
		} catch (Exception e) {
			logger.debug("An exception has occured:"+e);
		}
	}

	private void createXAxisBoxRegion(final AbstractPlottingSystem plottingSystem, 
			final ROIBase roi, final String roiName){
		try {
			if(roi instanceof RectangularROI){
				RectangularROI rroi = (RectangularROI)roi;
				IRegion region = plottingSystem.getRegion(roiName);
				//Test if the region is already there and update the currentRegion
				if(region!=null&&region.isVisible()){
					region.setROI(region.getROI());
				}else {
					IRegion newRegion = plottingSystem.createRegion(roiName, RegionType.XAXIS);
					newRegion.setROI(rroi);
					plottingSystem.addRegion(newRegion);
				}
			}
		} catch (Exception e) {
			logger.error("Couldn't create ROI", e);
		}
	}

	private AbstractDataset getLabelsFromLabels(AbstractDataset xl, RectangularROI bounds, int axisIndex) {
		try {
			int fromIndex = (int)bounds.getPoint()[axisIndex];
			int toIndex   = (int)bounds.getEndPoint()[axisIndex];
			int step      = toIndex>fromIndex ? 1 : -1;
			final AbstractDataset slice = xl.getSlice(new int[]{fromIndex}, new int[]{toIndex}, new int[]{step});
			return slice;
		} catch (Exception ne) {
			return null;
		}
	}

	private void registerTraces(final IRegion region, final Collection<ITrace> traces) {
		
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
	
	private synchronized void update(IRegion r, ROIBase rb, boolean isDrag) {
		if (!isActive()) return;
		if (r!=null) {
			if(!isRegionTypeSupported(r.getRegionType())) return; // Nothing to do.
			if (!r.isUserRegion()) return; // Likewise
		}
		updateProfiles.profile(r, rb, isDrag);
	}

	private String getRegionName() {
		return "Profile";
	}

	private final class ProfileJob extends Job {

		private IRegion currentRegion;
		private ROIBase currentROI;
		private boolean isDrag;

		ProfileJob() {
			super(getRegionName() + " update");
			setSystem(true);
			setUser(false);
			setPriority(Job.INTERACTIVE);
		}

		public void profile(IRegion r, ROIBase rb, boolean isDrag) {

			// This in principle is not needed and appears to make no difference
			// wether in or out.
			// However Irakli has advised that it is needed in some
			// circumstances.
			// This causes the defect reported here however:
			// http://jira.diamond.ac.uk/browse/DAWNSCI-214
			// therefore we are currently not using the extra cancelling.
			// for (Job job : Job.getJobManager().find(null))
			// if (job.getClass()==getClass() && job.getState() != Job.RUNNING)
			// job.cancel();

			this.currentRegion = r;
			this.currentROI = rb;
			this.isDrag = isDrag;

			schedule();
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {

			if (!isActive())
				return Status.CANCEL_STATUS;

			final Collection<ITrace> traces = getPlottingSystem().getTraces(
					IImageTrace.class);
			IImageTrace image = traces != null && traces.size() > 0 ? (IImageTrace) traces
					.iterator().next() : null;

			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;
			if (image == null) {
				zoomProfilePlottingSystem.clear();
				verticalProfilePlottingSystem.clear();
				horizontalProfilePlottingSystem.clear();
				return Status.OK_STATUS;
			}

			// if the current region is null try and update quickly (without
			// creating 1D)
			// if the trace is in the registered traces object
			if (currentRegion == null) {
				final Collection<IRegion> regions = getPlottingSystem()
						.getRegions();
				if (regions != null) {
					for (IRegion iRegion : regions) {
						if (!iRegion.isUserRegion())
							continue;
						if (monitor.isCanceled())
							return Status.CANCEL_STATUS;
						if (registeredTraces.containsKey(iRegion.getName())) {
							createProfile(image, iRegion, iRegion.getROI(),
									true, isDrag, monitor);
						} else {
							createProfile(image, iRegion, iRegion.getROI(),
									false, isDrag, monitor);
						}
					}
				} else {
					registeredTraces.clear();
					zoomProfilePlottingSystem.clear();
					verticalProfilePlottingSystem.clear();
					horizontalProfilePlottingSystem.clear();
				}
			} else {

				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				createProfile(
						image,
						currentRegion,
						currentROI != null ? currentROI : currentRegion
								.getROI(), true, isDrag, monitor);

			}

			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;
			zoomProfilePlottingSystem.repaint();
			verticalProfilePlottingSystem.repaint();
			horizontalProfilePlottingSystem.repaint();

			return Status.OK_STATUS;

		}
	}

	@Override
	public IPlottingSystem getToolPlottingSystem() {
		return zoomProfilePlottingSystem;
	}

	@Override
	public void roiSelected(ROIEvent evt) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isStaticTool() {
		return true;
	}
}
