package org.dawnsci.surfacescatter.ui;

public class BatchTracking {

	private SurfaceScatterPresenter ssp;
	boolean start = true;
	private String savePath;

	public void setSsp(SurfaceScatterPresenter ssp) {
		this.ssp = ssp;
	}

	protected void runTJ1(int[][] lenPt, String savepath1) {
		this.savePath = savepath1;

		ssp.regionOfInterestSetter(lenPt);

		ssp.regionOfInterestSetter(lenPt);
		
		boolean[] doneArray = new boolean[ssp.getDrm().getDatFilepaths().length];

		ssp.getDrm().setDoneArray(doneArray);

		new TrackingCore(doneArray, ssp, null, null, false, null,
				null);
		
		ssp.writeNexus(savePath);

		return;

	}

	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}

}
