package org.dawnsci.dedi.ui.widgets.plotting;

import java.util.EventObject;

import org.eclipse.swt.graphics.Color;

public class ColourChangeEvent extends EventObject {
	private static final long serialVersionUID = 1059740358205812736L;
	
	private Color colour;
	private String itemName;
	
	public ColourChangeEvent(Object source, Color colour, String itemName) {
		super(source);
		this.colour = colour;
		this.itemName = itemName;
	}
	
	
	public Color getColor(){
		return colour;
	}
	
	public String getItemName(){
		return itemName;
	}
}
