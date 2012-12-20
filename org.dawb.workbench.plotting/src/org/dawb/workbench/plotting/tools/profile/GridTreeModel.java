package org.dawb.workbench.plotting.tools.profile;

import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.dawnsci.common.widgets.tree.AbstractNodeModel;
import org.dawnsci.common.widgets.tree.LabelNode;
import org.dawnsci.common.widgets.tree.NumericNode;

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
		
        final LabelNode grid = new LabelNode("Grid", root);
        grid.setDefaultExpanded(true);
        registerNode(grid);
        
		final NumericNode<Length> xres = new NumericNode<Length>("X-axis Resolution", grid, SI.MICRO(SI.METER));
		xres.setEditable(true);
        registerNode(xres);
        
		final NumericNode<Length> yres = new NumericNode<Length>("Y-axis Resolution", grid, SI.MICRO(SI.METER));
		yres.setEditable(true);
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
