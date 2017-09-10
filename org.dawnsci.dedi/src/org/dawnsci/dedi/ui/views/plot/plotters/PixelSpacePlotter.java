package org.dawnsci.dedi.ui.views.plot.plotters;

import org.dawnsci.dedi.ui.views.plot.DefaultBeamlineConfigurationPlot;

public class PixelSpacePlotter extends AbstractBeamlineConfigurationPlotter {
	public PixelSpacePlotter(DefaultBeamlineConfigurationPlot context) {
		super(context);
	}
	
	
	@Override
	protected double getDetectorTopLeftX() {
		return 0; 
	}
	
	
	@Override
	protected double getDetectorTopLeftY() {
		return 0; 
	}
	
	
	@Override
	protected double getHorizontalLengthFromMM(double lengthMM) {
		return lengthMM/beamlineConfiguration.getDetector().getXPixelMM();
	}


	@Override
	protected double getHorizontalLengthFromPixels(double lengthPixels) {
		return lengthPixels;
	}


	@Override
	protected double getVerticalLengthFromMM(double lengthMM) {
		return lengthMM/beamlineConfiguration.getDetector().getYPixelMM();
	}


	@Override
	protected double getVerticalLengthFromPixels(double lengthPixels) {
		return lengthPixels;
	}
}
