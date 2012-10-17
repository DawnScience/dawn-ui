/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dawnsci.plotting.jreality.legend;

import org.dawnsci.plotting.jreality.impl.Plot1DStyles;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

/**
 * A special canvas specifically for the legend entries to visualise the
 * internal state of a Plot1DAppearance
 */
public class TableCanvas extends Canvas implements PaintListener {

	private Plot1DStyles currentStyle;
	private org.eclipse.swt.graphics.Color currentColour;
	/**
	 * Constructs a new instance of this class given its parent and a 
	 * style value describing its behaviour and appearance.
	 * @param parent a composite control which will be the parent of the new instance (cannot be null)
	 * @param style the style of control to construct
	 */
	public TableCanvas(Composite parent, int style) {
		super(parent, style);
		super.addPaintListener(this);
	}

	/**
	 * Set plotting style of this canvas
	 * @param style
	 */
	public void setStyle(Plot1DStyles style)
	{
		currentStyle = style;
	}
	
	/**
	 * Set the colour of this canvas
	 * @param colour
	 */
	public void setColour(org.eclipse.swt.graphics.Color colour)
	{
		currentColour = colour;
	}
	
	@Override
	public void paintControl(PaintEvent e) {
		Rectangle rect = this.getClientArea();
		e.gc.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		e.gc.fillRectangle(0,0,rect.width,rect.height);
		e.gc.setBackground(currentColour);
        int width = rect.width;
        int pos = (rect.height >> 1)-2;
        switch (currentStyle)
		{
			case SOLID:
			case SOLID_POINT:
				e.gc.fillRectangle(0,pos, width,4);
			break;
			case DASHED:
			{
				int dashSize = width >> 3;
				for (int i = 0; i < 4; i++)
				{
					e.gc.fillRectangle(i*2*dashSize,pos,dashSize,4);
				}
			}
			break;
			case DASHED_POINT:
			case POINT:
				int dashSize = width >> 3;
				for (int i = 0; i < 4; i++)
				{
					e.gc.fillOval(i*2*dashSize,pos,(dashSize >> 1),(dashSize >> 1));
				}
			break;
		}
	}

}
