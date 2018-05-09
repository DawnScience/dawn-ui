package org.dawnsci.plotting.tools.finding;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.preference.PeakFindingConstants;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IdentifiedPeak;
import uk.ac.diamond.scisoft.analysis.peakfinding.IPeakFindingData;
import uk.ac.diamond.scisoft.analysis.peakfinding.IPeakFindingService;

/**
 * Manages the interactions of peakfinders to view
 * 
 * @author Dean P. Ottewell
 */
public class PeakFindingManager {

	private static final String CONVOLVE_WIDTH_SIZE = "Convolve Width Size";

	private static final Logger logger = LoggerFactory.getLogger(PeakFindingManager.class);
	
	PeakFindingSearchJob peakSearchJob;
	
	// Peak Finders Load From Service Class
	private static IPeakFindingService peakFindServ = (IPeakFindingService) Activator
			.getService(IPeakFindingService.class);
	
	private Double searchScaleThreshold;
	
	private IPeakFindingData peakFindData; 
	private String peakFinderID;
	
	private HashSet<IPeakOpportunityListener> listeners;
	
	List<IdentifiedPeak> peaksIdentified = new ArrayList<IdentifiedPeak>();

	public PeakFindingManager(){
		listeners = new HashSet<IPeakOpportunityListener>();
	}
	
	public String getPeakFinderID() {
		return peakFinderID;
	}
	
	public void setPeakFinderID(String peakFinderID) {
		Activator.getPlottingPreferenceStore().setValue(PeakFindingConstants.PeakAlgorithm, peakFinderID);
		this.peakFinderID = peakFinderID;
	}
	
	public static IPeakFindingService getPeakFindServ() {
		return peakFindServ;
	}
	
	public static void setPeakFindServ(IPeakFindingService peakFindServ) {
		PeakFindingManager.peakFindServ = peakFindServ;
	}
	
	public IPeakFindingData getPeakFindData() {
		return peakFindData;
	}
	
	public void setPeakFindData(IPeakFindingData peakFindData) {
		this.peakFindData = peakFindData;
	}
	
	/**
	 *TODO: move setup to export function
	 * Exports found peaks in a .xy formated file
	 *
	 * @param path
	 * @return
	 * @throws IOException
	 */
	String exportFoundPeaks(final String path, List<IdentifiedPeak> peaks) throws IOException {
		File file = new File(path);
		if (!file.getName().toLowerCase().endsWith(".xy"))
			file = new File(path + ".xy");
		if (file.exists())
			file.delete();
		else
			file.createNewFile();

		final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
		try {
			//TODO: fix the formatter. have iterate over identified peaks 
			for (IdentifiedPeak p : peaks){
				writer.write(p.getPos() + " " + p.getHeight()+ "\n");
			}
			
		} finally {
			writer.close();
		}

		return file.getAbsolutePath();
	}

	public Double getSearchScaleThreshold() {
		return searchScaleThreshold;
	}

	public void setSearchThreshold(double searchScaleThreshold) {
		String peakfinder = Activator.getPlottingPreferenceStore().getString(PeakFindingConstants.PeakAlgorithm);	
		//TODO: tmp as only wavelet has the adjustment configured correctly
		if(peakfinder.equals("Wavelet Transform")){
			Activator.getPlottingPreferenceStore().setValue(CONVOLVE_WIDTH_SIZE, searchScaleThreshold);
			
		}
		String curVal = Activator.getPlottingPreferenceStore().getString(CONVOLVE_WIDTH_SIZE);
		this.searchScaleThreshold = searchScaleThreshold;
	}
	
	/*TRIGGERS*/
	
	public void setPeaksId(List<IdentifiedPeak> peaksId) {
		IPeakOpportunity peakOpp = new PeakOpportunity();
		peakOpp.setPeaksId(peaksId);
		everythingChangesListeners(new PeakOpportunityEvent(this, peakOpp));
	}
	
	public void setPeaks(Map<Integer,Double> peakpos,Dataset xData, Dataset yData) {
		IPeakOpportunity peakOpp = new PeakOpportunity();
		peakOpp.setPeaksId(convertIntoPeaks(peakpos, xData, yData));
		everythingChangesListeners(new PeakOpportunityEvent(this, peakOpp));
	}
	
	public void setPeakSearching() {
		IPeakOpportunity peakOpp = new PeakOpportunity();
		peakOpp.setPeakSearching(false);
		everythingChangesListeners(new PeakOpportunityEvent(this, peakOpp));
	}
	
	public void activateSearchRegion() {
		for(IPeakOpportunityListener listener : listeners) {
			listener.activateSearchRegion();
		}
	}
	
	public void finishedPeakSearching() {
		IPeakOpportunity peakOpp = new PeakOpportunity();
		peakOpp.setPeakSearching(true);
		everythingChangesListeners(new PeakOpportunityEvent(this, peakOpp));
	}
	
	
	public void addPeakListener(IPeakOpportunityListener listener) {
		listeners.add(listener);
	}
	
	public void removePeakListener(IPeakOpportunityListener listener) {
		listeners.remove(listener);
	}

	private void everythingChangesListeners(PeakOpportunityEvent evt) {
		for(IPeakOpportunityListener listener : listeners) {
			
			if(evt.getPeakOpp().getPeaksId() != null){
				
				sendPeakfindingEvent(evt.getPeakOpp().getPeaksId()); //TODO: way to enable and disable this degree of sending
				listener.peaksChanged(evt);
			}
			if(evt.getPeakOpp().getLowerBound() != 0 && evt.getPeakOpp().getUpperBound() != 0)
				listener.boundsChanged(evt.getPeakOpp().getUpperBound() , evt.getPeakOpp().getLowerBound());

			if (evt.getPeakOpp().getXData() != null && evt.getPeakOpp().getYData() != null)
				listener.dataChanged(evt.getPeakOpp().getXData(),evt.getPeakOpp().getYData());
			
			if (evt.getPeakOpp().getSearchingStatus() != null) {
				if(evt.getPeakOpp().getSearchingStatus()){
					listener.finishedPeakFinding();
				} else {
					listener.isPeakFinding();
				}
			}
		}
	}

