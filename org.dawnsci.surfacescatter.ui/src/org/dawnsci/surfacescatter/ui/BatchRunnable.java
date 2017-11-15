package org.dawnsci.surfacescatter.ui;

import java.io.PrintWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReadWriteLock;
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
	private boolean useStareMode;
	private int noRods;
	private ReadWriteLock lock;
	private PrintWriter writer;

	public BatchRunnable(String savepath1, BatchSavingAdvancedSettings[] bsas, BatchSetupMiscellaneousProperties bsmps,
			ProgressBar progress, BatchTrackingProgressAndAbortViewImproved bpaatv, Display display,
			String imageFolderPath, String paramFile, String[] datFiles, boolean useTrajectory, boolean useStareMode,
			int noRods, ReadWriteLock lock, PrintWriter writer) {

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
		this.useStareMode = useStareMode;
		this.lock = lock;
		this.noRods = noRods;
		this.writer = writer;

	}

	public Boolean call() {

		SetupModel stmi = new SetupModel();
		stmi.setImageFolderPath(imageFolderPath);

		SurfaceScatterPresenter sspi = new SurfaceScatterPresenter();
		sspi.setStm(stmi);
		sspi.createGm();

		NexusFile file = new NexusFileFactoryHDF5().newNexusFile(paramFile);

		try {
			file.close();
		} catch (NexusException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		FittingParametersInputReader.anglesAliasReaderFromNexus(file);

		FittingParametersInputReader.geometricalParametersReaderFromNexus(file, sspi.getGm(), sspi.getDrm());

		sspi.surfaceScatterPresenterBuildWithFrames(datFiles, sspi.getGm().getxName(),
				MethodSetting.toMethod(sspi.getGm().getExperimentMethod()));

		sspi.loadParameters(paramFile, useTrajectory, useStareMode);

		BatchTracking bat = new BatchTracking(sspi);
	
		bat.runTJ1(savePath, bsas, bsmps, noRods, lock, writer);

		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				
				if (!progress.isDisposed()) {
					progress.setSelection(progress.getSelection() + 1);

					if (progress.getSelection() == progress.getMaximum()) {
						bpaatv.close();
					}

				}
				return;
			}
		});

		
		return true;

	}
}
