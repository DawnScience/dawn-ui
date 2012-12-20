package org.dawb.workbench.plotting.tools.profile;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.dawb.common.ui.plot.region.IRegion;
import org.dawnsci.common.widgets.tree.AbstractNodeModel;
import org.dawnsci.common.widgets.tree.ColorNode;
import org.dawnsci.common.widgets.tree.LabelNode;
import org.dawnsci.common.widgets.tree.NumericNode;
import org.eclipse.draw2d.ColorConstants;
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
	
	GridTreeModel() {
		super();
		createGridNodes();
	}

	/**
	 * Same nodes to edit any 
	 */
	private void createGridNodes() {
		
        final LabelNode config = new LabelNode("Configuration", root);
        config.setDefaultExpanded(true);
        registerNode(config);
        
        final LabelNode roiName = new LabelNode("Region Name", config);
        registerNode(roiName);

        final ColorNode regionColor = new ColorNode("Region Color", IRegion.RegionType.GRID.getDefaultColor(), config);
        registerNode(regionColor);
       
        final ColorNode spotColor = new ColorNode("Spot Color", ColorConstants.white, config);
        registerNode(spotColor);
        
        final ColorNode gridColor = new ColorNode("Grid Color", ColorConstants.lightGray, config);
        registerNode(gridColor);

		
        final LabelNode grid = new LabelNode("Grid", root);
        grid.setDefaultExpanded(true);
        registerNode(grid);
        
		final NumericNode<Length> xres = new NumericNode<Length>("X-axis Resolution", grid, SI.MICRO(SI.METER));
		xres.setDefault(Amount.valueOf(0.01, SI.MICRO(SI.METER)));
		xres.setUnits(SI.MICRO(SI.METER), SI.MILLIMETER);
		xres.setEditable(true);
		xres.setLowerBound(Amount.valueOf(0.01, SI.MICRO(SI.METER)));
		xres.setUpperBound(Amount.valueOf(100, SI.MILLIMETER));
		xres.setIncrement(0.1);
        registerNode(xres);
        
		final NumericNode<Length> yres = new NumericNode<Length>("Y-axis Resolution", grid, SI.MICRO(SI.METER));
		yres.setDefault(Amount.valueOf(0.01, SI.MICRO(SI.METER)));
		yres.setUnits(SI.MICRO(SI.METER), SI.MILLIMETER);
		yres.setEditable(true);
		yres.setLowerBound(Amount.valueOf(0.01, SI.MICRO(SI.METER)));
		yres.setUpperBound(Amount.valueOf(100, SI.MILLIMETER));
		yres.setIncrement(0.1);
        registerNode(yres);

	}

	public GridROI getGroi() {
		return groi;
	}

	public void setGroi(GridROI groi) {
		this.groi = groi;
		// TODO set values into nodes.
	}

}
