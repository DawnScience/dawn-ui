package org.dawnsci.dedi.ui.widgets.plotting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.dawnsci.dedi.ui.GuiHelper;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

public class Legend extends Composite {
	private List<LegendItem> items;
	private Group legendGroup;
	
	
	public Legend(Composite parent){
		super(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(this);
		
		items = new ArrayList<>();
		legendGroup = GuiHelper.createGroup(parent, "Legend", 1);
	}
	
	
	public LegendItem addLegendItem(String name, Color defaultColour){
		LegendItem item = getItem(name);
		if(item != null) return item;
		item = new LegendItem(legendGroup, name, defaultColour);
		items.add(item);
		legendGroup.layout();
		return item;
	}
	
	
	public void removeLegendItem(String name){
		Iterator<LegendItem> iter = items.iterator();
		while(iter.hasNext()){
			LegendItem item = iter.next();
			if(item != null && Objects.equals(item.getItemName(), name)){
				iter.remove();
				item.dispose();
			}
		}
	}
	
	
	public void removeAllLegendItems() {
		Iterator<LegendItem> iter = items.iterator();
		while(iter.hasNext()){
			LegendItem item = iter.next();
			if(item != null){
				iter.remove();
				item.dispose();
			}
		}
	}
	
	
	public Color getColour(String name){
		for(LegendItem item : items){
			if(item != null && Objects.equals(item.getItemName(), name))
				return item.getColour();
		}
		return null;
	}
	
	
	private LegendItem getItem(String name){
		for(LegendItem item : items){
			if(item != null && Objects.equals(item.getItemName(), name))
				return item;
		}
		return null;
	}
}
