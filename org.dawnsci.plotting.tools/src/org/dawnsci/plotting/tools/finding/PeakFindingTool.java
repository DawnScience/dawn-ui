package org.dawnsci.plotting.tools.finding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dawb.common.ui.util.GridUtils;
import org.dawnsci.plotting.actions.ActionBarWrapper;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.axis.ClickEvent;
import org.eclipse.dawnsci.plotting.api.axis.IClickListener;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.PointStyle;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.TraceType;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.dawnsci.plotting.api.trace.TraceWillPlotEvent;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.january.dataset.BooleanDataset;
import org.eclipse.january.dataset.Comparisons;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.IPageSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IdentifiedPeak;

/**
 * @author Dean P. Ottewell
 */
public class PeakFindingTool extends AbstractToolPage implements IRegionListener {

	private static final Logger logger = LoggerFactory.getLogger(PeakFindingTool.class);

	public PeakFindingManager manager;

	// View Components
	private PeakFindingActions actions;
	private PeakFindingWidget widget;
	private PeakFindingTable table;

	// Page Components
	private Composite composite;

	// Traces
	private String PEAKSTRACENAME = "Peaks";
	private ILineTrace peaksTrace;

	// Peak Running Process
	private PeakFindingSearchJob peakSearch;

	// Click Button Active
	private Boolean isRemoving = false;
	private Boolean isAdding = false;

	// Upper & Lower Selection Bounds
	private String BOUNDTRACENAME = "Bounds";
	ILineTrace regionBndsTrace;

	// Selected Region Interests For Searching - TODO: clean on
	public IRegion searchRegion;

	// Bound limits for searching
	private Double upperBnd;
	private Double lowerBnd;

	private Dataset interestXData;
	private Dataset interestYData;

	private ITraceListener traceListener;
	private IClickListener clickListener;
	
	public Dataset gettingXData() {
		return this.interestXData;
	}
	public Dataset gettingYData() {
		return this.interestYData;
	}
	
	private List<IdentifiedPeak> peaksId = new ArrayList<IdentifiedPeak>();
	
	private IPeakOpportunityListener listener;

	public PeakFindingTool() {
		// Setup up a new PeakSearch Instance
		this.manager = new PeakFindingManager();
	}

	public void setAddMode(boolean status) {
		isAdding = status;
		if(searchRegion != null)
			searchRegion.setVisible(!status);
	}

	public void setRemoveMode(boolean status) {
		isRemoving = status;
		if(searchRegion != null)
			searchRegion.setVisible(!status);
	}

	public void resetActions() {
		if(searchRegion != null)
			searchRegion.setVisible(true);
	}

