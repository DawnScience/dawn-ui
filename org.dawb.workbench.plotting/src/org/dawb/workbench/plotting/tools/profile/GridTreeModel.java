package org.dawb.workbench.plotting.tools.profile;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.unit.SI;

import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.workbench.plotting.system.swtxy.selection.GridSelection;
import org.dawnsci.common.widgets.tree.AbstractNodeModel;
import org.dawnsci.common.widgets.tree.AmountEvent;
import org.dawnsci.common.widgets.tree.AmountListener;
import org.dawnsci.common.widgets.tree.BooleanNode;
import org.dawnsci.common.widgets.tree.ColorNode;
import org.dawnsci.common.widgets.tree.LabelNode;
import org.dawnsci.common.widgets.tree.NumericNode;
import org.dawnsci.common.widgets.tree.ObjectNode;
import org.dawnsci.common.widgets.tree.ValueEvent;
import org.dawnsci.common.widgets.tree.ValueListener;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;
import org.jscience.physics.amount.Amount;

import uk.ac.diamond.scisoft.analysis.roi.GridROI;

/**
 * A Grid Model used to edit any GridROI.
 * 
 * @author fcp94556
 *
 */
public class GridTreeModel extends AbstractNodeModel {
	
	private GridROI   groi;
	private IRegion   region;
	private boolean   adjustingValue = false;

	GridTreeModel() {
		super();
		createGridNodes();
	}

