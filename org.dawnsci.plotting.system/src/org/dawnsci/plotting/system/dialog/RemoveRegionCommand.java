package org.dawnsci.plotting.system.dialog;

import java.util.ArrayList;
import java.util.List;

import org.csstudio.swt.xygraph.undo.IUndoableCommand;
import org.dawnsci.plotting.draw2d.swtxy.XYRegionGraph;
import org.dawnsci.plotting.draw2d.swtxy.selection.AbstractSelectionRegion;

/**The undoable command to remove an annotation.
 * @author Xihui Chen
 *
 */
public class RemoveRegionCommand implements IUndoableCommand {
	
	private XYRegionGraph xyGraph;
	private List<AbstractSelectionRegion> regions;
	
	public RemoveRegionCommand(XYRegionGraph xyGraph, AbstractSelectionRegion region) {
		this.xyGraph = xyGraph;
		this.regions = new ArrayList<AbstractSelectionRegion>();
		regions.add(region);
	}

	public RemoveRegionCommand(XYRegionGraph xyGraph, List<AbstractSelectionRegion> regions) {
		this.xyGraph = xyGraph;
		this.regions = regions;
	}

	public void redo() {
		for (AbstractSelectionRegion region : regions)  xyGraph.removeRegion(region);
	}

	public void undo() {		
		for (AbstractSelectionRegion region : regions)  xyGraph.addRegion(region);
	}
	
	@Override
	public String toString() {
		return "Remove Region(s)";
	}

}
