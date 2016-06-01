/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting;

import org.eclipse.dawnsci.plotting.api.IPlottingSystemViewer;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Cursor;

/**
 * Do not make your viewer extend this class or it will not work.
 * 
 * This class contains draw2d specific methods which some old code requires to interact
 * with the plotting system properly. Once this old code has been replaced, these methods
 * will be removed.
 * 
 * @author Matthew Gerring
 *
 */
@Deprecated
public abstract class AbstractPlottingViewer<T> implements IPlottingSystemViewer<T> {

	public abstract void setShiftPoint(Point location);

	public abstract void addMouseClickListener(MouseListener clickListener);
	
	public abstract void removeMouseClickListener(MouseListener clickListener);

	public abstract void addMouseMotionListener(MouseMotionListener clickListener);

	public abstract void removeMouseMotionListener(MouseMotionListener clickListener);

	public abstract Point getShiftPoint();
	
	
	/**
	 * Can be overridden to detect any custom cursor 
	 * set on the plot. This cursor can also be lost 
	 * if a zoom is done.
	 * 
	 * @return
	 */
	@Deprecated
	public abstract Cursor getSelectedCursor();
	
	/**
	 * Set the cursor using a custom icon on the plot.
	 * This may get cancelled if other tools are used!
	 * 
	 * @deprecated SWT Specific. Can still be used for code that does not mind SWT link.
	 */
	@Deprecated
	public abstract void setSelectedCursor(Cursor des);
}
