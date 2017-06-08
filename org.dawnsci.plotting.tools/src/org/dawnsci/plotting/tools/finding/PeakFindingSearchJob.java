package org.dawnsci.plotting.tools.finding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.TreeMap;

import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.preference.PeakFindingConstants;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.peakfinding.IPeakFinderParameter;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IdentifiedPeak;
import uk.ac.diamond.scisoft.analysis.peakfinding.IPeakFindingService;

/**
 * TODO: have job mroe status to retreive how the peak search in proceeding
 * 
 * @author Dean P. Ottewell
 */
public class PeakFindingSearchJob extends Job {

	protected final Logger logger = LoggerFactory.getLogger(PeakFindingSearchJob.class);

	PeakFindingManager manager;

	IPeakFindingService peakFindServ = (IPeakFindingService) Activator.getService(IPeakFindingService.class);

	IDataset xData;
	IDataset yData;

	String peakAlgorithm;
	Map<String, IPeakFinderParameter> peakParams;

	public PeakFindingSearchJob(PeakFindingManager manager, IDataset xData, IDataset yData) {
		super("Peak Search");
		this.manager = manager;
		this.xData = xData;
		this.yData = yData;
		setPriority(Job.INTERACTIVE);
	}

	// Some sort of data load into the job and then run. Do not want things to
	// be changeing whilst running
	// public void loadData(IPeakOppurtunity evt){
	// //TODO: Do not realyl need to grab event peaks as we have them here but
	// maybe better off here putting them in a event structure rather than
	// passing this list object around...
	// controller.addPeaks(peaks);
	// }

	// if (controller.getPeakfindingtool().sampleTrace == null){
	// return Status.CANCEL_STATUS;
	// }
	// if (controller.getLowerBnd() == null || controller.getUpperBnd() == null)
	// {
	// RectangularROI rectangle = (RectangularROI)
	// controller.getPeakfindingtool().searchRegion.getROI();
	// // // Set the region bounds
	// controller.getPeakfindingtool().updateSearchBnds(rectangle);
	// }

	private void loadPeakFinderParams() {
		// TODO: clean control function
		// Free up active peakfinder calls
		if (manager.getPeakFindData().hasActivePeakFinders()) {
			Collection<String> actives = manager.getPeakFindData().getActivePeakFinders();
			for (String active : actives) {
				manager.getPeakFindData().deactivatePeakFinder(active);
			}
		}

		peakAlgorithm = Activator.getPlottingPreferenceStore().getString(PeakFindingConstants.PeakAlgorithm);
		manager.getPeakFindData().activatePeakFinder(peakAlgorithm);
		// Configure peak finder on preference store go through all the params
		// that match
		peakParams = peakFindServ.getPeakFinderParameters(peakAlgorithm);
		for (Entry<String, IPeakFinderParameter> peakParam : peakParams.entrySet()) {
			IPeakFinderParameter param = peakParam.getValue();
			String curVal = Activator.getPlottingPreferenceStore().getString(peakParam.getKey());
			Number val = Double.parseDouble(curVal);
			if (param.isInt())
				val = (int) val.doubleValue();
			param.setValue(val);
			// TODO: allow single param pass
			manager.getPeakFindData().setPFParameterByName(peakAlgorithm, param.getName(), param.getValue());
		}
		manager.getPeakFindData().setPFParametersByPeakFinder(peakAlgorithm, peakParams);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		// TODO: clean control function
		// Free up active peakfinder calls
		loadPeakFinderParams();

		manager.getPeakFindData().setPFParametersByPeakFinder(peakAlgorithm, peakParams);
		manager.getPeakFindData().setData(xData, yData);
		manager.getPeakFindData().setNPeaks(20);

		TreeMap<Integer, Double> peaksPos = new TreeMap<Integer, Double>();

		// Start the running man
		// Job runner = new RunningMan("Searching", xData, yData);
		// TODO: can i not manage a job async though?
		// Thread thread = new Thread(new Runnable() {
		// @Override
		// public void run() {
		// boolean running = true;
		// final List<Peak> peaks = new ArrayList<Peak>();
		// peaks.add(new Peak(0.0,0.0));
		// int stepSize = xData.getSize()/500;
		// while(running) {
		// xData.getSize();
		// for(int i = 0; i < xData.getSize(); i+= stepSize){
		// peaks.set(0, new Peak(xData.getDouble(i), yData.getDouble(i)));
		//
		//// try {
		//// Thread.sleep(100);
		//// } catch (InterruptedException e) {
		//// // TODO Auto-generated catch block
		//// e.printStackTrace();
		//// }
		// Display.getDefault().syncExec(new Runnable() {
		// @Override
		// public void run() {
		// controller.setPeaks(peaks);
		// }
		// });
		// }
		// }
		// }
		// });
		// thread.start();

		/* Perform Peak Search */
		try {
			manager.getPeakFindServ().findPeaks(manager.getPeakFindData());
		} catch (Exception e) {
			logger.debug("Finding peaks data resulted in error in peak service");
			// thread.stop();
			updatePeak(peaksPos);
			return Status.CANCEL_STATUS;
		}

		/* Extract Peak Search Data */
		peaksPos = (TreeMap<Integer, Double>) manager.getPeakFindData().getPeaks(peakAlgorithm);

		if (peaksPos.isEmpty()) {
			logger.debug("No peaks found with " + peakAlgorithm);
			// thread.stop();
			updatePeak(peaksPos);
			return Status.CANCEL_STATUS;
		}

		// TODO: tmp just wanted to see things play
		// thread.stop();
		/* Tmp place here, expand out later */
//		boolean limitNumber = false;
//		if (limitNumber) {
//			
//			//space peaks
//			TreeMap<Integer, Double> spacePos = new TreeMap<Integer, Double>();
//			
//			//Chpoose highest in range
//			int windowRange= 3;
//			int startPos = 0;
//			double rangeWidth =0.1;
//			
//			//Obnly bothered out x position
//			ArrayList<Integer> keys = new ArrayList<Integer>(peaksPos.keySet());
//			for (int idx = 0; idx < keys.size(); idx += windowRange){
//				int startKey = keys.get(idx);
//				
//				int endPos = idx + windowRange;
//				if(endPos >= peaksPos.size())
//					endPos = peaksPos.size()-1;
//				
//				boolean withinRange = true;
//
//				TreeMap<Integer, Double> interestKeyVal = new TreeMap<Integer, Double>();
//				interestKeyVal.put(startKey, peaksPos.get(startKey));
//				for (int aheadPos = idx+1; aheadPos < windowRange; ++aheadPos){
//					double diff = Math.abs(xData.getDouble(keys.get(idx)) -  xData.getDouble(keys.get(aheadPos)));
//					
//					//Looks like a lone wolf
//					if (diff > rangeWidth){
//						withinRange = false;
//						break;
//					}
//					
//					int interestKey = keys.get(aheadPos);
//					interestKeyVal.put(interestKey, peaksPos.get(interestKey));
//				}
//				
//				if (withinRange){
//					//Object theOneTheOnly = peaksPos.subMap(keys.get(idx), keys.get(endPos));
//					List<Entry<Integer, Double>> restrictedPos = findGreatest(interestKeyVal, 1);
//					for (Entry<Integer, Double> chosenOne : restrictedPos) {
//						spacePos.put(chosenOne.getKey(),chosenOne.getValue());
//					}
//					
//				} else {
//					//Lone wolf is a keeper
//					Integer key = keys.get(idx);
//					double val = peaksPos.get(key);
//					spacePos.put(key, val);
//					//Reset increment to just ahead of lone wolf
//					idx -= windowRange -1;
//				}
//			}
//			
//			List<Entry<Integer, Double>> restrictedPos = findGreatest(spacePos, 30);
//			peaksPos.clear();
//			
//			for (Entry<Integer, Double> chosenOne : restrictedPos) {
//				peaksPos.put(chosenOne.getKey(),chosenOne.getValue());
//			}
//		}

		updatePeak(peaksPos);

		return Status.OK_STATUS;
	}

