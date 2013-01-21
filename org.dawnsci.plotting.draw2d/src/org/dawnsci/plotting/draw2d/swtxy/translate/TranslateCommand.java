package org.dawnsci.plotting.draw2d.swtxy.translate;

import java.util.List;

import org.csstudio.swt.xygraph.undo.IUndoableCommand;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.UpdateManager;
import org.eclipse.draw2d.geometry.Dimension;

public class TranslateCommand implements IUndoableCommand {

	private IFigure figure;
	private Dimension undoOffset;
	private List<IFigure> toTranslate;
	private FigureTranslator notifier;

	public TranslateCommand(final IFigure       figure, 
			            final Dimension     offset, 
			            final List<IFigure> toTranslate,
			            final FigureTranslator   notifier) {
		this.figure      = figure;
		this.undoOffset  = offset;
		this.toTranslate = toTranslate;
		this.notifier    = notifier;
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

	    try {
	    	
	    	notifier.fireBeforeTranslation(new TranslationEvent(this));

	    	
		    for (int i = 0; i < toTranslate.size(); i++)
		    	((IFigure) toTranslate.get(i)).translate(undoOffset.width, undoOffset.height);
		    
			UpdateManager updateMgr = figure.getUpdateManager();
			updateMgr.addDirtyRegion(figure.getParent(), figure.getParent().getBounds());
		    
	    } finally {
	    	
	    	notifier.fireAfterTranslation(new TranslationEvent(this));
	    	
	    }
	    
	}

}
