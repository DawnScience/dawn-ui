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
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.dawnsci.plotting.api.trace.TraceWillPlotEvent;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.PointStyle;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.TraceType;
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

import uk.ac.diamond.scisoft.analysis.peakfinding.Peak;

/**
 * 
 * @author Dean P. Ottewell
 *
 */
public class PeakFindingTool extends AbstractToolPage implements IRegionListener {

	private static final Logger logger = LoggerFactory.getLogger(PeakFittingTool.class);

	PeakFindingManager manager;

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

	private IDataset interestXData;
	private IDataset interestYData;

	private List<Peak> peaks = new ArrayList<Peak>();

	private IPeakOpportunityListener listener;

	public PeakFindingTool() {
		// Setup up a new PeakSearch Instance
		this.manager = new PeakFindingManager();
		new ITraceListener.Stub() {
			@Override
			public void tracesUpdated(TraceEvent evt) {
				peakSearch.schedule();
			}
		};
	}

	public void setAddMode(boolean status) {
		isAdding = status;
		// if(searchRegion.isVisible() != !status)
		searchRegion.setMobile(!status);
		searchRegion.setVisible(!status);
	}

	public void setRemoveMode(boolean status) {
		isRemoving = status;
		// if(searchRegion.isVisible() != !status)
		searchRegion.setMobile(!status);
		searchRegion.setVisible(!status);
	}

	public void resetActions() {
		searchRegion.setMobile(true);
		searchRegion.setVisible(true);
	}

	public PeakFindingTool(IPlottingSystem system, PeakFindingManager controller) {
		this.setPlottingSystem(system);
		this.manager = controller;
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

		// TODO: id the listener...
		listener = new IPeakOpportunityListener() {
			@Override
			public void peaksChanged(PeakOpportunityEvent evt) {
				// Update Peaks
				peaks = evt.getPeaks();
				if (!peaks.isEmpty()) {
					updatePeakTrace(peaks);
				} else {
					//We have no longer any peaks to plot remove]
					//getPlottingSystem().getTrace(PEAKSTRACENAME) != null
					if(getPlottingSystem().getTrace(PEAKSTRACENAME) != null){
						getPlottingSystem().removeTrace(peaksTrace);
						peaksTrace.dispose();
					}
				}
			}

			@Override
			public void boundsChanged(double upper, double lower) {
				RectangularROI rect = (RectangularROI) searchRegion.getROI();
				double[] bounds = { lower, upper };

				// TODO: shouldnt be trigger anyway
				if (bounds[0] != rect.getPointRef()[0] && bounds[1] != rect.getPointRef()[1]) {
					// Update region
					rect.setPoint(bounds);

					lowerBnd = rect.getPointRef()[0];
					upperBnd = rect.getPointRef()[1];
					searchRegion.setROI(rect);

					Dataset xdata = DatasetFactory.createFromObject(bounds);
					Dataset ydata = genBoundsHeight();

					updateTraceBounds(xdata, ydata); // TODO: this is already
														// triggers
					if (!getPlottingSystem().getTraces().isEmpty()) {
						setSearchDataOnBounds((ILineTrace) getPlottingSystem().getTraces().iterator().next());
					}
				}
			}

			@Override
			public void dataChanged(IDataset nXData, IDataset nYData) {
				// xData = nXData;
				// yData = nYData;
			}
		};
		manager.addPeakListener(listener);

		// Control Peak Removal + Addition
		getPlottingSystem().addClickListener(new IClickListener() {
			@Override
			public void doubleClickPerformed(ClickEvent evt) {
				// TODO Auto-generated method stub
			}

			@Override
			public void clickPerformed(ClickEvent evt) {
				if (isAdding) {
					addPeakValue(evt.getxValue(), evt.getyValue());
				} else if (isRemoving) {
					if (!peaks.isEmpty())
						removePeakValue(evt.getxValue(), evt.getyValue());
				}
			}
		});

		getPlottingSystem().addTraceListener(new ITraceListener() {

			@Override
			public void tracesUpdated(TraceEvent evt) {
				// TODO Auto-generated method stub
				Object evtTrace = evt.getSource();
				ILineTrace traceUpdate = (ILineTrace) getPlottingSystem().getTraces().iterator().next();
				setSearchDataOnBounds(traceUpdate);
			}

			@Override
			public void tracesRemoved(TraceEvent evet) {
				// The last trace removed should be the trace of search interest
				if (getPlottingSystem().getTraces().isEmpty()) {
					// TODO:We have no data! Abort abort
					// disable all actions? dis able all running?
				}
			}

			@Override
			public void tracesAdded(TraceEvent evt) {
				// TODO Auto-generated method stub
				// evt.getSource().
				Object evtTrace = evt.getSource();

			}

			@Override
			public void traceWillPlot(TraceWillPlotEvent evt) {
				// When selecting data from cols this is triggered...
				ILineTrace traceUpdate = (ILineTrace) evt.getSource();
				traceUpdate.getName();
				// traceUpdate = (ILineTrace)
				// getPlottingSystem().getTraces().iterator().next();
				if (traceUpdate.getName() != BOUNDTRACENAME || traceUpdate.getName() != PEAKSTRACENAME) {
					// setSearchDataOnBounds(traceUpdate);
				}
			}

			@Override
			public void traceUpdated(TraceEvent evt) {
				// On a change here we should update the search based on thsi
				// new data
				ILineTrace traceUpdate = (ILineTrace) evt.getSource();
				if (traceUpdate.getName() != BOUNDTRACENAME && traceUpdate.getName() != PEAKSTRACENAME) {
					
					setSearchDataOnBounds(traceUpdate);
					// Load in new search bounds to beacon
					PeakOppurtunity peakOpp = new PeakOppurtunity();

					peakOpp.setXData(interestXData);
					peakOpp.setYData(interestYData);
					//peakOpp.setLowerBound(lowerBnd);
					//peakOpp.setUpperBound(upperBnd);

					manager.loadPeakOppurtunity(peakOpp);
				}
				
				//If the data has been changed I will assume wish for a search to be created.
				//TODO: activate widget search event
				
			}

			@Override
			public void traceRemoved(TraceEvent evt) {
				// if trace is my sample trace though

			}

			@Override
			public void traceCreated(TraceEvent evt) {
				// TODO Auto-generated method stub

			}

			@Override
			public void traceAdded(TraceEvent evt) {
				// TODO Auto-generated method stub

			}
		});

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
		// XXX: Unfortunately now this will be on the end of the result. The
		// order is important for the table view. Need comaprator there
		peaks.add(p);

		// Update Trace
		Dataset peakx = DatasetFactory.createFromList(pX);
		Dataset peaky = DatasetFactory.createFromList(pY);

		updatePeakTrace(peakx, peaky);

		manager.setPeaks(peaks);
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

		peaks.remove(toRemove);

		if (!peaks.isEmpty()) {
			peakx = DatasetFactory.createFromList(pX);
			peaky = DatasetFactory.createFromList(pY);
			updatePeakTrace(peakx, peaky);
		}

		manager.setPeaks(peaks);
	}

