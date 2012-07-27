package org.dawb.workbench.plotting.system.swtxy;

import org.dawb.common.services.ImageServiceBean.ImageOrigin;
import org.dawb.common.ui.plot.axis.IAxis;
import org.dawb.common.ui.plot.axis.ICoordinateSystem;
import org.dawb.common.ui.plot.axis.ICoordinateSystemListener;

public class RegionCoordinateSystem implements ICoordinateSystem {

	private ImageTrace imageTrace;
	private IAxis i,j;

	public RegionCoordinateSystem(ImageTrace imageTrace, IAxis i, IAxis j) {
		this.imageTrace = imageTrace;
		this.i          = i;
		this.j          = j;
	}

	@Override
	public int getValuePosition(double value) {
		return getApparentAxis().getValuePosition(value);
	}

	@Override
	public double getPositionValue(int position) {
		return getApparentAxis().getPositionValue(position);
	}

	@Override
	public void addCoordinateSystemListener(ICoordinateSystemListener l) {
		getApparentAxis().addCoordinateSystemListener(l);
	}

	@Override
	public void removeCoordinateSystemListener(ICoordinateSystemListener l) {
		getApparentAxis().removeCoordinateSystemListener(l);
	}
	
	private IAxis getApparentAxis() {
		if (imageTrace.getImageOrigin()==ImageOrigin.TOP_LEFT || 
			imageTrace.getImageOrigin()==ImageOrigin.BOTTOM_RIGHT) {
			return i;
		} else {
			return j;
		}
	}

}
