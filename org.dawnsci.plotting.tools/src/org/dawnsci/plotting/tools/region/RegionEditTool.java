package org.dawnsci.plotting.tools.region;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.dawnsci.common.widgets.celleditor.FloatSpinnerCellEditor;
import org.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegion.RegionType;
import org.dawnsci.plotting.api.region.RegionUtils;
import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.preference.RegionEditorConstants;
import org.dawnsci.plotting.tools.region.MeasurementLabelProvider.LabelType;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

/**
 * 
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

		int count = 0;
		TableViewerColumn var = new TableViewerColumn(viewer, SWT.LEFT, count);
		var.getColumn().setText("");
		var.getColumn().setWidth(22);
		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.VISIBLE));
		RegionEditingSupport regionEditor = new RegionEditingSupport(this,viewer, count);
		var.setEditingSupport(regionEditor);

		++count;
		var = new TableViewerColumn(viewer, SWT.LEFT, count);
		var.getColumn().setText("Name");
		var.getColumn().setWidth(120);
		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.ROINAME));
		regionEditor = new RegionEditingSupport(this,viewer, count);
		var.setEditingSupport(regionEditor);

		++count;
		var = new TableViewerColumn(viewer, SWT.CENTER, count);
		var.getColumn().setText("Start point x");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.STARTX));
		regionEditor = new RegionEditingSupport(this,viewer, count);
		var.setEditingSupport(regionEditor);

		++count;
		var = new TableViewerColumn(viewer, SWT.LEFT, count);
		var.getColumn().setText("Start point y");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.STARTY));
		regionEditor = new RegionEditingSupport(this,viewer, count);
		var.setEditingSupport(regionEditor);

		++count;
		var = new TableViewerColumn(viewer, SWT.LEFT, count);
		var.getColumn().setText("End Point x");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.ENDX));
		regionEditor = new RegionEditingSupport(this,viewer,count);
		var.setEditingSupport(regionEditor);

		++count;
		var = new TableViewerColumn(viewer, SWT.LEFT, count);
		var.getColumn().setText("End point y");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.ENDY));
		regionEditor = new RegionEditingSupport(this,viewer, count);
		var.setEditingSupport(regionEditor);

		++count;
		var = new TableViewerColumn(viewer, SWT.LEFT, count);
		var.getColumn().setText("Max Intensity");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.MAX));
		regionEditor = new RegionEditingSupport(this,viewer,count);
		var.setEditingSupport(regionEditor);

		++count;
		var = new TableViewerColumn(viewer, SWT.LEFT, count);
		var.getColumn().setText("Sum");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.SUM));
		regionEditor = new RegionEditingSupport(this,viewer, count);
		var.setEditingSupport(regionEditor);

		++count;
		var = new TableViewerColumn(viewer, SWT.LEFT, count);
		var.getColumn().setText("Active");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.ACTIVE));
		regionEditor = new RegionEditingSupport(this,viewer, count);
		var.setEditingSupport(regionEditor);
	}
		
	@Override
	protected boolean isRegionOk(IRegion iRegion) {
		return iRegion.isUserRegion();
	}

	protected void createActions() {
		getSite().getActionBars().getToolBarManager().add(getReselectAction());
		getSite().getActionBars().getToolBarManager().add(new Separator());
		super.createActions();
	}

	protected void createNewRegion() {
		try {
			getPlottingSystem().createRegion(RegionUtils.getUniqueName("Region", getPlottingSystem()), IRegion.RegionType.BOX);
		} catch (Exception e) {
			logger.error("Cannot create line region for selecting in measurement tool!", e);
		}
	}

	/**
	 * EditingSupport Class
	 *
	 */
}
