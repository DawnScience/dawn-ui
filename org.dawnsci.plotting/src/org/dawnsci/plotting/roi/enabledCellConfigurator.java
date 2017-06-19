package org.dawnsci.plotting.roi;

/**
 *  A configurator may be set on ROIEditTable to provide editable configuration to table cells.
 */
@FunctionalInterface
public interface enabledCellConfigurator {

	void configure(IRegionRow row);
}
