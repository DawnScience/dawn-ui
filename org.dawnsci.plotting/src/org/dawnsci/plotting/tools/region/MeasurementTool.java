package org.dawnsci.plotting.tools.region;

import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.RegionUtils;
import org.dawnsci.plotting.tools.region.MeasurementLabelProvider.LabelType;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;


public class MeasurementTool extends AbstractRegionTableTool {


	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_1D;
	}

	protected void createColumns(final TableViewer viewer) {
		
		ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);

		TableViewerColumn var = new TableViewerColumn(viewer, SWT.LEFT, 0);
		var.getColumn().setText("Name");
		var.getColumn().setWidth(120);
		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.ROINAME));

		var = new TableViewerColumn(viewer, SWT.CENTER, 1);
		var.getColumn().setText("Region Type");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.ROITYPE));

		var = new TableViewerColumn(viewer, SWT.LEFT, 2);
		var.getColumn().setText("dx");
		var.getColumn().setWidth(80);
		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.DX));

		var = new TableViewerColumn(viewer, SWT.LEFT, 3);
		var.getColumn().setText("dy");
		var.getColumn().setWidth(80);
		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.DY));

		var = new TableViewerColumn(viewer, SWT.LEFT, 4);
		var.getColumn().setText("length");
		var.getColumn().setWidth(80);
		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.LENGTH));

		var = new TableViewerColumn(viewer, SWT.LEFT, 5);
		var.getColumn().setText("Coordinates");
		var.getColumn().setWidth(500);
		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.ROISTRING));
	}
	
	
	protected void createNewRegion() {
		try {
			getPlottingSystem().createRegion(RegionUtils.getUniqueName("Measurement", getPlottingSystem()), IRegion.RegionType.LINE);
		} catch (Exception e) {
			logger.error("Cannot create line region for selecting in measurement tool!", e);
		}
	}
}
