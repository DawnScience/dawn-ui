package org.dawnsci.surfacescatter.ui;

import org.dawnsci.surfacescatter.BatchSavingAdvancedSettings;
import org.dawnsci.surfacescatter.BatchSetupMiscellaneousProperties;

public class BatchRunnable implements Runnable {

	private BatchTracking bat;
	private int[][] lenpt;
	private String savePath;
	private BatchSavingAdvancedSettings[] bsas; 
	private BatchSetupMiscellaneousProperties bsmps;

	public BatchRunnable(BatchTracking bat1, int[][] lenpt1, String savepath1,  BatchSavingAdvancedSettings[] bsas , BatchSetupMiscellaneousProperties bsmps) {

		setBat(bat1);
		setLenpt(lenpt1);
		setSavePath(savepath1);
		setBsas(bsas);
		setBsmps(bsmps);
		
	}

	@Override
	public void run() {

		bat.runTJ1(lenpt, savePath, bsas, bsmps);
	}

	public void setBat(BatchTracking bat1) {
		this.bat = bat1;
	}

	public void setLenpt(int[][] lenpt) {
		this.lenpt = lenpt;
	}

	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}
	
	public void setBsas(BatchSavingAdvancedSettings[] bsas) {
		this.bsas= bsas;
	}

	public void setBsmps( BatchSetupMiscellaneousProperties bsmps) {
		this.bsmps = bsmps;
	}

}
