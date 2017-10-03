package org.dawnsci.surfacescatter.ui;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.dawnsci.surfacescatter.FittingParametersInputReader;
import org.dawnsci.surfacescatter.LocationLenPtConverterUtils;
import org.dawnsci.surfacescatter.SetupModel;
import org.dawnsci.surfacescatter.MethodSettingEnum.MethodSetting;

public class BatchRunner {

	public static void batchRun(String[][] datFiles, String[] imageFolderPaths,
			MethodSetting[] correctionSelections, String[] paramFiles, String[] nexusSaveFilePaths) {

		SurfaceScatterPresenter[] sspArray = new SurfaceScatterPresenter[datFiles.length];

		int cores = Runtime.getRuntime().availableProcessors();


		ExecutorService executor = Executors.newFixedThreadPool(cores);
		for (int i = 0; i < datFiles.length; i++) {

			SetupModel stmi = new SetupModel();
			stmi.setImageFolderPath(imageFolderPaths[i]);

			SurfaceScatterPresenter sspi = new SurfaceScatterPresenter();
			sspi.setStm(stmi);
			sspi.createGm();

			FittingParametersInputReader.anglesAliasReaderFromNexus(paramFiles[i]);

			FittingParametersInputReader.geometricalParametersReaderFromNexus(paramFiles[i], sspi.getGm());

			sspi.surfaceScatterPresenterBuildWithFrames(datFiles[i], sspi.getGm().getxName(), MethodSetting.toMethod(sspi.getGm().getExperimentMethod()));

			sspi.loadParameters(paramFiles[i]);

			BatchTracking bat = new BatchTracking();
			bat.setSsp(sspi);

			sspArray[i] = sspi;

			int[][] lenpt = LocationLenPtConverterUtils.locationToLenPtConverter(sspi.getFms().get(0).getRoiLocation());

			BatchRunnable mr = new BatchRunnable(bat, lenpt,nexusSaveFilePaths[i] );

			executor.execute(mr);

		}
		executor.shutdown();
		
		while (!executor.isTerminated()) {
		}
		
	}

}