	// All the nodes member data, not great
	private NumericNode<Length> xres, yres;
	private ObjectNode  roiName, gridDims;
	private ColorNode   regionColor, spotColor, gridColor;
	private BooleanNode midPoints,   gridLines;
	private NumericNode<Dimensionless> x, y, width, height;
	/**
	 * Same nodes to edit any 
	 */
	private void createGridNodes() {
		
		final LabelNode config = new LabelNode("Configuration", root);
		config.setDefaultExpanded(true);
		registerNode(config);

		this.roiName = new ObjectNode("Region Name", "-", config);
		registerNode(roiName);

		this.regionColor = new ColorNode("Region Color", IRegion.RegionType.GRID.getDefaultColor(), config);
		registerNode(regionColor);
		regionColor.addValueListener(new ValueListener() {
			public void valueChanged(ValueEvent evt) {
				if (groi==null || region==null) return;
				try {
					adjustingValue = true;
					region.setRegionColor((Color)evt.getValue());
					region.repaint();	
				} finally {
					adjustingValue = false;
				}
			}
		});

		this.spotColor = new ColorNode("Spot Color", ColorConstants.white, config);
		registerNode(spotColor);
		spotColor.addValueListener(new ValueListener() {
			public void valueChanged(ValueEvent evt) {
				if (groi==null || region==null) return;
				if (!(region instanceof GridSelection)) return;
				try {
					adjustingValue = true;
					GridSelection gl = (GridSelection)region;
					gl.setPointColor((Color)evt.getValue());
					region.repaint();	
				} finally {
					adjustingValue = false;
				}
			}
		});

		this.gridColor = new ColorNode("Grid Color", ColorConstants.lightGray, config);
		registerNode(gridColor);
		gridColor.addValueListener(new ValueListener() {
			public void valueChanged(ValueEvent evt) {
				if (groi==null || region==null) return;
				if (!(region instanceof GridSelection)) return;
				try {
					adjustingValue = true;
					GridSelection gl = (GridSelection)region;
					gl.setGridColor((Color)evt.getValue());
					region.repaint();	
				} finally {
					adjustingValue = false;
				}
			}
		});


		final LabelNode grid = new LabelNode("Grid", root);
		grid.setDefaultExpanded(true);
		registerNode(grid);

		this.xres = new NumericNode<Length>("X-axis Resolution", grid, SI.MICRO(SI.METER));
		xres.setDefault(Amount.valueOf(0.01, SI.MICRO(SI.METER)));
		xres.setUnits(SI.MICRO(SI.METER), SI.MILLIMETER);
		xres.setEditable(true);
		xres.setLowerBound(Amount.valueOf(0.01, SI.MICRO(SI.METER)));
		xres.setUpperBound(Amount.valueOf(100, SI.MILLIMETER));
		xres.setIncrement(0.1);
		xres.addAmountListener(new AmountListener<Length>() {		
			@Override
			public void amountChanged(AmountEvent<Length> evt) {
				if (groi==null || region==null) return;
				try {
					adjustingValue = true;
					double xspacing = groi.getGridPreferences().getXPixelsFromMicronsCoord(evt.getAmount().doubleValue(SI.MICRO(SI.METER)));
					groi.setxSpacing(xspacing);
					region.setROI(groi);
					region.repaint();
					updateGridDimensions(groi);
				} finally {
					adjustingValue = false;
				}
			}
		});
		registerNode(xres);

		this.yres = new NumericNode<Length>("Y-axis Resolution", grid, SI.MICRO(SI.METER));
		yres.setDefault(Amount.valueOf(0.01, SI.MICRO(SI.METER)));
		yres.setUnits(SI.MICRO(SI.METER), SI.MILLIMETER);
		yres.setEditable(true);
		yres.setLowerBound(Amount.valueOf(0.01, SI.MICRO(SI.METER)));
		yres.setUpperBound(Amount.valueOf(100, SI.MILLIMETER));
		yres.setIncrement(0.1);
		yres.addAmountListener(new AmountListener<Length>() {		
			@Override
			public void amountChanged(AmountEvent<Length> evt) {
				if (groi==null || region==null) return;
				try {
					adjustingValue = true;
					double yspacing = groi.getGridPreferences().getYPixelsFromMicronsCoord(evt.getAmount().doubleValue(SI.MICRO(SI.METER)));
					groi.setySpacing(yspacing);
					region.setROI(groi);
					region.repaint();
					updateGridDimensions(groi);
				} finally {
					adjustingValue = false;
				}
			}
		});
		registerNode(yres);

		this.gridDims = new ObjectNode("Grid Dimensions", "0 x 0 = no grid", grid);
		registerNode(gridDims);

		this.midPoints = new BooleanNode("Display mid-points", true, grid);
		registerNode(midPoints);
		midPoints.addValueListener(new ValueListener() {		
			@Override
			public void valueChanged(ValueEvent evt) {
				if (groi==null || region==null) return;
				try {
					adjustingValue = true;
					groi.setMidPointOn((Boolean)evt.getValue());
					region.setROI(groi);
					region.repaint();
				} finally {
					adjustingValue = false;
				}
			}
		});

		this.gridLines = new BooleanNode("Display grid", false, grid);
		registerNode(gridLines);
		gridLines.addValueListener(new ValueListener() {		
			@Override
			public void valueChanged(ValueEvent evt) {
				if (groi==null || region==null) return;
				try {
					adjustingValue = true;
					groi.setGridLineOn((Boolean)evt.getValue());
					region.setROI(groi);
					region.repaint();
				} finally {
					adjustingValue = false;
				}
			}
		});


		final LabelNode pos = new LabelNode("Position", root);
		pos.setDefaultExpanded(true);
		registerNode(pos);


		// TODO Custom axes
		this.x = new NumericNode<Dimensionless>("x", pos, Dimensionless.UNIT);
		x.setDefault(Amount.valueOf(0, Dimensionless.UNIT));
		x.setUnits(Dimensionless.UNIT);
		x.setEditable(true);
		x.setFormat("#####0.##");
		x.setLowerBound(Amount.valueOf(0, Dimensionless.UNIT));
		x.setUpperBound(Amount.valueOf(10000, Dimensionless.UNIT));
		x.setIncrement(0.1);
		x.addAmountListener(new AmountListener<Dimensionless>() {		
			@Override
			public void amountChanged(AmountEvent<Dimensionless> evt) {
				if (groi==null || region==null) return;
				try {
					adjustingValue = true;
					final double xVal = evt.getAmount().doubleValue(Dimensionless.UNIT);
					groi.setPoint(xVal, groi.getPoint()[1]);
					region.setROI(groi);
				} finally {
					adjustingValue = false;
				}
			}
		});
		registerNode(x);

		// TODO Custom axes
		this.y = new NumericNode<Dimensionless>("y", pos, Dimensionless.UNIT);
		y.setDefault(Amount.valueOf(0, Dimensionless.UNIT));
		y.setUnits(Dimensionless.UNIT);
		y.setFormat("#####0.##");
		y.setEditable(true);
		y.setLowerBound(Amount.valueOf(0, Dimensionless.UNIT));
		y.setUpperBound(Amount.valueOf(10000, Dimensionless.UNIT));
		y.setIncrement(0.1);
		y.addAmountListener(new AmountListener<Dimensionless>() {		
			@Override
			public void amountChanged(AmountEvent<Dimensionless> evt) {
				if (groi==null || region==null) return;
				try {
					adjustingValue = true;
					final double yVal = evt.getAmount().doubleValue(Dimensionless.UNIT);
					groi.setPoint(groi.getPoint()[0], yVal);
					region.setROI(groi);
				} finally {
					adjustingValue = false;
				}
			}
		});
		registerNode(y);

	}

