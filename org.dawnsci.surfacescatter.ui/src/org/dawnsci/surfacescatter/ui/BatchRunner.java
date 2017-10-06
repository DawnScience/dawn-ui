package org.dawnsci.surfacescatter.ui;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.dawnsci.surfacescatter.BatchRodDataTransferObject;
import org.dawnsci.surfacescatter.BatchRodModel;
import org.dawnsci.surfacescatter.FittingParametersInputReader;
import org.dawnsci.surfacescatter.LocationLenPtConverterUtils;
import org.dawnsci.surfacescatter.SetupModel;
import org.dawnsci.surfacescatter.MethodSettingEnum.MethodSetting;

public class BatchRunner {

	public static void batchRun(BatchRodModel brm){
		
		String[][] datFiles = new String[brm.getBrdtoList().size()][];
		String[] imageFolderPaths = new String[brm.getBrdtoList().size()];
		String[] paramFiles = new String[brm.getBrdtoList().size()];
		String[] nexusSaveFilePaths = new String[brm.getBrdtoList().size()];
		boolean[] useTrajectories = new boolean[brm.getBrdtoList().size()];
		
		for(int i = 0; i<brm.getBrdtoList().size();i++){
			BatchRodDataTransferObject b = brm.getBrdtoList().get(i);
			datFiles[i] = b.getDatFiles();
			imageFolderPaths[i] = b.getImageFolderPath();
			paramFiles[i] = b.getParamFiles();
			
			String nexusName = brm.getNxsFolderPath() + File.separator + b.getRodName()+".nxs";
			nexusSaveFilePaths[i] = nexusName;
			
			useTrajectories[i]= b.isUseTrajectory();
		}
		
		batchRun(datFiles, imageFolderPaths, paramFiles, nexusSaveFilePaths, useTrajectories);
		
	}
	
	
	public static void batchRun(String[][] datFiles, String[] imageFolderPaths, String[] paramFiles, String[] nexusSaveFilePaths,
			boolean[] useTrajectories) {

//		SurfaceScatterPresenter[] sspArray = new SurfaceScatterPresenter[datFiles.length];

		int cores = Runtime.getRuntime().availableProcessors();


		ExecutorService executor = Executors.newFixedThreadPool(cores);
		for (int i = 0; i < datFiles.length; i++) {

			SetupModel stmi = new SetupModel();
			stmi.setImageFolderPath(imageFolderPaths[i]);

			SurfaceScatterPresenter sspi = new SurfaceScatterPresenter();
			sspi.setStm(stmi);
			sspi.createGm();

			FittingParametersInputReader.anglesAliasReaderFromNexus(paramFiles[i]);

			FittingParametersInputReader.geometricalParametersReaderFromNexus(paramFiles[i], sspi.getGm(), sspi.getDrm());

			sspi.surfaceScatterPresenterBuildWithFrames(datFiles[i], sspi.getGm().getxName(), MethodSetting.toMethod(sspi.getGm().getExperimentMethod()));

			sspi.loadParameters(paramFiles[i], useTrajectories[i]);

			BatchTracking bat = new BatchTracking();
			bat.setSsp(sspi);

//			sspArray[i] = sspi;

			int[][] lenpt = LocationLenPtConverterUtils.locationToLenPtConverter(sspi.getFms().get(0).getRoiLocation());

			BatchRunnable mr = new BatchRunnable(bat, lenpt,nexusSaveFilePaths[i], useTrajectories[i]);

			executor.execute(mr);

		}
		executor.shutdown();
		
		while (!executor.isTerminated()) {
		}
		
	}

}