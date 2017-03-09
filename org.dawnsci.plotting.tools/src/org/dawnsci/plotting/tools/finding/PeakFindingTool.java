package org.dawnsci.plotting.tools.finding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.plotting.tools.fitting.PeakFittingTool;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.axis.ClickEvent;
import org.eclipse.dawnsci.plotting.api.axis.IClickListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.RegionEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.PointStyle;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.TraceType;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.january.dataset.BooleanDataset;
import org.eclipse.january.dataset.Comparisons;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
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

import uk.ac.diamond.scisoft.analysis.peakfinding.Peak;

/**
 * 
 * @author Dean P. Ottewell
 *
 */
public class PeakFindingTool extends AbstractToolPage implements IRegionListener {

	private static final Logger logger = LoggerFactory.getLogger(PeakFittingTool.class);
	
	PeakFindingManager controller;
	
	//View Compomnents
	private PeakFindingActions actions;
	private PeakFindingWidget widget;
	private PeakFindingTable table;
	
	// Page Components
	private Composite composite;
	
	// Traces
	//TODO: just making public whilst poking
	public ILineTrace sampleTrace;
	private ILineTrace peaksTrace;

	// Peak Running Process
	private PeakSearchJob peakSearch;

	private ITraceListener traceListener;

	// Click Button Active TODO: set up differently
	private Boolean isRemoving = false; 
	private Boolean isAdding = false;

	// Upper & Lower Selection Bounds
	ILineTrace regionBndsTrace;

	// Selected Region Interest For Searching - TODO: clean on
	public IRegion searchRegion = null;

	//Bound limits for searching 
	private Double upperBnd;
	private Double lowerBnd;
	
	private IDataset xData;
	private IDataset yData;
	
	private List<Peak> peaks = new ArrayList<Peak>();
	
	//TODO: should this default constructor generate controller...
	public PeakFindingTool() {
		// Setup up a new PeakSearch Instance
		this.controller = new PeakFindingManager();
		this.traceListener = new ITraceListener.Stub() {
			@Override
			public void tracesUpdated(TraceEvent evt) {
				peakSearch.schedule();
			}
		};
	}
	
	public void setAddMode(boolean status){
		isAdding = status;

	}
	
	public void setRemoveMode(boolean status){
		isRemoving = status;
	}
	
