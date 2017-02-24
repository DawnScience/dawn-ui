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
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.january.dataset.BooleanDataset;
import org.eclipse.january.dataset.Comparisons;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.fitting.functions.APeak;
import uk.ac.diamond.scisoft.analysis.peakfinding.IPeakFindingService;
import uk.ac.diamond.scisoft.analysis.peakfinding.Peak;

/**
 * 
 * TODO: remove activator dependecy here and just get through controller
 * TODO: finally call formatPeakSearch
 * 
 * @author Dean P. Ottewell
 *
 */
public class PeakSearchJob extends Job {

		protected final Logger logger = LoggerFactory.getLogger(PeakSearchJob.class);

		PeakFindingController controller;

		IPeakFindingService peakFindServ = (IPeakFindingService)Activator.getService(IPeakFindingService.class);
		
		
		//TODO: job depends too much on controller.
		public PeakSearchJob(PeakFindingController controller) {
			super("Peak Search");
			this.controller = controller;
			setPriority(Job.INTERACTIVE);
		}
		

		@Override
		protected IStatus run(IProgressMonitor monitor) {
	
			if (controller.peakfindingtool.sampleTrace == null){
				syncFormatPeakSearch();
				return Status.CANCEL_STATUS;
			}
			if (controller.getLowerBnd() == null || controller.getUpperBnd() == null) {
				RectangularROI rectangle = (RectangularROI) controller.peakfindingtool.searchRegion.getROI();
				// // Set the region bounds
				controller.peakfindingtool.updateSearchBnds(rectangle);
			}
	
			/* Clean up last peak search */
			controller.clearPeaks();
			
			//TODO: clean control function
			// Free up active peakfinder calls
			if (controller.getPeakFindData().hasActivePeakFinders()) {
				Collection<String> actives = controller.getPeakFindData().getActivePeakFinders();
				for (String active : actives) {
					controller.getPeakFindData().deactivatePeakFinder(active);
				}
			}
			
			String peakAlgorithm= Activator.getPlottingPreferenceStore().getString(PeakFindingConstants.PeakAlgorithm);
			controller.getPeakFindData().activatePeakFinder(peakAlgorithm); //controller.getPeakFinderID());
						
			/*Configure peak finder on preference store
			 *
			 * go through all the params that match
			 */
	
			Map<String, IPeakFinderParameter> peakParams = peakFindServ.getPeakFinderParameters(peakAlgorithm);
			for (Entry<String, IPeakFinderParameter> peakParam : peakParams.entrySet()){
				IPeakFinderParameter param = peakParam.getValue();
				String curVal = Activator.getPlottingPreferenceStore().getString(peakParam.getKey());
				Number val = Double.parseDouble(curVal);
				if (param.isInt())
					val = (int) val.doubleValue();
				param.setValue(val);
				//TODO: allow signle param pass
				controller.getPeakFindData().setPFParameterByName(peakAlgorithm, param.getName(), param.getValue());
			}
			
			controller.getPeakFindData().setPFParametersByPeakFinder(peakAlgorithm, peakParams);
			
			
			
			
			

			// Obtain Upper and Lower Bounds
			Dataset xData = DatasetUtils.convertToDataset(controller.peakfindingtool.sampleTrace.getXData().squeeze());
			Dataset yData = DatasetUtils.convertToDataset(controller.peakfindingtool.sampleTrace.getYData().squeeze());

			BooleanDataset allowed = Comparisons.withinRange(xData, controller.getLowerBnd(), controller.getUpperBnd());
			xData = xData.getByBoolean(allowed);
			yData = yData.getByBoolean(allowed);

			controller.getPeakFindData().setData(xData, yData);
			controller.getPeakFindData().setNPeaks(20);

			
			/*Do Peak Search*/
			
			try {
				controller.getPeakFindServ().findPeaks(controller.getPeakFindData());
			} catch (Exception e) {
				logger.debug("Finding peaks data resulted in error in peak service");
				syncFormatPeakSearch();
				return Status.CANCEL_STATUS;
			}

			
			/*Extract Peak Data */
			//Should just call a controller function for this
			TreeMap<Integer, Double> peaksPos = (TreeMap<Integer, Double>) controller.getPeakFindData().getPeaks(peakAlgorithm);

			if(peaksPos.isEmpty()){
				logger.debug("No peaks found with " + peakAlgorithm);
				syncFormatPeakSearch();
				return Status.CANCEL_STATUS;
			}
			
			List<Double> pPos = new ArrayList<Double>(peaksPos.values());
			List<Integer> pHeight = new ArrayList<Integer>(peaksPos.keySet());

			controller.peaksY = DatasetFactory.createFromList(pPos);
			controller.peaksX = xData.getBy1DIndex((IntegerDataset) DatasetFactory.createFromList(pHeight));

			
			
			APeak peak;
			
			// Create peaks
			for (int i = 0; i < controller.peaksY.getSize(); ++i) {
				Peak p = new Peak(controller.peaksX.getDouble(i), controller.peaksY.getDouble(i));
				p.setName("P" + i);
				controller.peaks.add(p);
			}
			
			
			
			
			
			
			
			

			syncFormatPeakSearch();

			return Status.OK_STATUS;
		}
		
		void syncFormatPeakSearch(){
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					controller.formatPeakSearch();
				}
			});
		}
		
}

