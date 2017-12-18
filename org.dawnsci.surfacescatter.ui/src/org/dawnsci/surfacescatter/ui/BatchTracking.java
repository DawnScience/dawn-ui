package org.dawnsci.surfacescatter.ui;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import org.dawnsci.surfacescatter.AxisEnums.yAxes;
import org.dawnsci.surfacescatter.AttenuationFactors;
import org.dawnsci.surfacescatter.BatchSavingAdvancedSettings;
import org.dawnsci.surfacescatter.BatchSetupMiscellaneousProperties;
import org.dawnsci.surfacescatter.BatchSetupYAxes;
import org.dawnsci.surfacescatter.LocationLenPtConverterUtils;
import org.dawnsci.surfacescatter.SavingFormatEnum.SaveFormatSetting;

public class BatchTracking {

	private SurfaceScatterPresenter ssp;
	boolean start = true;
	private String savePath;

	public BatchTracking(SurfaceScatterPresenter ssp) {
		this.ssp = ssp;
	}

	public void setSsp(SurfaceScatterPresenter ssp) {
		this.ssp = ssp;
	}

	protected void runTJ1(String savepath1, BatchSavingAdvancedSettings[] bsas, BatchSetupMiscellaneousProperties bsmps,
			int noRods, ReadWriteLock lock, PrintWriter writer, ArrayList<AttenuationFactors> afs) {

		this.savePath = savepath1;

		String progressReport = savePath + "   :  ";

		try {
			int[][] lenpt = LocationLenPtConverterUtils.locationToLenPtConverter(ssp.getFms().get(0).getRoiLocation());

			ssp.regionOfInterestSetter(lenpt);

			boolean[] doneArray = new boolean[ssp.getDrm().getDatFilepaths().length];

			ssp.getDrm().setDoneArray(doneArray);

			new TrackingCore(doneArray, ssp, null, null, false, null, null, bsmps.isDitchNegativeValues(), afs);

			progressReport += "   tracked successfully; ";
		}

		catch (Exception p) {
			progressReport += "   track failed : " + p.getMessage();
			lock.writeLock().lock();
			writer.println(progressReport);
			lock.writeLock().unlock();
			return;

		}

		try {
			if (bsmps.isOutputNexusFiles()) {
				ssp.writeNexus(savePath + ".nxs", noRods, lock);
			}

			progressReport += "   wrote NeXus successfully; ";

		} catch (Exception n) {
			progressReport += "   NeXus write out failed : " + n.getMessage();
			lock.writeLock().lock();
			writer.println(progressReport);
			lock.writeLock().unlock();
			return;

		}

		yAxes[] yA = goodYAxes(bsmps.getBsya());

		try {

			for (BatchSavingAdvancedSettings bsa : bsas) {
				if (bsa != null) {
					SaveFormatSetting sfs = bsa.getSfs();
					for (yAxes y : yA) {
						if (bsa.isAllPoints()) {
							ssp.arbitrarySavingMethodCore(bsmps.isUseQ(), false, sfs, ssp.getDrm().getCsdp(), y,
									savePath + "_" + sfs.getDisplayName() + "_" + "ALL_POINTS" + "_"
											+ y.getYAxisName());
						}
						if (bsa.isGoodPoints()) {
							ssp.arbitrarySavingMethodCore(bsmps.isUseQ(), true, sfs, ssp.getDrm().getCsdp(), y, savePath
									+ "_" + sfs.getDisplayName() + "_" + "GOOD_POINTS_ONLY" + "_" + y.getYAxisName());
						}
					}
				}
			}
			progressReport += "   reduced save files written successfully : ALL RAN FINE";

			lock.writeLock().lock();
			writer.println(progressReport);
			lock.writeLock().unlock();
			return;
		}

		catch (Exception j) {
			progressReport += "   reduced save files write failed : FAILURE  " + j.getMessage();

			lock.writeLock().lock();
			writer.println(progressReport);
			lock.writeLock().unlock();
			return;
		}

	}

	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}

	private yAxes[] goodYAxes(BatchSetupYAxes[] bsya) {

		ArrayList<yAxes> a = new ArrayList<>();

		for (BatchSetupYAxes b : bsya) {
			if (b.isUse()) {
				a.add(b.getY());
			}
		}

		return a.toArray(new yAxes[a.size()]);
	}
}
