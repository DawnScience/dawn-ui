package org.dawnsci.surfacescatter.ui;

import java.util.concurrent.Callable;

import org.dawnsci.surfacescatter.BatchSavingAdvancedSettings;
import org.dawnsci.surfacescatter.BatchSetupMiscellaneousProperties;
import org.dawnsci.surfacescatter.FittingParametersInputReader;
import org.dawnsci.surfacescatter.MethodSettingEnum.MethodSetting;
import org.dawnsci.surfacescatter.SetupModel;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;

@SuppressWarnings("rawtypes")
public class BatchRunnable implements Callable {

	private String savePath;
	private BatchSavingAdvancedSettings[] bsas;
	private BatchSetupMiscellaneousProperties bsmps;
	private ProgressBar progress;
	private BatchTrackingProgressAndAbortViewImproved bpaatv;
	private Display display;
	private String imageFolderPath;
	private String paramFile;
	private String[] datFiles;
	private boolean useTrajectory;

	public BatchRunnable(String savepath1, BatchSavingAdvancedSettings[] bsas, BatchSetupMiscellaneousProperties bsmps,
			ProgressBar progress, BatchTrackingProgressAndAbortViewImproved bpaatv, Display display,
			String imageFolderPath, String paramFile, String[] datFiles, boolean useTrajectory) {

		this.savePath = savepath1;
		this.bsas = bsas;
		this.bsmps = bsmps;
		this.progress = progress;
		this.bpaatv = bpaatv;
		this.display = display;
		this.imageFolderPath = imageFolderPath;
		this.paramFile = paramFile;
		this.datFiles = datFiles;
		this.useTrajectory = useTrajectory;

	}

	public Boolean call() {

		SetupModel stmi = new SetupModel();
		stmi.setImageFolderPath(imageFolderPath);

		SurfaceScatterPresenter sspi = new SurfaceScatterPresenter();
		sspi.setStm(stmi);
		sspi.createGm();

		long startTime = System.nanoTime();

		NexusFile file = new NexusFileFactoryHDF5().newNexusFile(paramFile);

		try {
			file.close();
		} catch (NexusException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		FittingParametersInputReader.anglesAliasReaderFromNexus(file);

		long angleTime = System.nanoTime();

		System.out.println(" anglesAliasTime :   " + (angleTime - startTime) / 1000000);

		startTime = System.nanoTime();

		FittingParametersInputReader.geometricalParametersReaderFromNexus(file, sspi.getGm(), sspi.getDrm());

		long geometryTime = System.nanoTime();

		System.out.println(" geometryAliasTime :   " + (geometryTime - startTime) / 1000000);

		sspi.surfaceScatterPresenterBuildWithFrames(datFiles, sspi.getGm().getxName(),
				MethodSetting.toMethod(sspi.getGm().getExperimentMethod()));

		sspi.loadParameters(paramFile, useTrajectory);

		BatchTracking bat = new BatchTracking();
		bat.setSsp(sspi);

		startTime = System.nanoTime();

		bat.runTJ1(savePath, bsas, bsmps, imageFolderPath, paramFile, datFiles, useTrajectory);

		long batchTime = System.nanoTime();

		System.out.println(" batchTime :   " + (batchTime - startTime) / 1000000);

//		display.syncExec(new Runnable() {
//			@Override
//			public void run() {
//
//				if (progress.isDisposed() != true) {
//					progress.setSelection(progress.getSelection() + 1);
//
//					if (progress.getSelection() == progress.getMaximum()) {
//						bpaatv.close();
//					}
//
//				}
//				return;
//			}
//		});

		return true;

	}
}
