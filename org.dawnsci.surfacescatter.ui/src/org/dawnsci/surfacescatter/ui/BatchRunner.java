package org.dawnsci.surfacescatter.ui;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.dawnsci.surfacescatter.BatchRodDataTransferObject;
import org.dawnsci.surfacescatter.BatchRodModel;
import org.dawnsci.surfacescatter.BatchSavingAdvancedSettings;
import org.dawnsci.surfacescatter.BatchSetupMiscellaneousProperties;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;

public class BatchRunner {

	private ExecutorService executor;
	private List<Future<Boolean>> batch;
	private PrintWriter writer;


	public BatchRunner(BatchRodModel brm, ProgressBar progress, BatchTrackingProgressAndAbortViewImproved bpaatv,
			Display display, PrintWriter writer) {

		
		this.writer = writer;
		String[][] datFiles = new String[brm.getBrdtoList().size()][];
		String[] imageFolderPaths = new String[brm.getBrdtoList().size()];
		String[] paramFiles = new String[brm.getBrdtoList().size()];
		String[] nexusSaveFilePaths = new String[brm.getBrdtoList().size()];
		String[] baseSaveFilePaths = new String[brm.getBrdtoList().size()];
		boolean[] useTrajectories = new boolean[brm.getBrdtoList().size()];
		boolean[] useStareModes = new boolean[brm.getBrdtoList().size()];
		BatchSavingAdvancedSettings[] bsas = brm.getBsas();
		BatchSetupMiscellaneousProperties bsmps = brm.getBsmps();
	
		for (int i = 0; i < brm.getBrdtoList().size(); i++) {
			BatchRodDataTransferObject b = brm.getBrdtoList().get(i);
			datFiles[i] = b.getDatFiles();
			imageFolderPaths[i] = b.getImageFolderPath();
			paramFiles[i] = b.getParamFiles();
			String baseName = brm.getNxsFolderPath() + File.separator+ brm.getBatchTitle()  + File.separator + b.getRodName();
			String nexusName = brm.getNxsFolderPath() + File.separator + brm.getBatchTitle()  + File.separator + b.getRodName() + ".nxs";
			nexusSaveFilePaths[i] = nexusName;
			baseSaveFilePaths[i] = baseName;
			useTrajectories[i] = b.isUseTrajectory();
			useStareModes[i] = b.isUseStareMode();
		}
		
		
		batchRun(datFiles, imageFolderPaths, paramFiles, baseSaveFilePaths, useTrajectories, useStareModes, bsas, bsmps,
				progress, bpaatv, display);

	}

	public void batchRun(String[][] datFiles, String[] imageFolderPaths, String[] paramFiles,
			String[] nexusSaveFilePaths, boolean[] useTrajectories, boolean[] useStareModes,
			BatchSavingAdvancedSettings[] bsas, BatchSetupMiscellaneousProperties bsmps, ProgressBar progress,
			BatchTrackingProgressAndAbortViewImproved bpaatv, Display display) {

		int cores = Runtime.getRuntime().availableProcessors();

		BatchRunnable[] brs = new BatchRunnable[datFiles.length];

		batch = new ArrayList<>();

		ReadWriteLock lock = new ReentrantReadWriteLock();

		executor = Executors.newFixedThreadPool(cores - 1);

		for (int i = 0; i < datFiles.length; i++) {

			brs[i] = new BatchRunnable(nexusSaveFilePaths[i], bsas, bsmps, progress, bpaatv, display,
					imageFolderPaths[i], paramFiles[i], datFiles[i], useTrajectories[i], useStareModes[i],
					datFiles.length, lock, writer);

			@SuppressWarnings("unchecked")
			Callable<Boolean> cb = brs[i];
			Future<Boolean> fb = executor.submit(cb);
			batch.add(fb);

		}

		executor.shutdown();


	}

	public List<Future<Boolean>> getBatch() {
		return batch;
	}

	public ExecutorService getExecutor() {

		return executor;
	}

}