	public PeakFindingTool(IPlottingSystem<?> system) {
		this.manager = new PeakFindingManager();
		setPlottingSystem(system);
	}

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_1D;
	}

	private ActionBarWrapper generateActionBar(Composite parent) {
		ActionBarWrapper actionBarWrapper = null;
		parent = new Composite(composite, SWT.RIGHT);
		parent.setLayout(new GridLayout(1, false));
		actionBarWrapper = ActionBarWrapper.createActionBars(parent, null);
		actionBarWrapper.update(true);
		return actionBarWrapper;
	}

	@Override
	public void createControl(Composite parent) {
		this.composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		GridUtils.removeMargins(composite);
		
		final IPageSite site = getSite();
		IActionBars actionbars = site != null ? site.getActionBars() : generateActionBar(parent);

		actions = new PeakFindingActions(manager, this);
		actions.createActions(actionbars.getToolBarManager());

		table = new PeakFindingTable(manager);
		table.createTableControl(composite);

		widget = new PeakFindingWidget(manager);
		widget.createControl(composite);
		
		//TODO: additional plot service
		IPlottingSystem<Composite> system = getPlottingSystem();
		system.createPlotPart(composite, getTitle(), actionbars, PlotType.XY, this.getViewPart());
		
		// TODO: id the listener...
		listener = new IPeakOpportunityListener() {
			@Override
			public void peaksChanged(PeakOpportunityEvent evt) {
				//TODO: now ill just place these identifed peaks here too
				if(evt.getPeakOpp().getPeaksId() != null){
					peaksId = evt.getPeakOpp().getPeaksId();
					
					if (!peaksId.isEmpty()) {
						updatePeakTrace(peaksId);
					} else {
						//We have no longer any peaks to plot remove]
						//getPlottingSystem().getTrace(PEAKSTRACENAME) != null
						if(getPlottingSystem().getTrace(PEAKSTRACENAME) != null){
							getPlottingSystem().removeTrace(peaksTrace);
							peaksTrace.dispose();
						}
					}
				
				}
			}

			@Override
			public void boundsChanged(double upper, double lower) {
				RectangularROI rectBounds = (RectangularROI) searchRegion.getROI().getBounds();
				double[] bounds = { lower, upper };

				// TODO: shouldnt be trigger anyway
				if (bounds[0] != rectBounds.getPoint()[0] || bounds[1] != rectBounds.getEndPoint()[0]) {
					// Update region
					rectBounds.setPoint(lower, rectBounds.getPoint()[1]);
					rectBounds.setEndPoint(new double[]{upper, rectBounds.getEndPoint()[1]});
					searchRegion.setROI(rectBounds);


					Collection<ITrace> traces = getPlottingSystem().getTraces();
					if (!traces.isEmpty()) {
						setSearchDataOnBounds((ILineTrace) traces.iterator().next());
					}
				}
			}

			@Override
			public void dataChanged(Dataset nXData, Dataset nYData) {
				// xData = nXData;
				// yData = nYData;
			}

			@Override
			public void isPeakFinding() {
				// TODO Auto-generated method stub
			}

			@Override
			public void finishedPeakFinding() {
				// TODO Auto-generated method stub
			}

			@Override
			public void activateSearchRegion() {
				createNewSearch();
			}
		};
		manager.addPeakListener(listener);

		clickListener = new IClickListener() {
			@Override
			public void doubleClickPerformed(ClickEvent evt) {
				// TODO Auto-generated method stub
			}

			@Override
			public void clickPerformed(ClickEvent evt) {
				if (isAdding) {
					addPeakValue(evt.getxValue(), evt.getyValue());
				} else if (isRemoving) {
					if (!peaksId.isEmpty())
						removePeakValue(evt.getxValue(), evt.getyValue());
				}
			}
		};
		
		
		// Control Peak Removal + Addition
		system.addClickListener(clickListener);

		
		traceListener = new ITraceListener() {

			@Override
			public void tracesUpdated(TraceEvent evt) {
			}

			@Override
			public void tracesRemoved(TraceEvent evet) {
				// The last trace removed should be the trace of search interest
				if (getPlottingSystem().getTraces().isEmpty()) {
					// TODO:We have no data! Abort abort disable all actions? disable all running?
				}	
			}

			@Override
			public void tracesAdded(TraceEvent evt) {
				List<ILineTrace> traceUpdate = (List<ILineTrace>) evt.getSource();
				
				for(ILineTrace trace : traceUpdate){
					if (isValidTraceForSearch((ITrace) trace)) {
						runTraceSearch(trace);
					}
				}
			}

			@Override
			public void traceWillPlot(TraceWillPlotEvent evt) {
				// TODO Auto-generated method stub
			}

			@Override
			public void traceUpdated(TraceEvent evt) {
				ILineTrace traceUpdate = (ILineTrace) evt.getSource();
				
				//TODO: check against object but unfortunately do not have it hanging around ...
//				traceUpdate.equals(peaksTrace);
//				traceUpdate.equals(getPlottingSystem().getTrace(BOUNDTRACENAME));
				if (isValidTraceForSearch(traceUpdate)) {
					runTraceSearch(traceUpdate);
				}
			}

			@Override
			public void traceRemoved(TraceEvent evt) {
				// TODO Auto-generated method stub

			}

			@Override
			public void traceCreated(TraceEvent evt) {
				// TODO Auto-generated method stub

			}

			@Override
			public void traceAdded(TraceEvent evt) {
				ILineTrace traceUpdate = (ILineTrace) evt.getSource();
				if (isValidTraceForSearch(traceUpdate)) {
					runTraceSearch(traceUpdate);
				}
			}
		};
		
		system.addTraceListener(traceListener);

		// Begin with the search tool ready to then run on
		createNewSearch();

	}
	
	private void runTraceSearch(ILineTrace trace){
		setSearchDataOnBounds(trace);
		// Load in new search bounds to beacon
		PeakOpportunity peakOpp = new PeakOpportunity();
		peakOpp.setXData(interestXData);
		peakOpp.setYData(interestYData);
		
		IRectangularROI interest = searchRegion.getROI().getBounds();
		double[] start = interest.getPoint();
		double[] end = interest.getEndPoint();

		peakOpp.setLowerBound(start[0]);
		peakOpp.setUpperBound(end[0]);
		
		manager.loadPeakOpportunity(peakOpp);
		manager.setPeakSearching();
	}

	private void addPeakValue(Double x, Double y) {

		List<Double> pX = new ArrayList<Double>();
		List<Double> pY = new ArrayList<Double>();

		if (!peaksId.isEmpty()) {
			for (int i = 0; i < peaksId.size(); ++i) {
				pX.add(peaksId.get(i).getPos());
				pY.add(peaksId.get(i).getHeight());
			}
		}

		pX.add(x);
		pY.add(y);
		
		IdentifiedPeak p = new IdentifiedPeak();
		p.setPos(x);
		p.setHeight(y);
		// XXX: Unfortunately now this will be on the end of the result. The
		// order is important for the table view. Need comaprator there
		peaksId.add(p);

		// Update Trace
		Dataset peakx = DatasetFactory.createFromList(pX);
		Dataset peaky = DatasetFactory.createFromList(pY);

		manager.setPeaksId(peaksId);
	}

	private void removePeakValue(Double x, Double y) {
		Dataset peakx = DatasetUtils.convertToDataset(peaksTrace.getXData());
		Dataset peaky = DatasetUtils.convertToDataset(peaksTrace.getYData());

		int toRemove = closestPoint(peakx, peaky, new Double[] { x, y });

		List<Double> pX = new ArrayList<Double>();
		List<Double> pY = new ArrayList<Double>();

		for (int i = 0; i < peakx.getSize(); ++i) {
			pX.add((Double) peakx.getDouble(i));
			pY.add((Double) peaky.getDouble(i));
		}

		pX.remove(toRemove);
		pY.remove(toRemove);

		peaksId.remove(toRemove);

		if (!peaksId.isEmpty()) {
			peakx = DatasetFactory.createFromList(pX);
			peaky = DatasetFactory.createFromList(pY);
		}
		
		manager.setPeaksId(peaksId);
	}

	public void configureTraces() {
		IPlottingSystem<?> system = getPlottingSystem();
		Collection<ITrace> traces = system.getTraces();
		if (!traces.isEmpty()) {
			// XXX:assumes base trace is sample trace
			ILineTrace sampleTrace = (ILineTrace) traces.iterator().next(); 
			// Setup Upper & lower bound for search region
			if (system.getTrace(BOUNDTRACENAME) == null) {
				regionBndsTrace = generateBoundTrace(system, BOUNDTRACENAME);
				system.addTrace(regionBndsTrace);
			} else {
				regionBndsTrace = (ILineTrace) system.getTrace(BOUNDTRACENAME);
			}

			// Initialise to upper and low limit of sample trace
			Dataset xData = DatasetUtils.convertToDataset(sampleTrace.getXData());
			
			Double lwrBnd = xData.getDouble(0);
			Double uprBnd = xData.getDouble(xData.argMax());

			
			Dataset xBnds = DatasetFactory.createFromObject(new double[] { lwrBnd, uprBnd });
			Dataset bndHeight = genBoundsHeight();

			updateTraceBounds(system, xBnds, bndHeight);
		}
	}

	private ILineTrace generateBoundTrace(IPlottingSystem<?> system, String tracename) {
		ILineTrace trace = system.createLineTrace(BOUNDTRACENAME);
		trace.setLineWidth(3);
		trace.setTraceType(TraceType.HISTO);
		trace.setTraceColor(ColorConstants.orange);
		return trace;
	}

	private ILineTrace generatePeakTrace(IPlottingSystem<?> system, String tracename) {
		ILineTrace trace = system.createLineTrace(tracename);
		trace.setLineWidth(1);
		trace.setPointStyle(PointStyle.CIRCLE);
		trace.setPointSize(3);
		trace.setTraceType(TraceType.HISTO);
		return trace;
	}

	public void createNewSearch() {
		try {
			IPlottingSystem<?> system = getPlottingSystem();
			for (IRegion i : system.getRegions()) {
				system.removeRegion(i);
			}

			if (peaksId != null && !peaksId.isEmpty()) {
				// Regenerate the trace to older times
				system.removeTrace(peaksTrace);
				peaksTrace.dispose();
				//Update reset
				PeakOpportunity peakOpp = new PeakOpportunity();
				manager.setPeaksId(peaksId);
				manager.loadPeakOpportunity(peakOpp);
			}

			system.addRegionListener(this);
			this.searchRegion = system.createRegion(
					RegionUtils.getUniqueName("Search selection", system),
					system.is2D() ? IRegion.RegionType.BOX : IRegion.RegionType.XAXIS);

			searchRegion.setLineWidth(5);
			searchRegion.setRegionColor(ColorConstants.red);
			searchRegion.setMobile(true);

			searchRegion.addROIListener(new IROIListener() {
				@Override
				public void roiSelected(ROIEvent evt) {
					// TODO Auto-generated method stub
				}

				@Override
				public void roiDragged(ROIEvent evt) {
					// TODO Auto-generated method stub
					// RectangularROI rectangle = (RectangularROI)
					// searchRegion.getROI();
					// updateSearchBnds(rectangle);
				}

				@Override
				public void roiChanged(ROIEvent evt) {

					// TODO Auto-generated method stub
					RectangularROI rectangle = (RectangularROI) searchRegion.getROI();
					updateSearchBnds(rectangle);
					for (ITrace trace : getPlottingSystem().getTraces()) {
						if (isValidTraceForSearch(trace)) {
							runTraceSearch((ILineTrace) trace);
						}
					}
				}
			});

			// SELECTING THOSE BOUNDS UPPER AND LOWER
			// TODO: cant't yet do this here as rectangularROI does not exist yet
			// TODO: this can be do e when a region is added instead ... why so?
			if (searchRegion != null && searchRegion.getROI() instanceof RectangularROI) {
				RectangularROI rectangle = (RectangularROI) searchRegion.getROI();
				// Set the region bounds
				updateSearchBnds(rectangle);
			}


		} catch (Exception e) {
			logger.error("Cannot put the selection into searching region mode!", e);
		}
	}
	
	private boolean isValidTraceForSearch(ITrace trace){
		boolean valid = false;
		
		ILineTrace lineTrace = (ILineTrace) trace;
		//Check against being a trace created for me
		if (trace.getName() != BOUNDTRACENAME && trace.getName() != PEAKSTRACENAME) {
			valid = true;

			if(lineTrace.getXData() == null)
				valid =false;
			
			if(lineTrace.getXData() == null)
				valid =false;
		}

		return valid;
	}

	public void setSearchDataOnBounds(ILineTrace trace) {
		// Obtain Upper and Lower Bounds
		Dataset xDataRaw = DatasetUtils.convertToDataset(trace.getXData());
		Dataset yDataRaw = DatasetUtils.convertToDataset(trace.getYData());

		DoubleDataset xData = DatasetUtils.cast(DoubleDataset.class, xDataRaw);
		DoubleDataset yData = DatasetUtils.cast(DoubleDataset.class, yDataRaw);
		
		IRectangularROI interest = searchRegion.getROI().getBounds();
		double[] start = interest.getPoint();
		double[] end = interest.getEndPoint();
						
		BooleanDataset allowed = Comparisons.withinRange(xData, start[0], end[0]);
		this.interestXData = xData.getByBoolean(allowed);
		this.interestYData = yData.getByBoolean(allowed);
	}

	public void updateSearchBnds(RectangularROI rect) {
		lowerBnd = rect.getPointRef()[0];
		upperBnd = rect.getPointRef()[0] + rect.getLengths()[0];

		double[] x = { lowerBnd, upperBnd };

		Dataset xdata = DatasetFactory.createFromObject(x);
		Dataset ydata = genBoundsHeight();

		updateTraceBounds(getPlottingSystem(), xdata, ydata); // TODO: this is already triggered
	}

	private void updateTraceBounds(IPlottingSystem<?> system, Dataset x, Dataset y) {
		if(regionBndsTrace == null) {
			regionBndsTrace = generateBoundTrace(system, BOUNDTRACENAME);
			system.addTrace(regionBndsTrace);
		}
		regionBndsTrace.setData(x, y);
		
		
		if (!regionBndsTrace.isVisible())
			regionBndsTrace.setVisible(true);

		system.repaint();
	}

	public void updatePeakTrace(Dataset x, Dataset y) {
		IPlottingSystem<?> system = getPlottingSystem();
		if (system.getTrace(PEAKSTRACENAME) == null) {
			peaksTrace = generatePeakTrace(system, PEAKSTRACENAME);
			system.addTrace(peaksTrace);
		}

		peaksTrace.setData(x, y);

		if (!peaksTrace.isVisible()) {
			peaksTrace.setVisible(true);		
		}

		system.repaint();
	}
	
	private void updatePeakTrace(List<IdentifiedPeak> peakSet) {
		List<Double> pX = new ArrayList<Double>();
		List<Double> pY = new ArrayList<Double>();
		
		if (!peakSet.isEmpty()) {
			for (int i = 0; i < peakSet.size(); ++i) {
				pX.add(peakSet.get(i).getPos());
				pY.add(peakSet.get(i).getHeight());
			}
		}

		// Update Trace
		Dataset peakx = DatasetFactory.createFromList(pX);
		Dataset peaky = DatasetFactory.createFromList(pY);

		updatePeakTrace(peakx, peaky);
	}


	/**
	 * Finds the closest point to position given data
	 *
	 * TODO: we believe in x supremacy in this function. y means nothing to us.
	 * down with y!
	 *
	 * @param x
	 * @param y
	 * @param xy
	 * @return
	 */
	private int closestPoint(IDataset x, IDataset y, Double[] xy) {

		int closeIdx = 0;

		for (int i = 1; i < x.getSize(); ++i) {
			Double curX = (Double) x.getDouble(closeIdx);
			Double nwX = (Double) x.getDouble(i);

			if (Math.abs(nwX - xy[0]) < Math.abs(curX - xy[0])) {
				closeIdx = i;
			}
		}
		return closeIdx;
	}

	public Dataset genBoundsHeight() {
		double top = getPlottingSystem().getAxes().get(0).getUpper();
		double[] h = { top, top }; // Only can have two boundary points
		return DatasetFactory.createFromObject(h);
	}

	@Override
	public void regionCreated(RegionEvent evt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void regionCancelled(RegionEvent evt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void regionNameChanged(RegionEvent evt, String oldName) {
		// TODO Auto-generated method stub
	}

	@Override
	public void regionAdded(RegionEvent evt) {

		if (evt == null || evt.getRegion() == null) {
			getPlottingSystem().clearRegions();
			return;
		}
		if (evt.getRegion().getRegionType() == RegionType.XAXIS) {
			searchRegion = evt.getRegion();
			searchRegion.setAlpha(3);

//			RectangularROI rectangle = (RectangularROI) searchRegion.getROI();
//
//			updateSearchBnds(rectangle);
			// Make inactive so can touch around
			// evt.getRegion().setVisible(false);
		}
	}

	@Override
	public void regionRemoved(RegionEvent evt) {
		// TODO Auto-generated method stub
	}

	@Override
	public void regionsRemoved(RegionEvent evt) {
		// TODO Auto-generated method stub
	}

	@Override
	public void activate() {
		super.activate();
		configureTraces();
	}

	@Override
	public Control getControl() {
		return this.composite;
	}

	@Override
	public void setFocus() {
		composite.setFocus();
	}

	/* DECONSTRUCTION */

	@Override
	public void deactivate() {
		super.deactivate();
		// Now remove any listeners to the plotting providing
		// getPlottingSystem()!=null
		
		IPlottingSystem<?> system = getPlottingSystem();
		if (system != null) {
			if (searchRegion != null)
				system.removeRegion(searchRegion);

			if (regionBndsTrace != null)
				system.removeTrace(regionBndsTrace);

			if (peaksTrace != null)
				system.removeTrace(peaksTrace);
			
			system.removeTraceListener(traceListener);
			system.removeClickListener(clickListener);

			for (IRegion region : system.getRegions()) {
				system.removeRegion(region);
			}
		}

		//TODO: just kill manager?
		manager.destroyAllListeners();
	}

	@Override
	public void dispose() {
		deactivate();
		super.dispose();
		widget.dispose();

		// TODO: kill manager jobs... maybe might not be storing the jobs...
	}

	public List<IdentifiedPeak> getPeaksId() {
		return peaksId;
	}

	
}
