package org.dawnsci.surfacescatter.ui;

import java.util.ArrayList;
import org.dawnsci.surfacescatter.DirectoryModel;
import org.dawnsci.surfacescatter.FrameModel;
import org.dawnsci.surfacescatter.GeometricParametersModel;

public class BatchTracking {

	private GeometricParametersModel gm;
	private SurfaceScatterPresenter ssp;
	boolean start = true;
	private ArrayList<FrameModel> fms;
	private DirectoryModel drm;
	private String savePath;

	public void setGm(GeometricParametersModel gms) {
		this.gm = gms;
	}

	public void setSsp(SurfaceScatterPresenter ssp) {
		this.ssp = ssp;
	}

	protected void runTJ1(int[][] lenPt, String savepath1) {

		this.gm = ssp.getGm();
		this.fms = ssp.getFms();
		this.drm = ssp.getDrm();
		this.savePath = savepath1;

		ssp.regionOfInterestSetter(lenPt);

		ssp.regionOfInterestSetter(lenPt);

		boolean[] doneArray = new boolean[drm.getDatFilepaths().length];

		drm.setDoneArray(doneArray);

		new TrackingCore(doneArray, drm, fms, ssp, gm, null, null, false, null,
				null);
		
		ssp.writeNexus(savePath);

		return;

	}

	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}

}
