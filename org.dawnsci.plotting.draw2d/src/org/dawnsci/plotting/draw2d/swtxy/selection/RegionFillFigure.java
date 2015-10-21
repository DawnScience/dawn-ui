/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.draw2d.swtxy.selection;

import java.util.Iterator;

import org.dawnsci.plotting.draw2d.swtxy.translate.FigureTranslator;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegionContainer;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.Shape;

/**
 * You do not have to use this class for the figure which fills the region and
 * is not a control point, but it can be handy if you do.
 * 
 * It also has a right click menu which will appear when the user presses right click.
 * This moves the region up or down in order and 
 * 
 * @author Matthew Gerring
 *
 */
public abstract class RegionFillFigure<T extends IROI> extends Shape implements IRegionContainer {
	
	protected AbstractSelectionRegion<T> region;

	protected boolean fill = true; // local version is super class has a private one

	public RegionFillFigure(AbstractSelectionRegion<T> region) {
		this.region = region;
		setEnabled(false); // Allows mouse events to see through content to report position
	}

	@Override
	public void setFill(boolean fill) {
		this.fill = fill;
		super.setFill(fill); // keep superclass's flag up-to-date 
	}

	public boolean isFill() {
		return fill;
	}

	public void setMobile(final boolean mobile) {
		
		final FigureTranslator mover = getFigureMover();
		if (mover==null) return;
		
		mover.setActive(mobile);
	}

	public boolean isMobile() {
		final FigureTranslator mover = getFigureMover();
		if (mover==null) return false;
		
		return mover.isActive();
	}

	protected FigureTranslator getFigureMover() {
		final Iterator<?> it = getListeners(MouseListener.class);
		if (it!=null && it.hasNext()) {
			MouseListener l = null;
			
			while(it.hasNext()) {
				l=(MouseListener)it.next();
				if (l instanceof FigureTranslator) return (FigureTranslator)l;
			}
		}
		return null;
	}

	public IRegion getRegion() {
		return region;
	}

	@SuppressWarnings("unchecked")
	public void setRegion(IRegion region) {
		if (region instanceof AbstractSelectionRegion<?>)
			this.region = (AbstractSelectionRegion<T>) region;
	}
	
	private boolean areaTranslatable = false;
	/**
	 * Overridden so that we can set enabled to false and have the
	 * shape draw the same.
	 */
	public void paintFigure(Graphics graphics) {
		
		if (!areaTranslatable) {
			if (getAntialias() != null) {
				graphics.setAntialias(getAntialias().intValue());
			}
			if (getAlpha() != null) {
				graphics.setAlpha(getAlpha().intValue());
			}
			if (fill) {
				fillShape(graphics);
			}
			outlineShape(graphics);
		} else {
			super.paintFigure(graphics);
		}
		
	}

	public boolean isAreaTranslatable() {
		return areaTranslatable;
	}

	/**
	 * Call to set if the area of the region allows the user to click and translate the shape.
	 * @param areaTranslatable
	 */
	public void setAreaTranslatable(boolean areaTranslatable) {
		this.areaTranslatable = areaTranslatable;
		setEnabled(areaTranslatable); // Allows mouse events to see through content to report position
		setCursor(areaTranslatable?Cursors.HAND:null);
	}

}
