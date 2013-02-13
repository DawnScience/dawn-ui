package org.dawnsci.plotting.tools.region;

import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.RegionUtils;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;

/**
 * TODO - modify so that this tool allows editing of regions.
 * @author fcp94556
 *
 */
public class RegionEditTool extends AbstractRegionTableTool {


	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}

	protected void createColumns(final TableViewer viewer) {
		
		ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);

		TableViewerColumn var = new TableViewerColumn(viewer, SWT.LEFT, 0);
		var.getColumn().setText("Name");
		var.getColumn().setWidth(120);
		var.setLabelProvider(new MeasurementLabelProvider(this, 0));

		var = new TableViewerColumn(viewer, SWT.CENTER, 1);
		var.getColumn().setText("Region Type");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new MeasurementLabelProvider(this, 1));

		var = new TableViewerColumn(viewer, SWT.LEFT, 2);
		var.getColumn().setText("dx");
		var.getColumn().setWidth(80);
		var.setLabelProvider(new MeasurementLabelProvider(this, 2));

		var = new TableViewerColumn(viewer, SWT.LEFT, 3);
		var.getColumn().setText("dy");
		var.getColumn().setWidth(80);
		var.setLabelProvider(new MeasurementLabelProvider(this, 3));

		var = new TableViewerColumn(viewer, SWT.LEFT, 4);
		var.getColumn().setText("length");
		var.getColumn().setWidth(80);
		var.setLabelProvider(new MeasurementLabelProvider(this, 4));

		var = new TableViewerColumn(viewer, SWT.LEFT, 5);
		var.getColumn().setText("Max Intensity");
		var.getColumn().setWidth(80);
		var.setLabelProvider(new MeasurementLabelProvider(this, 5));

		var = new TableViewerColumn(viewer, SWT.LEFT, 6);
		var.getColumn().setText("Coordinates");
		var.getColumn().setWidth(500);
		var.setLabelProvider(new MeasurementLabelProvider(this, 8));
	}
	
	
	protected void createNewRegion() {
		try {
			getPlottingSystem().createRegion(RegionUtils.getUniqueName("Region", getPlottingSystem()), IRegion.RegionType.BOX);
		} catch (Exception e) {
			logger.error("Cannot create line region for selecting in measurement tool!", e);
		}
	}

}
