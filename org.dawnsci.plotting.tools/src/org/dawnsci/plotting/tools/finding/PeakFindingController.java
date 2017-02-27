package org.dawnsci.plotting.tools.finding;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.plaf.synth.Region;

import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.preference.PeakFindingConstants;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.jface.viewers.TableViewer;

import uk.ac.diamond.scisoft.analysis.fitting.functions.Add;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IdentifiedPeak;
import uk.ac.diamond.scisoft.analysis.peakfinding.IPeakFindingData;
import uk.ac.diamond.scisoft.analysis.peakfinding.IPeakFindingService;
import uk.ac.diamond.scisoft.analysis.peakfinding.Peak;

/**
 * Manages the interactions of peakfinders to view
 * 
 * 
 * @author Dean P. Ottewell
 *
 */
public class PeakFindingController {
	
	PeakFindingTool peakfindingtool;
	
	public IPlottingSystem plottingSystem;
	
	
	//TODO: intialise in the cosntructor
	private PeakFindingActionsDelegate actions = new PeakFindingActionsDelegate(this);
	private PeakFindingWidget widget = new PeakFindingWidget(this);
	private PeakFindingTable table = new PeakFindingTable(this);
	
	PeakSearchJob peakSearchJob;
	
	// Peak Finders Load From Service Class
	private static IPeakFindingService peakFindServ = (IPeakFindingService) Activator
			.getService(IPeakFindingService.class);
	
	//TODO: decide on if also storing the raw peaks values as well as the id peaks. There can only be one or else both are superfluous
	Dataset peaksY;
	Dataset peaksX;
	
	//Controls for user picking the peaks
	public Boolean isRemoving = false; 
	public Boolean isAdding = false;
	
	//Bound limits for searching 
	private Double upperBnd;
	private Double lowerBnd;
	
	private Double searchScaleIntensity;
	
	private IPeakFindingData peakFindData; 
	private String peakFinderID;

	//The table viewer should exist here
	//TODO:Move these control values
	
	// Peak Details - could store as a series of functions however this will eventually be realised by the fitting
	private Add peaksCompFunc;
	
	//Really need that intermediate of a identified peak. COuld the below be the answer
	List<IdentifiedPeak> peaksIdentified = new ArrayList<IdentifiedPeak>();
	List<Peak> peaks = new ArrayList<Peak>();
	
	public void clearPeaks(){
		this.peaksCompFunc = null; //TODO: isn't there a proper way to clear?
		peaks.clear();
	}
	