	public void configureTraces() {
		if (!getPlottingSystem().getTraces().isEmpty()) {
			ILineTrace sampleTrace = (ILineTrace) getPlottingSystem().getTraces().iterator().next(); // XXX:assumes
																										// base
																										// trace
																										// is
																										// the
																										// sample
																										// trace

			// Setup Upper & lower bound for search region
			if (getPlottingSystem().getTrace(BOUNDTRACENAME) == null) {
				regionBndsTrace = generateBoundTrace(BOUNDTRACENAME);
				getPlottingSystem().addTrace(regionBndsTrace);
			} else {
				regionBndsTrace = (ILineTrace) getPlottingSystem().getTrace(BOUNDTRACENAME);
			}

			// Intialise to upper and low limit of sample trace
			Dataset xData = (Dataset) sampleTrace.getXData();
			Double lwrBnd = xData.getDouble(0);
			Double uprBnd = xData.getDouble(xData.argMax());

			Dataset xBnds = DatasetFactory.createFromObject(new double[] { lwrBnd, uprBnd });
			Dataset bndHeight = genBoundsHeight();

			updateTraceBounds(xBnds, bndHeight);
		}
	}

	private ILineTrace generateBoundTrace(String tracename) {
		ILineTrace trace = getPlottingSystem().createLineTrace(BOUNDTRACENAME);
		trace.setLineWidth(3);
		trace.setTraceType(TraceType.HISTO);
		trace.setTraceColor(ColorConstants.orange);
		return trace;
	}

	private ILineTrace generatePeakTrace(String tracename) {
		ILineTrace trace = getPlottingSystem().createLineTrace(tracename);
		trace.setLineWidth(1);
		trace.setPointStyle(PointStyle.CIRCLE);
		trace.setPointSize(3);
		trace.setTraceType(TraceType.HISTO);
		return trace;
	}

