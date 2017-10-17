package org.dawnsci.surfacescatter.ui;

import java.util.ArrayList;

import org.dawnsci.surfacescatter.AxisEnums;
import org.dawnsci.surfacescatter.AxisEnums.yAxes;
import org.dawnsci.surfacescatter.BatchSavingAdvancedSettings;
import org.dawnsci.surfacescatter.BatchSetupMiscellaneousProperties;
import org.dawnsci.surfacescatter.BatchSetupYAxes;
import org.dawnsci.surfacescatter.CurveStitchDataPackage;
import org.dawnsci.surfacescatter.SavingFormatEnum.SaveFormatSetting;
import org.eclipse.swt.widgets.Shell;

public class BatchTracking {

	private SurfaceScatterPresenter ssp;
	boolean start = true;
	private String savePath;

	public void setSsp(SurfaceScatterPresenter ssp) {
		this.ssp = ssp;
	}

	protected void runTJ1(int[][] lenPt, String savepath1, BatchSavingAdvancedSettings[] bsas , BatchSetupMiscellaneousProperties bsmps) {
		this.savePath = savepath1;

		ssp.regionOfInterestSetter(lenPt);

		ssp.regionOfInterestSetter(lenPt);

		boolean[] doneArray = new boolean[ssp.getDrm().getDatFilepaths().length];

		ssp.getDrm().setDoneArray(doneArray);

		new TrackingCore(doneArray, ssp, null, null, false, null, null);

		ssp.writeNexus(savePath);
		
		yAxes[] yA = goodYAxes(bsmps.getBsya());
		
		for (BatchSavingAdvancedSettings bsa : bsas) {
			SaveFormatSetting sfs = bsa.getSfs();
			for(yAxes y : yA) {
				if (bsa.isAllPoints()) {
					ssp.arbitrarySavingMethodCore(bsmps.isUseQ(), false, sfs, ssp.getDrm().getCsdp(), y, savePath);
				}
				if (bsa.isGoodPoints()) {
					ssp.arbitrarySavingMethodCore(bsmps.isUseQ(), true, sfs, ssp.getDrm().getCsdp(), y, savePath);
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
		
		for(BatchSetupYAxes b : bsya) {
			if (b.isUse()) {
				a.add(b.getY());
			}
		}
		
		return  a.toArray(new yAxes[a.size()]);
	}
}
