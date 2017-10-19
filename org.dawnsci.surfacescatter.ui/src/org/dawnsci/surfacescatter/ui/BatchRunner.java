package org.dawnsci.surfacescatter.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.dawnsci.surfacescatter.BatchRodDataTransferObject;
import org.dawnsci.surfacescatter.BatchRodModel;
import org.dawnsci.surfacescatter.BatchSavingAdvancedSettings;
import org.dawnsci.surfacescatter.BatchSetupMiscellaneousProperties;
import org.dawnsci.surfacescatter.FittingParametersInputReader;
import org.dawnsci.surfacescatter.LocationLenPtConverterUtils;
import org.dawnsci.surfacescatter.SetupModel;
import org.dawnsci.surfacescatter.MethodSettingEnum.MethodSetting;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;

public class BatchRunner {

	private ExecutorService executor;

	
	public BatchRunner(BatchRodModel brm, ProgressBar progress,  BatchTrackingProgressAndAbortViewImproved bpaatv, Display display){
				
		String[][] datFiles = new String[brm.getBrdtoList().size()][];
		String[] imageFolderPaths = new String[brm.getBrdtoList().size()];
		String[] paramFiles = new String[brm.getBrdtoList().size()];
		String[] nexusSaveFilePaths = new String[brm.getBrdtoList().size()];
		String[] baseSaveFilePaths = new String[brm.getBrdtoList().size()];
		boolean[] useTrajectories = new boolean[brm.getBrdtoList().size()];
		BatchSavingAdvancedSettings[] bsas = brm.getBsas();
		BatchSetupMiscellaneousProperties bsmps = brm.getBsmps();
		
		for(int i = 0; i<brm.getBrdtoList().size();i++){
			BatchRodDataTransferObject b = brm.getBrdtoList().get(i);
			datFiles[i] = b.getDatFiles();
			imageFolderPaths[i] = b.getImageFolderPath();
			paramFiles[i] = b.getParamFiles();
			String baseName = brm.getNxsFolderPath() + File.separator + b.getRodName();
			String nexusName = brm.getNxsFolderPath() + File.separator + b.getRodName()+".nxs";
			nexusSaveFilePaths[i] = nexusName;
			baseSaveFilePaths[i] = baseName;
			useTrajectories[i]= b.isUseTrajectory();
		}
		
		batchRun(datFiles, imageFolderPaths, paramFiles, baseSaveFilePaths, useTrajectories, bsas, bsmps, progress, bpaatv, display);
		
	}
	
	
	@SuppressWarnings("rawtypes")
	public void batchRun(String[][] datFiles, String[] imageFolderPaths, String[] paramFiles, String[] nexusSaveFilePaths,
			boolean[] useTrajectories, BatchSavingAdvancedSettings[] bsas, BatchSetupMiscellaneousProperties bsmps, ProgressBar progress,
			 BatchTrackingProgressAndAbortViewImproved bpaatv, Display display) {

		int cores = Runtime.getRuntime().availableProcessors();

		BatchRunnable[] brs =  new BatchRunnable[datFiles.length]; 

		List<Future<Boolean>> batch =  new ArrayList<>();
		
		executor = Executors.newFixedThreadPool(cores-1);
		
		for (int i = 0; i < datFiles.length; i++) {

//			SetupModel stmi = new SetupModel();
//			stmi.setImageFolderPath(imageFolderPaths[i]);
//
//			SurfaceScatterPresenter sspi = new SurfaceScatterPresenter();
//			sspi.setStm(stmi);
//			sspi.createGm();
//
//			FittingParametersInputReader.anglesAliasReaderFromNexus(paramFiles[i]);
//
//			FittingParametersInputReader.geometricalParametersReaderFromNexus(paramFiles[i], sspi.getGm(), sspi.getDrm());
//
//			sspi.surfaceScatterPresenterBuildWithFrames(datFiles[i], sspi.getGm().getxName(), MethodSetting.toMethod(sspi.getGm().getExperimentMethod()));
//
//			sspi.loadParameters(paramFiles[i], useTrajectories[i]);
//
//			BatchTracking bat = new BatchTracking();
//			bat.setSsp(sspi);
//
//			int[][] lenpt = LocationLenPtConverterUtils.locationToLenPtConverter(sspi.getFms().get(0).getRoiLocation());

			brs[i]= new BatchRunnable(
					nexusSaveFilePaths[i], 
					bsas, bsmps, progress, bpaatv, display, imageFolderPaths[i], paramFiles[i],
					datFiles[i], useTrajectories[i]);
			
			Callable<Boolean> cb = brs[i];
			Future<Boolean> fb = executor.submit(cb);
			batch.add(fb);
			
			
		}
		
		executor.shutdown();
		
		while (!executor.isShutdown()) {
		}
		
	}
	
	public ExecutorService getExecutor() {
		
		return executor;
	}
	
}