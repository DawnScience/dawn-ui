package org.dawnsci.plotting.tools.finding;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.fitting.FunctionFittingTool;
import org.dawnsci.plotting.tools.preference.PeakFindingConstants;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.fitting.Generic1DFitter;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IdentifiedPeak;
import uk.ac.diamond.scisoft.analysis.peakfinding.IPeakFindingData;
import uk.ac.diamond.scisoft.analysis.peakfinding.IPeakFindingService;
import uk.ac.diamond.scisoft.analysis.peakfinding.Peak;

/**
 * Manages the interactions of peakfinders to view
 * 
 * @author Dean P. Ottewell
 */
public class PeakFindingManager {

	private static final Logger logger = LoggerFactory.getLogger(PeakFindingManager.class);
	
	PeakFindingSearchJob peakSearchJob;
	
	// Peak Finders Load From Service Class
	private static IPeakFindingService peakFindServ = (IPeakFindingService) Activator
			.getService(IPeakFindingService.class);
	
	private Double searchScaleIntensity;
	
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
	String exportFoundPeaks(final String path, List<Peak> peaks) throws IOException {
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
			for (Peak p : peaks) {
				writer.write(p.getXYFormat());
				writer.newLine();
			}
			
		} finally {
			writer.close();
		}

		return file.getAbsolutePath();
	}

	public Double getSearchScaleIntensity() {
		return searchScaleIntensity;
	}

	public void setSearchScaleIntensity(double searchScaleIntensity) {
		String peakfinder = Activator.getPlottingPreferenceStore().getString(PeakFindingConstants.PeakAlgorithm);	
		//TODO: tmp as only wavelet has the adjustability
		if(peakfinder.equals("Wavelet Transform")){
			Activator.getPlottingPreferenceStore().setValue("widthSz", searchScaleIntensity);
		}
		
		this.searchScaleIntensity = searchScaleIntensity;
	}
	
	/*TRIGGERS*/
	
	public void setPeaks(List<Peak> peaks){
		IPeakOpportunity peakOpp = new PeakOppurtunity();
		peakOpp.setPeaks(peaks);
		everythingChangesListeners(new PeakOpportunityEvent(this, peakOpp));
	}
	
	public void setPeaks(Map<Integer,Double> peakpos,IDataset xData, IDataset yData){
		IPeakOpportunity peakOpp = new PeakOppurtunity();
		peakOpp.setPeaksId(convertIntoPeaks(peakpos, (Dataset) xData, (Dataset) yData));
		everythingChangesListeners(new PeakOpportunityEvent(this, peakOpp));
	}
	
	public void setPeakSearching(){
		IPeakOpportunity peakOpp = new PeakOppurtunity();
		peakOpp.setSearching(false);
		everythingChangesListeners(new PeakOpportunityEvent(this, peakOpp));
	
	}
	
	public void finishedPeakSearching(){
		IPeakOpportunity peakOpp = new PeakOppurtunity();
		peakOpp.setSearching(true);
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
			
			if(evt.getPeaks() != null)
				listener.peaksChanged(evt);			

			if(evt.getPeakOpp().getPeaksId() != null)
				listener.peaksChanged(evt);
			
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

	public void loadPeakOppurtunity(IPeakOpportunity peaksOpp){
		everythingChangesListeners(new PeakOpportunityEvent(this, peaksOpp));
	}
	
	public void destroyAllListeners(){
		Iterator<IPeakOpportunityListener> itr = listeners.iterator();
		while(itr.hasNext()){
			listeners.remove(itr);
			itr.next();
		}
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
	private List<IdentifiedPeak> convertIntoPeaks(Map<Integer, Double> peakpos, Dataset xData, Dataset yData){
		
		if(xData.getSize() != yData.getSize())
			logger.error("Signal data must be matching size");
		
		ArrayList<IdentifiedPeak> peaks = new ArrayList<IdentifiedPeak>();
		int backPos, forwardPos;	
		double backTotal, forwardTotal;
		double backValue, forwardValue;
		
		for (Map.Entry<Integer, Double> peak : peakpos.entrySet()) {
			
			//Get a intial backwards position
			backPos = peak.getKey() >= 0 ? peak.getKey() : peak.getKey()-1;
			backValue = yData.getElementDoubleAbs(backPos);
			

			//Get a intial forward posiiton
			forwardPos = peak.getKey() <= yData.getSize() ? peak.getKey(): peak.getKey()+1;
			forwardValue = yData.getElementDoubleAbs(forwardPos);
			
			//Check state of calucalteing back and forward posiition is not greater tha	n peaks 
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
			
			Dataset slicedXData = (Dataset) xData.getSlice(start, stop, step);
			Dataset slicedYData = (Dataset) yData.getSlice(start, stop, step);
			
			List<Double> crossings = DatasetUtils.crossings(slicedXData, slicedYData, slicedYData.max()
					.doubleValue() / 2);
			
			//No slice gathered as range too small. Must be current values
			if (crossings.size() <= 1) {
				crossings.clear();
				crossings.add((double) backPos);
				crossings.add((double) forwardPos);
			}
			
			double position = peak.getKey();
			double minXValue = xData.getElementDoubleAbs(backPos) ;
			double maxXValue = xData.getElementDoubleAbs(forwardPos); 
			double area = Math.min(backTotal, forwardTotal); 
			double height = slicedYData.peakToPeak().doubleValue();
			int indexOfMinXVal = backPos; 
			int indexofMaxXVal = forwardPos;
			List<Double> crossingsFnd = crossings;
			
			IdentifiedPeak newPeak = new IdentifiedPeak(position, minXValue,maxXValue,area,height,indexOfMinXVal, indexofMaxXVal,crossingsFnd);
			peaks.add(newPeak);
		}
		
		return peaks;
	}
	
}