	/**
	 * Assumes peakpos are those represented in yData passed into. 
	 * 
	 * xData and yData must be same size
	 * 
	 * @param peakpos
	 * @param xData
	 * @param yData
	 * @return every peak pos inside @peakpos cast to identified Peak
	 */
	private List<IdentifiedPeak> convertIntoPeaks(Map<Integer, Double> peakpos, Dataset xData, Dataset yData){
		
		ArrayList<IdentifiedPeak> peaks = new ArrayList<IdentifiedPeak>();
		int backPos, forwardPos;
		double backTotal, forwardTotal;
		double backValue, forwardValue;
		
		for (Map.Entry<Integer, Double> peak : peakpos.entrySet()) {

			backPos = peak.getKey() - 1;
			backValue = yData.getElementDoubleAbs(backPos);
			
			
			forwardPos = peak.getKey() + 1;
			forwardValue = yData.getElementDoubleAbs(forwardPos);
			
			/*XXX: well if not nromalized to zero can not really assume that zero is the turning point...*/

			// Found zero crossing from positive to negative (maximum)
			// now, work out left and right height differences from local minima or edge
			backTotal = 0;
			// get the backwards points
			while (backPos > 0) {
				if (backValue >= 0) {
					backTotal += backValue;
					backPos -= 1;
					backValue = yData.getElementDoubleAbs(backPos);
				} else {
					break;
				}
			}

			// get the forward points
			forwardTotal = 0;
			while (forwardPos < xData.getSize()) {
				if (forwardValue <= 0) {
					forwardTotal -= forwardValue;
					forwardPos += 1;
					forwardValue = yData.getElementDoubleAbs(forwardPos);
				} else {
					break;
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
			
			IdentifiedPeak newPeak = new IdentifiedPeak(position, minXValue,maxXValue,area,height,indexofMaxXVal,indexOfMinXVal,crossingsFnd);
			peaks.add(newPeak);
		}
		
		return peaks;
	}
	
	
	public void refreshTable(){
		getTable().viewer.refresh();
	}
	
	public TableViewer getTableViewer(){
		return getTable().viewer;
	}
		
	public Double getLowerBnd() {
		return lowerBnd;
	}

	public void setLowerBnd(Double lowerBnd) {
		getWidget().setLwrBndVal(lowerBnd);
		
		if(this.peakfindingtool != null)
			this.peakfindingtool.updateBoundsLower(lowerBnd);
		
		if(this.selectedRegion != null){
			double[] endPnt = this.selectedRegion.getPointRef();
			this.selectedRegion.setEndPoint(new double[]{upperBnd,endPnt[1]});
			
		}
		this.lowerBnd = lowerBnd;
	}
	
	public Double getUpperBnd() {
		return upperBnd;
	}
	
	public void setUpperBnd(Double upperBnd) {
		getWidget().setUprBndVal(upperBnd);
		
		if(peakfindingtool != null)
			peakfindingtool.updateBoundsUpper(upperBnd);
		//TMP impl
		if(selectedRegion != null){
			double[] endPnt = this.selectedRegion.getEndPoint();
			endPnt[0] = upperBnd;
			this.selectedRegion.setEndPoint(endPnt);
		}
		
		this.upperBnd = upperBnd;
	}
	
	
	
	RectangularROI selectedRegion;
	
	public void setRegion(RectangularROI region){
		this.selectedRegion = region;
	}
	
	public RectangularROI getSelectedRegion(){
		return this.selectedRegion;
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
		PeakFindingController.peakFindServ = peakFindServ;
	}
	
	public IPeakFindingData getPeakFindData() {
		return peakFindData;
	}
	
	public void setPeakFindData(IPeakFindingData peakFindData) {
		this.peakFindData = peakFindData;
	}

	public void updatePeakTrace(){
		if(peaksX != null || peaksY != null)
			peakfindingtool.updatePeakTrace(peaksX, peaksY);
	}
		
	public void formatPeakSearch(){
		updatePeakTrace();

		if (getTable().viewer != null)
			getTable().viewer.refresh();

		// TODO: CLEAN UP ON ACTIVATE PEAKFINDER
		getPeakFindData().deactivatePeakFinder(getPeakFinderID());
		// TODO: Update with the new peaks
		peakfindingtool.getPlottingSystem().repaint();

		// Reset peak finder 
		if (!getWidget().runPeakSearch.isEnabled())
			getWidget().runPeakSearch.setImage(Activator.getImageDescriptor("icons/peakSearch.png").createImage());
		getWidget().runPeakSearch.setEnabled(true);
	}
	
	public <T> void setPlottingSystem(IPlottingSystem<T> system) {
		this.plottingSystem = system;
	}
	
	/**
	 *TODO: move setup to export function
	 * Exports found peaks in a .xy formated file
	 *
	 * @param path
	 * @return
	 * @throws IOException
	 */
	String exportFoundPeaks(final String path) throws IOException {
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
		//TODO: tmp as only wavelet has this value
		if(peakfinder.equals("Wavelet Transform")){
			Activator.getPlottingPreferenceStore().setValue("widthSz", searchScaleIntensity);
		}
		
		this.searchScaleIntensity = searchScaleIntensity;
	}

	public PeakFindingWidget getWidget() {
		return widget;
	}

	public void setWidget(PeakFindingWidget widget) {
		this.widget = widget;
	}

	public PeakFindingTable getTable() {
		return table;
	}

	public void setTable(PeakFindingTable table) {
		this.table = table;
	}

	public PeakFindingActionsDelegate getActions() {
		return actions;
	}

	public void setActions(PeakFindingActionsDelegate actions) {
		this.actions = actions;
	}
	
	
}
