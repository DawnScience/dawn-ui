package org.dawnsci.plotting.draw2d.swtxy;

import org.dawnsci.plotting.draw2d.swtxy.XYRegionGraph;
import org.dawnsci.plotting.draw2d.swtxy.selection.AbstractSelectionRegion;
import org.eclipse.nebula.visualization.xygraph.undo.IUndoableCommand;

/**The undoable command to add an annotation.
 * @author Xihui Chen
 *
 */
public class AddRegionCommand implements IUndoableCommand {
	
	private XYRegionGraph xyGraph;
	private AbstractSelectionRegion region;
	
	public AddRegionCommand(XYRegionGraph xyGraph, AbstractSelectionRegion region) {
		this.xyGraph = xyGraph;
		this.region = region;
	}

	public void redo() {
		xyGraph.addRegion(region);
	}

	public void undo() {
		xyGraph.removeRegion(region);
	}
	
	@Override
	public String toString() {
		return "Add Region";
	}

}