	protected void updateGridDimensions(GridROI groi) {
		String value = String.format("%d x %d = %d point%s", groi.getDimensions()[0], groi.getDimensions()[1], groi
				.getDimensions()[0]
						* groi.getDimensions()[1], groi.getDimensions()[0] * groi.getDimensions()[1] == 1 ? "" : "s");
		this.gridDims.setValue(value, false);
		viewer.update(gridDims,  new String[]{"Value"});
	}

	private void setGridROI(GridROI groi) {

		if (!adjustingValue) viewer.cancelEditing();

		if (this.groi != groi) { // Grid spacings may have changed.
			xres.setValueQuietly(Amount.valueOf(groi.getGridPreferences().getXMicronsFromPixelsLen(groi.getxSpacing()), SI.MICRO(SI.METER)));
			viewer.update(xres, new String[]{"Value"});
			
			yres.setValueQuietly(Amount.valueOf(groi.getGridPreferences().getYMicronsFromPixelsLen(groi.getySpacing()), SI.MICRO(SI.METER)));
			viewer.update(yres, new String[]{"Value"});
			
			roiName.setValue(region.getName());
			viewer.update(roiName, new String[]{"Value"});
			
			// Grid, spots, on/off
			midPoints.setValue(groi.isMidPointOn(), false);
			viewer.update(midPoints, new String[]{"Value"});
			
			gridLines.setValue(groi.isGridLineOn(), false);
			viewer.update(gridLines, new String[]{"Value"});
			
			updateGridDimensions(groi);
		}
		this.groi = groi;
        
		this.x.setValueQuietly(groi.getPointX(), Dimensionless.UNIT);
		viewer.update(x, new String[]{"Value"});
		
		this.y.setValueQuietly(groi.getPointY(), Dimensionless.UNIT);
		viewer.update(y, new String[]{"Value"});
	}

	/**
	 * 
	 * ATTENTION: When dragging the groi might be != to region.getROI().
	 * @param region
	 * @param groi
	 */
	public void setRegion(IRegion region, GridROI groi) {
		if (!(region instanceof GridSelection)) return;
		if (region!=this.region) {
			GridSelection grid = (GridSelection)region;
			regionColor.setValue(grid.getRegionColor(), false);
			viewer.update(regionColor, new String[]{"Value"});

			spotColor.setValue(grid.getPointColor(),    false);
			viewer.update(spotColor, new String[]{"Value"});

			gridColor.setValue(grid.getGridColor(),   false);
			viewer.update(gridColor, new String[]{"Value"});

		}
		this.region = region;
		setGridROI(groi);
		
		
	}


}
