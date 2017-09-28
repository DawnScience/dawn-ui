package org.dawnsci.surfacescatter.ui;

public class BatchRunnable implements Runnable {

	private BatchTracking bat;
	private int[][] lenpt;
	private String savePath;

	public BatchRunnable(BatchTracking bat1, int[][] lenpt1, String savepath1) {

		setBat(bat1);
		setLenpt(lenpt1);
		setSavePath(savepath1);
	}

	@Override
	public void run() {

		bat.runTJ1(lenpt, savePath);
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

}