	public void loadPeakOpportunity(IPeakOpportunity peaksOpp) {
		everythingChangesListeners(new PeakOpportunityEvent(this, peaksOpp));
	}
	
	public void destroyAllListeners(){
		listeners.clear();
	}

	/**
	 * Assumes peakpos are those represented in yData passed into. 
	 * xData and yData must be same size
	 * 
	 * @param peakpos
	 * @param xData
	 * @param yData 
	 * @return every peak pos inside @peakpos cast to identified Peak
	 */
	List<IdentifiedPeak> convertIntoPeaks(Map<Integer, Double> peakpos, Dataset xData, Dataset yData) {
		
		if(xData.getSize() != yData.getSize())
			logger.error("Signal data must be matching size");
		
		List<IdentifiedPeak> peaksID = new ArrayList<IdentifiedPeak>();
		for (Map.Entry<Integer, Double> peak : peakpos.entrySet()) {
			peaksID.add(generateIdentifedPeak(peak.getKey(),xData,yData));
		}
		return peaksID;
	}
	
	public IdentifiedPeak generateIdentifedPeak(Integer pos, Dataset xData, Dataset yData) {
		int backPos, forwardPos;	
		double backTotal, forwardTotal;
		double backValue, forwardValue;
		
		backPos = pos >= 0 ? pos : pos-1;
		backValue = yData.getElementDoubleAbs(backPos);

		//Get a initial forward position
		forwardPos = pos <= yData.getSize() ? pos: pos+1;
		forwardValue = yData.getElementDoubleAbs(forwardPos);
		
		//Check state of calculating back and forward position is not greater that	n peaks 
		boolean backDescending = true;//(peak.getValue() >= backValue);
		boolean forwardDescending = true;//(peak.getValue() >= forwardValue);
		
		backTotal = 0;
		while(backDescending){
			if(backPos > 0) {
				double nextBackVal = yData.getElementDoubleAbs(backPos+-1); //get nextPos
				backValue = yData.getElementDoubleAbs(backPos);
				if (backValue >= nextBackVal) { 
					backTotal += backValue;
					backPos-=1; 
				} else {
					backDescending = false;
				}
			} else {
				backDescending = false;
			}
		}
		
		forwardTotal = 0;
		while(forwardDescending){
			if(forwardPos < yData.getSize()-1) {
				double nextBackVal = yData.getElementDoubleAbs(forwardPos + 1); //get nextPos
				forwardValue = yData.getElementDoubleAbs(forwardPos);
				if (forwardValue >= nextBackVal) {
					forwardTotal -= forwardValue;
					forwardPos++;  
				} else {
					forwardDescending= false;
				}
			} else {
				forwardDescending = false;
			}
		}

		//Okay so below is some logic can grab to finding the peak info
		int[] start = { backPos };
		int[] stop = { forwardPos };
		int[] step = { 1 };
		
		Dataset slicedXData = xData.getSlice(start, stop, step);
		Dataset slicedYData = yData.getSlice(start, stop, step);
		
		List<Double> crossings = DatasetUtils.crossings(slicedXData, slicedYData, slicedYData.max()
				.doubleValue() / 2);
		
		//No slice gathered as range too small. Must be current values
		if (crossings.size() <= 1) {
			crossings.clear();
			crossings.add((double) backPos);
			crossings.add((double) forwardPos);
		}
		
		double positionVal = xData.getElementDoubleAbs(pos);
		double minXValue = xData.getElementDoubleAbs(backPos) ;
		double maxXValue = xData.getElementDoubleAbs(forwardPos); 
		double area = Math.min(backTotal, forwardTotal); 
		double height = yData.getElementDoubleAbs(pos); //slicedYData.peakToPeak().doubleValue(); //or just yData.getElementDoubleAbs(pos);
		int indexOfMinXVal = backPos; 
		int indexofMaxXVal = forwardPos;
		List<Double> crossingsFnd = crossings;
		
		IdentifiedPeak newPeak = new IdentifiedPeak(positionVal, minXValue,maxXValue,area,height,indexOfMinXVal, indexofMaxXVal,crossingsFnd);
		
		return newPeak;
	}
	
	
	public void sendPeakfindingEvent(List<IdentifiedPeak> peaksId) {
		//TODO:Spawn plug in view
		
		BundleContext ctx = FrameworkUtil.getBundle(Activator.class).getBundleContext();
		ServiceReference<EventAdmin> ref = ctx.getServiceReference(EventAdmin.class);
	    
		EventAdmin eventAdmin = ctx.getService(ref);
	    //The object in this case being a list of cell parametr
		Map<String,Object> properties = new HashMap<String, Object>();
		
		/*
		 * TMP cells pass
		 * */
		 //List<IdentifiedPeak> idPeaks = new ArrayList<IdentifiedPeak>();

		//TODO: where to put this trigger line?
	    properties.put("PEAKRESULTS", peaksId);
	    
	    //Going to be triggered on a button and would like to know its arrived. However, should check before beforeing this action the 
	    //view is active... If thats the case maybe based to have it async...
	    Event event = new Event("peakfinding/syncEvent", properties);
	    eventAdmin.sendEvent(event);
	}
	
}
