package org.dawnsci.surfacescatter.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class InputTileGenerator {

	private Label label;
	private Group tile;
	private Text text;
	private Combo comboDropDown;
	private Spinner spinner;
	
	
	public InputTileGenerator(String l, String t, Composite parent){
		
		tile = new Group(parent, SWT.NONE);
        GridLayout 	tileLayout = new GridLayout(2,true);
    	
        tile.setLayout(tileLayout);
		GridData tileData = new GridData(SWT.FILL, SWT.NULL, true, false);
		tile.setLayoutData(tileData);
		
		label = new Label(tile, SWT.NULL);
		label.setText(l);
		
		text = new Text(tile,SWT.SINGLE);
		text.setText(t);
	    
	}
	
	public InputTileGenerator(String l, String t, Group parent){
		
		tile = new Group(parent, SWT.NONE);
        GridLayout 	tileLayout = new GridLayout(2,true);
    	
        tile.setLayout(tileLayout);
		GridData tileData = new GridData(SWT.FILL, SWT.NULL, true, false);
		tile.setLayoutData(tileData);
		
		label = new Label(tile, SWT.NULL);
		label.setText(l);
		
		text = new Text(tile,SWT.SINGLE | SWT.BORDER | SWT.FILL);
		text.setText(t);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    
	}
	
	public InputTileGenerator(String l, Group parent){
		
		tile = new Group(parent, SWT.NONE);
        GridLayout 	tileLayout = new GridLayout(2,true);
    	
        tile.setLayout(tileLayout);
		GridData tileData = new GridData(SWT.FILL, SWT.NULL, true, false);
		tile.setLayoutData(tileData);
		
		label = new Label(tile, SWT.NULL);
		label.setText(l);
		
		comboDropDown = new Combo (tile,SWT.DROP_DOWN| SWT.BORDER | SWT.FILL);
		comboDropDown.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    
	}
	
	public InputTileGenerator(String l, Group parent, int min, int max, int inc){
		
		tile = new Group(parent, SWT.NONE);
        GridLayout 	tileLayout = new GridLayout(2,true);
    	
        tile.setLayout(tileLayout);
		GridData tileData = new GridData(SWT.FILL, SWT.NULL, true, false);
		tile.setLayoutData(tileData);
		
		label = new Label(tile, SWT.NULL);
		label.setText(l);
		
		spinner = new Spinner(tile, SWT.SINGLE |  SWT.BORDER);;
		spinner.setMinimum(min);
		spinner.setMaximum(max);
		spinner.setIncrement(inc);
		spinner.setSelection(10);
		spinner.setPageIncrement(10);
		spinner.setData(new GridData(SWT.FILL, SWT.FILL, true, false));
		spinner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    
	}
	
	
	
	public Group getGroup(){
		return tile;
	}
	
	public Spinner getSpinner(){
		return spinner;
	}
	
	
	public Combo getCombo(){
		return comboDropDown;
	}

	public Label getLabel(){
		return label;
	}
	
	public Text getText(){
		return text;
	}
}