	public void createNewSearch() {
		try {
			Collection<IRegion> regions = getPlottingSystem().getRegions();
			if (!regions.isEmpty()) {
				for (IRegion i : regions) {
					getPlottingSystem().removeRegion(i);
				}
			}

			if (!peaks.isEmpty()) {
				// Regenerate the trace to older times
				getPlottingSystem().removeTrace(peaksTrace);
				peaksTrace.dispose();
				//Update reset
				PeakOppurtunity peakOpp = new PeakOppurtunity();
				manager.setPeaks(peaks);
				manager.loadPeakOppurtunity(peakOpp);
			}

			getPlottingSystem().addRegionListener(this);
			this.searchRegion = getPlottingSystem().createRegion(
					RegionUtils.getUniqueName("Search selection", getPlottingSystem()),
					getPlottingSystem().is2D() ? IRegion.RegionType.BOX : IRegion.RegionType.XAXIS);

			searchRegion.setLineWidth(5);
			searchRegion.setRegionColor(ColorConstants.red);
			searchRegion.setMobile(true);
			// searchRegion.setTrackMouse(true);//TODO: expands the region by
			// hovering...

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
					if (!getPlottingSystem().getTraces().isEmpty()) {
						setSearchDataOnBounds((ILineTrace) getPlottingSystem().getTraces().iterator().next());
						// Load in new search bounds to beacon
						PeakOppurtunity peakOpp = new PeakOppurtunity();

						peakOpp.setXData(interestXData); // TODO: slice from
															// sample data with
															// region here
															// instead
						peakOpp.setYData(interestYData);

						peakOpp.setLowerBound(lowerBnd);
						peakOpp.setUpperBound(upperBnd);
						// TODO: might need to postpone whilst configure more on
						// peakOpp..
						manager.loadPeakOppurtunity(peakOpp);

					}

				}
			});

			// SELECTING THOSE BOUNDS UPPER AND LOWER

			// TODO: cant't yet do this here as rectangularROI doesnt exist yet
			// TODO: this can be do e when a region is added isntead ... why so?
			if (searchRegion != null && searchRegion.getROI() instanceof RectangularROI) {
				RectangularROI rectangle = (RectangularROI) searchRegion.getROI();
				// Set the region bounds
				updateSearchBnds(rectangle);
			}


		} catch (Exception e) {
			logger.error("Cannot put the selection into searching region mode!", e);
		}
	}

	public void setSearchDataOnBounds(ILineTrace trace) {
		// Obtain Upper and Lower Bounds
		Dataset xDataRaw = DatasetUtils.convertToDataset(trace.getXData().squeeze());
		Dataset yDataRaw = DatasetUtils.convertToDataset(trace.getYData().squeeze());

		// TODO: believe a cast in the peakfinder foreced me to use
		// DoubleDateset here. Fix interface setup peakfinder.
		DoubleDataset xData = DatasetUtils.cast(DoubleDataset.class, xDataRaw);
		DoubleDataset yData = DatasetUtils.cast(DoubleDataset.class, yDataRaw);

		BooleanDataset allowed = Comparisons.withinRange(xData, lowerBnd, upperBnd);
		this.interestXData = xData.getByBoolean(allowed);
		this.interestYData = yData.getByBoolean(allowed);
	}

	public void updateSearchBnds(RectangularROI rect) {
		lowerBnd = rect.getPointRef()[0];
		upperBnd = rect.getPointRef()[0] + rect.getLengths()[0];

		double[] x = { lowerBnd, upperBnd };

		Dataset xdata = DatasetFactory.createFromObject(x);
		Dataset ydata = genBoundsHeight();

		updateTraceBounds(xdata, ydata); // TODO: this is already triggers
	}

	public void updateBoundsUpper(double upperVal) {
		Dataset xData = (Dataset) regionBndsTrace.getXData();
		xData.set(upperVal, 1);
		updateTraceBounds(xData, (Dataset) regionBndsTrace.getYData());
	}

	public void updateBoundsLower(double lowerVal) {
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

		if (getPlottingSystem() != null)
			getPlottingSystem().repaint();
	}

	public void updatePeakTrace(Dataset x, Dataset y) {
		if (getPlottingSystem().getTrace(PEAKSTRACENAME) == null) {
			peaksTrace = generatePeakTrace(PEAKSTRACENAME);
			getPlottingSystem().addTrace(peaksTrace);
		}

		peaksTrace.setData(x, y);

		if (!peaksTrace.isVisible()) {
			peaksTrace.setVisible(true);		
		}

		getPlottingSystem().repaint();
	}

	private void updatePeakTrace(List<Peak> peakSet) {
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
			Double cur_x = (Double) x.getDouble(closeIdx);
			Double nw_x = (Double) x.getDouble(i);

			if (Math.abs(nw_x - xy[0]) < Math.abs(cur_x - xy[0])) {
				closeIdx = i;
			}
		}
		return closeIdx;
	}

	public Dataset genBoundsHeight() {
		double top = getPlottingSystem().getSelectedYAxis().getUpper();
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
		if (getPlottingSystem() != null) {
			if (searchRegion != null)
				getPlottingSystem().removeRegion(searchRegion);

			if (regionBndsTrace != null)
				getPlottingSystem().removeTrace(regionBndsTrace);

			if (peaksTrace != null)
				getPlottingSystem().removeTrace(peaksTrace);

		}
		// TODO: icon removal
		// TODO: clear peaks
		// TODO: manager remove the manager listeners? destorying anwyay though
		// manager.destroyAllListeners();

		Collection<IRegion> regions = getPlottingSystem().getRegions();
		for (IRegion region : regions) {
			getPlottingSystem().removeRegion(region);
		}

	}

	@Override
	public void dispose() {
		deactivate();
		super.dispose();
		composite.dispose();
		// TODO: kill manager jobs... maybe might not be storing the jobs...
		// there scheduled though so should have segment of all jobs runnign
	}

}
