package org.dawnsci.surfacescatter.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class InputTileGenerator {

	private Label label;
	private Group tile;
	private Text text;
	
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
	
	
	public Group getGroup(){
		return tile;
	}

	public Label getLabel(){
		return label;
	}
	
	public Text getText(){
		return text;
	}
}
