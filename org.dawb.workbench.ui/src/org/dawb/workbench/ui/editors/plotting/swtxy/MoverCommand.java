package org.dawb.workbench.ui.editors.plotting.swtxy;

import java.util.List;

import org.csstudio.swt.xygraph.undo.IUndoableCommand;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.UpdateManager;
import org.eclipse.draw2d.geometry.Dimension;

public class MoverCommand implements IUndoableCommand {

	private IFigure figure;
	private Dimension undoOffset;
	private boolean translateChildren;

	public MoverCommand(IFigure figure, Dimension offset, boolean translateChildren) {
		this.figure = figure;
		this.undoOffset = offset;
		this.translateChildren = translateChildren;
	}

	@Override
	public void undo() {
		toggleLocation();
	}

	@Override
	public void redo() {
		toggleLocation();
	}

	private void toggleLocation() {

	    undoOffset = new Dimension(-1*undoOffset.width, -1*undoOffset.height);
        if (!translateChildren) {
           	figure.translate(undoOffset.width, undoOffset.height);
       	    
        } else {
			final List<IFigure> children = figure.getChildren();
			for (int i = 0; i < children.size(); i++)
				((IFigure) children.get(i)).translate(undoOffset.width, undoOffset.height);

        }
        
		UpdateManager updateMgr = figure.getUpdateManager();
		updateMgr.addDirtyRegion(figure.getParent(), figure.getParent().getBounds());
	
	}

}
