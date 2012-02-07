package org.dawb.workbench.ui.editors.plotting.swtxy;

import java.util.List;

import org.csstudio.swt.xygraph.undo.IUndoableCommand;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.UpdateManager;
import org.eclipse.draw2d.geometry.Dimension;

public class MoverCommand implements IUndoableCommand {

	private IFigure figure;
	private Dimension undoOffset;
	private List<IFigure> toTranslate;

	public MoverCommand(IFigure figure, Dimension offset, List<IFigure> toTranslate) {
		this.figure = figure;
		this.undoOffset = offset;
		this.toTranslate = toTranslate;
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

	    for (int i = 0; i < toTranslate.size(); i++)
	    	((IFigure) toTranslate.get(i)).translate(undoOffset.width, undoOffset.height);

        
		UpdateManager updateMgr = figure.getUpdateManager();
		updateMgr.addDirtyRegion(figure.getParent(), figure.getParent().getBounds());
	
	}

}
