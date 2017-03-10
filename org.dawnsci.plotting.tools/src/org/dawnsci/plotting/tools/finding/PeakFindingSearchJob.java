package org.dawnsci.plotting.tools.finding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.preference.PeakFindingConstants;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.peakfinding.IPeakFinderParameter;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.PointStyle;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.TraceType;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.peakfinding.IPeakFindingService;
import uk.ac.diamond.scisoft.analysis.peakfinding.Peak;

/**
 * TODO: have job update trace to look like things are going on
 * TODO: finally call formatPeakSearch
 * 
 * @author Dean P. Ottewell
 */
public class PeakFindingSearchJob extends Job {

		protected final Logger logger = LoggerFactory.getLogger(PeakFindingSearchJob.class);

		PeakFindingManager controller;

		IPeakFindingService peakFindServ = (IPeakFindingService)Activator.getService(IPeakFindingService.class);

		IDataset xData;
		IDataset yData;
		
		String peakAlgorithm;
		Map<String, IPeakFinderParameter> peakParams;

		public PeakFindingSearchJob(PeakFindingManager controller,IDataset xData, IDataset yData) {
			super("Peak Search");
			this.controller = controller;
			this.xData = xData;
			this.yData = yData;
			setPriority(Job.INTERACTIVE);
		}

		//Some sort of data load into the job and then run. Do not want things to be changeing whilst running
//		public void loadData(IPeakOppurtunity evt){
//				//TODO: Do not realyl need to grab event peaks as we have them here but maybe better off here putting them in a event structure rather than passing this list object around...
//		controller.addPeaks(peaks);
//		}
		
	

//TODO: should be done previously		
//		if (controller.getPeakfindingtool().sampleTrace == null){
//			return Status.CANCEL_STATUS;
//		}
//		if (controller.getLowerBnd() == null || controller.getUpperBnd() == null) {
//			RectangularROI rectangle = (RectangularROI) controller.getPeakfindingtool().searchRegion.getROI();
//			// // Set the region bounds
//			controller.getPeakfindingtool().updateSearchBnds(rectangle);
//		}

		
		private void loadPeakFinderParams(){
			//TODO: clean control function
			// Free up active peakfinder calls
			if (controller.getPeakFindData().hasActivePeakFinders()) {
				Collection<String> actives = controller.getPeakFindData().getActivePeakFinders();
				for (String active : actives) {
					controller.getPeakFindData().deactivatePeakFinder(active);
				}
			}
			
			peakAlgorithm= Activator.getPlottingPreferenceStore().getString(PeakFindingConstants.PeakAlgorithm);
			controller.getPeakFindData().activatePeakFinder(peakAlgorithm);
			//Configure peak finder on preference store go through all the params that match
			peakParams = peakFindServ.getPeakFinderParameters(peakAlgorithm);
			for (Entry<String, IPeakFinderParameter> peakParam : peakParams.entrySet()){
				IPeakFinderParameter param = peakParam.getValue();
				String curVal = Activator.getPlottingPreferenceStore().getString(peakParam.getKey());
				Number val = Double.parseDouble(curVal);
				if (param.isInt())
					val = (int) val.doubleValue();
				param.setValue(val);
				//TODO: allow single param pass
				controller.getPeakFindData().setPFParameterByName(peakAlgorithm, param.getName(), param.getValue());
			}
			controller.getPeakFindData().setPFParametersByPeakFinder(peakAlgorithm, peakParams);
		}
		
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			//TODO: clean control function
			// Free up active peakfinder calls
			loadPeakFinderParams();
			
			controller.getPeakFindData().setPFParametersByPeakFinder(peakAlgorithm, peakParams);
			controller.getPeakFindData().setData(xData, yData);
			controller.getPeakFindData().setNPeaks(20);
			
			final List<Peak> peaks = new ArrayList<Peak>();
			
			//Start the running man	
			Job runner = new RunningMan("Searching", xData, yData);
			
			//TODO: can i not manage a job async though?
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					boolean running = true;
					final List<Peak> peaks = new ArrayList<Peak>();
					peaks.add(new Peak(0.0,0.0));
					int stepSize = xData.getSize()/500;
					while(running) {
						xData.getSize();
						for(int i = 0; i < xData.getSize(); i+= stepSize){
							peaks.set(0, new Peak(xData.getDouble(i), yData.getDouble(i)));
							
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							Display.getDefault().syncExec(new Runnable() {
								@Override
								public void run() {
									controller.setPeaks(peaks);
								}
							});
						}
					}
				}
			});
			thread.start();
			
			/*Perform Peak Search*/
			try {
				controller.getPeakFindServ().findPeaks(controller.getPeakFindData());
			} catch (Exception e) {
				logger.debug("Finding peaks data resulted in error in peak service");
				thread.stop();
				updatePeak(peaks);
				return Status.CANCEL_STATUS;
			}
	
			/*Extract Peak Search Data */
			TreeMap<Integer, Double> peaksPos = (TreeMap<Integer, Double>) controller.getPeakFindData().getPeaks(peakAlgorithm);

			if(peaksPos.isEmpty()){
				logger.debug("No peaks found with " + peakAlgorithm);
				thread.stop();
				updatePeak(peaks);
				return Status.CANCEL_STATUS;
			}
			
			/*Format peaks*/
			List<Double> pPos = new ArrayList<Double>(peaksPos.values());
			List<Integer> pHeight = new ArrayList<Integer>(peaksPos.keySet());
			
			IDataset peaksY= DatasetFactory.createFromList(pPos);
			IDataset peaksX = ((Dataset) xData).getBy1DIndex((IntegerDataset) DatasetFactory.createFromList(pHeight));
			
			
			// Create peaks
			for (int i = 0; i < peaksY.getSize(); ++i) {
				Peak p = new Peak(peaksX.getDouble(i), peaksY.getDouble(i));
				p.setName("P" + i);
				peaks.add(p);
			}
			
			//TODO: tmp just wanted to see things play
			thread.stop();
			
			updatePeak(peaks);
			
			
			
			return Status.OK_STATUS;
		}
		
		private void updatePeak(final List<Peak> peaks){
			/*Send peaks update*/
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					controller.setPeaks(peaks);
				}
			});
	
		}
		
		class RunningMan extends Job{

			IDataset xData;
			IDataset yData;
			public RunningMan(String name, IDataset xpos, IDataset ypos) {
				super(name);
				this.xData = xpos;
				this.yData = ypos;
				// TODO Auto-generated constructor stub
			}

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				boolean running = true;
				final List<Peak> peaks = new ArrayList<Peak>();
				peaks.add(new Peak(0.0,0.0));
				while(running) {
					xData.getSize();
					
					for(int i = 0; i < xData.getSize(); ++i){
						peaks.set(0, new Peak(xData.getDouble(i), yData.getDouble(i)));
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							logger.debug("Runner collapses");
							e.printStackTrace();
						}
						
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								controller.setPeaks(peaks);
							}
						});
					}
				}
				return Status.OK_STATUS;
			}
			
		}
			
}