	private void updatePeak(final Map<Integer, Double> peaksPos) {
		/* Send peaks update */
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {

				List<IdentifiedPeak> peaksId = manager.convertIntoPeaks(peaksPos, (Dataset) xData, (Dataset) yData);
				manager.setPeaks(peaksPos, xData, yData);

				manager.finishedPeakSearching();
			}
		});

	}

	// class RunningMan extends Job{
	//
	// IDataset xData;
	// IDataset yData;
	// public RunningMan(String name, IDataset xpos, IDataset ypos) {
	// super(name);
	// this.xData = xpos;
	// this.yData = ypos;
	// // TODO Auto-generated constructor stub
	// }
	//
	// @Override
	// protected IStatus run(IProgressMonitor monitor) {
	// boolean running = true;
	// final List<Peak> peaks = new ArrayList<Peak>();
	// peaks.add(new Peak(0.0,0.0));
	// while(running) {
	// xData.getSize();
	//
	// for(int i = 0; i < xData.getSize(); ++i){
	// peaks.set(0, new Peak(xData.getDouble(i), yData.getDouble(i)));
	// try {
	// Thread.sleep(100);
	// } catch (InterruptedException e) {
	// logger.debug("Runner collapses");
	// e.printStackTrace();
	// }
	//
	// Display.getDefault().syncExec(new Runnable() {
	// @Override
	// public void run() {
	// //manager.setPeaks(peaks);
	// }
	// });
	// }
	// }
	// return Status.OK_STATUS;
	// }
	//
	// }

	// http://stackoverflow.com/questions/21465821/how-to-get-5-highest-values-from-a-hashmap
	// Proioty queue method to pick fast
	private static <K, V extends Comparable<? super V>> List<Entry<K, V>> findGreatest(Map<K, V> map, int n) {
		Comparator<? super Entry<K, V>> comparator = new Comparator<Entry<K, V>>() {
			@Override
			public int compare(Entry<K, V> e0, Entry<K, V> e1) {
				V v0 = e0.getValue();
				V v1 = e1.getValue();
				return v0.compareTo(v1);
			}
		};
		PriorityQueue<Entry<K, V>> highest = new PriorityQueue<Entry<K, V>>(n, comparator);
		for (Entry<K, V> entry : map.entrySet()) {
			highest.offer(entry);
			while (highest.size() > n) {
				highest.poll();
			}
		}

		List<Entry<K, V>> result = new ArrayList<Map.Entry<K, V>>();
		while (highest.size() > 0) {
			result.add(highest.poll());
		}
		return result;
	}

}
