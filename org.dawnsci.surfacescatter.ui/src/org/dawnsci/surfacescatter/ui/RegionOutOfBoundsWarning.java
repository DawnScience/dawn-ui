package org.dawnsci.surfacescatter.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;



public class RegionOutOfBoundsWarning extends Dialog {


	private int selector;
	private String note;
	private Button override;


	public RegionOutOfBoundsWarning(Shell parentShell, int selector, String note) {

		super(parentShell);
		this.selector = selector;
		this.note = note;		
		setShellStyle(getShellStyle() | SWT.RESIZE);

		//		createDialogArea(parentShell.getParent());

	}

	@Override
	protected Control createDialogArea(Composite parent) {

		final Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout());
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label warning = new Label(container, SWT.FILL);


		switch(selector){

		case 0:
			warning.setText("Bounday Box has over run image boundaries");	
			break;
		case 1:
			warning.setText("Enter number as int or double");	
			break;
		case 2:
			warning.setText("Error in geometric corrections. Check experimental setup");	
			break;
		case 3:
			warning.setText("Unable to locate images. Check images file path, or image name in geometric paramters unavwindow.");	
			break;
		case 4:
			warning.setText("Unable to locate flux data.");	
			break;
		case 5:
			warning.setText("Ran out of memory. Abort the process, then try a smaller region of interest, or a shorter image stack.");	
			break;
		case 6:
			warning.setText("Check that flux normalisation file covers scanned range of current experiment.");	
			break;
		case 7:
			warning.setText("Can't have two rods with the same name.");	
			break;
		case 8:
			warning.setText("No parameter file selected");	
			break;
		case 9:
			warning.setText("Unable to create output files.");	
			break;
		case 10:
			warning.setText("Set batch name.");	
			break;
		case 11:
			warning.setText("Set working directory.");
			break;
		case 12:
			warning.setText("Can't find files corresponding to the entered values.");	
			break;
		case 13:
			warning.setText("NO! STOP IT!");	
			break;
		case 14:
			warning.setText("No curve selected to save.");	
			break;
		case 15:
			warning.setText("Tracker has run off the image.");	
			break;
		case 16:
			warning.setText("Nexus file improperly formatted/ data cannot be read");	
			break;
		case 17:
			warning.setText("Problem when calculating correction factors. Check geometry/metadata.");	
			break;
//		case 18:
//			warning.setText("Fuck you. Get fucked. Fuck off.");	
//			break;
		default:
			warning.setText("Unknown or novel error.");	
			break;
		}
		
		if(note != null){
			Label noteLabel = new Label(container, SWT.FILL);
			noteLabel.setText(note);
		}

		return container;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Error Warning");
	}

	@Override
	protected Point getInitialSize() {
		Rectangle rect = PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell().getBounds();
		int h = rect.height;
		int w = rect.width;

		return new Point((int) Math.round(0.4*w), (int) Math.round(0.2*h));
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "OK", true);
		override = createButton(parent, IDialogConstants.CANCEL_ID,
				"Override", false);
	}

	public Button getOverride(){
		return override;
	}

}