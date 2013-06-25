package org.dawnsci.plotting.api.region;

import org.eclipse.swt.graphics.Color;

public interface IGridSelection extends IRegion {

	void setPointColor(Color value);

	void setGridColor(Color value);

	Color getPointColor();

	Color getGridColor();

}
