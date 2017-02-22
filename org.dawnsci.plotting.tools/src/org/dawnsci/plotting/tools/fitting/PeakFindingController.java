package org.dawnsci.plotting.tools.fitting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.preference.PeakFindingConstants;
import org.eclipse.dawnsci.analysis.api.peakfinding.IPeakFinderParameter;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.jface.viewers.TableViewer;
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
	
	public PeakFindingActionsDelegate actions = new PeakFindingActionsDelegate(this);
	public PeakFindingWidget widget = new PeakFindingWidget(this);
	public PeakFindingTable table = new PeakFindingTable(this);
	
	PeakSearchJob peakSearchJob;
	
	// Peak Finders Load From Service Class
	private static IPeakFindingService peakFindServ = (IPeakFindingService) Activator
			.getService(IPeakFindingService.class);
	
	
	private IPeakFindingData peakFindData; 
	private String peakFinderID;

	//The table viewer should exist here
	//TODO:Move these control values
	
	// Peak Details
	List<Peak> peaks = new ArrayList<Peak>();
	
	//TODO: decide on one of them
	Dataset peaksY;
	Dataset peaksX;

	//Controls for user picking the peaks
	public Boolean isRemoving = false; 
	public Boolean isAdding = false;
	
	
	//Bound limits for searching 
	private Double upperBnd;
	private Double lowerBnd;
	
	private Double searchScaleIntensity;
	
	public void refreshTable(){
		table.viewer.refresh();
	}
	
	public TableViewer getTableViewer(){
		return table.viewer;
	}
		
	public Double getLowerBnd() {
		return lowerBnd;
	}
	public void setLowerBnd(Double lowerBnd) {
		widget.setLwrBndVal(lowerBnd);
		peakfindingtool.updateBoundsLower(lowerBnd);
		this.lowerBnd = lowerBnd;
	}
	public Double getUpperBnd() {
		return upperBnd;
	}
	public void setUpperBnd(Double upperBnd) {
		widget.setUprBndVal(upperBnd);
		peakfindingtool.updateBoundsUpper(upperBnd);
		this.upperBnd = upperBnd;
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

		if (table.viewer != null)
			table.viewer.refresh();

		// TODO: CLEAN UP ON ACTIVATE PEAKFINDER
		getPeakFindData().deactivatePeakFinder(getPeakFinderID());
		// TODO: Update with the new peaks
		peakfindingtool.getPlottingSystem().repaint();

		// Reset peak finder 
		if (!widget.runPeakSearch.isEnabled())
			widget.runPeakSearch.setImage(Activator.getImageDescriptor("icons/peakSearch.png").createImage());
		widget.runPeakSearch.setEnabled(true);
	}
	
	
	/**
	 *
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
	
	
}
