package org.dawb.workbench.ui.editors.plotting.swtxy;

import java.util.List;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Shape used for ROIs which has bounds fixed to the graph area.
 * 
 * @author fcp94556
 *
 */
public class FixedBoundsShape extends Shape implements IFixedBounds {
	
	/**
	 * Change to ensure bounds are not translated for this part
	 * in the plotting system.
	 */
	protected void primTranslate(final int dx, final int dy) {
		super.primTranslate(dx, dy);
		if (fixedBounds!=null) this.bounds = fixedBounds.getCopy();
	}

	private Rectangle fixedBounds;

	@Override
	public void setFixedBounds(Rectangle bounds) {
		this.fixedBounds = bounds;
		super.setBounds(bounds);
		
		final List<IFigure> children = getChildren();
		for (IFigure child : children) {
			if (child instanceof IFixedBounds) ((IFixedBounds)child).setFixedBounds(bounds);
		}
	}

	@Override
	protected void fillShape(Graphics graphics) {
		
	}

	@Override
	protected void outlineShape(Graphics graphics) {
		
	}
	
	private boolean active = false;

	/**
	 * active when user has made first click.
	 * @return
	 */
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
	/**
	 * Creates a mouse listener that after the first click, shows this
	 * figure at that location, then removes the listener.
	 * @param parent
	 */
	public void createActiveListener(final IFigure parent) {
		
		final ParentListener listener = new ParentListener(parent);
		parent.addMouseListener(listener);
	}

	private class ParentListener implements MouseListener {

		private IFigure parent;

		public ParentListener(IFigure parent) {
			this.parent = parent;
		}

		@Override
		public void mousePressed(MouseEvent me) {
			if (isActive() || !parent.getChildren().contains(FixedBoundsShape.this)) {
				remove();
				return;
			}
			FixedBoundsShape.this.setVisible(true);
			FixedBoundsShape.this.notifyStart(me);
			remove();
		}

		private void remove() {
			setActive(true);
			parent.removeMouseListener(this);				
		}


		@Override
		public void mouseDoubleClicked(MouseEvent me) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseReleased(MouseEvent me) {
			// TODO Auto-generated method stub
			
		}
	}


	protected void notifyStart(MouseEvent me) {
		// Override if your shape should do something when start location changes.
	}

}
