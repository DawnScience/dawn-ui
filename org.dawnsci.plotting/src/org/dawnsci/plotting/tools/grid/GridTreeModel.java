package org.dawnsci.plotting.tools.grid;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;

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
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.draw2d.swtxy.selection.GridSelection;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;
import org.jscience.physics.amount.Amount;

import uk.ac.diamond.scisoft.analysis.roi.GridPreferences;
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

		this.xres = new NumericNode<Length>("X-axis Resolution", grid, SI.MICRO(SI.METRE));
		xres.setDefault(Amount.valueOf(0.01, SI.MICRO(SI.METRE)));
		xres.setUnits(SI.MICRO(SI.METRE), SI.MILLIMETRE);
		xres.setEditable(true);
		xres.setLowerBound(Amount.valueOf(0.01, SI.MICRO(SI.METRE)));
		xres.setUpperBound(Amount.valueOf(100, SI.MILLIMETRE));
		xres.setIncrement(0.1);
		xres.addAmountListener(new AmountListener<Length>() {		
			@Override
			public void amountChanged(AmountEvent<Length> evt) {
				if (groi==null || region==null) return;
				try {
					adjustingValue = true;
					double xspacing = groi.getGridPreferences().getXPixelsFromMicronsLen(evt.getAmount().doubleValue(SI.MICRO(SI.METRE)));
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

		this.yres = new NumericNode<Length>("Y-axis Resolution", grid, SI.MICRO(SI.METRE));
		yres.setDefault(Amount.valueOf(0.01, SI.MICRO(SI.METRE)));
		yres.setUnits(SI.MICRO(SI.METRE), SI.MILLIMETRE);
		yres.setEditable(true);
		yres.setLowerBound(Amount.valueOf(0.01, SI.MICRO(SI.METRE)));
		yres.setUpperBound(Amount.valueOf(100, SI.MILLIMETRE));
		yres.setIncrement(0.1);
		yres.addAmountListener(new AmountListener<Length>() {		
			@Override
			public void amountChanged(AmountEvent<Length> evt) {
				if (groi==null || region==null) return;
				try {
					adjustingValue = true;
					double yspacing = groi.getGridPreferences().getYPixelsFromMicronsLen(evt.getAmount().doubleValue(SI.MICRO(SI.METRE)));
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

		createDetector();
		
		final LabelNode pos = new LabelNode("Position", root);
		pos.setDefaultExpanded(true);
		registerNode(pos);

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

		this.width = new NumericNode<Dimensionless>("width", pos, Dimensionless.UNIT);
		width.setDefault(Amount.valueOf(0, Dimensionless.UNIT));
		width.setUnits(Dimensionless.UNIT);
		width.setFormat("#####0.##");
		width.setEditable(true);
		width.setLowerBound(Amount.valueOf(0, Dimensionless.UNIT));
		width.setUpperBound(Amount.valueOf(10000, Dimensionless.UNIT));
		width.setIncrement(0.1);
		width.addAmountListener(new AmountListener<Dimensionless>() {		
			@Override
			public void amountChanged(AmountEvent<Dimensionless> evt) {
				if (groi==null || region==null) return;
				try {
					adjustingValue = true;
					final double val = evt.getAmount().doubleValue(Dimensionless.UNIT);
					groi.setLengths(val, groi.getLengths()[1]);
					region.setROI(groi);
				} finally {
					adjustingValue = false;
				}
			}
		});
		registerNode(width);

		this.height = new NumericNode<Dimensionless>("height", pos, Dimensionless.UNIT);
		height.setDefault(Amount.valueOf(0, Dimensionless.UNIT));
		height.setUnits(Dimensionless.UNIT);
		height.setFormat("#####0.##");
		height.setEditable(true);
		height.setLowerBound(Amount.valueOf(0, Dimensionless.UNIT));
		height.setUpperBound(Amount.valueOf(10000, Dimensionless.UNIT));
		height.setIncrement(0.1);
		height.addAmountListener(new AmountListener<Dimensionless>() {		
			@Override
			public void amountChanged(AmountEvent<Dimensionless> evt) {
				if (groi==null || region==null) return;
				try {
					adjustingValue = true;
					final double val = evt.getAmount().doubleValue(Dimensionless.UNIT);
					groi.setLengths(groi.getLengths()[0], val);
					region.setROI(groi);
				} finally {
					adjustingValue = false;
				}
			}
		});
		registerNode(height);

	}
	
	public static final String GRIDSCAN_RESOLUTION_X = "gridscan.res.x";
	public static final String GRIDSCAN_RESOLUTION_Y = "gridscan.res.y";
	public static final String GRIDSCAN_BEAMLINE_POSX = "gridscan.beamline.posx";
	public static final String GRIDSCAN_BEAMLINE_POSY = "gridscan.beamline.posy";

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
			xres.setValueQuietly(Amount.valueOf(groi.getGridPreferences().getXMicronsFromPixelsLen(groi.getxSpacing()), SI.MICRO(SI.METRE)));
			viewer.update(xres, new String[]{"Value"});
			
			yres.setValueQuietly(Amount.valueOf(groi.getGridPreferences().getYMicronsFromPixelsLen(groi.getySpacing()), SI.MICRO(SI.METRE)));
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
		
		this.width.setValueQuietly(groi.getLengths()[0], Dimensionless.UNIT);
		viewer.update(width, new String[]{"Value"});
		
		this.height.setValueQuietly(groi.getLengths()[1], Dimensionless.UNIT);
		viewer.update(height, new String[]{"Value"});

	}
	
	private void createDetector() {
	       
		// Detector Meta
        final LabelNode detectorMeta = new LabelNode("Detector", root);
        registerNode(detectorMeta);
        detectorMeta.setDefaultExpanded(true);
        
	    // Beam Centre
        final LabelNode beamCen = new LabelNode("Beam Centre", detectorMeta);
        beamCen.setTooltip("The beam centre is the intersection of the direct beam with the detector in terms of image coordinates. Can be undefined when there is no intersection.");
        registerNode(beamCen);
        beamCen.setDefaultExpanded(true);

        Unit<Length> unknown = SI.MILLIMETRE;
        UnitFormat.getInstance().label(unknown, "unit");
        NumericNode<Dimensionless> beamX = new NumericNode<Dimensionless>("X", beamCen, Dimensionless.UNIT);
        beamX.setUnits(Dimensionless.UNIT);
        beamX.addAmountListener(new AmountListener<Dimensionless>() {		
			@Override
			public void amountChanged(AmountEvent<Dimensionless> evt) {
				if (groi==null || region==null) return;
				try {
					adjustingValue = true;
					final double val = evt.getAmount().doubleValue(Dimensionless.UNIT);
					GridPreferences gp = groi.getGridPreferences();
					gp.setBeamlinePosX(val);
					groi.setGridPreferences(gp);
					region.setROI(groi);
				} finally {
					adjustingValue = false;
				}
			}
		});
        registerNode(beamX);
        beamX.setEditable(true);

        NumericNode<Dimensionless> beamY = new NumericNode<Dimensionless>("Y", beamCen, Dimensionless.UNIT);
        beamY.setUnits(Dimensionless.UNIT);
        beamY.addAmountListener(new AmountListener<Dimensionless>() {		
			@Override
			public void amountChanged(AmountEvent<Dimensionless> evt) {
				if (groi==null || region==null) return;
				try {
					adjustingValue = true;
					final double val = evt.getAmount().doubleValue(Dimensionless.UNIT);
					GridPreferences gp = groi.getGridPreferences();
					gp.setBeamlinePosY(val);
					groi.setGridPreferences(gp);
					region.setROI(groi);
				} finally {
					adjustingValue = false;
				}
			}
		});
        registerNode(beamY);
        beamY.setEditable(true);

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