	public PeakFindingTool(IPlottingSystem system, PeakFindingManager controller){
		this.setPlottingSystem(system);
		this.controller = controller;
	}

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_1D;
	}

	private ActionBarWrapper generateActionBar(Composite parent){
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
		
		//TODO: can not really remove this...component of tool
		configureTraces();
		
		final IPageSite site = getSite();
		IActionBars actionbars = site != null ? site.getActionBars() : generateActionBar(parent);

		actions = new PeakFindingActions(controller, this);
		actions.createActions(actionbars.getToolBarManager());
		
		table = new PeakFindingTable(controller);
		table.createTableControl(composite);
		
		widget = new PeakFindingWidget(controller);
		widget.createControl(composite);

		// Control Peak Removal + Addition
		getPlottingSystem().addClickListener(new IClickListener() {
			@Override
			public void doubleClickPerformed(ClickEvent evt) {
				// TODO Auto-generated method stub
			}
			@Override
			public void clickPerformed(ClickEvent evt) {
				if (evt.isShiftDown()) {
					// getPlottingSystem().plotDataWithHold(evt.getxValue(),
					// evt.getyValue());
					// addPeakValue(evt.getxValue(), evt.getyValue());
				} else {
					if (isAdding) {	
						addPeakValue(evt.getxValue(), evt.getyValue());
					} else if (isRemoving) {
						if(!peaks.isEmpty())
							removePeakValue(evt.getxValue(), evt.getyValue());
					}
					//TODO: refresh table data...
					getPlottingSystem().repaint(); // update plots
				}
			}
		});
		controller.addPeakListener(new IPeakOpportunityListener() {

			@Override
			public void peaksChanged(PeakOpportunityEvent evt) {
				//Update Peaks
				if(!evt.getPeaks().isEmpty()){
					peaks = evt.getPeaks();
					updatePeakTrace(peaks);
				}
			}

			@Override
			public void boundsChanged(double upper, double lower) {
				//TODO: call functio nto update trace bounds and set new data areas
				upperBnd = upper;
				lowerBnd = lower;
				double[] x = { lowerBnd, upperBnd};
				
				Dataset xdata = DatasetFactory.createFromObject(x);
				Dataset ydata = genBoundsHeight();
				
				updateTraceBounds(xdata, ydata); //TODO: this is already triggers
				setSearchDataOnBounds();
			}

			@Override
			public void dataChanged(IDataset nXData, IDataset nYData) {
				xData = nXData;
				yData = nYData;				
			}
			
		});
		
		super.activate();
		
		// Begin with the search tool ready to then run on
		createNewSearch();
	}

	private void addPeakValue(Double x, Double y) {

		List<Double> pX = new ArrayList<Double>();
		List<Double> pY = new ArrayList<Double>();

		if (!peaks.isEmpty()) {
			for (int i = 0; i < peaks.size(); ++i) {
				pX.add(peaks.get(i).getX());
				pY.add(peaks.get(i).getY());
			}
		}
		
		pX.add(x);
		pY.add(y);
		Peak p = new Peak(x, y);
		//XXX: Unfortunately now this will be on the end of the result. The order is important for the table view. Need comaprator there
		peaks.add(p);
		
		// Update Trace
		Dataset peakx = DatasetFactory.createFromList(pX);
		Dataset peaky = DatasetFactory.createFromList(pY);

		updatePeakTrace(peakx, peaky);

		controller.setPeaks(peaks);
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

		// TODO: renive hot fix setup and functionalize
		// remove peaks from table should be at same index?
		peaks.remove(toRemove);
		
		
		if(!peaks.isEmpty()){
			peakx = DatasetFactory.createFromList(pX);
			peaky = DatasetFactory.createFromList(pY);
			updatePeakTrace(peakx, peaky);
		} else {
			//Set as not visible as no peaks. Do not want to destroy, tis wasteful
			peaksTrace.setVisible(false);
			getPlottingSystem().repaint(); 
		}


		controller.setPeaks(peaks);
	}

	public void configureTraces() {
		// Grab the current trace data
		// TODO: this must have like a name? just feel concerned in just
		// assuming the trace i get is the plotting trace ...
		// TODO: should check first if trace is loaded... because will fail if
		// not. or better
		sampleTrace = (ILineTrace) getPlottingSystem().getTraces().iterator().next();

		// Setup Upper & lower bound for search region
		regionBndsTrace = getPlottingSystem().createLineTrace("SearchBounds");
		regionBndsTrace.setLineWidth(3);
		regionBndsTrace.setTraceType(TraceType.HISTO);
		regionBndsTrace.setTraceColor(ColorConstants.orange);
		getPlottingSystem().addTrace(regionBndsTrace);

		// Intialise to upper and low limit of sample trace
		Dataset xData = (Dataset) sampleTrace.getXData();
		Double lwrBnd = xData.getDouble(0);
		Double uprBnd = xData.getDouble(xData.argMax());

		Dataset xBnds = DatasetFactory.createFromObject(new double[] { lwrBnd, uprBnd });
		Dataset bndHeight = genBoundsHeight();

		updateTraceBounds(xBnds, bndHeight);

		// Setup PeakTrace
		peaksTrace = getPlottingSystem().createLineTrace("Peaks");
		peaksTrace.setLineWidth(1);
		peaksTrace.setPointStyle(PointStyle.CIRCLE);
		peaksTrace.setPointSize(3);
		peaksTrace.setTraceType(TraceType.HISTO);

		getPlottingSystem().addTrace(peaksTrace);
	}


	public void createNewSearch() {
		try {
			Collection<IRegion> regions = getPlottingSystem().getRegions();
			if (!regions.isEmpty()) {
				for (IRegion i : regions) {
					getPlottingSystem().removeRegion(i);
				}

//TODO: still need to clean up the view and just update peaks with a empty list i guess??
//				if (!controller.getPeaks().isEmpty()) {
//					controller.getPeaks().clear();
//				}

				peaksTrace.setVisible(false);
				regionBndsTrace.setVisible(false);

				// hide region bounds whilst search
				regionBndsTrace.setVisible(true);
			}

			getPlottingSystem().addRegionListener(this);
			this.searchRegion = getPlottingSystem().createRegion(
					RegionUtils.getUniqueName("Search selection", getPlottingSystem()),
					getPlottingSystem().is2D() ? IRegion.RegionType.BOX : IRegion.RegionType.XAXIS);
			searchRegion.setRegionColor(ColorConstants.red);
			searchRegion.setMobile(false);

			// SELECTING THOSE BOUNDS UPPER AND LOWER

			// TODO: cant't yet do this here as rectangularROI doesnt exist yet
			// TODO: this can be do e when a region is added isntead ... why so?
			if (searchRegion != null && searchRegion.getROI() instanceof RectangularROI) {
				RectangularROI rectangle = (RectangularROI) searchRegion.getROI();
				// Set the region bounds
				updateSearchBnds(rectangle);
			}


			
			// TODO: need to now connect up this to have a resultant effect on
			// the viewer
			if (table.viewer != null) {
				table.viewer.refresh();
			}

		} catch (Exception e) {
			logger.error("Cannot put the selection into searching region mode!", e);
		}
	}
	
	
	
	public void setSearchDataOnBounds(){
		// Obtain Upper and Lower Bounds
		Dataset xData = DatasetUtils.convertToDataset(sampleTrace.getXData().squeeze());
		Dataset yData = DatasetUtils.convertToDataset(sampleTrace.getYData().squeeze());

		BooleanDataset allowed = Comparisons.withinRange(xData, lowerBnd, upperBnd);
		xData = xData.getByBoolean(allowed);
		yData = yData.getByBoolean(allowed);
	
	}
	

	public void updateSearchBnds(RectangularROI rect) {
		lowerBnd = rect.getPointRef()[0];
		upperBnd = rect.getPointRef()[0] + rect.getLengths()[0];
		
		double[] x = { lowerBnd, upperBnd};
		
		Dataset xdata = DatasetFactory.createFromObject(x);
		Dataset ydata = genBoundsHeight();
		
		updateTraceBounds(xdata, ydata); //TODO: this is already triggers
		
		//TODO: update manager search data 
		setSearchDataOnBounds();
		
		
		//Load in new search bounds to beacon
		
		PeakOppurtunity peakOpp = new PeakOppurtunity();
		peakOpp.setXData(xData);
		peakOpp.setYData(yData);
		peakOpp.setLowerBound(lowerBnd);
		peakOpp.setUpperBound(upperBnd);
		
		//TODO: might need to postpone whilst configure more on peakOpp..
		controller.loadPeakOppurtunity(peakOpp);
		
		
		
	}

	public void updateBoundsUpper(double upperVal){
		Dataset xData = (Dataset) regionBndsTrace.getXData();
		xData.set(upperVal, 1);
		updateTraceBounds(xData, (Dataset) regionBndsTrace.getYData());
	}
	
	public void updateBoundsLower(double lowerVal){
		Dataset xData = (Dataset) regionBndsTrace.getXData();
		xData.set(lowerVal, 0);
		updateTraceBounds(xData, (Dataset) regionBndsTrace.getYData());
	}

	// TODO: HAVE A UPDATE OR A SET? really do they do anything different
	// TODO: this needs another trigger in roder to redraw to the plot ...
	private void updateTraceBounds(Dataset x, Dataset y) {
		regionBndsTrace.setData(x, y);
		if (!regionBndsTrace.isVisible())
			regionBndsTrace.setVisible(true);

		getPlottingSystem().repaint();
	}

	public void updatePeakTrace(Dataset x, Dataset y) {
		peaksTrace.setData(x, y);
		if (!peaksTrace.isVisible())
			peaksTrace.setVisible(true);
		
		getPlottingSystem().repaint(); 
	}

	private void updatePeakTrace(List<Peak> peakSet) {
		//TODO: do i need this
		List<Double> pX = new ArrayList<Double>();
		List<Double> pY = new ArrayList<Double>();

		if (!peakSet.isEmpty()) {
			for (int i = 0; i < peakSet.size(); ++i) {
				pX.add(peakSet.get(i).getX());
				pY.add(peakSet.get(i).getY());
			}
		}

		// Update Trace
		Dataset peakx = DatasetFactory.createFromList(pX);
		Dataset peaky = DatasetFactory.createFromList(pY);
	
		updatePeakTrace(peakx,peaky);
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
			Double cur_x = (Double) x.getDouble(closeIdx);
			Double nw_x = (Double) x.getDouble(i);

			if (Math.abs(nw_x - xy[0]) < Math.abs(cur_x - xy[0])) {
				closeIdx = i;
			}
		}
		return closeIdx;
	}

	public Dataset genBoundsHeight() {
		double top = getPlottingSystem().getAxes().get(1).getUpper();
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

			RectangularROI rectangle = (RectangularROI) searchRegion.getROI();

			updateSearchBnds(rectangle);

			// Make inactive so can touch around
			evt.getRegion().setVisible(false);
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
	}

	@Override
	public Control getControl() {
		return this.composite;
	}

	@Override
	public void setFocus() {
		composite.setFocus();
	}

	
}
