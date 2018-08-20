package org.dawnsci.spectrum.ui.wizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class DropDownMenuTestComposite extends Composite {


    
	//	Display display = new Display();
//	Shell shell = new Shell(display);
	private final Combo comboDropDown0;
	private final Combo comboDropDown1;

	public DropDownMenuTestComposite (Composite parent, int style) throws Exception {

		super(parent, style);
		
		
		GridLayout gdtest = new GridLayout(2, true);
		

	    this.setLayout(gdtest);
	    
	    new Label(this, SWT.LEFT).setText("Fit Direction");
	    new Label(this, SWT.LEFT).setText("Fit Power");
	    comboDropDown0 = new Combo(this, SWT.DROP_DOWN | SWT.BORDER | SWT.LEFT);
	    
	    comboDropDown1 = new Combo(this, SWT.DROP_DOWN | SWT.BORDER | SWT.RIGHT);
	    //Combo comboSimple = new Combo(this, SWT.SIMPLE | SWT.BORDER);
	    
	    
	    comboDropDown0.add("X");
	    comboDropDown0.add("Y");
	    comboDropDown0.add("2D");
	    
	    comboDropDown1.add("0");
	    comboDropDown1.add("1");
	    comboDropDown1.add("2");
	    comboDropDown1.add("3");
	    comboDropDown1.add("4");
	    
		
		//this.CreateDropDownMenu();
		
	}		
		
//	public void CreateDropDownMenu() {
//		
//	    
//		GridLayout gdtest = new GridLayout(2, true);
//		
////		RowLayout rowLayout = new RowLayout();
////	    rowLayout.spacing = 15;
////	    rowLayout.marginWidth = 15;
////	    rowLayout.marginHeight = 15;
//	    
//	    this.setLayout(gdtest);
//	    
//	    new Label(this, SWT.LEFT).setText("Fit Direction");
//	    new Label(this, SWT.LEFT).setText("Fit Power");
//	    comboDropDown0 = new Combo(this, SWT.DROP_DOWN | SWT.BORDER | SWT.LEFT);
//	    
//	    comboDropDown1 = new Combo(this, SWT.DROP_DOWN | SWT.BORDER | SWT.RIGHT);
//	    //Combo comboSimple = new Combo(this, SWT.SIMPLE | SWT.BORDER);
//	    
//	    
//	    comboDropDown0.add("X");
//	    comboDropDown0.add("Y");
//	    comboDropDown0.add("2D");
//	    
//	    comboDropDown1.add("0");
//	    comboDropDown1.add("1");
//	    comboDropDown1.add("2");
//	    comboDropDown1.add("3");
//	    comboDropDown1.add("4");
//	    
//	    comboDropDown0.getText()
//	    
//	      //comboSimple.add("item " + i);
//	    }

	public String getFitDirection(){
		return comboDropDown0.getText();
		
	}
	
	public String getFitPower(){
		return comboDropDown1.getText();
		
	}

}




