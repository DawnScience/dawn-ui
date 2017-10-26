package org.dawnsci.surfacescatter.ui;

import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;

import org.dawnsci.surfacescatter.AxisEnums.yAxes;
import org.dawnsci.surfacescatter.BatchSavingAdvancedSettings;
import org.dawnsci.surfacescatter.BatchSetupMiscellaneousProperties;
import org.dawnsci.surfacescatter.BatchSetupYAxes;
import org.dawnsci.surfacescatter.LocationLenPtConverterUtils;
import org.dawnsci.surfacescatter.SavingFormatEnum.SaveFormatSetting;

public class BatchTracking {

	private SurfaceScatterPresenter ssp;
	boolean start = true;
	private String savePath;

	public void setSsp(SurfaceScatterPresenter ssp) {
		this.ssp = ssp;
	}

	protected void runTJ1(String savepath1, BatchSavingAdvancedSettings[] bsas, 
			BatchSetupMiscellaneousProperties bsmps,
		  int noRods, ReadWriteLock lock) {

		int[][] lenpt = LocationLenPtConverterUtils.locationToLenPtConverter(ssp.getFms().get(0).getRoiLocation());

		this.savePath = savepath1;

		ssp.regionOfInterestSetter(lenpt);

		ssp.regionOfInterestSetter(lenpt);

		boolean[] doneArray = new boolean[ssp.getDrm().getDatFilepaths().length];

		ssp.getDrm().setDoneArray(doneArray);

		long startTime = System.nanoTime();

		new TrackingCore(doneArray, ssp, null, null, false, null, null);

		long trackingCoreTime = System.nanoTime();

		System.out.println(" TrackingCoreTime :   " + (trackingCoreTime - startTime) / 1000000);

		startTime = System.nanoTime();
		
		if(bsmps.isOutputNexusFiles()) {
			ssp.writeNexus(savePath + ".nxs", noRods , lock);
		}
		
		long nexusTime = System.nanoTime();

		System.out.println(" nexusTime :   " + (nexusTime - startTime) / 1000000);

		yAxes[] yA = goodYAxes(bsmps.getBsya());

		for (BatchSavingAdvancedSettings bsa : bsas) {
			if (bsa != null) {
				SaveFormatSetting sfs = bsa.getSfs();
				for (yAxes y : yA) {
					if (bsa.isAllPoints()) {
						ssp.arbitrarySavingMethodCore(bsmps.isUseQ(), false, sfs, ssp.getDrm().getCsdp(), y,
								savePath + "_" + sfs.getDisplayName() + "_" + "ALL_POINTS" + "_" + y.getYAxisName());
					}
					if (bsa.isGoodPoints()) {
						ssp.arbitrarySavingMethodCore(bsmps.isUseQ(), true, sfs, ssp.getDrm().getCsdp(), y, savePath
								+ "_" + sfs.getDisplayName() + "_" + "GOOD_POINTS_ONLY" + "_" + y.getYAxisName());
					}
				}
			}
		}

		return;

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
