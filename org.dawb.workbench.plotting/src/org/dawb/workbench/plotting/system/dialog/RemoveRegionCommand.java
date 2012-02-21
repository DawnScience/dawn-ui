package org.dawb.workbench.plotting.system.dialog;

import java.util.ArrayList;
import java.util.List;

import org.csstudio.swt.xygraph.undo.IUndoableCommand;
import org.dawb.workbench.plotting.system.swtxy.Region;
import org.dawb.workbench.plotting.system.swtxy.XYRegionGraph;

/**The undoable command to remove an annotation.
 * @author Xihui Chen
 *
 */
public class RemoveRegionCommand implements IUndoableCommand {
	
	private XYRegionGraph xyGraph;
	private List<Region> regions;
	
	public RemoveRegionCommand(XYRegionGraph xyGraph, Region region) {
		this.xyGraph = xyGraph;
		this.regions = new ArrayList<Region>();
		regions.add(region);
	}

	public RemoveRegionCommand(XYRegionGraph xyGraph, List<Region> regions) {
		this.xyGraph = xyGraph;
		this.regions = regions;
	}

	public void redo() {
		for (Region region : regions)  xyGraph.removeRegion(region);
	}

	public void undo() {		
		for (Region region : regions)  xyGraph.addRegion(region);
	}
	
	@Override
	public String toString() {
		return "Remove Region(s)";
	}

}
