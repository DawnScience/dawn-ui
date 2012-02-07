package org.dawb.workbench.ui.editors.plotting.dialog;

import org.csstudio.swt.xygraph.undo.IUndoableCommand;
import org.dawb.workbench.ui.editors.plotting.swtxy.Region;
import org.dawb.workbench.ui.editors.plotting.swtxy.XYRegionGraph;

/**The undoable command to remove an annotation.
 * @author Xihui Chen
 *
 */
public class RemoveRegionCommand implements IUndoableCommand {
	
	private XYRegionGraph xyGraph;
	private Region region;
	
	public RemoveRegionCommand(XYRegionGraph xyGraph, Region region) {
		this.xyGraph = xyGraph;
		this.region = region;
	}

	public void redo() {
		xyGraph.removeRegion(region);
	}

	public void undo() {		
		xyGraph.addRegion(region);
	}
	
	@Override
	public String toString() {
		return "Remove Annotation";
	}

}
