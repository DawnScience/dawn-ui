package org.dawnsci.surfacescatter.ui;

public class NexusWriteOutRunnable implements Runnable {

	private SurfaceScatterPresenter ssp;
	private String savePath;

	public NexusWriteOutRunnable(SurfaceScatterPresenter ssp1, String savePath1) {
		setSsp(ssp1);
		setSavePath(savePath1);
	}

	@Override
	public void run() {

		ssp.writeNexus(savePath, 10);
	}

	public void setSsp(SurfaceScatterPresenter ssp1) {
		this.ssp = ssp1;
	}
	public void setSavePath(String savePath1) {
		this.savePath = savePath1;
	}
	
	
}
