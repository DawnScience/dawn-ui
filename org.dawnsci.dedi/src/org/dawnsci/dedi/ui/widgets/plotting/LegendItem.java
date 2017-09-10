package org.dawnsci.dedi.ui.widgets.plotting;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.dedi.ui.GuiHelper;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class LegendItem extends Composite {
	private String itemName;
	private Label colourLabel;
	private Button chooseColourButton;
	
	private ResourceManager resourceManager;
	private Color colour;
	
	private List<ColourChangeListener> listeners = new ArrayList<>();
	
	
	public LegendItem(Composite parent, String name, Color defaultColour) {
		super(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(3).equalWidth(true).applyTo(this);
		
		resourceManager = new LocalResourceManager(JFaceResources.getResources(), this);
		
		colour = defaultColour;
		
		GuiHelper.createLabel(this, name);
		colourLabel = GuiHelper.createLabel(this, "   ");
		colourLabel.addPaintListener(e -> createLegendColourLabel(e.gc));
		
	    chooseColourButton = new Button(this, SWT.PUSH); 
		chooseColourButton.setText("Change colour");
		chooseColourButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				ColorDialog cd = new ColorDialog(parent.getShell());
				cd.setText("Choose colour");
				RGB newColour = cd.open();
				if(newColour == null) return;
				colour = resourceManager.createColor(newColour);
				colourLabel.redraw();
				notifyColourChangeListeners();
			}
		});
		
		this.itemName = name;
	}
	
	
	private void createLegendColourLabel(GC gc){
		gc.setBackground(colour);
        gc.fillRectangle(0, 0, colourLabel.getBounds().width, colourLabel.getBounds().height);
	}
	
	
	public Color getColour(){
		checkWidget();
		return colour;
	}
	
	
	public String getItemName(){
		checkWidget();
		return itemName;
	}
	
	
	public void addColourChangeListener(ColourChangeListener listener){
		listeners.add(listener);
	}
	
	
	public void removeColourChangeListener(ColourChangeListener listener){
		listeners.remove(listener);
	}
	
	
	private void notifyColourChangeListeners(){
		ColourChangeEvent event = new ColourChangeEvent(this, colour, this.itemName);
		for(ColourChangeListener listener : listeners) listener.colourChanged(event);
	}
}
