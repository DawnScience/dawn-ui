/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.grid;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Length;
import javax.measure.spi.ServiceProvider;
import javax.measure.Quantity;
import javax.measure.Unit;

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
import org.dawnsci.plotting.tools.diffraction.DiffractionUtils;
import org.dawnsci.plotting.tools.utils.ToolUtils;
import org.eclipse.dawnsci.analysis.dataset.roi.GridPreferences;
import org.eclipse.dawnsci.analysis.dataset.roi.GridROI;
import org.eclipse.dawnsci.plotting.api.region.IGridSelection;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

import tec.units.ri.quantity.Quantities;

/**
 * A Grid Model used to edit any GridROI.
 * 
 * @author Matthew Gerring
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
	private NumericNode<Length> x, y, width, height;
	private Unit<Length> xPosPixel = new Pixel();
	private Unit<Length> xDimPixel = new Pixel();
	private Unit<Length> yPosPixel = new Pixel();
	private Unit<Length> yDimPixel = new Pixel();

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
				if (region.getRegionType() != RegionType.GRID) return;
				try {
					adjustingValue = true;
					IGridSelection gl = (IGridSelection)region;
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
				if (region.getRegionType() != RegionType.GRID) return;
				try {
					adjustingValue = true;
					IGridSelection gl = (IGridSelection)region;
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

		this.xres = new NumericNode<Length>("X-axis Resolution", grid, ToolUtils.MICRO);
		xres.setDefault(Quantities.getQuantity(0.01, ToolUtils.MICRO));
		xres.setUnits(ToolUtils.MICRO, ToolUtils.MILLIMETRE);
		xres.setEditable(true);
		xres.setLowerBound(Quantities.getQuantity(0.01, ToolUtils.MICRO));
		xres.setUpperBound(Quantities.getQuantity(2000, ToolUtils.MILLIMETRE));
		xres.setIncrement(0.1);
		xres.addAmountListener(new AmountListener<Length>() {		
			@Override
			public void amountChanged(AmountEvent<Length> evt) {
				if (groi==null || region==null) return;
				try {
					adjustingValue = true;
					double xspacing = groi.getGridPreferences().getXPixelsFromMicronsLen(evt.getAmount().to(ToolUtils.MICRO).getValue().doubleValue());
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

		this.yres = new NumericNode<Length>("Y-axis Resolution", grid, ToolUtils.MICRO);
		yres.setDefault(Quantities.getQuantity(0.01, ToolUtils.MICRO));
		yres.setUnits(ToolUtils.MICRO, ToolUtils.MILLIMETRE);
		yres.setEditable(true);
		yres.setLowerBound(Quantities.getQuantity(0.01, ToolUtils.MICRO));
		yres.setUpperBound(Quantities.getQuantity(2000, ToolUtils.MILLIMETRE));
		yres.setIncrement(0.1);
		yres.addAmountListener(new AmountListener<Length>() {		
			@Override
			public void amountChanged(AmountEvent<Length> evt) {
				if (groi==null || region==null) return;
				try {
					adjustingValue = true;
					double yspacing = groi.getGridPreferences().getYPixelsFromMicronsLen(evt.getAmount().to(ToolUtils.MICRO).getValue().doubleValue());
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


		this.x = new NumericNode<Length>("x", pos, xPosPixel);
		x.setDefault(Quantities.getQuantity(0, ToolUtils.MILLIMETRE));
		x.setUnits(xPosPixel, ToolUtils.MILLIMETRE);
		x.setEditable(true);
		x.setFormat("#####0.##");
		x.setLowerBound(Quantities.getQuantity(-10000, xPosPixel));
		x.setUpperBound(Quantities.getQuantity(10000, xPosPixel));
		x.setIncrement(0.1);
		x.addAmountListener(new AmountListener<Length>() {		
			@Override
			public void amountChanged(AmountEvent<Length> evt) {
				adjustingValue = true;
				double val = changeAmount(evt);
				groi.setPoint(val, groi.getPoint()[1]);
				region.setROI(groi);
				adjustingValue = false;
			}
		});
		registerNode(x);

		this.y = new NumericNode<Length>("y", pos, yPosPixel);
		y.setDefault(Quantities.getQuantity(0, ToolUtils.MILLIMETRE));
		y.setUnits(yPosPixel, ToolUtils.MILLIMETRE);
		y.setFormat("#####0.##");
		y.setEditable(true);
		y.setLowerBound(Quantities.getQuantity(-1000, yPosPixel));
		y.setUpperBound(Quantities.getQuantity(10000, yPosPixel));
		y.setIncrement(0.1);
		y.addAmountListener(new AmountListener<Length>() {		
			@Override
			public void amountChanged(AmountEvent<Length> evt) {
				adjustingValue = true;
				double val = changeAmount(evt);
				groi.setPoint(groi.getPoint()[0], val);
				region.setROI(groi);
				adjustingValue = false;
			}
		});
		registerNode(y);

		this.width = new NumericNode<Length>("width", pos, xDimPixel);
		width.setDefault(Quantities.getQuantity(0, ToolUtils.MILLIMETRE));
		width.setUnits(xDimPixel, ToolUtils.MILLIMETRE);
		width.setFormat("#####0.##");
		width.setEditable(true);
		width.setLowerBound(Quantities.getQuantity(0, xDimPixel));
		width.setUpperBound(Quantities.getQuantity(10000, xDimPixel));
		width.setIncrement(0.1);
		width.addAmountListener(new AmountListener<Length>() {		
			@Override
			public void amountChanged(AmountEvent<Length> evt) {
				adjustingValue = true;
				double val = changeAmount(evt);
				groi.setLengths(val, groi.getLengths()[1]);
				region.setROI(groi);
				adjustingValue = false;
			}
		});
		registerNode(width);

		this.height = new NumericNode<Length>("height", pos, yDimPixel);
		height.setDefault(Quantities.getQuantity(0, ToolUtils.MILLIMETRE));
		height.setUnits(yDimPixel, ToolUtils.MILLIMETRE);
		height.setFormat("#####0.##");
		height.setEditable(true);
		height.setLowerBound(Quantities.getQuantity(0, yDimPixel));
		height.setUpperBound(Quantities.getQuantity(10000, yDimPixel));
		height.setIncrement(0.1);
		height.addAmountListener(new AmountListener<Length>() {		
			@Override
			public void amountChanged(AmountEvent<Length> evt) {
				adjustingValue = true;
				double val = changeAmount(evt);
				groi.setLengths(groi.getLengths()[0], val);
				region.setROI(groi);
				adjustingValue = false;
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

	public void newGridPreferences(GridPreferences gp) {
		setGridROI(groi, gp);
	}

	private void setGridROI(GridROI groi) {
 		setGridROI(groi, null);
 	}

	//This is a bit of a workaround to allow new gridpreferences to be used 
	//before and Async thread has updated them
	private void setGridROI(GridROI groi, GridPreferences gp) {
	
		if (!adjustingValue) viewer.cancelEditing();

		if (this.groi != groi) { // Grid spacings may have changed.
			xres.setValueQuietly(Quantities.getQuantity(groi.getGridPreferences().getXMicronsFromPixelsLen(groi.getxSpacing()), ToolUtils.MICRO));
			viewer.update(xres, new String[]{"Value"});
			
			yres.setValueQuietly(Quantities.getQuantity(groi.getGridPreferences().getYMicronsFromPixelsLen(groi.getySpacing()), ToolUtils.MICRO));
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

		if (gp == null) gp = groi.getGridPreferences();
		
		updateUnits(gp);
		updateNode(x, groi.getPointX());
		updateNode(y, groi.getPointY());
		updateNode(width, groi.getLength(0));
		updateNode(height, groi.getLength(1));
	}
	
	private void createDetector() {
		Unit<?> dimensionLessUnit = ServiceProvider.current().getQuantityFactory(Dimensionless.class).getSystemUnit();
		// Detector Meta
        final LabelNode detectorMeta = new LabelNode("Detector", root);
        registerNode(detectorMeta);
        detectorMeta.setDefaultExpanded(true);
        
	    // Beam Centre
        final LabelNode beamCen = new LabelNode("Beam Centre", detectorMeta);
        beamCen.setTooltip("The beam centre is the intersection of the direct beam with the detector in terms of image coordinates. Can be undefined when there is no intersection.");
        registerNode(beamCen);
        beamCen.setDefaultExpanded(true);

        NumericNode<Dimensionless> beamX = new NumericNode<Dimensionless>("X", beamCen, dimensionLessUnit);
        beamX.setUnits(dimensionLessUnit);
        registerNode(beamX);
        beamX.setEditable(true);

        NumericNode<Dimensionless> beamY = new NumericNode<Dimensionless>("Y", beamCen, dimensionLessUnit);
        beamY.setUnits(dimensionLessUnit);
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
		if (region.getRegionType() != RegionType.GRID) return;
		if (region!=this.region) {
			regionColor.setValue(region.getRegionColor(), false);
			viewer.update(regionColor, new String[]{"Value"});

			IGridSelection grid = (IGridSelection)region;
			spotColor.setValue(grid.getPointColor(),    false);
			viewer.update(spotColor, new String[]{"Value"});

			gridColor.setValue(grid.getGridColor(),   false);
			viewer.update(gridColor, new String[]{"Value"});

		}
		this.region = region;
		setGridROI(groi);
	}

	private void updateNode(NumericNode<Length> node, double newValue) {
		node.setValueQuietly(Quantities.getQuantity(newValue, node.getDefaultUnit()).to(node.getUnit()));
		viewer.update(node, new String[]{"Value"});
	}
	
	public void updateUnits(GridPreferences gp) {
		double xPPMm = gp.getResolutionX(); //x pixels per mm
		double yPPMm = gp.getResolutionY();
		double xOffset = gp.getBeamlinePosX();
		double yOffset = gp.getBeamlinePosY();
		
		((Pixel) xPosPixel).setPixelsPerMm(xPPMm);
		((Pixel) xPosPixel).setOffset(xOffset);
		
		((Pixel) yPosPixel).setPixelsPerMm(yPPMm);
		((Pixel) yPosPixel).setOffset(yOffset);
		
		((Pixel) xDimPixel).setPixelsPerMm(xPPMm);
		((Pixel) yDimPixel).setPixelsPerMm(yPPMm);
	}
	
	@SuppressWarnings("unchecked")
	private double changeAmount(AmountEvent<Length> evt) {
		if (groi==null || region==null) return 0;
		try {
			Quantity<Length> amt = evt.getAmount();
			return amt.to(((NumericNode<Length>) evt.getSource()).getDefaultUnit()).getValue().doubleValue();
		} finally {
		}
	}
} 
