package org.dawb.workbench.plotting.tools.profile;

import javax.measure.quantity.Length;
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
	

	GridTreeModel() {
		super();
		createGridNodes();
	}

	private NumericNode<Length> xres, yres;
	private ObjectNode roiName;
	/**
	 * Same nodes to edit any 
	 */
	private void createGridNodes() {
		
        final LabelNode config = new LabelNode("Configuration", root);
        config.setDefaultExpanded(true);
        registerNode(config);
        
        this.roiName = new ObjectNode("Region Name", "-", config);
        registerNode(roiName);

        final ColorNode regionColor = new ColorNode("Region Color", IRegion.RegionType.GRID.getDefaultColor(), config);
        registerNode(regionColor);
        regionColor.addValueListener(new ValueListener() {
        	public void valueChanged(ValueEvent evt) {
				if (groi==null || region==null) return;
				region.setRegionColor((Color)evt.getValue());
				region.repaint();	
        	}
        });
       
        final ColorNode spotColor = new ColorNode("Spot Color", ColorConstants.white, config);
        registerNode(spotColor);
        spotColor.addValueListener(new ValueListener() {
        	public void valueChanged(ValueEvent evt) {
				if (groi==null || region==null) return;
				if (!(region instanceof GridSelection)) return;
				GridSelection gl = (GridSelection)region;
				gl.setPointColor((Color)evt.getValue());
				region.repaint();	
        	}
        });
       
        final ColorNode gridColor = new ColorNode("Grid Color", ColorConstants.lightGray, config);
        registerNode(gridColor);

		
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
				double xspacing = groi.getGridPreferences().getXPixelsFromMicronsCoord(evt.getAmount().doubleValue(SI.MICRO(SI.METER)));
				groi.setxSpacing(xspacing);
				region.setROI(groi);
				region.repaint();
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
				double yspacing = groi.getGridPreferences().getYPixelsFromMicronsCoord(evt.getAmount().doubleValue(SI.MICRO(SI.METER)));
				groi.setySpacing(yspacing);
				region.setROI(groi);
				region.repaint();
			}
		});
        registerNode(yres);

        final BooleanNode midPoints = new BooleanNode("Display mid-points", true, grid);
        registerNode(midPoints);
        midPoints.addValueListener(new ValueListener() {		
			@Override
			public void valueChanged(ValueEvent evt) {
				if (groi==null || region==null) return;
				groi.setMidPointOn((Boolean)evt.getValue());
				region.setROI(groi);
				region.repaint();
			}
		});
        
        final BooleanNode gridLines = new BooleanNode("Display grid", false, grid);
        registerNode(gridLines);
        gridLines.addValueListener(new ValueListener() {		
			@Override
			public void valueChanged(ValueEvent evt) {
				if (groi==null || region==null) return;
				groi.setGridLineOn((Boolean)evt.getValue());
				region.setROI(groi);
				region.repaint();
			}
		});
        
        
	}

	private void setGridROI(GridROI groi) {
		
		if (this.groi != groi) { // Grid spacings may have changed.
			xres.setValueQuietly(Amount.valueOf(groi.getGridPreferences().getXMicronsFromPixelsLen(groi.getxSpacing()), SI.MICRO(SI.METER)));
			viewer.update(xres, new String[]{"Value"});
			
			yres.setValueQuietly(Amount.valueOf(groi.getGridPreferences().getYMicronsFromPixelsLen(groi.getySpacing()), SI.MICRO(SI.METER)));
			viewer.update(yres, new String[]{"Value"});
			
			roiName.setValue(region.getName());
			viewer.update(roiName, new String[]{"Value"});
		}
		this.groi = groi;
        
		// TODO Position
		
	}

	/**
	 * 
	 * ATTENTION: When dragging the groi might be != to region.getROI().
	 * @param region
	 * @param groi
	 */
	public void setRegion(IRegion region, GridROI groi) {
		this.region = region;
		setGridROI(groi);
	}


}
